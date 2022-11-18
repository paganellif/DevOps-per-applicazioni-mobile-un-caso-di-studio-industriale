package it.filo.maggioliebook.usecase.core.progression

import it.filo.maggioliebook.domain.core.book.Progression
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class GetBookProgressionUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(isbn: String): List<Progression> =
        libroRepository.getBookProgressionByIsbn(isbn)

    fun invokeNative(isbn: String,
                     onSuccess: (bookProgression: List<Progression>) -> Unit,
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