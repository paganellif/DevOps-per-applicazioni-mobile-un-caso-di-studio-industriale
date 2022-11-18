package it.filo.maggioliebook.db

import kotlin.Long
import kotlin.String

public data class Bookmark(
  public val id: Long,
  public val created_date: Long?,
  public val isbn: String,
  public val publication_id: String,
  public val resource_index: Long,
  public val resource_href: String,
  public val resource_type: String,
  public val resource_title: String,
  public val location: String,
  public val locator_text: String
) {
  public override fun toString(): String = """
  |Bookmark [
  |  id: $id
  |  created_date: $created_date
  |  isbn: $isbn
  |  publication_id: $publication_id
  |  resource_index: $resource_index
  |  resource_href: $resource_href
  |  resource_type: $resource_type
  |  resource_title: $resource_title
  |  location: $location
  |  locator_text: $locator_text
  |]
  """.trimMargin()
}
