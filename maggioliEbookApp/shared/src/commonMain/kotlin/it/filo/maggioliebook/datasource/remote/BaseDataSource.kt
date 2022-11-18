package it.filo.maggioliebook.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import it.filo.maggioliebook.util.JwtManager
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

internal abstract class BaseDataSource(private val jwtManager: JwtManager) {

    abstract val apiUrl: String

    @OptIn(ExperimentalSerializationApi::class)
    val client = HttpClient {
        install(HttpCache)
        install(HttpTimeout)
        install(ContentNegotiation) {
            json( Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
    }

    fun HttpRequestBuilder.apiUrl(path: String) {
        url {
            takeFrom(apiUrl)
            path(path)
        }

        if (jwtManager.isStoredAuthToken())
            bearerAuth(jwtManager.getAuthToken().token)
    }
}

fun HttpResponse.isValid(): Boolean = status == HttpStatusCode.OK // TODO: fix
