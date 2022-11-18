package it.filo.maggioliebook.usecase.core

import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class GetBookCoverUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(isbn: String): ByteArray? = libroRepository.getBookCover(isbn)

    fun invokeNative(isbn: String,
                     onSuccess: (bookCover: ByteArray?) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke(isbn))
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}