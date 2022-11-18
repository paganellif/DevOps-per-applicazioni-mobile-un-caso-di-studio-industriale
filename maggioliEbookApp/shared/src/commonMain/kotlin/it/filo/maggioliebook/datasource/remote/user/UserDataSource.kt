package it.filo.maggioliebook.datasource.remote.user

import io.ktor.client.request.get
import io.ktor.http.*
import it.filo.maggioliebook.datasource.remote.BaseDataSource
import it.filo.maggioliebook.util.JwtManager

internal class UserDataSource(
    private val jwtManager: JwtManager
): BaseDataSource(jwtManager) {
    override val apiUrl: String = "https://reda-test.maggiolicloud.it"
    private val baseUrl: String = "api/account"

    /**
     *
     */
    suspend fun getUserInfo() =
        client.get {
            apiUrl(baseUrl)
            contentType(ContentType.Application.Json)
        }
}