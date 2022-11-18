package it.filo.maggioliebook.android.utils.extensions

import it.filo.maggioliebook.domain.core.book.Bookmark
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator

fun Bookmark.toLocator(): Locator = Locator(
    href = resourceHref,
    type = resourceType,
    title = resourceTitle,
    locations = Locator.Locations.fromJSON(JSONObject(location)),
    text = Locator.Text.fromJSON(JSONObject(locatorText))
)