package it.filo.maggioliebook.android.reader

import org.readium.r2.shared.publication.*
import java.net.URL

sealed class ReaderInitData {
    abstract val isbn: String
    abstract val publication: Publication
}

data class VisualReaderInitData(
    override val isbn: String,
    override val publication: Publication,
    val baseUrl: URL? = null,
    val initialLocation: Locator? = null
) : ReaderInitData()

/* @ExperimentalMedia
data class MediaReaderInitData(
    override val isbn: String,
    override val publication: Publication,
    val mediaNavigator: MediaNavigator,
) : ReaderInitData()*/

data class DummyReaderInitData(
    override val isbn: String,
) : ReaderInitData() {
    override val publication: Publication = Publication(Manifest(
        metadata = Metadata(identifier = "dummy", localizedTitle = LocalizedString(""))
    ))
}
