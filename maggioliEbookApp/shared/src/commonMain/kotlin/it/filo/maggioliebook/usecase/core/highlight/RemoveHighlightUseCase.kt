package it.filo.maggioliebook.usecase.core.highlight

import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class RemoveHighlightUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(id: Long): Unit = libroRepository.removeHighlightById(id)

    fun invokeNative(id: Long,
                     onSuccess: (removeHighlight: Unit) -> Unit,
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