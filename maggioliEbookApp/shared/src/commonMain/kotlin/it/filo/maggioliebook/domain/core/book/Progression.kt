package it.filo.maggioliebook.domain.core.book

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Progression(
    @SerialName("isbn")
    val isbn: String,

    @SerialName("progression")
    val progression: String = "{}"
)