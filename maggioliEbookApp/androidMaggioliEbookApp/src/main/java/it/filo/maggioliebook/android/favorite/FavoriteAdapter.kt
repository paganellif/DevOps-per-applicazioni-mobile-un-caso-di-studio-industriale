package it.filo.maggioliebook.android.favorite

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import it.filo.maggioliebook.android.R
import it.filo.maggioliebook.domain.core.book.Libro

class FavoriteAdapter(
    private val activity: Activity,
    private val onBookClick: (Libro?) -> Unit):
    PagingDataAdapter<Libro, FavoriteViewHolder>(BOOK_COMPARATOR) {

    private val logTag: String = this.javaClass.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder =
        FavoriteViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.book_card, parent, false),
            activity, onBookClick
        )

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val book = getItem(position)
        Log.d(logTag, "Binding Book: $book")
        holder.bind(book)
    }

    companion object {
        private val BOOK_COMPARATOR = object : DiffUtil.ItemCallback<Libro>() {

            override fun areItemsTheSame(oldItem: Libro, newItem: Libro): Boolean =
                !oldItem.isbn.isNullOrEmpty() && !newItem.isbn.isNullOrEmpty()
                        && (oldItem.isbn == newItem.isbn)

            override fun areContentsTheSame(oldItem: Libro, newItem: Libro): Boolean =
                oldItem.autori == newItem.autori &&
                        oldItem.name == newItem.name &&
                        oldItem.description == newItem.description
        }
    }
}
