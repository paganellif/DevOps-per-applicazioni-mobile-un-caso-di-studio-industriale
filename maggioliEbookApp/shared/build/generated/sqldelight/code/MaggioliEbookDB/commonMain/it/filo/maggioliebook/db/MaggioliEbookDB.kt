package it.filo.maggioliebook.db

import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.db.SqlDriver
import it.filo.maggioliebook.db.shared.newInstance
import it.filo.maggioliebook.db.shared.schema

public interface MaggioliEbookDB : Transacter {
  public val bookmarkQueries: BookmarkQueries

  public val favoriteBooksQueries: FavoriteBooksQueries

  public val highlightsQueries: HighlightsQueries

  public val progressionQueries: ProgressionQueries

  public companion object {
    public val Schema: SqlDriver.Schema
      get() = MaggioliEbookDB::class.schema

    public operator fun invoke(driver: SqlDriver): MaggioliEbookDB =
        MaggioliEbookDB::class.newInstance(driver)
  }
}
