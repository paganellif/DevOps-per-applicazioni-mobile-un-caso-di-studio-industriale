package it.filo.maggioliebook.datasource.remote.core

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import it.filo.maggioliebook.datasource.remote.DocDataSource
import it.filo.maggioliebook.util.JwtManager

internal class LibroDataSource(
    private val jwtManager: JwtManager
): DocDataSource(jwtManager) {

    override val baseUrl: String = "api/libro"

    /**
     *
     */
    suspend fun getIndice(isbn: String) = client.get {
        apiUrl("$baseUrl/$isbn/indice")
    }

    /**
     *
     */
    suspend fun listPagineWithRif(isbn: String, page: String?) = client.get {
        apiUrl("$baseUrl/$isbn/pagine-with-rif")
        if(!page.isNullOrEmpty()) parameter("page", page)
    }

    /**
     *
     */
    suspend fun getPagina(isbn: String, numPagina: Int) = client.get {
        apiUrl("$baseUrl/$isbn/pagina/$numPagina")
    }

}