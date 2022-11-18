package it.filo.maggioliebook.usecase.core.highlight

import it.filo.maggioliebook.domain.core.book.Highlight
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class GetBookHighlightsUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    fun invoke(isbn: String): Flow<List<Highlight>> =
        libroRepository.getHighlightByIsbn(isbn)

    fun invokeNative(isbn: String,
                     onSuccess: (bookHighlights: List<Highlight>) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke(isbn).filter { it.isNotEmpty() } .map { it[0] }.toList())
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}