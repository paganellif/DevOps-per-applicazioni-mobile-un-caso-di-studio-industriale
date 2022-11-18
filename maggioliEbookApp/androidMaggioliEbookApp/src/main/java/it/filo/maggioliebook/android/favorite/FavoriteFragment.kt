package it.filo.maggioliebook.android.favorite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.filo.maggioliebook.android.R
import it.filo.maggioliebook.android.databinding.FragmentFavoriteBinding
import it.filo.maggioliebook.android.home.HomeViewModel
import it.filo.maggioliebook.android.reader.ReaderActivityContract
import it.filo.maggioliebook.repository.core.LibroRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.readium.r2.shared.extensions.tryOrLog

class FavoriteFragment: Fragment(), KoinComponent {

    private var _binding: FragmentFavoriteBinding? = null
    private var favoriteAdapter: FavoriteAdapter? = null
    private var favoriteViewModel: FavoriteViewModel? = null
    private lateinit var documentPickerLauncher: ActivityResultLauncher<String>
    private lateinit var readerLauncher: ActivityResultLauncher<ReaderActivityContract.Arguments>
    private val libroRepository: LibroRepository by inject()
    private val logTag: String = this.javaClass.simpleName

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var showDataJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        favoriteViewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteViewModel!!.channel.receive(viewLifecycleOwner) { handleEvent(it) }

        favoriteAdapter = FavoriteAdapter(requireActivity()) {
            favoriteViewModel!!.openBook(it, requireActivity())
            Log.d(logTag, "Requested to open book $it")
            showSnackbar("Opening book ${it!!.name}")
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.favorite_view)
        recyclerView.adapter = favoriteAdapter

        documentPickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    binding.favoriteProgressBar.visibility = View.VISIBLE
                }
            }

        readerLauncher = registerForActivityResult(ReaderActivityContract()) { input ->
                input?.let { tryOrLog { favoriteViewModel!!.closeBook(input.isbn) } }
            }

        binding.favoriteSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query == "CLEAAR")
                    viewLifecycleOwner.lifecycleScope.launch {
                        libroRepository.removeAllFavoriteBooks()
                    }
                else
                    showData(query)

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        binding.favoriteSearchView.setOnCloseListener {
            showData()
            true
        }

        showData()
    }

    override fun onStart() {
        super.onStart()
        showData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showData(query: String? = null) {
        if(showDataJob != null)
            showDataJob!!.cancel()

        showDataJob = viewLifecycleOwner.lifecycleScope.launch {
            favoriteViewModel!!.getSearchResultStream(query).collectLatest {
                favoriteAdapter!!.submitData(it)
            }
        }
    }

    private fun handleEvent(event: HomeViewModel.Event) {
        val message =
            when (event) {
                is HomeViewModel.Event.OpenBookError -> {
                    val detail = event.errorMessage
                        ?: "Unable to open book. An unexpected error occurred."
                    "Error: $detail"
                }
                is HomeViewModel.Event.LaunchReader -> {
                    readerLauncher.launch(event.arguments)
                    null
                }
                else -> {
                    Log.d(logTag, "Event $event Received")
                    event.toString()
                }
            }

        binding.favoriteProgressBar.visibility = View.GONE
        message?.let { showSnackbar(it) }
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
    }
}
