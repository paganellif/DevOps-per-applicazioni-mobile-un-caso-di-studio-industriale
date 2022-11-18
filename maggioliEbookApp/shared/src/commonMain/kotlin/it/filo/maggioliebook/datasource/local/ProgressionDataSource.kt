package it.filo.maggioliebook.datasource.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import it.filo.maggioliebook.db.MaggioliEbookDB
import it.filo.maggioliebook.domain.core.book.Progression
import it.filo.maggioliebook.util.DispatcherProvider
import kotlinx.coroutines.withContext

class ProgressionDataSource(
    database: MaggioliEbookDB,
    private val dispatcherProvider: DispatcherProvider
) {

    private val dao = database.progressionQueries

    val progressions = dao.selectAll().asFlow().mapToList()

    suspend fun selectAll() = withContext(dispatcherProvider.io) {
        dao.selectAll().executeAsList().map {
            Progression(it.isbn, it.progression)
        }
    }

    suspend fun select(isbn: String) = withContext(dispatcherProvider.io) {
        dao.select(isbn).executeAsList().map {
            Progression(it.isbn, it.progression)
        }
    }

    suspend fun insert(progression: Progression) = withContext(dispatcherProvider.io) {
        dao.insert(progression.isbn, progression.progression)
    }

    suspend fun remove(isbn: String) = withContext(dispatcherProvider.io) {
        dao.remove(isbn)
    }

    suspend fun clear() = withContext(dispatcherProvider.io) {
        dao.clear()
    }
}