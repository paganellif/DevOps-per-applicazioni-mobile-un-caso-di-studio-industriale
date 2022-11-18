package it.filo.maggioliebook.domain.core.book

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Libro (
    @SerialName("isbn")
    val isbn: String?,

    @SerialName("name")
    val name: String?,

    @SerialName("type")
    val type: String?,

    @SerialName("aree")
    val aree: List<String>?,

    @SerialName("numberOfPages")
    val numberOfPages: Int = 0,

    @SerialName("description")
    val description: String?,

    @SerialName("editore")
    val editore: String?,

    @SerialName("short_description")
    val shortDescription: String?,

    @SerialName("id_legacy")
    val idLegacy: Int?,

    @SerialName("anno")
    val anno: Int?,

    @SerialName("mese")
    val mese: Int?,

    @SerialName("pagina_sito")
    val paginaSito: String?,

    @SerialName("autori")
    val autori: List<String>?,

    @SerialName("firstValidPage")
    val firstValidPage: Int = 0,

    @SerialName("lastValidPage")
    val lastValidPage: Int?,

    @SerialName("edizione")
    val edizione: String?,

    @SerialName("collana")
    val collana: String?,

    @SerialName("formato_cartaceo")
    val formatoCartaceo: String?,

    @SerialName("anno_mese")
    val annoMese: String?,

    @SerialName("sottotitolo")
    val sottotitolo: String?,

    @SerialName("image_url")
    val imageUrl: String?,

    @SerialName("codice_interno_shop")
    val codiceInternoShop: String?,

    @SerialName("createdDate")
    val createdDate: Instant?,

    @SerialName("lastModifiedDate")
    val lastModifiedDate: Instant?
)