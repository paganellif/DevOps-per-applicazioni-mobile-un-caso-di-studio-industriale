package it.filo.maggioliebook.repository.core

import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.util.*
import it.filo.maggioliebook.datasource.local.BookmarkDataSource
import it.filo.maggioliebook.datasource.local.FavoriteDataSource
import it.filo.maggioliebook.datasource.local.HighlightDataSource
import it.filo.maggioliebook.datasource.local.ProgressionDataSource
import it.filo.maggioliebook.datasource.remote.core.LibroDataSource
import it.filo.maggioliebook.datasource.remote.isValid
import it.filo.maggioliebook.domain.core.book.Libro
import it.filo.maggioliebook.domain.core.SearchParams
import it.filo.maggioliebook.datasource.remote.util.Pdf2EpubService
import it.filo.maggioliebook.domain.core.book.Bookmark
import it.filo.maggioliebook.domain.core.book.Highlight
import it.filo.maggioliebook.domain.core.book.Progression
import it.filo.maggioliebook.domain.user.Favorite
import it.filo.maggioliebook.util.DispatcherProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.round

class LibroRepository: KoinComponent {
    private val dispatcherProvider: DispatcherProvider by inject()
    private val libroDataSource: LibroDataSource by inject()
    private val favoriteDataSource: FavoriteDataSource by inject()
    private val highlightDataSource: HighlightDataSource by inject()
    private val bookmarkDataSource: BookmarkDataSource by inject()
    private val progressionDataSource: ProgressionDataSource by inject()
    private val pdf2EpubService: Pdf2EpubService by inject()

    /**
     *
     */
    suspend fun getBookMetadata(isbn: String): Libro? = withContext(dispatcherProvider.io){
        val response = libroDataSource.getInfo(isbn)
        if (response.isValid()) response.body() else null
    }

    /**
     *
     */
    suspend fun getBookCover(isbn: String): ByteArray? = withContext(dispatcherProvider.io){
        try {
            val response = libroDataSource.downloadImage(isbn)
            if (response.isValid()) response.bodyAsChannel().toByteArray() else null
        } catch (e: HttpRequestTimeoutException){
            Napier.e(e.message!!)
            return@withContext null
        }
    }

    /**
     *
     */
    suspend fun getBookContent(isbn: String): ByteArray? = withContext(dispatcherProvider.io){
        val response = libroDataSource.downloadFile(isbn)
        if (response.isValid())
            response.bodyAsChannel().toByteArray()
        else null
    }

    /**
     *
     */
    suspend fun searchBooks(
        query: String? = null,
        type: List<String>? = null,
        id: List<String>? = null,
        did: List<String>? = null,
        limit: String? = null,
        size: Int? = null,
        page: Int? = null,
        sort: String? = null
    ): List<Libro>? = withContext(dispatcherProvider.io){
        val response = libroDataSource.search(SearchParams(
            query.orEmpty(),
            type.orEmpty(),
            id.orEmpty(),
            did.orEmpty(),
            limit.orEmpty(),
            size.toString().orEmpty(),
            page.toString().orEmpty(),
            sort.orEmpty()
        ))

        if (response.isValid()) response.body() else listOf()
    }

    /**
     *
     */
    suspend fun getBooksCount(): Int = withContext(dispatcherProvider.io){
        val response = libroDataSource.search(SearchParams())
        response.headers["x-total-count"]?.toInt() ?: 0
    }

    /**
     *
     */
    suspend fun getAllBooksMetadata(): MutableList<Libro> {

        val result: MutableList<Libro> = mutableListOf()

        val totalBooks = getBooksCount()
        result.addAll(searchBooks(page = 0) as MutableList<Libro>)

        if (totalBooks > 0){
            val totalPage = round(totalBooks.toFloat() / result.size.toFloat()).toInt()
            Napier.d("TOT PAGE: $totalPage - BOOKS/PAGE: ${result.size}")

            for (i in 1..totalPage){
                val books = searchBooks(page = i)

                if (books == null)
                    Napier.d("Error GET books with pagination - page: $i")

                result.addAll(books?.toMutableList() ?: mutableListOf())
            }
        }

        return result
    }
    
    /**
     *
     */
    suspend fun convertBookPdf2Epub(isbn: String): ByteArray? = withContext(dispatcherProvider.io){
        val bookMetadata = getBookMetadata(isbn)
        val book = getBookContent(isbn)

        if(bookMetadata != null && book != null && book.isNotEmpty()){
            Napier.d("Downloading book $isbn")
            val pdfToBeConverted: ByteArray? = libroDataSource.downloadFile(isbn).body()
            //Napier.d(pdfToBeConverted!!.decodeToString())
            Napier.d("DOWNLOADED FILE SIZE: ${pdfToBeConverted!!.size} byte")

            if(pdfToBeConverted.isNotEmpty()) {
                Napier.d("Started conversion for book $isbn")
                val response = pdf2EpubService.convert(
                    pdfToBeConverted = pdfToBeConverted,
                    isbn = bookMetadata.isbn!!,
                    title = bookMetadata.name!!,
                    publisher = bookMetadata.editore,
                    authors = bookMetadata.autori
                )

                val epub = response.bodyAsChannel().toByteArray()
                Napier.d("Ended conversion for book $isbn")
                Napier.d("CONVERTED FILE SIZE: ${epub.size} byte")

                return@withContext epub
            } else
                return@withContext null
        } else
            return@withContext null
    }
    
    suspend fun isBookFavorite(isbn: String): Boolean {
        val result = favoriteDataSource.select(isbn).contains(isbn)
        if(result)
            Napier.d("Book $isbn is favorite")
        else
            Napier.d("Book $isbn is NOT favorite")
        return result
    }

    suspend fun getAllFavorites(): List<Favorite> {
        val result = favoriteDataSource.selectAll().map { Favorite(it) }.toList()
        Napier.d("Favorite Books: $result")
        return result
    }

    suspend fun setFavoriteBook(isbn: String) =
        favoriteDataSource.insert(Favorite(isbn))

    suspend fun removeFavoriteBook(isbn: String) = favoriteDataSource.remove(Favorite(isbn))

    suspend fun removeAllFavoriteBooks() = favoriteDataSource.clear()

    fun addHighlight(id: Long? = null, isbn: String, location: String, style: String,
                             tint: Int = 0, href: String, type: String, title: String?,
                             text: String = "{}", annotation: String) {
        val highlight = Highlight(
            id = id,
            isbn = isbn,
            location = location,
            style = style,
            tint = tint,
            href = href,
            type = type,
            title = title,
            text = text,
            annotation = annotation
        )
        Napier.d("ADDING HIGHLIGHT $highlight")
        highlightDataSource.insert(highlight)
    }

    suspend fun getHighlightById(id: Long): List<Highlight> {
        Napier.d("SELECT HIGHLIGHT ID $id")
        return highlightDataSource.select(id).first()
    }

    fun getHighlightByIsbn(isbn: String) = highlightDataSource.selectByIsbn(isbn)

    suspend fun removeHighlightById(id: Long) {
        Napier.d("REMOVING HIGHLIGHT ID $id")
        highlightDataSource.remove(id)
    }

    suspend fun removeHighlightByIsbn(isbn: String) = highlightDataSource.removeByIsbn(isbn)

    suspend fun addBookmark(id: Long? = null, createdDate: Long?, isbn: String, publicationId: String,
                            resourceIndex: Long, resourceHref: String, resourceType: String,
                            resourceTitle: String, location: String, locatorText: String) {

        val bookmark = Bookmark(
            id = id,
            createdDate = createdDate,
            isbn = isbn,
            publicationId = publicationId,
            resourceIndex = resourceIndex,
            resourceHref = resourceHref,
            resourceType = resourceType,
            resourceTitle = resourceTitle,
            location = location,
            locatorText = locatorText
        )

        bookmarkDataSource.insert(bookmark)
    }

    suspend fun getBookmarkById(id: Long) = bookmarkDataSource.select(id)

    suspend fun getBookmarkByIsbn(isbn: String) = bookmarkDataSource.selectByIsbn(isbn)

    suspend fun removeBookmarkById(id: Long) = bookmarkDataSource.remove(id)

    suspend fun removeBookmarkByIsbn(isbn: String) = bookmarkDataSource.removeByIsbn(isbn)

    suspend fun getBookProgressionByIsbn(isbn: String) = progressionDataSource.select(isbn)

    suspend fun setBookProgression(isbn: String, progression: String) = progressionDataSource.insert(
        Progression(isbn, progression)
    )

    suspend fun removeBookProgression(isbn: String) = progressionDataSource.remove(isbn)
}