package it.filo.maggioliebook.domain.core.book

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Highlight(
    @SerialName("id")
    var id: Long? = null,

    @SerialName("created_date")
    var createdDate: Long? = null,

    @SerialName("isbn")
    var isbn: String,

    @SerialName("style")
    var style: String, // TODO: Style Readium

    @SerialName("tint")
    var tint: Int = 0,

    @SerialName("href")
    var href: String,

    @SerialName("type")
    var type: String,

    @SerialName("title")
    var title: String? = null,

    @SerialName("total_progression")
    var totalProgression: Double = 0.0,

    @SerialName("location")
    var location: String = "{}", // TODO Locator.Locations Readium

    @SerialName("text")
    var text: String = "{}", // TODO Locator.Text Readium

    @SerialName("annotation_")
    var annotation: String = ""
)
