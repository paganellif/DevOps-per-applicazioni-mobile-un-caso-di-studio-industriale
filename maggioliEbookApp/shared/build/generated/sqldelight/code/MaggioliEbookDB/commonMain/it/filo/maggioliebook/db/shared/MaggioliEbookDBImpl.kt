package it.filo.maggioliebook.db.shared

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.TransacterImpl
import com.squareup.sqldelight.`internal`.copyOnWriteList
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import it.filo.maggioliebook.db.Bookmark
import it.filo.maggioliebook.db.BookmarkQueries
import it.filo.maggioliebook.db.FavoriteBooksQueries
import it.filo.maggioliebook.db.Highlights
import it.filo.maggioliebook.db.HighlightsQueries
import it.filo.maggioliebook.db.MaggioliEbookDB
import it.filo.maggioliebook.db.Progression
import it.filo.maggioliebook.db.ProgressionQueries
import kotlin.Any
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.collections.MutableList
import kotlin.reflect.KClass

internal val KClass<MaggioliEbookDB>.schema: SqlDriver.Schema
  get() = MaggioliEbookDBImpl.Schema

internal fun KClass<MaggioliEbookDB>.newInstance(driver: SqlDriver): MaggioliEbookDB =
    MaggioliEbookDBImpl(driver)

private class MaggioliEbookDBImpl(
  driver: SqlDriver
) : TransacterImpl(driver), MaggioliEbookDB {
  public override val bookmarkQueries: BookmarkQueriesImpl = BookmarkQueriesImpl(this, driver)

  public override val favoriteBooksQueries: FavoriteBooksQueriesImpl =
      FavoriteBooksQueriesImpl(this, driver)

  public override val highlightsQueries: HighlightsQueriesImpl = HighlightsQueriesImpl(this, driver)

  public override val progressionQueries: ProgressionQueriesImpl = ProgressionQueriesImpl(this,
      driver)

  public object Schema : SqlDriver.Schema {
    public override val version: Int
      get() = 1

    public override fun create(driver: SqlDriver): Unit {
      driver.execute(null, """
          |CREATE TABLE progression(
          |    isbn TEXT PRIMARY KEY NOT NULL,
          |    progression TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE highlights(
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    created_date INTEGER,
          |    isbn TEXT NOT NULL,
          |    style TEXT NOT NULL,
          |    tint INTEGER NOT NULL,
          |    href TEXT NOT NULL,
          |    type TEXT NOT NULL,
          |    title TEXT,
          |    total_progression REAL NOT NULL,
          |    location TEXT NOT NULL,
          |    text TEXT NOT NULL,
          |    annotation TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE favoriteBooks(
          |    isbn TEXT PRIMARY KEY NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE bookmark(
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    created_date INTEGER,
          |    isbn TEXT NOT NULL,
          |    publication_id TEXT NOT NULL,
          |    resource_index INTEGER NOT NULL,
          |    resource_href TEXT NOT NULL,
          |    resource_type TEXT NOT NULL,
          |    resource_title TEXT NOT NULL,
          |    location TEXT NOT NULL,
          |    locator_text TEXT NOT NULL
          |)
          """.trimMargin(), 0)
    }

    public override fun migrate(
      driver: SqlDriver,
      oldVersion: Int,
      newVersion: Int
    ): Unit {
    }
  }
}

private class ProgressionQueriesImpl(
  private val database: MaggioliEbookDBImpl,
  private val driver: SqlDriver
) : TransacterImpl(driver), ProgressionQueries {
  internal val select: MutableList<Query<*>> = copyOnWriteList()

  internal val selectAll: MutableList<Query<*>> = copyOnWriteList()

  public override fun <T : Any> select(isbn: String, mapper: (isbn: String,
      progression: String) -> T): Query<T> = SelectQuery(isbn) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!
    )
  }

  public override fun select(isbn: String): Query<Progression> = select(isbn) { isbn_,
      progression ->
    Progression(
      isbn_,
      progression
    )
  }

  public override fun <T : Any> selectAll(mapper: (isbn: String, progression: String) -> T):
      Query<T> = Query(2101228220, selectAll, driver, "Progression.sq", "selectAll",
      "SELECT * FROM progression") { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!
    )
  }

  public override fun selectAll(): Query<Progression> = selectAll { isbn, progression ->
    Progression(
      isbn,
      progression
    )
  }

  public override fun insert(isbn: String, progression: String): Unit {
    driver.execute(-215130526, """
    |INSERT OR REPLACE INTO progression(isbn, progression)
    |    VALUES (?, ?)
    """.trimMargin(), 2) {
      bindString(1, isbn)
      bindString(2, progression)
    }
    notifyQueries(-215130526, {database.progressionQueries.select +
        database.progressionQueries.selectAll})
  }

  public override fun remove(isbn: String): Unit {
    driver.execute(34051117, """DELETE FROM progression WHERE isbn = ?""", 1) {
      bindString(1, isbn)
    }
    notifyQueries(34051117, {database.progressionQueries.select +
        database.progressionQueries.selectAll})
  }

  public override fun clear(): Unit {
    driver.execute(125993348, """DELETE FROM progression""", 0)
    notifyQueries(125993348, {database.progressionQueries.select +
        database.progressionQueries.selectAll})
  }

  private inner class SelectQuery<out T : Any>(
    public val isbn: String,
    mapper: (SqlCursor) -> T
  ) : Query<T>(select, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(62640293,
        """SELECT * FROM progression WHERE isbn = ?""", 1) {
      bindString(1, isbn)
    }

    public override fun toString(): String = "Progression.sq:select"
  }
}

private class HighlightsQueriesImpl(
  private val database: MaggioliEbookDBImpl,
  private val driver: SqlDriver
) : TransacterImpl(driver), HighlightsQueries {
  internal val select: MutableList<Query<*>> = copyOnWriteList()

  internal val selectByIsbn: MutableList<Query<*>> = copyOnWriteList()

  internal val selectAll: MutableList<Query<*>> = copyOnWriteList()

  public override fun <T : Any> select(id: Long, mapper: (
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
  ) -> T): Query<T> = SelectQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!.toInt(),
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7),
      cursor.getDouble(8)!!,
      cursor.getString(9)!!,
      cursor.getString(10)!!,
      cursor.getString(11)!!
    )
  }

  public override fun select(id: Long): Query<Highlights> = select(id) { id_, created_date, isbn,
      style, tint, href, type, title, total_progression, location, text, annotation_ ->
    Highlights(
      id_,
      created_date,
      isbn,
      style,
      tint,
      href,
      type,
      title,
      total_progression,
      location,
      text,
      annotation_
    )
  }

  public override fun <T : Any> selectByIsbn(isbn: String, mapper: (
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
  ) -> T): Query<T> = SelectByIsbnQuery(isbn) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!.toInt(),
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7),
      cursor.getDouble(8)!!,
      cursor.getString(9)!!,
      cursor.getString(10)!!,
      cursor.getString(11)!!
    )
  }

  public override fun selectByIsbn(isbn: String): Query<Highlights> = selectByIsbn(isbn) { id,
      created_date, isbn_, style, tint, href, type, title, total_progression, location, text,
      annotation_ ->
    Highlights(
      id,
      created_date,
      isbn_,
      style,
      tint,
      href,
      type,
      title,
      total_progression,
      location,
      text,
      annotation_
    )
  }

  public override fun <T : Any> selectAll(mapper: (
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
  ) -> T): Query<T> = Query(760684216, selectAll, driver, "Highlights.sq", "selectAll",
      "SELECT * FROM highlights") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!.toInt(),
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7),
      cursor.getDouble(8)!!,
      cursor.getString(9)!!,
      cursor.getString(10)!!,
      cursor.getString(11)!!
    )
  }

  public override fun selectAll(): Query<Highlights> = selectAll { id, created_date, isbn, style,
      tint, href, type, title, total_progression, location, text, annotation_ ->
    Highlights(
      id,
      created_date,
      isbn,
      style,
      tint,
      href,
      type,
      title,
      total_progression,
      location,
      text,
      annotation_
    )
  }

  public override fun insert(
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
  ): Unit {
    driver.execute(-1284484122, """
    |INSERT OR REPLACE INTO highlights(
    |        id, created_date, isbn, style, tint, href, type,
    |        title, total_progression, location, text,
    |        annotation
    |    )
    |    VALUES (?, ?, ?,?,?,?,?,?,?,?,?,?)
    """.trimMargin(), 12) {
      bindLong(1, id)
      bindLong(2, created_date)
      bindString(3, isbn)
      bindString(4, style)
      bindLong(5, tint.toLong())
      bindString(6, href)
      bindString(7, type)
      bindString(8, title)
      bindDouble(9, total_progression)
      bindString(10, location)
      bindString(11, text)
      bindString(12, annotation_)
    }
    notifyQueries(-1284484122, {database.highlightsQueries.select +
        database.highlightsQueries.selectAll + database.highlightsQueries.selectByIsbn})
  }

  public override fun remove(id: Long): Unit {
    driver.execute(-1035302479, """DELETE FROM highlights WHERE id = ?""", 1) {
      bindLong(1, id)
    }
    notifyQueries(-1035302479, {database.highlightsQueries.select +
        database.highlightsQueries.selectAll + database.highlightsQueries.selectByIsbn})
  }

  public override fun removeByIsbn(isbn: String): Unit {
    driver.execute(-1558535938, """DELETE FROM highlights WHERE isbn = ?""", 1) {
      bindString(1, isbn)
    }
    notifyQueries(-1558535938, {database.highlightsQueries.select +
        database.highlightsQueries.selectAll + database.highlightsQueries.selectByIsbn})
  }

  public override fun clear(): Unit {
    driver.execute(1476971392, """DELETE FROM highlights""", 0)
    notifyQueries(1476971392, {database.highlightsQueries.select +
        database.highlightsQueries.selectAll + database.highlightsQueries.selectByIsbn})
  }

  private inner class SelectQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T
  ) : Query<T>(select, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(-1006713303,
        """SELECT * FROM highlights WHERE id = ?""", 1) {
      bindLong(1, id)
    }

    public override fun toString(): String = "Highlights.sq:select"
  }

  private inner class SelectByIsbnQuery<out T : Any>(
    public val isbn: String,
    mapper: (SqlCursor) -> T
  ) : Query<T>(selectByIsbn, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(1335731062,
        """SELECT * FROM highlights WHERE isbn = ? ORDER BY total_progression ASC""", 1) {
      bindString(1, isbn)
    }

    public override fun toString(): String = "Highlights.sq:selectByIsbn"
  }
}

private class FavoriteBooksQueriesImpl(
  private val database: MaggioliEbookDBImpl,
  private val driver: SqlDriver
) : TransacterImpl(driver), FavoriteBooksQueries {
  internal val select: MutableList<Query<*>> = copyOnWriteList()

  internal val selectAll: MutableList<Query<*>> = copyOnWriteList()

  public override fun select(isbn: String): Query<String> = SelectQuery(isbn) { cursor ->
    cursor.getString(0)!!
  }

  public override fun selectAll(): Query<String> = Query(-1608831511, selectAll, driver,
      "FavoriteBooks.sq", "selectAll", "SELECT * FROM favoriteBooks") { cursor ->
    cursor.getString(0)!!
  }

  public override fun insert(isbn: String): Unit {
    driver.execute(1104476757, """
    |INSERT OR REPLACE INTO favoriteBooks(isbn)
    |    VALUES (?)
    """.trimMargin(), 1) {
      bindString(1, isbn)
    }
    notifyQueries(1104476757, {database.favoriteBooksQueries.selectAll +
        database.favoriteBooksQueries.select})
  }

  public override fun remove(isbn: String): Unit {
    driver.execute(1353658400, """DELETE FROM favoriteBooks WHERE isbn = ?""", 1) {
      bindString(1, isbn)
    }
    notifyQueries(1353658400, {database.favoriteBooksQueries.selectAll +
        database.favoriteBooksQueries.select})
  }

  public override fun clear(): Unit {
    driver.execute(307108657, """DELETE FROM favoriteBooks""", 0)
    notifyQueries(307108657, {database.favoriteBooksQueries.selectAll +
        database.favoriteBooksQueries.select})
  }

  private inner class SelectQuery<out T : Any>(
    public val isbn: String,
    mapper: (SqlCursor) -> T
  ) : Query<T>(select, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(1382247576,
        """SELECT * FROM favoriteBooks WHERE isbn = ?""", 1) {
      bindString(1, isbn)
    }

    public override fun toString(): String = "FavoriteBooks.sq:select"
  }
}

private class BookmarkQueriesImpl(
  private val database: MaggioliEbookDBImpl,
  private val driver: SqlDriver
) : TransacterImpl(driver), BookmarkQueries {
  internal val select: MutableList<Query<*>> = copyOnWriteList()

  internal val selectByIsbn: MutableList<Query<*>> = copyOnWriteList()

  internal val selectAll: MutableList<Query<*>> = copyOnWriteList()

  public override fun <T : Any> select(id: Long, mapper: (
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
  ) -> T): Query<T> = SelectQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!,
      cursor.getString(9)!!
    )
  }

  public override fun select(id: Long): Query<Bookmark> = select(id) { id_, created_date, isbn,
      publication_id, resource_index, resource_href, resource_type, resource_title, location,
      locator_text ->
    Bookmark(
      id_,
      created_date,
      isbn,
      publication_id,
      resource_index,
      resource_href,
      resource_type,
      resource_title,
      location,
      locator_text
    )
  }

  public override fun <T : Any> selectByIsbn(isbn: String, mapper: (
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
  ) -> T): Query<T> = SelectByIsbnQuery(isbn) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!,
      cursor.getString(9)!!
    )
  }

  public override fun selectByIsbn(isbn: String): Query<Bookmark> = selectByIsbn(isbn) { id,
      created_date, isbn_, publication_id, resource_index, resource_href, resource_type,
      resource_title, location, locator_text ->
    Bookmark(
      id,
      created_date,
      isbn_,
      publication_id,
      resource_index,
      resource_href,
      resource_type,
      resource_title,
      location,
      locator_text
    )
  }

  public override fun <T : Any> selectAll(mapper: (
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
  ) -> T): Query<T> = Query(-478286559, selectAll, driver, "Bookmark.sq", "selectAll",
      "SELECT * FROM bookmark") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8)!!,
      cursor.getString(9)!!
    )
  }

  public override fun selectAll(): Query<Bookmark> = selectAll { id, created_date, isbn,
      publication_id, resource_index, resource_href, resource_type, resource_title, location,
      locator_text ->
    Bookmark(
      id,
      created_date,
      isbn,
      publication_id,
      resource_index,
      resource_href,
      resource_type,
      resource_title,
      location,
      locator_text
    )
  }

  public override fun insert(
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
  ): Unit {
    driver.execute(1189430813, """
    |INSERT OR REPLACE INTO bookmark(
    |        id, created_date, isbn, publication_id, resource_index, resource_href, resource_type,
    |        resource_title, location, locator_text
    |    )
    |    VALUES (?,?,?,?, ?,?,?,?,?,?)
    """.trimMargin(), 10) {
      bindLong(1, id)
      bindLong(2, created_date)
      bindString(3, isbn)
      bindString(4, publication_id)
      bindLong(5, resource_index)
      bindString(6, resource_href)
      bindString(7, resource_type)
      bindString(8, resource_title)
      bindString(9, location)
      bindString(10, locator_text)
    }
    notifyQueries(1189430813, {database.bookmarkQueries.selectAll +
        database.bookmarkQueries.select + database.bookmarkQueries.selectByIsbn})
  }

  public override fun remove(id: Long): Unit {
    driver.execute(1438612456, """DELETE FROM bookmark WHERE id = ?""", 1) {
      bindLong(1, id)
    }
    notifyQueries(1438612456, {database.bookmarkQueries.selectAll +
        database.bookmarkQueries.select + database.bookmarkQueries.selectByIsbn})
  }

  public override fun removeByIsbn(isbn: String): Unit {
    driver.execute(-787952139, """DELETE FROM bookmark WHERE isbn = ?""", 1) {
      bindString(1, isbn)
    }
    notifyQueries(-787952139, {database.bookmarkQueries.selectAll +
        database.bookmarkQueries.select + database.bookmarkQueries.selectByIsbn})
  }

  public override fun clear(): Unit {
    driver.execute(-1214171543, """DELETE FROM bookmark""", 0)
    notifyQueries(-1214171543, {database.bookmarkQueries.selectAll +
        database.bookmarkQueries.select + database.bookmarkQueries.selectByIsbn})
  }

  private inner class SelectQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T
  ) : Query<T>(select, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(1467201632,
        """SELECT * FROM bookmark WHERE id = ?""", 1) {
      bindLong(1, id)
    }

    public override fun toString(): String = "Bookmark.sq:select"
  }

  private inner class SelectByIsbnQuery<out T : Any>(
    public val isbn: String,
    mapper: (SqlCursor) -> T
  ) : Query<T>(selectByIsbn, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(2106314861,
        """SELECT * FROM bookmark WHERE isbn = ?""", 1) {
      bindString(1, isbn)
    }

    public override fun toString(): String = "Bookmark.sq:selectByIsbn"
  }
}
