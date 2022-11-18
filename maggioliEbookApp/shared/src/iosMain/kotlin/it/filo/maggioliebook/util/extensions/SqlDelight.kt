package it.filo.maggioliebook.util.extensions

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import it.filo.maggioliebook.db.MaggioliEbookDB
import org.koin.core.scope.Scope

internal actual fun Scope.createDriver(databaseName: String): SqlDriver =
    NativeSqliteDriver(MaggioliEbookDB.Schema, databaseName)