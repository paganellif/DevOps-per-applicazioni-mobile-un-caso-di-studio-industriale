package it.filo.maggioliebook.domain.core

import kotlinx.serialization.Serializable

@Serializable
internal data class Autore (
    val id: Int?,
    val nome: String?,
    val cognome: String?,
    val autore: String?
)
