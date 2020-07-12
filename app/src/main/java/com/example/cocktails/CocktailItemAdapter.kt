package com.example.cocktails

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.ivbaranov.mfb.MaterialFavoriteButton
import java.util.*
import kotlin.collections.ArrayList


class CocktailItemAdapter internal constructor(context: Context, data: List<Cocktail>) :
    RecyclerView.Adapter<CocktailItemAdapter.ViewHolder>(), Filterable {

    var onItemClick: ((Cocktail) -> Unit)? = null
    private val items: List<Cocktail> = data
    private var filteredItems: List<Cocktail> = items
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val cnt: Context = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = inflater.inflate(R.layout.grid_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Cocktail = filteredItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return filteredItems.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filteredItems = items
                } else {
                    val resultList = ArrayList<Cocktail>()
                    for (row in items) if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                        resultList.add(row)
                    }
                    filteredItems = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredItems
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = results?.values as ArrayList<Cocktail>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var header: TextView = itemView.findViewById(R.id.item_title)
        var description: TextView = itemView.findViewById(R.id.item_description)
        var imageView: ImageView = itemView.findViewById(R.id.item_clipart)
        var favorite: MaterialFavoriteButton = itemView.findViewById(R.id.favorite_button)

        init {
            itemView.setOnClickListener{
                onItemClick?.invoke(filteredItems[adapterPosition])
            }

            favorite.setOnFavoriteChangeListener{ _: MaterialFavoriteButton, value: Boolean ->
                (cnt.applicationContext as Cocktails).mFavorites.edit()
                    .putBoolean(filteredItems[adapterPosition].name, value).apply()
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(cocktail: Cocktail) {
            val applicationContext = (cnt.applicationContext as Cocktails)
            header.text = cocktail.name
            description.text = "Type: ${cocktail.type}"
            applicationContext.mStorageRef.child("cliparts/" + cocktail.image + ".png")
                .downloadUrl.addOnSuccessListener {
                    Glide.with(cnt)
                        .load(it)
                        .into(imageView)
                }
            favorite.isFavorite = applicationContext.mFavorites.getBoolean(cocktail.name, false)

            // get image from local storage (drawable folder):
            //            val imageId = cnt.resources.getIdentifier(cocktail.image, "drawable", cnt.packageName);
            //            imageView.setImageResource(imageId)
        }
    }
}