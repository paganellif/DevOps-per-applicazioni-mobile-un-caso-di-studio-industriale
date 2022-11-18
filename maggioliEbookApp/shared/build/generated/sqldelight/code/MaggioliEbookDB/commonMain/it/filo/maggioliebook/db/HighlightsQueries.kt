package it.filo.maggioliebook.db

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit

public interface HighlightsQueries : Transacter {
  public fun <T : Any> select(id: Long, mapper: (
    id: Long,
    created_date: Long?,
    isbn: String,
    style: String,
    tint: Int,
    href: String,
    type: String,
    title: String?,
    total_progression: Double,
    location: String,
    text: String,
    annotation_: String
  ) -> T): Query<T>

  public fun select(id: Long): Query<Highlights>

  public fun <T : Any> selectByIsbn(isbn: String, mapper: (
    id: Long,
    created_date: Long?,
    isbn: String,
    style: String,
    tint: Int,
    href: String,
    type: String,
    title: String?,
    total_progression: Double,
    location: String,
    text: String,
    annotation_: String
  ) -> T): Query<T>

  public fun selectByIsbn(isbn: String): Query<Highlights>

  public fun <T : Any> selectAll(mapper: (
    id: Long,
    created_date: Long?,
    isbn: String,
    style: String,
    tint: Int,
    href: String,
    type: String,
    title: String?,
    total_progression: Double,
    location: String,
    text: String,
    annotation_: String
  ) -> T): Query<T>

  public fun selectAll(): Query<Highlights>

  public fun insert(
    id: Long?,
    created_date: Long?,
    isbn: String,
    style: String,
    tint: Int,
    href: String,
    type: String,
    title: String?,
    total_progression: Double,
    location: String,
    text: String,
    annotation_: String
  ): Unit

  public fun remove(id: Long): Unit

  public fun removeByIsbn(isbn: String): Unit

  public fun clear(): Unit
}
