package it.filo.maggioliebook.util

import com.liftric.kvault.KVault
import it.filo.maggioliebook.domain.user.JwtToken

internal class JwtManager(private val vault: KVault) {

    private val key: String = "token"

    /**
     *
     */
    fun getAuthToken(): JwtToken = JwtToken(vault.string(key).orEmpty())

    /**
     *
     */
    fun isStoredAuthToken(): Boolean = vault.existsObject(key)

    /**
     *
     */
    fun storeAuthToken(token: String): Boolean = vault.set(key, token)

    /**
     *
     */
    fun removeAuthToken(): Boolean = vault.deleteObject(key)
}