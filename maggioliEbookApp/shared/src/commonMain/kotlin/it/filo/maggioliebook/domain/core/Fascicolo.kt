package it.filo.maggioliebook.domain.core

import kotlinx.serialization.Serializable

@Serializable
data class Fascicolo (
    val id: Int?,
    val anno: Int?,
    val numero: String?,
    val podcastId: String?,
    val periodoRiferimento: String?,
    val numberOfPages: Int?,
    val imageExist: Boolean?,
    val gratuito: Boolean?,
    val rivista: Rivista?,
    //val voceContenutiAggiuntivi: VoceAlbero,
    val abstractExist: Boolean?
)
