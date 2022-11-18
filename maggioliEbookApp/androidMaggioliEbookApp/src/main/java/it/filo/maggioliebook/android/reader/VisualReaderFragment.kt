package it.filo.maggioliebook.android.reader

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import it.filo.maggioliebook.android.R
import it.filo.maggioliebook.android.databinding.FragmentReaderBinding
import it.filo.maggioliebook.android.extensions.*
import it.filo.maggioliebook.android.utils.extensions.DecorationStyleAnnotationMark
import it.filo.maggioliebook.android.utils.extensions.Style
import it.filo.maggioliebook.android.utils.extensions.toLocator
import it.filo.maggioliebook.domain.core.book.Highlight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.readium.r2.navigator.*
import org.readium.r2.navigator.util.BaseActionModeCallback
import org.readium.r2.navigator.util.EdgeTapNavigation

@OptIn(ExperimentalDecorator::class)
abstract class VisualReaderFragment: BaseReaderFragment(), VisualNavigator.Listener {

    protected var _binding: FragmentReaderBinding? = null
    private lateinit var navigatorFragment: Fragment
    private val binding get() = _binding!!
    private val logTag: String = "VisualReaderFragment"

    private val decorationListener by lazy { DecorationListener() }

    /**
     * When true, the user won't be able to interact with the navigator.
     */
    private var disableTouches by mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigatorFragment = navigator as Fragment

        setupObservers()

        childFragmentManager.addOnBackStackChangedListener {
            updateSystemUiVisibility()
        }

        binding.fragmentReaderContainer.setOnApplyWindowInsetsListener { container, insets ->
            updateSystemUiPadding(container, insets)
            insets
        }

        binding.overlay.setContent {
            if (disableTouches) {
                // Add an invisible box on top of the navigator to intercept touch gestures.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                requireActivity().toggleSystemUi()
                            }
                        }
                )
            }
        }
    }

    /**
     *
     */
    override fun onDestroyView() {
        (navigator as? DecorableNavigator)?.removeDecorationListener(decorationListener)
        super.onDestroyView()
    }

    /**
     *
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navigator.currentLocator
                    .onEach { model.saveProgression(it) }
                    .launchIn(this)

                setupHighlights(this)
                setupSearch(this)
            }
        }
    }

    /**
     *
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setMenuVisibility(!hidden)
        requireActivity().invalidateOptionsMenu()
    }

    /**
     *
     */
    inner class DecorationListener : DecorableNavigator.Listener {
        override fun onDecorationActivated(event: DecorableNavigator.OnActivatedEvent): Boolean {
            val decoration = event.decoration
            // We stored the highlight's database ID in the `Decoration.extras` bundle, for
            // easy retrieval. You can store arbitrary information in the bundle.
            val _id = decoration.extras.getLong("id") // FIXME: _id è sempre null
            Log.d(logTag, "DECOR LISTENER HIGHLIGHT ID: $_id")
            val id = _id.takeIf { it > 0 } ?: return false

            // This listener will be called when tapping on any of the decorations in the
            // "highlights" group. To differentiate between the page margin icon and the
            // actual highlight, we check for the type of `decoration.style`. But you could
            // use any other information, including the decoration ID or the extras bundle.
            if (decoration.style is DecorationStyleAnnotationMark) {
                showAnnotationPopup(id)
            } else {
                event.rect?.let { rect ->
                    val isUnderline = (decoration.style is Decoration.Style.Underline)
                    showHighlightPopup(rect,
                        style = if (isUnderline) Style.UNDERLINE
                        else Style.HIGHLIGHT,
                        highlightId = id
                    )
                }
            }

            return true
        }

    }

    /**
     *
     */
    
    private suspend fun setupHighlights(scope: CoroutineScope){
        (navigator as? DecorableNavigator)?.let { navigator ->
            navigator.addDecorationListener("highlights", decorationListener)
            model.highlightDecorations
                .onEach { navigator.applyDecorations(it, "highlights") }
                .launchIn(scope)
        }
    }

    /**
     *
     */
    
    private suspend fun setupSearch(scope: CoroutineScope) {
        (navigator as? DecorableNavigator)?.let { navigator ->
            model.searchDecorations
                .onEach { navigator.applyDecorations(it, "search") }
                .launchIn(scope)
        }
    }

    private var popupWindow: PopupWindow? = null
    private var mode: ActionMode? = null

    /**
     *
     */
    private fun showAnnotationPopup(highlightId: Long? = null) =
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val activity = activity ?: return@launchWhenResumed
            val view: View = layoutInflater.inflate(R.layout.popup_note, null, false)
            val note = view.findViewById<EditText>(R.id.note)
            val alert = AlertDialog.Builder(activity).setView(view).create()

            fun dismiss() {
                alert.dismiss()
                mode?.finish()
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(note.applicationWindowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS)
            }

            with(view) {
                val highlight: Highlight? = highlightId?.let {
                    model.getHighlightById(highlightId)
                }

                if (highlight != null) {
                    note.setText(highlight.annotation)
                    findViewById<View>(R.id.sidemark).setBackgroundColor(highlight.tint)
                    findViewById<TextView>(R.id.select_text).text = highlight.toLocator().text.highlight

                    findViewById<TextView>(R.id.positive).setOnClickListener {
                        val text = note.text.toString()
                        model.updateHighlightAnnotation(highlight.id, annotation = text)
                        dismiss()
                    }
                } else {
                    val tint = highlightTints.values.random()
                    findViewById<View>(R.id.sidemark).setBackgroundColor(tint)
                    val navigator = navigator as? SelectableNavigator ?: return@launchWhenResumed
                    val selection = navigator.currentSelection() ?: return@launchWhenResumed
                    navigator.clearSelection()
                    findViewById<TextView>(R.id.select_text).text = selection.locator.text.highlight

                    findViewById<TextView>(R.id.positive).setOnClickListener {
                        model.addHighlight(locator = selection.locator,
                                            style = Style.HIGHLIGHT, tint = tint,
                                                    annotation = note.text.toString()
                        )
                        dismiss()
                    }
                }

                findViewById<TextView>(R.id.negative).setOnClickListener {
                    dismiss()
                }
            }

            alert.show()
        }

    /**
     *
     */
    private fun selectHighlightTint(highlightId: Long? = null, style: Style, @ColorInt tint: Int)
            = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        if (highlightId != null) {
            model.updateHighlightStyle(highlightId, style, tint)
        } else {
            (navigator as? SelectableNavigator)?.let { navigator ->
                navigator.currentSelection()?.let { selection ->
                    model.addHighlight(locator = selection.locator, style = style, tint = tint)
                }
                navigator.clearSelection()
            }
        }

        popupWindow?.dismiss()
        mode?.finish()
    }

    /**
     *
     */
    private fun showHighlightPopup(rect: RectF, style: Style, highlightId: Long? = null) =
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            if (popupWindow?.isShowing == true) return@launchWhenResumed

            model.activeHighlightId.value = highlightId

            Log.d(logTag, "ACTIVE HIGHLIGHT ID $highlightId")

            val isReverse = (rect.top > 60)
            val popupView = layoutInflater.inflate(
                if (isReverse) R.layout.view_action_mode_reverse else R.layout.view_action_mode,
                null,
                false
            )

            popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                isFocusable = true
                setOnDismissListener {
                    model.activeHighlightId.value = null
                }
            }

            val x = rect.left
            val y = if (isReverse) rect.top else rect.bottom + rect.height()

            popupWindow?.showAtLocation(popupView, Gravity.NO_GRAVITY, x.toInt(), y.toInt())

            val highlight = highlightId?.let { model.getHighlightById(highlightId) }
            Log.d(logTag, "GET HIGHLIGHT ID $highlight")
            popupView.run {
                findViewById<View>(R.id.notch).run {
                    setX(rect.left * 2)
                }

                fun selectTint(view: View) {
                    val tint = highlightTints[view.id] ?: return
                    selectHighlightTint(highlightId, style, tint)
                }

                findViewById<View>(R.id.red).setOnClickListener(::selectTint)
                findViewById<View>(R.id.green).setOnClickListener(::selectTint)
                findViewById<View>(R.id.blue).setOnClickListener(::selectTint)
                findViewById<View>(R.id.yellow).setOnClickListener(::selectTint)
                findViewById<View>(R.id.purple).setOnClickListener(::selectTint)

                findViewById<View>(R.id.annotation).setOnClickListener {
                    popupWindow?.dismiss()
                    showAnnotationPopup(highlightId)
                }

                findViewById<View>(R.id.del).run {
                    visibility = if (highlight != null) View.VISIBLE else View.GONE
                    setOnClickListener {
                        highlightId?.let {
                            //viewLifecycleOwner.lifecycleScope.launch {
                                model.removeHighlightById(highlightId)
                            //}
                        }
                        popupWindow?.dismiss()
                        mode?.finish()
                    }
                }
            }
        }

    /**
     *  Available tint colors for highlight and underline annotations.
     */
    private val highlightTints = mapOf(
        R.id.red to Color.rgb(247, 124, 124),
        R.id.green to Color.rgb(173, 247, 123),
        R.id.blue to Color.rgb(124, 198, 247),
        R.id.yellow to Color.rgb(249, 239, 125),
        R.id.purple to Color.rgb(182, 153, 255),
    )

    val customSelectionActionModeCallback: ActionMode.Callback by lazy { SelectionActionModeCallback() }

    /**
     *
     */
    private inner class SelectionActionModeCallback : BaseActionModeCallback() {
        
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_action_mode, menu)
            if (navigator is DecorableNavigator) {
                menu.findItem(R.id.highlight).isVisible = true
                menu.findItem(R.id.underline).isVisible = true
                menu.findItem(R.id.note).isVisible = true
            }
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.highlight -> showHighlightPopupWithStyle(Style.HIGHLIGHT)
                R.id.underline -> showHighlightPopupWithStyle(Style.UNDERLINE)
                R.id.note -> showAnnotationPopup()
                else -> return false
            }

            mode.finish()
            return true
        }
    }

    /**
     *
     */
    private fun showHighlightPopupWithStyle(style: Style) =
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            // Get the rect of the current selection to know where to position the highlight
            // popup.
            (navigator as? SelectableNavigator)?.currentSelection()?.rect?.let { selectionRect ->
                showHighlightPopup(selectionRect, style)
            }
        }

    /**
     *
     */
    fun updateSystemUiVisibility() {
        if (navigatorFragment.isHidden)
            requireActivity().showSystemUi()
        else
            requireActivity().hideSystemUi()

        requireView().requestApplyInsets()
    }

    /**
     *
     */
    private fun updateSystemUiPadding(container: View, insets: WindowInsets) {
        if (navigatorFragment.isHidden) {
            container.padSystemUi(insets, requireActivity() as AppCompatActivity)
        } else {
            container.clearPadding()
        }
    }

    /**
     *
     */
    private val edgeTapNavigation by lazy {
        EdgeTapNavigation(
            navigator = navigator as VisualNavigator
        )
    }

    /**
     *
     */
    override fun onTap(point: PointF): Boolean {
        val navigated = edgeTapNavigation.onTap(point, requireView())
        if (!navigated) {
            requireActivity().toggleSystemUi()
        }
        return true
    }
}