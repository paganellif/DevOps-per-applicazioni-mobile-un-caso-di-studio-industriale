package it.filo.maggioliebook.datasource.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import it.filo.maggioliebook.db.MaggioliEbookDB
import it.filo.maggioliebook.domain.core.book.Highlight
import it.filo.maggioliebook.util.DispatcherProvider
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

class HighlightDataSource(
    database: MaggioliEbookDB,
    private val dispatcherProvider: DispatcherProvider
) {

    private val dao = database.highlightsQueries
    val highlights = dao.selectAll().asFlow()

    fun selectAll() =
        dao.selectAll().asFlow().mapToList().map {
            it.map { highlight ->
                Highlight(
                    id = highlight.id,
                    createdDate = highlight.created_date,
                    isbn = highlight.isbn,
                    style = highlight.style,
                    tint = highlight.tint,
                    href = highlight.href,
                    type = highlight.type,
                    title = highlight.title,
                    totalProgression = highlight.total_progression,
                    location = highlight.location,
                    text = highlight.text,
                    annotation = highlight.annotation_
                )
            }
        }

    fun select(id: Long) =
        dao.select(id).asFlow().mapToList().map {
            it.map { highlight ->
                Highlight(
                    id = highlight.id,
                    createdDate = highlight.created_date,
                    isbn = highlight.isbn,
                    style = highlight.style,
                    tint = highlight.tint,
                    href = highlight.href,
                    type = highlight.type,
                    title = highlight.title,
                    totalProgression = highlight.total_progression,
                    location = highlight.location,
                    text = highlight.text,
                    annotation = highlight.annotation_
                )
            }
        }


    fun selectByIsbn(isbn: String) =
        dao.selectByIsbn(isbn).asFlow().mapToList().map {
            it.map { highlight ->
                Highlight(
                    id = highlight.id,
                    createdDate = highlight.created_date,
                    isbn = highlight.isbn,
                    style = highlight.style,
                    tint = highlight.tint,
                    href = highlight.href,
                    type = highlight.type,
                    title = highlight.title,
                    totalProgression = highlight.total_progression,
                    location = highlight.location,
                    text = highlight.text,
                    annotation = highlight.annotation_
                )
            }
        }


    fun insert(highlight: Highlight) =
        dao.insert(
            id = highlight.id,
            created_date = highlight.createdDate,
            isbn = highlight.isbn,
            style = highlight.style,
            tint = highlight.tint,
            href = highlight.href,
            type = highlight.type,
            title = highlight.title,
            total_progression = highlight.totalProgression,
            location = highlight.location,
            text = highlight.text,
            annotation_ = highlight.annotation
        )

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