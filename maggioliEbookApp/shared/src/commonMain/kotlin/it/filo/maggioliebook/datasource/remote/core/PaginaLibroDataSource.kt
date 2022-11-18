package it.filo.maggioliebook.datasource.remote.core

import io.ktor.client.request.get
import it.filo.maggioliebook.datasource.remote.BaseDataSource
import it.filo.maggioliebook.domain.core.book.LibroPaginaType
import it.filo.maggioliebook.util.JwtManager

internal class PaginaLibroDataSource(
    private val jwtManager: JwtManager
): BaseDataSource(jwtManager) {
    override val apiUrl: String = "https://reda-test.maggiolicloud.it"
    private val baseUrl: String = "api/pagina"

    /**
     *
     */
    suspend fun getBookPage(id: String) = client.get {
        apiUrl("$baseUrl/$id")
    }

    /**
     *
     */
    suspend fun getHtml(id: String, type: LibroPaginaType) = client.get {
        apiUrl("$baseUrl/$id/preview-$type")
    }
}