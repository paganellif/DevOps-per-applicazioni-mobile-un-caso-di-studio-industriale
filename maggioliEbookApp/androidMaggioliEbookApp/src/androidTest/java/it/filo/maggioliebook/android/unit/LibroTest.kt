package it.filo.maggioliebook.android.unit

import androidx.test.ext.junit.rules.ActivityScenarioRule
import it.filo.maggioliebook.android.MainActivity
import it.filo.maggioliebook.repository.core.LibroRepository
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.junit.Assert.*
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class LibroTest: KoinTest{

    val libroRepository: LibroRepository by inject()

    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun searchSizeBooksTest() {
        runBlocking {
            val size = 20
            val response = libroRepository.searchBooks("", size = size)
            assertEquals(true, response!!.isNotEmpty())
            assertEquals(size, response.size)
        }
    }

    @Test
    fun searchQueryBooksTest() {
        runBlocking {
            val response = libroRepository.searchBooks(UUID.randomUUID().toString())
            assertEquals(true, response!!.isEmpty())
        }
    }

    @Test
    fun bookCountTest() {
        runBlocking {
            val response = libroRepository.getBooksCount()
            assertNotEquals(0, response)
        }
    }

    @Test
    fun bookEmptyMetadataTest() {
        runBlocking {
            val isbn = "FaKe-IsBn!"
            val response = libroRepository.getBookMetadata(isbn)
            assertEquals(true, response == null)
        }
    }

    @Test
    fun bookMetadataTest() {
        runBlocking {
            val isbn = "9788891658395"
            val expectedTitle = "Fiere, Sagre, Feste Paesane e Spettacoli viaggianti"
            val expectedAuthors = listOf("Elena Fiore","Miranda Corradi")
            val response = libroRepository.getBookMetadata(isbn)
            assertEquals(true, response != null)
            assertEquals(expectedTitle, response!!.name)
            assertEquals(true, response.autori!!.containsAll(expectedAuthors))
        }
    }
}