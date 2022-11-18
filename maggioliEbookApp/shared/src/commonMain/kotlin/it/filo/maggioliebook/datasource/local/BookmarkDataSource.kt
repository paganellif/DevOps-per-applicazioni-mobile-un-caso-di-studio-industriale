package it.filo.maggioliebook.datasource.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import it.filo.maggioliebook.db.MaggioliEbookDB
import it.filo.maggioliebook.domain.core.book.Bookmark
import it.filo.maggioliebook.util.DispatcherProvider
import kotlinx.coroutines.withContext

class BookmarkDataSource(
    database: MaggioliEbookDB,
    private val dispatcherProvider: DispatcherProvider
) {

    private val dao = database.bookmarkQueries
    val bookmarks = dao.selectAll().asFlow()

    suspend fun selectAll() = withContext(dispatcherProvider.io) {
        dao.selectAll().executeAsList().map {
            Bookmark(
                id = it.id,
                createdDate = it.created_date,
                isbn = it.isbn,
                publicationId = it.publication_id,
                resourceIndex = it.resource_index,
                resourceHref = it.resource_href,
                resourceType = it.resource_type,
                resourceTitle = it.resource_title,
                location = it.location,
                locatorText = it.locator_text
            )
        }
    }

    suspend fun select(id: Long) = withContext(dispatcherProvider.io) {
        dao.select(id).executeAsList().map {
            Bookmark(
                id = it.id,
                createdDate = it.created_date,
                isbn = it.isbn,
                publicationId = it.publication_id,
                resourceIndex = it.resource_index,
                resourceHref = it.resource_href,
                resourceType = it.resource_type,
                resourceTitle = it.resource_title,
                location = it.location,
                locatorText = it.locator_text
            )
        }
    }

    suspend fun selectByIsbn(isbn: String) = withContext(dispatcherProvider.io) {
        dao.selectByIsbn(isbn).executeAsList().map {
            Bookmark(
                id = it.id,
                createdDate = it.created_date,
                isbn = it.isbn,
                publicationId = it.publication_id,
                resourceIndex = it.resource_index,
                resourceHref = it.resource_href,
                resourceType = it.resource_type,
                resourceTitle = it.resource_title,
                location = it.location,
                locatorText = it.locator_text
            )
        }
    }

    suspend fun insert(bookmark: Bookmark) = withContext(dispatcherProvider.io) {
        dao.insert(
            id = bookmark.id,
            created_date = bookmark.createdDate,
            isbn = bookmark.isbn,
            publication_id = bookmark.publicationId,
            resource_index = bookmark.resourceIndex,
            resource_href = bookmark.resourceHref,
            resource_type = bookmark.resourceType,
            resource_title = bookmark.resourceTitle,
            location = bookmark.location,
            locator_text = bookmark.locatorText
        )
    }

    suspend fun remove(id: Long) = withContext(dispatcherProvider.io) {
        dao.remove(id)
    }

    suspend fun removeByIsbn(isbn: String) = withContext(dispatcherProvider.io) {
        dao.removeByIsbn(isbn)
    }

    suspend fun clear() = withContext(dispatcherProvider.io) {
        dao.clear()
    }
}