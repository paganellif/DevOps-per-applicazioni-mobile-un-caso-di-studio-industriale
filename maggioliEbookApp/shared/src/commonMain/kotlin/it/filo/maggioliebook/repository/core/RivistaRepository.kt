package it.filo.maggioliebook.repository.core

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.util.*
import it.filo.maggioliebook.datasource.remote.core.RivistaDataSource
import it.filo.maggioliebook.domain.core.Rivista
import it.filo.maggioliebook.util.DispatcherProvider
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RivistaRepository: KoinComponent {
    private val dispatcherProvider: DispatcherProvider by inject()
    private val rivistaService: RivistaDataSource by inject()

    /**
     *
     */
    suspend fun getRivistaMetadata(id: String): Rivista = withContext(dispatcherProvider.io){
        rivistaService.getInfo(id).body()
    }

    /**
     *
     */
    suspend fun getRivistaCover(id: String): ByteArray = withContext(dispatcherProvider.io){
        rivistaService.downloadImage(id).bodyAsChannel().toByteArray()
    }
}