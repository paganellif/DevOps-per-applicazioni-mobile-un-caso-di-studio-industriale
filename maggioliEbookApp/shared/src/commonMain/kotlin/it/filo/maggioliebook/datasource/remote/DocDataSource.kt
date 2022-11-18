package it.filo.maggioliebook.datasource.remote

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import it.filo.maggioliebook.domain.core.SearchParams
import it.filo.maggioliebook.util.JwtManager

internal abstract class DocDataSource(jwtManager: JwtManager): BaseDataSource(jwtManager) {

    abstract val baseUrl: String
    override val apiUrl: String = "https://reda-test.maggiolicloud.it"

    /**
     *
     */
    suspend fun getInfo(id: String) = client.get {
        apiUrl("$baseUrl/$id")
    }

    /**
     *
     */
    suspend fun getOutline(id: String) = client.get {
        apiUrl("$baseUrl/$id/linkless-outline")
    }

    /**
     *
     */
    suspend fun search(searchParams: SearchParams) = client.get {
        contentType(ContentType.Application.Json)
        if (!searchParams.query.isNullOrEmpty() && searchParams.query != "null")
            parameter("query", searchParams.query)
        if (!searchParams.type.isNullOrEmpty())
            parameter("type", searchParams.type)
        if (!searchParams.id.isNullOrEmpty())
            parameter("id", searchParams.id)
        if (!searchParams.did.isNullOrEmpty())
            parameter("did", searchParams.did)
        if (!searchParams.limit.isNullOrEmpty() && searchParams.limit != "null")
            parameter("limit", searchParams.limit)
        if (!searchParams.size.isNullOrEmpty() && searchParams.size != "null")
            parameter("size", searchParams.size)
        if (!searchParams.page.isNullOrEmpty() && searchParams.page != "null")
            parameter("page", searchParams.page)
        if (!searchParams.sort.isNullOrEmpty() && searchParams.sort != "null")
            parameter("sort", searchParams.sort)
        apiUrl(baseUrl)
    }

    /**
     *
     */
    suspend fun listPagine(id: String, page: String?) = client.get {
        apiUrl("$baseUrl/$id/pagine")
        if (!page.isNullOrEmpty()) parameter("page", page)
    }

    /**
     *
     */
    suspend fun downloadFile(id: String) = client.get {
        apiUrl("$baseUrl/$id/file")
    }

    /**
     *
     */
    suspend fun downloadImage(id: String) = client.get {
        apiUrl("$baseUrl/$id/image")
        timeout {
            requestTimeoutMillis = 10000
        }
    }
}