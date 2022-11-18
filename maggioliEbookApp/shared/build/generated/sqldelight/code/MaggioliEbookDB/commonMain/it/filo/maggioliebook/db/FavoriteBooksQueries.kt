package it.filo.maggioliebook.db

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.String
import kotlin.Unit

public interface FavoriteBooksQueries : Transacter {
  public fun select(isbn: String): Query<String>

  public fun selectAll(): Query<String>

  public fun insert(isbn: String): Unit

  public fun remove(isbn: String): Unit

  public fun clear(): Unit
}
