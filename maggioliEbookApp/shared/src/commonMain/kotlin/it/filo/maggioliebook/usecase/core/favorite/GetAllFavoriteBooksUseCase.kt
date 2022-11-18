package it.filo.maggioliebook.usecase.core.favorite

import it.filo.maggioliebook.domain.user.Favorite
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class GetAllFavoriteBooksUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(): List<Favorite> = libroRepository.getAllFavorites()

    fun invokeNative(onSuccess: (favoriteBooks: List<Favorite>) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke())
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}