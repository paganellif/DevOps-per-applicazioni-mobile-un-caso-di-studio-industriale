package it.filo.maggioliebook.domain.core.book

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("created_date")
    val createdDate: Long? = null,

    @SerialName("isbn")
    val isbn: String,

    @SerialName("publication_id")
    val publicationId: String,

    @SerialName("resource_index")
    val resourceIndex: Long,

    @SerialName("resource_href")
    val resourceHref: String,

    @SerialName("resource_type")
    val resourceType: String,

    @SerialName("resource_title")
    val resourceTitle: String,

    @SerialName("location")
    val location: String,

    @SerialName("locator_text")
    val locatorText: String
)