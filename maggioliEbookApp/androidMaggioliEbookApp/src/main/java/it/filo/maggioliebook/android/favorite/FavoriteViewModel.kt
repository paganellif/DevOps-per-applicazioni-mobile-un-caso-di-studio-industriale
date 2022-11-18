package it.filo.maggioliebook.android.favorite

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import it.filo.maggioliebook.android.home.HomeViewModel
import it.filo.maggioliebook.android.home.LibroPagingSource
import it.filo.maggioliebook.android.reader.EventChannel
import it.filo.maggioliebook.android.reader.ReaderActivityContract
import it.filo.maggioliebook.android.reader.ReaderRepository
import it.filo.maggioliebook.domain.core.book.Libro
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.core.favorite.IsBookFavoriteUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val ITEMS_PER_PAGE = 20
private const val STARTING_PAGE_INDEX = 0

class FavoriteViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    val channel = EventChannel(Channel<HomeViewModel.Event>(Channel.BUFFERED), viewModelScope)
    private val readerRepository: ReaderRepository by inject()
    private val logTag: String = this.javaClass.simpleName

    private val preferences =
        application.getSharedPreferences("org.readium.r2.settings", Context.MODE_PRIVATE)

    fun getSearchResultStream(query: String?): Flow<PagingData<Libro>> {
        return Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE),
            initialKey = STARTING_PAGE_INDEX,
            pagingSourceFactory = { LibroPagingSource(query) }
        ).flow.map { pagingData ->
            pagingData.filter { book ->
                !book.isbn.isNullOrEmpty() && IsBookFavoriteUseCase().invoke(book.isbn!!)
            }
        }.cachedIn(viewModelScope)
    }

    fun openBook(book: Libro?, activity: Activity) = viewModelScope.launch {
        readerRepository.openBook(book, activity).onFailure {
            Log.e(logTag, "Cannot open book $book")
            Log.e(logTag, it.stackTraceToString())
            channel.send(HomeViewModel.Event.OpenBookError("Cannot open book ${book!!.isbn}"))
        }.onSuccess {
            val arguments = ReaderActivityContract.Arguments(book!!.isbn!!)
            Log.d(logTag, "Opening book $book : argument = $arguments")
            channel.send(HomeViewModel.Event.LaunchReader(arguments))
        }
    }

    fun closeBook(isbn: String) = viewModelScope.launch {
        readerRepository.close(isbn)
    }
}