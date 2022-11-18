package it.filo.maggioliebook.domain.core.book

import kotlinx.serialization.Serializable

@Serializable
internal data class PaginaLibro (
    val id: String?,
    val libro: Libro?,

    val pagina: Int?,
    val sezione: String?,
    val libreriaConversione: String?,
    val content: String?
    //val riferimentiHtml: List<HtmlCleaner2Corrected>?
)