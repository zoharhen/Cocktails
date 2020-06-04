package com.example.cocktails

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CocktailItemAdapter internal constructor(context: Context, data: List<Cocktail>) :
    RecyclerView.Adapter<CocktailItemAdapter.ViewHolder>() {

    var onItemClick: ((Cocktail) -> Unit)? = null
    private val items: List<Cocktail> = data
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val cnt: Context = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = inflater.inflate(R.layout.grid_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Cocktail = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var header: TextView = itemView.findViewById(R.id.item_title)
        var description: TextView = itemView.findViewById(R.id.item_description)
        var imageView: ImageView = itemView.findViewById(R.id.item_clipart)

        init {
            itemView.setOnClickListener{
                onItemClick?.invoke(items[adapterPosition])
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(cocktail: Cocktail) {
            header.text = cocktail.name
            description.text = "Type: ${cocktail.type}"
            val imageId = cnt.resources.getIdentifier(cocktail.image, "drawable", cnt.packageName);
            imageView.setImageResource(imageId)
        }
    }
}