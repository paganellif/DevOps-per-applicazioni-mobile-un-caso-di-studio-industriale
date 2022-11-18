package it.filo.maggioliebook.android.home

import androidx.paging.PagingSource
import androidx.paging.PagingState
import it.filo.maggioliebook.domain.core.book.Libro
import it.filo.maggioliebook.usecase.core.SearchBookUseCase

// https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data
class LibroPagingSource(private val query: String?): PagingSource<Int, Libro>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Libro> {
        val nextPageNumber = params.key ?: 0
        val response = SearchBookUseCase().invoke(query = query, page = nextPageNumber)

        return LoadResult.Page(
            data = response?: mutableListOf(),
            prevKey = null, // Only paging forward.
            nextKey = if (response!!.isNotEmpty()) nextPageNumber + 1 else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Libro>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}