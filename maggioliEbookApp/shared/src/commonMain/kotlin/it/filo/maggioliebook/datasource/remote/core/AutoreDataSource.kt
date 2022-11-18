package it.filo.maggioliebook.datasource.remote.core

import io.ktor.client.request.get
import it.filo.maggioliebook.datasource.remote.BaseDataSource
import it.filo.maggioliebook.util.JwtManager

internal class AutoreDataSource(
    private val jwtManager: JwtManager
): BaseDataSource(jwtManager) {
    override val apiUrl: String = "https://reda-test.maggiolicloud.it"
    private val baseUrl: String = "api/autori"

    /**
     *
     */
    suspend fun getAutore(id: String) = client.get {
        apiUrl("$baseUrl/$id")
    }

    /**
     *
     */
    suspend fun searchAutore() = client.get {
        apiUrl(baseUrl) // TODO
    }

}