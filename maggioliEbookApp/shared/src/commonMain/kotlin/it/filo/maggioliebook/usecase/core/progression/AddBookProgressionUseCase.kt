package it.filo.maggioliebook.usecase.core.progression

import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class AddBookProgressionUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(isbn: String, progression: String): Unit =
        libroRepository.setBookProgression(isbn, progression)

    fun invokeNative(isbn: String, progression: String,
                     onSuccess: (addBookProgression: Unit) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke(isbn, progression))
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}