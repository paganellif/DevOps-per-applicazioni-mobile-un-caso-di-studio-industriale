package it.filo.maggioliebook.android.reader

import android.graphics.Color
import android.util.Log
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import androidx.paging.*
import it.filo.maggioliebook.android.utils.extensions.Style
import it.filo.maggioliebook.android.reader.search.SearchPagingSource
import it.filo.maggioliebook.domain.core.book.Bookmark
import it.filo.maggioliebook.domain.core.book.Highlight
import it.filo.maggioliebook.android.utils.extensions.toDecorations
import it.filo.maggioliebook.usecase.core.bookmark.AddBookmarkForBookUseCase
import it.filo.maggioliebook.usecase.core.bookmark.GetBookBookmarksUseCase
import it.filo.maggioliebook.usecase.core.bookmark.RemoveBookmarkForBookUseCase
import it.filo.maggioliebook.usecase.core.highlight.AddHighlightUseCase
import it.filo.maggioliebook.usecase.core.highlight.GetBookHighlightsUseCase
import it.filo.maggioliebook.usecase.core.highlight.GetHighlightUseCase
import it.filo.maggioliebook.usecase.core.highlight.RemoveHighlightUseCase
import it.filo.maggioliebook.usecase.core.progression.AddBookProgressionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.ExperimentalDecorator
import org.readium.r2.shared.Search
import org.readium.r2.shared.UserException
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.LocatorCollection
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.indexOfFirstWithHref
import org.readium.r2.shared.publication.services.search.SearchIterator
import org.readium.r2.shared.publication.services.search.SearchTry
import org.readium.r2.shared.publication.services.search.search
import org.readium.r2.shared.util.Try

@OptIn(Search::class)
class ReaderViewModel(
    val readerInitData: ReaderInitData,
): ViewModel(){

    private val logTag: String = this.javaClass.simpleName

    private var _searchLocators = MutableStateFlow<List<Locator>>(emptyList())
    val searchLocators: StateFlow<List<Locator>> get() = _searchLocators

    val publication: Publication = readerInitData.publication

    val isbn: String = readerInitData.isbn

    val activityChannel: EventChannel<Event> =
        EventChannel(Channel(Channel.BUFFERED), viewModelScope)

    val fragmentChannel: EventChannel<FeedbackEvent> =
        EventChannel(Channel(Channel.BUFFERED), viewModelScope)

    val highlights: Flow<List<Highlight>> by lazy {
        getHighlightByIsbn(isbn).asFlow()
    }

    /**
     * Database ID of the active highlight for the current highlight pop-up. This is used to show
     * the highlight decoration in an "active" state.
     */
    var activeHighlightId = MutableStateFlow<Long?>(null)

    /**
     * Current state of the highlight decorations.
     *
     * It will automatically be updated when the highlights database table or the current
     * [activeHighlightId] change.
     */
    @OptIn(ExperimentalDecorator::class)
    val highlightDecorations: Flow<List<Decoration>> by lazy {
        highlights.combine(activeHighlightId) { highlights, activeId ->
            highlights.flatMap { highlight ->
                val isActive = highlight.id == activeId
                Log.d(logTag, "HIGHLIGHT DECORATION $highlight - ISACTIVE: $isActive")
                highlight.toDecorations(isActive = isActive)
            }
        }
    }

    fun saveProgression(locator: Locator) = viewModelScope.launch {
        AddBookProgressionUseCase().invoke(isbn, locator.toJSON().toString())
    }

    /**
     * Maps the current list of search result locators into a list of [Decoration] objects to
     * underline the results in the navigator.
     */
    @OptIn(ExperimentalDecorator::class)
    val searchDecorations: Flow<List<Decoration>> by lazy {
        searchLocators.map {
            it.mapIndexed { index, locator ->
                Decoration(
                    // The index in the search result list is a suitable Decoration ID, as long as
                    // we clear the search decorations between two searches.
                    id = index.toString(),
                    locator = locator,
                    style = Decoration.Style.Underline(tint = Color.RED)
                )
            }
        }
    }

    private var lastSearchQuery: String? = null

    private var searchIterator: SearchIterator? = null

    private val pagingSourceFactory = InvalidatingPagingSourceFactory {
        SearchPagingSource(listener = PagingSourceListener())
    }

    inner class PagingSourceListener : SearchPagingSource.Listener {
        override suspend fun next(): SearchTry<LocatorCollection?> {
            val iterator = searchIterator ?: return Try.success(null)
            return iterator.next().onSuccess {
                _searchLocators.value += (it?.locators ?: emptyList())
            }
        }
    }


    fun search(query: String) = viewModelScope.launch {
        if (query == lastSearchQuery) return@launch
        lastSearchQuery = query
        _searchLocators.value = emptyList()
        searchIterator = publication.search(query)
            .onFailure { activityChannel.send(Event.Failure(it)) }
            .getOrNull()
        pagingSourceFactory.invalidate()
        activityChannel.send(Event.StartNewSearch)
    }

    fun cancelSearch() = viewModelScope.launch {
        _searchLocators.value = emptyList()
        searchIterator?.close()
        searchIterator = null
        pagingSourceFactory.invalidate()
    }

    val searchResult: Flow<PagingData<Locator>> =
        Pager(PagingConfig(pageSize = 20), pagingSourceFactory = pagingSourceFactory)
            .flow.cachedIn(viewModelScope)

    fun insertBookmark(locator: Locator) = viewModelScope.launch {
        val resource = publication.readingOrder.indexOfFirstWithHref(locator.href)!!

        AddBookmarkForBookUseCase().invoke(
            isbn = isbn,
            createdDate = DateTime().toDate().time,
            publicationId = publication.metadata.identifier ?: publication.metadata.title,
            resourceIndex = resource.toLong(),
            resourceHref = locator.href,
            resourceType = locator.type,
            resourceTitle = locator.title.orEmpty(),
            location = locator.locations.toJSON().toString(),
            locatorText = Locator.Text().toJSON().toString()
        )

        fragmentChannel.send(FeedbackEvent.BookmarkSuccessfullyAdded)
    }

    fun getBookmarks(): LiveData<List<Bookmark>> {
        val _bookmarks = MutableLiveData<List<Bookmark>>().apply {
            value = emptyList()
        }

        viewModelScope.launch {
            _bookmarks.value = GetBookBookmarksUseCase().invoke(isbn)
        }

        return _bookmarks
    }

    fun removeBookmarkById(id: Long) = viewModelScope.launch {
        RemoveBookmarkForBookUseCase().invoke(id)
    }

    fun addHighlight(id: Long? = null, locator: Locator, style: Style, @ColorInt tint: Int, annotation: String = "") =
        viewModelScope.launch {
            AddHighlightUseCase().invoke(
                id = id,
                isbn = isbn,
                location = locator.toJSON().toString(),
                style = style.toString(),
                tint = tint,
                href = locator.href,
                type = locator.type,
                title = locator.title,
                text = locator.text.toJSON().toString(),
                annotation = annotation
            )
        }

    fun addHighlight(highlight: Highlight) = viewModelScope.launch {
        AddHighlightUseCase().invoke(
            id = highlight.id,
            isbn = highlight.isbn,
            location = highlight.location,
            style = highlight.style,
            tint = highlight.tint,
            href = highlight.href,
            type = highlight.type,
            title = highlight.title,
            text = highlight.text,
            annotation = highlight.annotation
        )
    }

    suspend fun getHighlightById(id: Long): Highlight {
        val highlights = GetHighlightUseCase().invoke(id)
        Log.d(logTag, "SELECT HIGHLIGHTS BY ID $id : $highlights")
        return highlights[0]
    }

    fun getHighlightByIsbn(isbn: String): LiveData<List<Highlight>> =
        GetBookHighlightsUseCase().invoke(isbn).asLiveData()

    fun removeHighlightById(id: Long) = viewModelScope.launch {
        RemoveHighlightUseCase().invoke(id)
    }

    fun updateHighlightAnnotation(id: Long?, annotation: String = "") = viewModelScope.launch {
        if(id != null){
            val oldHighlight: Highlight? = getHighlightById(id)
            if(oldHighlight != null){
                val newHighlight = Highlight(
                    id = id,
                    isbn = isbn,
                    location = oldHighlight.location,
                    style = oldHighlight.style,
                    tint = oldHighlight.tint,
                    href = oldHighlight.href,
                    type = oldHighlight.type,
                    title = oldHighlight.title,
                    annotation = annotation
                )

                addHighlight(newHighlight)
            }
        }
    }

    fun updateHighlightStyle(id: Long?, style: Style, tint: Int) = viewModelScope.launch {
        if(id != null){
            val oldHighlight: Highlight? = getHighlightById(id)
            if(oldHighlight != null){
                val newHighlight = Highlight(
                    id = id,
                    isbn = isbn,
                    location = oldHighlight.location,
                    style = style.toString(),
                    tint = tint,
                    href = oldHighlight.href,
                    type = oldHighlight.type,
                    title = oldHighlight.title,
                    annotation = oldHighlight.annotation
                )

                addHighlight(newHighlight)
            }
        }
    }

    sealed class Event {
        object OpenOutlineRequested : Event()
        object OpenDrmManagementRequested : Event()
        object StartNewSearch : Event()
        class Failure(val error: UserException) : Event()
    }

    sealed class FeedbackEvent {
        object BookmarkSuccessfullyAdded : FeedbackEvent()
        object BookmarkFailed : FeedbackEvent()
    }

    companion object: KoinComponent {
    fun createFactory(arguments: ReaderActivityContract.Arguments) =
        createViewModelFactory {
            val readerRepository: ReaderRepository by inject()
            val readerInitData =
                try {
                    Log.d(ReaderViewModel::class.simpleName, "READER INIT DATA WITH ARGUMENTS $arguments")
                    checkNotNull(readerRepository[arguments.isbn]!!)
                } catch (e: Exception) {
                    // Fallbacks on a dummy Publication to avoid crashing the app until the Activity finishes.
                    Log.e(ReaderViewModel::class.simpleName, e.stackTraceToString())
                    DummyReaderInitData(arguments.isbn)
                }

            ReaderViewModel(readerInitData)
        }
    }
}