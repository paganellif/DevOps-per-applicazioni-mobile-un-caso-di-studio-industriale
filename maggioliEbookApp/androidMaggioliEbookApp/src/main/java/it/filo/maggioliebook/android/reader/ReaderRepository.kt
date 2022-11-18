package it.filo.maggioliebook.android.reader

import android.app.Activity
import android.app.Application
import android.util.Log
import it.filo.maggioliebook.android.Readium
import it.filo.maggioliebook.domain.core.book.Libro
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.core.ConvertPdf2EpubUseCase
import it.filo.maggioliebook.usecase.core.progression.GetBookProgressionUseCase
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.readium.r2.shared.Injectable
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.asset.FileAsset
import org.readium.r2.shared.publication.services.isRestricted
import org.readium.r2.shared.publication.services.protectionError
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.mediatype.MediaType
import java.io.File
import java.net.URL
import kotlin.math.log

class ReaderRepository(private val application: Application, private val readium: Readium): KoinComponent {
    private val repository: MutableMap<String, ReaderInitData> = mutableMapOf()
    private val logTag: String = this.javaClass.simpleName

    operator fun get(isbn: String): ReaderInitData? = repository[isbn]

    suspend fun openBook(book: Libro?, activity: Activity): Try<Unit, Exception> {
        return try {
            openThrowing(book, activity)
            Try.success(Unit)
        } catch (e: Exception) {
            Try.failure(e)
        }
    }

    private suspend fun openThrowing(book: Libro?, activity: Activity) {
        if(book != null){
            val epubFile = File(application.filesDir, "/${book.isbn}.epub")

            Log.d(logTag, "EPUB FILE LOCATION: ${epubFile.path}")

            if(!epubFile.exists()){
                Log.d(logTag, "Epub file doesn't exist")
                ConvertPdf2EpubUseCase().invoke(book.isbn!!)?.let { epubFile.writeBytes(it) }
            } else {
                Log.d(logTag, "Epub file already exists")
            }

            val asset = FileAsset(epubFile, MediaType.EPUB)

            val publication: Publication = readium.streamer
                .open(asset, allowUserInteraction = true, sender = activity)
                .getOrThrow()

            // The publication is protected with a DRM and not unlocked.
            if (publication.isRestricted) {
                throw publication.protectionError
                    ?: Exception()
            }

            val initialLocator = GetBookProgressionUseCase().invoke(book.isbn!!).let {
                if(it.isNotEmpty())
                    Locator.fromJSON(JSONObject(it[0].progression))
                else
                    Locator.fromJSON(JSONObject("{}"))
            }

            Log.d(logTag, "INITIAL LOCATOR: $initialLocator")
            Log.d(logTag, "PUBLICATION: $publication")
            Log.d(logTag, "ISBN: ${book.isbn!!}")

            repository[book.isbn!!] = openVisual(book.isbn!!, publication, initialLocator)
        }
    }

    private fun openVisual(isbn: String, publication: Publication, initialLocator: Locator?): VisualReaderInitData {
        val url = prepareToServe(publication)
        return VisualReaderInitData(isbn, publication, url, initialLocator)
    }

    private fun prepareToServe(publication: Publication): URL {
        val userProperties = application.filesDir.path +
                "/" + Injectable.Style.rawValue + "/UserProperties.json"

        val url = checkNotNull(readium.server)
            .addPublication(publication, userPropertiesFile = File(userProperties))

        return url ?: throw Exception("Cannot add the publication to the HTTP server.")
    }

    fun close(isbn: String) {
        repository.remove(isbn)!!.publication.close()
    }
}