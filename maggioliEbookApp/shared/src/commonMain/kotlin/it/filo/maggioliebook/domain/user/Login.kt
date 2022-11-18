package it.filo.maggioliebook.domain.user

import kotlinx.serialization.Serializable

@Serializable
internal data class Login(
    val username: String,
    val password: String,
    val rememberMe: Boolean
)
