package it.filo.maggioliebook.domain.core

import kotlinx.serialization.Serializable

@Serializable
data class PaginaFascicolo (
    val id: String?,
    val pagina: Int?,
    val content: String?
)