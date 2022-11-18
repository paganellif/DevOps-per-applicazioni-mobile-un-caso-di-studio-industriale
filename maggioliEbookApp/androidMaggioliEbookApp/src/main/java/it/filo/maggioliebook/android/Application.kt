package it.filo.maggioliebook.android

import android.app.Application
import it.filo.maggioliebook.android.reader.ReaderRepository
import it.filo.maggioliebook.di.initKoin
import it.filo.maggioliebook.util.initNapier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.io.File
import java.util.*

class Application: Application() {

    lateinit var readium: Readium
        private set

    lateinit var storageDir: File

    private val coroutineScope: CoroutineScope =
        MainScope()

    private val readiumModule = module { single { ReaderRepository(this@Application, readium) }}

    override fun onCreate() {
        super.onCreate()

        // Init KMM Logging Library
        initNapier()

        // Init KMM Dependency Injection Library
        initKoin {
            androidContext(this@Application)
            modules(listOf(readiumModule))
        }

        readium = Readium(this)

        readium.onAppStart()

        storageDir = computeStorageDir()

    }

    override fun onTerminate() {
        super.onTerminate()
        readium.onAppTerminate()
        stopKoin()
    }

    private fun computeStorageDir(): File {
        val properties = Properties()
        val inputStream = assets.open("configs/config.properties")
        properties.load(inputStream)
        val useExternalFileDir =
            properties.getProperty("useExternalFileDir", "false")!!.toBoolean()

        return File(
            if (useExternalFileDir) getExternalFilesDir(null)?.path + "/"
            else filesDir?.path + "/"
        )
    }
}