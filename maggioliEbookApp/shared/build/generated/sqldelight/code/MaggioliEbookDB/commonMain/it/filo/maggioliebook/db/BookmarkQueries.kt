package it.filo.maggioliebook.db

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.Long
import kotlin.String
import kotlin.Unit

public interface BookmarkQueries : Transacter {
  public fun <T : Any> select(id: Long, mapper: (
    id: Long,
    created_date: Long?,
    isbn: String,
    publication_id: String,
    resource_index: Long,
    resource_href: String,
    resource_type: String,
    resource_title: String,
    location: String,
    locator_text: String
  ) -> T): Query<T>

  public fun select(id: Long): Query<Bookmark>

  public fun <T : Any> selectByIsbn(isbn: String, mapper: (
    id: Long,
    created_date: Long?,
    isbn: String,
    publication_id: String,
    resource_index: Long,
    resource_href: String,
    resource_type: String,
    resource_title: String,
    location: String,
    locator_text: String
  ) -> T): Query<T>

  public fun selectByIsbn(isbn: String): Query<Bookmark>

  public fun <T : Any> selectAll(mapper: (
    id: Long,
    created_date: Long?,
    isbn: String,
    publication_id: String,
    resource_index: Long,
    resource_href: String,
    resource_type: String,
    resource_title: String,
    location: String,
    locator_text: String
  ) -> T): Query<T>

  public fun selectAll(): Query<Bookmark>

  public fun insert(
    id: Long?,
    created_date: Long?,
    isbn: String,
    publication_id: String,
    resource_index: Long,
    resource_href: String,
    resource_type: String,
    resource_title: String,
    location: String,
    locator_text: String
  ): Unit

  public fun remove(id: Long): Unit

  public fun removeByIsbn(isbn: String): Unit

  public fun clear(): Unit
}
