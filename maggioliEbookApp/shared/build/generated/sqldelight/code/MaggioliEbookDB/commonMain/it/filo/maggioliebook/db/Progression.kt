package it.filo.maggioliebook.db

import kotlin.String

public data class Progression(
  public val isbn: String,
  public val progression: String
) {
  public override fun toString(): String = """
  |Progression [
  |  isbn: $isbn
  |  progression: $progression
  |]
  """.trimMargin()
}
