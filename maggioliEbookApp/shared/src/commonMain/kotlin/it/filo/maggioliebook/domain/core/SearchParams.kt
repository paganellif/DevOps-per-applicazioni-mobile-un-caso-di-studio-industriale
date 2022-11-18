package it.filo.maggioliebook.domain.core

import kotlinx.serialization.Serializable

@Serializable
internal class SearchParams(
    val query: String? = null,
    val type: List<String>? = null,
    val id: List<String>? = null,
    val did: List<String>? = null,
    val limit: String? = null,
    val size: String? = null,
    val page: String? = null,
    val sort: String? = null
)
