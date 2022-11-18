package it.filo.maggioliebook.datasource.remote.util

import io.github.aakira.napier.Napier
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import it.filo.maggioliebook.datasource.remote.BaseDataSource
import it.filo.maggioliebook.util.JwtManager

internal class Pdf2EpubService(
    private val jwtManager: JwtManager
): BaseDataSource(jwtManager) {

    override val apiUrl: String = "https://ng-onlyoffice.maggiolicloud.it"
    val baseUrl: String = "pdf2epub"

    /**
     *
     */
    suspend fun convert(
        pdfToBeConverted: ByteArray,
        isbn: String,
        title: String,
        epubVersion: Int = 3,
        publisher: String? = null,
        authors: List<String>? = null
    ) = client.submitFormWithBinaryData(
            formData = formData {
                append("file", pdfToBeConverted, Headers.build {
                    append(HttpHeaders.ContentType, "application/pdf")
                    append(HttpHeaders.ContentDisposition, "filename=\"$isbn.pdf\"")
                })

                // METADATA OPTION
                append("publisher", publisher.toString().escapeIfNeeded())
                append("authors", authors.toString().escapeIfNeeded())
                append("author-sort", "")
                append("isbn", isbn.escapeIfNeeded())
                append("title", title.escapeIfNeeded())

                // EPUB OUTPUT OPTION
                append("pretty-print", "")
                append("no-default-epub-cover", "")
                append("epub-version", epubVersion)
            }
        ){
        apiUrl("$baseUrl/convert")
    }

}