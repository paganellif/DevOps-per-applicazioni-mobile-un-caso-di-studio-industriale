package it.filo.maggioliebook.usecase.core.bookmark

import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.BaseUseCase
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class AddBookmarkForBookUseCase: BaseUseCase() {

    private val libroRepository: LibroRepository by inject()

    suspend fun invoke(id: Long? = null, createdDate: Long?, isbn: String, publicationId: String,
                       resourceIndex: Long, resourceHref: String, resourceType: String,
                       resourceTitle: String, location: String, locatorText: String): Unit =
        libroRepository.addBookmark(id, createdDate, isbn, publicationId, resourceIndex,
            resourceHref, resourceType, resourceTitle, location, locatorText)

    fun invokeNative(id: Long? = null, createdDate: Long?, isbn: String, publicationId: String,
                     resourceIndex: Long, resourceHref: String, resourceType: String,
                     resourceTitle: String, location: String, locatorText: String,
                     onSuccess: (addBookmark: Unit) -> Unit,
                     onError: (error: Throwable) -> Unit) {
        try {
            nativeScope.launch {
                onSuccess(invoke(id, createdDate, isbn, publicationId, resourceIndex,
                    resourceHref, resourceType, resourceTitle, location, locatorText))
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }
}