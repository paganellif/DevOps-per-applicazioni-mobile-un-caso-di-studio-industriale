package it.filo.maggioliebook.android.home

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import it.filo.maggioliebook.android.reader.*
import it.filo.maggioliebook.domain.core.book.Libro
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private const val ITEMS_PER_PAGE = 20
private const val STARTING_PAGE_INDEX = 0

class HomeViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    val channel = EventChannel(Channel<Event>(Channel.BUFFERED), viewModelScope)
    private val logTag: String = this.javaClass.simpleName
    private val app get() = getApplication<it.filo.maggioliebook.android.Application>()
    private val readerRepository: ReaderRepository by inject()

    private val preferences =
        application.getSharedPreferences("org.readium.r2.settings", Context.MODE_PRIVATE)

    fun getSearchResultStream(query: String? = null): Flow<PagingData<Libro>> =
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE),
            initialKey = STARTING_PAGE_INDEX
        ) {
            LibroPagingSource(query)
        }.flow.cachedIn(viewModelScope)

    fun openBook(book: Libro?, activity: Activity) = viewModelScope.launch {
        readerRepository.openBook(book, activity).onFailure {
            Log.e(logTag, "Cannot open book $book")
            Log.e(logTag, it.stackTraceToString())
            channel.send(Event.OpenBookError("Cannot open book $book"))
        }.onSuccess {
            val arguments = ReaderActivityContract.Arguments(book!!.isbn!!)
            Log.d(logTag, "Opening book $book : argument = $arguments")
            channel.send(Event.LaunchReader(arguments))
        }
    }

    fun closeBook(isbn: String) = viewModelScope.launch {
        readerRepository.close(isbn)
    }

    sealed class Event {

        class OpenBookError(val errorMessage: String?) : Event()

        class LaunchReader(val arguments: ReaderActivityContract.Arguments) : Event()
    }

}