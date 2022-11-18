package it.filo.maggioliebook.android.utils.extensions

import android.os.Bundle
import androidx.annotation.ColorInt
import it.filo.maggioliebook.domain.core.book.Highlight
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.ExperimentalDecorator
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.MapWithDefaultCompanion


enum class Style(val value: String) {
    HIGHLIGHT("highlight"), UNDERLINE("underline");

    companion object : MapWithDefaultCompanion<String, Style>(values(), Style::value, HIGHLIGHT)
}

/**
 * Decoration Style for a page margin icon.
 *
 * This is an example of a custom Decoration Style declaration.
 */
@Parcelize
@OptIn(ExperimentalDecorator::class)
data class DecorationStyleAnnotationMark(@ColorInt val tint: Int) : Decoration.Style

fun Highlight.toLocator(): Locator = Locator(
    href = href,
    type = type,
    title = title,
    locations = if (location.isNotEmpty()) Locator.Locations.fromJSON(JSONObject(location)) else Locator.Locations(),
    text = if (text.isNotEmpty()) Locator.Text.fromJSON(JSONObject(text)) else Locator.Text()
)

/**
 * Creates a list of [Decoration] for the receiver [Highlight].
 */
@OptIn(ExperimentalDecorator::class)
fun Highlight.toDecorations(isActive: Boolean): List<Decoration> {
    fun createDecoration(idSuffix: String, style: Decoration.Style) = Decoration(
        id = "$id-$idSuffix",
        locator = this.toLocator(),
        style = style,
        extras = Bundle().apply {
            // We store the highlight's database ID in the extras bundle, for easy retrieval
            // later. You can store arbitrary information in the bundle.
            putLong("id", id!!) // FIXME: id è null quando viene creato, nel db c'è autoincrement
        }
    )

    return listOfNotNull(
        // Decoration for the actual highlight / underline.
        createDecoration(
            idSuffix = "highlight",
            style = when (style) {
                Style.HIGHLIGHT.toString() -> Decoration.Style.Highlight(tint = tint, isActive = isActive)
                Style.UNDERLINE.toString() -> Decoration.Style.Underline(tint = tint, isActive = isActive)
                else -> Decoration.Style.Highlight(tint = tint, isActive = isActive)
            }
        ),
        // Additional page margin icon decoration, if the highlight has an associated note.
        annotation.takeIf { it.isNotEmpty() }?.let {
            createDecoration(
                idSuffix = "annotation",
                style = DecorationStyleAnnotationMark(tint = tint),
            )
        }
    )
}