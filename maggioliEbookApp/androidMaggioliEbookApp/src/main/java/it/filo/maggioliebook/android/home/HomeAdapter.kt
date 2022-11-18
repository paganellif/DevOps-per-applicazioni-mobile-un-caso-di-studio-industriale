package it.filo.maggioliebook.android.home

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import it.filo.maggioliebook.android.databinding.BookCardBinding
import it.filo.maggioliebook.domain.core.book.Libro

class HomeAdapter(
    private val activity: Activity,
    private val onBookClick: (Libro?) -> Unit):
    PagingDataAdapter<Libro, HomeViewHolder>(BOOK_COMPARATOR) {

    private val logTag: String = this.javaClass.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            BookCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), activity, onBookClick
        )
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val book = getItem(position)
        Log.d(logTag, "Binding Book: $book")
        holder.bind(book)
    }

    companion object {
        private val BOOK_COMPARATOR = object : DiffUtil.ItemCallback<Libro>() {

            override fun areItemsTheSame(oldItem: Libro, newItem: Libro): Boolean =
                oldItem.isbn == newItem.isbn

            override fun areContentsTheSame(oldItem: Libro, newItem: Libro): Boolean =
                oldItem == newItem &&
                oldItem.autori == newItem.autori &&
                        oldItem.name == newItem.name
        }
    }
}
