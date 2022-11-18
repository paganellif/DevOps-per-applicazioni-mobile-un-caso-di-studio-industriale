package it.filo.maggioliebook.usecase.core.bookmark

import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class RemoveBookmarkForBookUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(id: Long): Unit = libroRepository.removeBookmarkById(id)

    fun invokeNative(id: Long,
                     onSuccess: (removeBookmark: Unit) -> Unit,
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