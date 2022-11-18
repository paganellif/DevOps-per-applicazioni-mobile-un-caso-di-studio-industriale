package it.filo.maggioliebook.usecase.core.favorite

import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class IsBookFavoriteUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(isbn: String): Boolean = libroRepository.isBookFavorite(isbn)

    fun invokeNative(isbn: String,
                     onSuccess: (isBookFavorite: Boolean) -> Unit,
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