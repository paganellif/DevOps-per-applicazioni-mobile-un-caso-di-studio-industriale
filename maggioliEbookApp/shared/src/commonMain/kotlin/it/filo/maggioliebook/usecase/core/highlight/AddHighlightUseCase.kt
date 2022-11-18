package it.filo.maggioliebook.usecase.core.highlight

import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class AddHighlightUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    fun invoke(id: Long? = null, isbn: String, location: String, style: String,
               tint: Int = 0, href: String, type: String, title: String?,
               text: String = "{}", annotation: String): Unit =
        libroRepository.addHighlight(id, isbn, location, style, tint, href, type, title, text,
            annotation)

    fun invokeNative(id: Long? = null, isbn: String, location: String, style: String,
                     tint: Int = 0, href: String, type: String, title: String?,
                     text: String = "{}", annotation: String,
                     onSuccess: (highlight: Unit) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke(id, isbn, location, style, tint, href, type, title, text,
                    annotation))
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}