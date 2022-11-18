package it.filo.maggioliebook.android.home

import android.app.Activity
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import it.filo.maggioliebook.android.R
import it.filo.maggioliebook.android.databinding.BookCardBinding
import it.filo.maggioliebook.domain.core.book.Libro
import it.filo.maggioliebook.repository.core.LibroRepository
import it.filo.maggioliebook.usecase.core.GetBookCoverUseCase
import it.filo.maggioliebook.usecase.core.favorite.IsBookFavoriteUseCase
import it.filo.maggioliebook.usecase.core.favorite.SetBookAsFavoriteUseCase
import it.filo.maggioliebook.usecase.core.favorite.UnsetBookAsFavoriteUseCase
import it.filo.maggioliebook.util.getDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HomeViewHolder(private val itemView: BookCardBinding, private val activity: Activity,
                      private val onBookClick: (Libro?) -> Unit):
    RecyclerView.ViewHolder(itemView.root) {

    private val bookImageView: ImageView = itemView.findViewById(R.id.book_cover)
    private val favoriteImageView: ImageView = itemView.findViewById(R.id.book_favorite)
    private val bookTitleTextView: TextView = itemView.findViewById(R.id.book_title)
    private val bookAuthorsTextView: TextView = itemView.findViewById(R.id.book_authors)
    private val bookIsbnTextView: TextView = itemView.findViewById(R.id.book_description)

    private val logTag: String = this.javaClass.simpleName

    fun bind(book: Libro?) {

        if (book != null){
            itemView.rootView.setOnClickListener {
                onBookClick(book)
            }

            bookTitleTextView.text = book.name?: ""
            bookAuthorsTextView.text = (book.autori?: mutableListOf())
                .fold("") { acc: String, s: String -> "$acc $s" }
            bookIsbnTextView.text = book.isbn?: ""

            bookImageView.setImageDrawable(
                AppCompatResources.getDrawable(activity, R.drawable.default_book_cover)
            )

            CoroutineScope(getDispatcherProvider().main).launch {
                if(book.isbn == bookIsbnTextView.text) {
                    val bookCover: ByteArray? = GetBookCoverUseCase().invoke(book.isbn!!)
                    if (bookCover != null && bookCover.isNotEmpty()) {
                        bookImageView.setImageBitmap(
                            BitmapFactory
                                .decodeByteArray(bookCover, 0, bookCover.size)
                        )
                        Log.d(logTag, "Set image for book $book")
                    }

                    val isFavorite = IsBookFavoriteUseCase().invoke(book.isbn!!)

                    if (isFavorite) {
                        favoriteImageView.setImageDrawable(
                            AppCompatResources.getDrawable(activity, R.drawable.full_heart)
                        )
                        Log.d(logTag, "Set favorite image for book $book")
                    } else {
                        favoriteImageView.setImageDrawable(
                            AppCompatResources.getDrawable(activity, R.drawable.empty_heart)
                        )
                    }
                }
            }

            favoriteImageView.setOnClickListener {
                val isbn = bookIsbnTextView.text.toString()

                if (isbn.isNotEmpty())
                    CoroutineScope(getDispatcherProvider().main).launch {
                        if(IsBookFavoriteUseCase().invoke(isbn)){
                            Log.d(logTag, "Book $isbn removed!")
                            Toast.makeText(activity.applicationContext,
                                R.string.remove_favorite_book, Toast.LENGTH_SHORT).show()
                            UnsetBookAsFavoriteUseCase().invoke(isbn)
                            favoriteImageView.setImageDrawable(
                                AppCompatResources.getDrawable(activity, R.drawable.empty_heart)
                            )
                        } else {
                            Log.d(logTag, "Book $isbn added!")
                            Toast.makeText(activity.applicationContext,
                                R.string.add_favorite_book, Toast.LENGTH_SHORT).show()
                            SetBookAsFavoriteUseCase().invoke(isbn)
                            favoriteImageView.setImageDrawable(
                                AppCompatResources.getDrawable(activity, R.drawable.full_heart)
                            )
                        }
                    }
            }
        } else {
            // TODO
        }
    }
}