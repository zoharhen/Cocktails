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
import com.bumptech.glide.request.RequestOptions
import com.github.ivbaranov.mfb.MaterialFavoriteButton
import android.app.Activity
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

        val imageView: ImageView = holder.itemView.findViewById(R.id.item_clipart)
        imageView.setImageDrawable(null)
        val item: Cocktail = filteredItems[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return filteredItems.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredItems = if (charSearch.isEmpty()) items else manageFiltering(charSearch)
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

    private fun manageFiltering(toSearch: String): ArrayList<Cocktail> {
        val resultList = ArrayList<Cocktail>()
        val favoriteList = (cnt.applicationContext as Cocktails).mFavorites
        when (toSearch) {
            "favorites" -> items.filter { favoriteList.getBoolean(it.name, false)}.forEach { resultList.add(it) }
            "custom" -> items.filter { it.isCustom }.forEach { resultList.add(it) }
            else -> items.filter { it.name.toLowerCase(Locale.ROOT).contains(toSearch.toLowerCase(Locale.ROOT))}.forEach { resultList.add(it) }
        }
        return resultList
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var header: TextView = itemView.findViewById(R.id.item_title)
        private var description: TextView = itemView.findViewById(R.id.item_description)
        private var imageView: ImageView = itemView.findViewById(R.id.item_clipart)
        private var favorite: MaterialFavoriteButton = itemView.findViewById(R.id.favorite_button)

        init {
            itemView.setOnClickListener { v ->
                onItemClick?.invoke(filteredItems[adapterPosition])
                return@setOnClickListener
            }

            favorite.setOnFavoriteChangeListener{ _: MaterialFavoriteButton, value: Boolean ->
                (cnt.applicationContext as Cocktails).mFavorites.edit()
                    .putBoolean(filteredItems[adapterPosition].name, value).apply()
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(cocktail: Cocktail, position: Int) {
            val applicationContext = (cnt.applicationContext as Cocktails)
            header.text = cocktail.name
            description.text = "Type: ${cocktail.type}"
            // continue only if ViewHolder still displays the same position as it was requested to
            // display when started downloading the image
            fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)
            if (cnt.isValidGlideContext() && this.adapterPosition == position) {
                applicationContext.mStorageRef.child("cliparts/" + cocktail.clipart + ".png")
                    .downloadUrl.addOnSuccessListener {
                        Glide.with(applicationContext)
                            .load(it)
                            .apply(RequestOptions().placeholder(null).dontAnimate().fitCenter())
                            .into(imageView)
                            .clearOnDetach()
                    }
            }
            favorite.isFavorite = applicationContext.mFavorites.getBoolean(cocktail.name, false)

            // get image from local storage (drawable folder):
            //            val imageId = cnt.resources.getIdentifier(cocktail.image, "drawable", cnt.packageName);
            //            imageView.setImageResource(imageId)
        }
    }
}