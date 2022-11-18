package it.filo.maggioliebook.domain.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val activated: Boolean?,
    val authorities: List<String>?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val langKey: String?,
    val login: String?
)
