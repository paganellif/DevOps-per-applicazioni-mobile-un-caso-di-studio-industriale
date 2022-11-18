package it.filo.maggioliebook.datasource.remote.user

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.ContentType
import it.filo.maggioliebook.datasource.remote.BaseDataSource
import it.filo.maggioliebook.domain.user.Login
import it.filo.maggioliebook.util.JwtManager
import kotlin.native.concurrent.SharedImmutable

internal class LoginDataSource(
    private val jwtManager: JwtManager
): BaseDataSource(jwtManager) {
    override val apiUrl: String = "https://reda-test.maggiolicloud.it"
    private val baseUrl: String = "api/authenticate"

    /**
     *
     */
    suspend fun login(username: String, password: String, rememberMe: Boolean) =
        client.post {
            apiUrl(baseUrl)
            contentType(ContentType.Application.Json)
            setBody(Login(username, password, rememberMe))
        }
}