package it.filo.maggioliebook.util.extensions

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import it.filo.maggioliebook.db.MaggioliEbookDB
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

internal actual fun Scope.createDriver(databaseName: String): SqlDriver =
    AndroidSqliteDriver(MaggioliEbookDB.Schema, androidContext(), databaseName)