package it.filo.maggioliebook.datasource.local

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import it.filo.maggioliebook.db.MaggioliEbookDB
import it.filo.maggioliebook.domain.user.Favorite
import it.filo.maggioliebook.util.DispatcherProvider
import it.filo.maggioliebook.util.getDispatcherProvider
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FavoriteDataSource(
    database: MaggioliEbookDB,
    private val dispatcherProvider: DispatcherProvider
) {

    private val dao = database.favoriteBooksQueries

    val favoriteBooks = dao.selectAll().asFlow().mapToList().map {
        favoriteBooks -> favoriteBooks.map { Favorite(it) }
    }

    suspend fun selectAll() = withContext(dispatcherProvider.io) {
        dao.selectAll().executeAsList()
    }


    suspend fun select(isbn: String) = withContext(dispatcherProvider.io) {
        dao.select(isbn).executeAsList()
    }

    suspend fun insert(favorite: Favorite) = withContext(dispatcherProvider.io) {
        dao.insert(favorite.isbn)
    }

    suspend fun remove(favorite: Favorite) = withContext(dispatcherProvider.io) {
        dao.remove(favorite.isbn)
    }

    suspend fun clear() = withContext(dispatcherProvider.io) {
        dao.clear()
    }
}