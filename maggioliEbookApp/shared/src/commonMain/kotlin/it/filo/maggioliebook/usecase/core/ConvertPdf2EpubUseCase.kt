package it.filo.maggioliebook.usecase.core

import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class ConvertPdf2EpubUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(isbn: String): ByteArray? = libroRepository.convertBookPdf2Epub(isbn)

    fun invokeNative(isbn: String,
                     onSuccess: (convertedBook: ByteArray?) -> Unit,
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