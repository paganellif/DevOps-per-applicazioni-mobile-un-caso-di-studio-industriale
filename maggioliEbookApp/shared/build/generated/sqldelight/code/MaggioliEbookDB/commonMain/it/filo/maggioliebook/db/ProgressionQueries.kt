package it.filo.maggioliebook.db

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.String
import kotlin.Unit

public interface ProgressionQueries : Transacter {
  public fun <T : Any> select(isbn: String, mapper: (isbn: String, progression: String) -> T):
      Query<T>

  public fun select(isbn: String): Query<Progression>

  public fun <T : Any> selectAll(mapper: (isbn: String, progression: String) -> T): Query<T>

  public fun selectAll(): Query<Progression>

  public fun insert(isbn: String, progression: String): Unit

  public fun remove(isbn: String): Unit

  public fun clear(): Unit
}
