package it.filo.maggioliebook.datasource.remote.core

import it.filo.maggioliebook.datasource.remote.DocDataSource
import it.filo.maggioliebook.util.JwtManager

internal class RivistaDataSource(
    private val jwtManager: JwtManager
): DocDataSource(jwtManager) {

    override val baseUrl: String = "api/rivista"
}