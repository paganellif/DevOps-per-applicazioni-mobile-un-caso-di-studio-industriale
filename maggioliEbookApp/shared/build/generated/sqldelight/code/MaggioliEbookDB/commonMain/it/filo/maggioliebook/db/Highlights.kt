package it.filo.maggioliebook.db

import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String

public data class Highlights(
  public val id: Long,
  public val created_date: Long?,
  public val isbn: String,
  public val style: String,
  public val tint: Int,
  public val href: String,
  public val type: String,
  public val title: String?,
  public val total_progression: Double,
  public val location: String,
  public val text: String,
  public val annotation_: String
) {
  public override fun toString(): String = """
  |Highlights [
  |  id: $id
  |  created_date: $created_date
  |  isbn: $isbn
  |  style: $style
  |  tint: $tint
  |  href: $href
  |  type: $type
  |  title: $title
  |  total_progression: $total_progression
  |  location: $location
  |  text: $text
  |  annotation_: $annotation_
  |]
  """.trimMargin()
}
