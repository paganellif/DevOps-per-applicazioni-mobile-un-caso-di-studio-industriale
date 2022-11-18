package it.filo.maggioliebook.domain.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class JwtToken(
    @SerialName("id_token")
    val token: String
)
