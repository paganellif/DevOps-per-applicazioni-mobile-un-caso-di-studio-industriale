package it.filo.maggioliebook.db

import kotlin.String

public data class FavoriteBooks(
  public val isbn: String
) {
  public override fun toString(): String = """
  |FavoriteBooks [
  |  isbn: $isbn
  |]
  """.trimMargin()
}
