package it.filo.maggioliebook.android.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.filo.maggioliebook.android.R
import it.filo.maggioliebook.android.databinding.FragmentHomeBinding
import it.filo.maggioliebook.android.reader.ReaderActivityContract
import it.filo.maggioliebook.android.utils.extensions.viewLifecycle
import it.filo.maggioliebook.usecase.user.CheckUserLoggedUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.readium.r2.shared.extensions.tryOrLog

class HomeFragment: Fragment() {

    private lateinit var homeAdapter: HomeAdapter
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var readerLauncher: ActivityResultLauncher<ReaderActivityContract.Arguments>
    private val logTag: String = this.javaClass.simpleName
    private var binding: FragmentHomeBinding by viewLifecycle()
    private var showDataJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        if (!CheckUserLoggedUseCase().invoke())
            findNavController().navigate(R.id.action_home_to_login)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.channel.receive(viewLifecycleOwner) { handleEvent(it) }

        homeAdapter = HomeAdapter(requireActivity()) {
            homeViewModel.openBook(it, requireActivity())
            Log.d(logTag, "Requested to open book $it")
            showSnackbar("Opening book ${it!!.name}")
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.home_view)
        recyclerView.adapter = homeAdapter

        homeAdapter.addLoadStateListener { Log.d(logTag, it.toString()) }

        readerLauncher = registerForActivityResult(ReaderActivityContract()) { input ->
                input?.let { tryOrLog { homeViewModel.closeBook(input.isbn) } }
            }

        binding.homeSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                showData(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                  if(newText!!.isEmpty()){
                      showData()
                      return true
                  } else
                      return false
            }
        })

        binding.homeSearchView.setOnCloseListener {
            showData()
            true
        }

        showData()
    }

    private fun showData(query: String? = null) {
        if (showDataJob != null)
            showDataJob!!.cancel()

        showDataJob = viewLifecycleOwner.lifecycleScope.launch {
            Log.d(logTag, lifecycle.currentState.toString())
            homeViewModel.getSearchResultStream(query).collectLatest {
                homeAdapter.submitData(it)
            }
        }
    }

    private fun flushData() {
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(logTag, lifecycle.currentState.toString())
            homeAdapter.submitData(PagingData.empty())
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
                /*else -> {
                    Log.d(logTag, "Event $event Received")
                    event.toString()
                }*/ // exhaustive
            }

        message?.let { showSnackbar(it) }
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
    }
}
