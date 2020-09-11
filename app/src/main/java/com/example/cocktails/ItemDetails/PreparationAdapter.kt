package com.example.cocktails.ItemDetails

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cocktails.R

data class ItemPreparation(val step: String, val number: Number)

class PreparationAdapter(context: Context, arrayList: List<ItemPreparation>, private val clickListener: ViewHolder.ClickListener) :
    SelectableAdapter<PreparationAdapter.ViewHolder?>() {

    private val mArrayList: List<ItemPreparation> = arrayList
    private val mContext: Context = context

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemLayoutView: View = LayoutInflater.from(parent.context).inflate(R.layout.preparation_item, null)
        return ViewHolder(itemLayoutView, clickListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.step.text = mArrayList[position].step
        if (isSelected(position)) {
            viewHolder.done.visibility = View.VISIBLE
            viewHolder.number.text = ""
            viewHolder.step.setTextColor(ContextCompat.getColor(mContext, R.color.colorRipple))
        } else {
            viewHolder.step.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
            viewHolder.number.text = mArrayList[position].number.toString()
            viewHolder.done.visibility = View.GONE
        }
    }

    class ViewHolder(itemLayoutView: View, private val listener: ClickListener?) :
        RecyclerView.ViewHolder(itemLayoutView), View.OnClickListener, OnLongClickListener {
        var step: TextView = itemView.findViewById(R.id.tv_step)
        var number: TextView = itemView.findViewById(R.id.tv_number)
        var done: ImageView = itemView.findViewById(R.id.iv_done)

        override fun onClick(v: View) { }

        override fun onLongClick(view: View): Boolean {
            return listener?.onItemLongClicked(adapterPosition) ?: false
        }

        interface ClickListener {
            fun onItemLongClicked(position: Int): Boolean
        }

        init {
            itemLayoutView.setOnLongClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }
}