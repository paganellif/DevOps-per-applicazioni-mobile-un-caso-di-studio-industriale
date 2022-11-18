package it.filo.maggioliebook.repository.core

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.util.*
import it.filo.maggioliebook.datasource.remote.core.FascicoloDataSource
import it.filo.maggioliebook.domain.core.Fascicolo
import it.filo.maggioliebook.domain.core.PaginaFascicolo
import it.filo.maggioliebook.util.DispatcherProvider
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FascicoloRepository: KoinComponent {
    private val dispatcherProvider: DispatcherProvider by inject()
    private val fascicoloService: FascicoloDataSource by inject()

    /**
     *
     */
    suspend fun getFascicoloMetadata(id: String): Fascicolo = withContext(dispatcherProvider.io){
        fascicoloService.getInfo(id).body()
    }

    /**
     *
     */
    suspend fun getFascicoloCover(id: String): ByteArray = withContext(dispatcherProvider.io){
        fascicoloService.downloadImage(id).bodyAsChannel().toByteArray()
    }

    /**
     *
     */
    suspend fun getFascicoloContent(id: String): ByteArray = withContext(dispatcherProvider.io){
        fascicoloService.downloadFile(id).bodyAsChannel().toByteArray()
    }

    /**
     *
     */
    suspend fun getFascicoloAbstract(id: String): ByteArray = withContext(dispatcherProvider.io){
        fascicoloService.downloadAbstract(id).bodyAsChannel().toByteArray()
    }

    /**
     *
     */
    suspend fun getPagineFascicolo(id: String, page: String?): List<PaginaFascicolo> =
        withContext(dispatcherProvider.io){
            fascicoloService.listPagine(id, page).body()
        }


}