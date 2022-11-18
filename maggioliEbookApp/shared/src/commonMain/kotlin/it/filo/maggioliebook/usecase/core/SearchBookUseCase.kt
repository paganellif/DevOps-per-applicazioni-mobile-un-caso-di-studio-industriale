package it.filo.maggioliebook.usecase.core

import it.filo.maggioliebook.domain.core.book.Libro
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.*
import org.koin.core.component.inject

class SearchBookUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(query: String? = null,
                       type: List<String>? = null,
                       id: List<String>? = null,
                       did: List<String>? = null,
                       limit: String? = null,
                       size: Int? = null,
                       page: Int? = null,
                       sort: String? = null): List<Libro>? =
        libroRepository.searchBooks(query, type, id, did, limit, size, page, sort)

    fun invokeNative(query: String? = null,
                     type: List<String>? = null,
                     id: List<String>? = null,
                     did: List<String>? = null,
                     limit: String? = null,
                     size: Int? = null,
                     page: Int? = null,
                     sort: String? = null,
                     onSuccess: (books: List<Libro>?) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke(query, type, id, did, limit, size, page, sort))
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}