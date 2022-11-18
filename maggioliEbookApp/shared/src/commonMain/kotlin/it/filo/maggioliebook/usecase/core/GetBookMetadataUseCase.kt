package it.filo.maggioliebook.usecase.core

import it.filo.maggioliebook.domain.core.book.Libro
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.*
import org.koin.core.component.inject

class GetBookMetadataUseCase: BaseUseCase() {
    private val libroRepository: LibroRepository by inject()
    suspend fun invoke(isbn: String): Libro? = libroRepository.getBookMetadata(isbn)

    fun invokeNative(isbn: String,
                     onSuccess: (book: Libro?) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            this.nativeScope.launch {
                onSuccess(invoke(isbn))
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}