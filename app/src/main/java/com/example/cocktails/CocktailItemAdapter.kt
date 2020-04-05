package com.example.cocktails

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cocktails.R.drawable.mango_tango

class CocktailItemAdapter internal constructor(context: Context, data: List<Cocktail>) :
    RecyclerView.Adapter<CocktailItemAdapter.ViewHolder>() {

    private val items: List<Cocktail>
    private val inflater: LayoutInflater
    private var clickListener: ItemClickListener? = null
    private val cnt: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = inflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Cocktail = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var header: TextView
        var description: TextView
        var imageView: ImageView

        override fun onClick(view: View) {
            if (clickListener != null) clickListener!!.onItemClick(view, adapterPosition)
        }

        init {
            header = itemView.findViewById(R.id.list_item_header)
            description = itemView.findViewById(R.id.list_item_description)
            imageView = itemView.findViewById(R.id.list_item_clipart)
            itemView.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun bind(cocktail: Cocktail) {
            header.text = cocktail.name
            description.text = "Type: ${cocktail.type}\nGlass: ${cocktail.glass}"
            imageView.setImageResource(cocktail.image)
    }
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        clickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    init {
        inflater = LayoutInflater.from(context)
        items = data
        cnt = context
    }
}