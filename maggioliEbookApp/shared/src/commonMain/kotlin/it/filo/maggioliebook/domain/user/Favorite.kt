package it.filo.maggioliebook.domain.user

import kotlinx.serialization.Serializable

@Serializable
data class Favorite (
    val isbn: String
)
