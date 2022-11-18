package it.filo.maggioliebook.usecase.core.highlight

import it.filo.maggioliebook.domain.core.book.Highlight
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class GetHighlightUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(id: Long): List<Highlight> = libroRepository.getHighlightById(id)

    fun invokeNative(id: Long,
                     onSuccess: (highlight: List<Highlight>) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke(id))
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}