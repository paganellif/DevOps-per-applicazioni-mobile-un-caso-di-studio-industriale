package it.filo.maggioliebook.domain.core

import kotlinx.serialization.Serializable

@Serializable
data class Rivista (
    val titolo: String?,
    val descrizione: String?,
    val presentazione: String?,
    val periodicita: String?,
    val direttore: String?,
    val area: String?,
    val fondazione: String?,
    val attiva: String?,
    val imageExist: String?,
    //image: MultimediaObj?,
    val showSpreakerId: String?
)
