package it.filo.maggioliebook.datasource.remote.core

import io.ktor.client.request.get
import it.filo.maggioliebook.datasource.remote.DocDataSource
import it.filo.maggioliebook.util.JwtManager

internal class FascicoloDataSource(
    private val jwtManager: JwtManager
): DocDataSource(jwtManager) {

    override val baseUrl: String = "api/fascicolo"

    /**
     *
     */
    suspend fun downloadAbstract(id: String) = client.get {
        apiUrl("$baseUrl/$id/abstract")
    }
}