package it.filo.maggioliebook.android.reader

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.ViewModelProvider
import it.filo.maggioliebook.android.R
import it.filo.maggioliebook.android.databinding.ActivityReaderBinding
import it.filo.maggioliebook.android.reader.drm.DrmManagementContract
import it.filo.maggioliebook.android.reader.drm.DrmManagementFragment
import it.filo.maggioliebook.android.reader.outline.OutlineContract
import it.filo.maggioliebook.android.reader.outline.OutlineFragment
import org.readium.r2.shared.UserException
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import kotlin.math.log

open class ReaderActivity: AppCompatActivity() {
    private val model: ReaderViewModel by viewModels()
    private lateinit var binding: ActivityReaderBinding
    private lateinit var readerFragment: BaseReaderFragment
    private val logTag: String = this.javaClass.simpleName

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        val arguments = ReaderActivityContract.parseIntent(this)
        return ReaderViewModel.createFactory(arguments)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        /*
         * We provide dummy publications if the [ReaderActivity] is restored after the app process
         * was killed because the [ReaderRepository] is empty.
         * In that case, finish the activity as soon as possible and go back to the previous one.
         */
        if (model.publication.readingOrder.isEmpty()) {
            finish()
        }

        super.onCreate(savedInstanceState)

        val binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.binding = binding

        val readerFragment = supportFragmentManager.findFragmentByTag(READER_FRAGMENT_TAG)
            ?.let { it as BaseReaderFragment }
            ?: run { createReaderFragment(model.readerInitData) }

        if (readerFragment is VisualReaderFragment) {
            val fullscreenDelegate = FullscreenReaderActivityDelegate(this, readerFragment, binding)
            lifecycle.addObserver(fullscreenDelegate)
        }

        readerFragment?.let { this.readerFragment = it }
        readerFragment?.let { this.readerFragment = it }

        model.activityChannel.receive(this) { handleReaderFragmentEvent(it) }

        reconfigureActionBar()

        supportFragmentManager.setFragmentResultListener(
            OutlineContract.REQUEST_KEY,
            this,
            FragmentResultListener { _, result ->
                val locator = OutlineContract.parseResult(result).destination
                closeOutlineFragment(locator)
            }
        )

        supportFragmentManager.setFragmentResultListener(
            DrmManagementContract.REQUEST_KEY,
            this,
            FragmentResultListener { _, result ->
                if (DrmManagementContract.parseResult(result).hasReturned)
                    finish()
            }
        )

        supportFragmentManager.addOnBackStackChangedListener {
            reconfigureActionBar()
        }

        // Add support for display cutout.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    override fun onStart() {
        super.onStart()
        reconfigureActionBar()
    }

    private fun reconfigureActionBar() {
        val currentFragment = supportFragmentManager.fragments.lastOrNull()

        title = when (currentFragment) {
            is OutlineFragment -> model.publication.metadata.title
            is DrmManagementFragment -> getString(R.string.title_fragment_drm_management)
            else -> null
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(
            when (currentFragment) {
                is OutlineFragment, is DrmManagementFragment -> true
                else -> false

            }
        )
    }

    override fun finish() {
        setResult(Activity.RESULT_OK, Intent().putExtras(intent))
        super.finish()
    }

    private fun createReaderFragment(readerData: ReaderInitData): BaseReaderFragment? {
        Log.d(logTag, readerData.publication.metadata.toString())
        val readerClass: Class<out Fragment>? = when {
            readerData.publication.conformsTo(Publication.Profile.EPUB) ->
                EpubReaderFragment::class.java
            // TODO readerData.publication.conformsTo(Publication.Profile.PDF) ->
            //    PdfReaderFragment::class.java
            else ->{
                Log.e(logTag, "The Activity should stop as soon as possible because readerData are fake")
                null
            }

        }

        readerClass?.let { it ->
            supportFragmentManager.commitNow {
                replace(R.id.activity_container, it, Bundle(), READER_FRAGMENT_TAG)
            }
        }

        return supportFragmentManager.findFragmentByTag(READER_FRAGMENT_TAG) as BaseReaderFragment?
    }

    private fun handleReaderFragmentEvent(event: ReaderViewModel.Event) {
        when(event) {
            is ReaderViewModel.Event.OpenOutlineRequested -> showOutlineFragment()
            is ReaderViewModel.Event.OpenDrmManagementRequested -> showDrmManagementFragment()
            is ReaderViewModel.Event.Failure -> showError(event.error)
            else -> {}
        }
    }

    private fun showError(error: UserException) {
        Toast.makeText(this, error.getUserMessage(this), Toast.LENGTH_LONG).show()
    }

    private fun showOutlineFragment() {
        supportFragmentManager.commit {
            add(R.id.activity_container, OutlineFragment::class.java, Bundle(), OUTLINE_FRAGMENT_TAG)
            hide(readerFragment)
            addToBackStack(null)
        }
    }

    private fun closeOutlineFragment(locator: Locator) {
        readerFragment.go(locator, true)
        supportFragmentManager.popBackStack()
    }

    private fun showDrmManagementFragment() {
        supportFragmentManager.commit {
            add(
                R.id.activity_container,
                DrmManagementFragment::class.java,
                Bundle(),
                DRM_FRAGMENT_TAG
            )
            hide(readerFragment)
            addToBackStack(null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFragmentManager.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val READER_FRAGMENT_TAG = "reader"
        const val OUTLINE_FRAGMENT_TAG = "outline"
        const val DRM_FRAGMENT_TAG = "drm"
    }
}