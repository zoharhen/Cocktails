package com.example.cocktails.ui.main

import androidx.recyclerview.widget.RecyclerView
import android.util.SparseBooleanArray
import java.util.*


abstract class SelectableAdapter<VH : RecyclerView.ViewHolder?> :   RecyclerView.Adapter<VH>() {

    private val selectedItems: SparseBooleanArray = SparseBooleanArray()

    fun isSelected(position: Int): Boolean {
        return getSelectedItems().contains(position)
    }

    fun toggleSelection(position: Int) {
        if (selectedItems[position, false]) selectedItems.delete(position) else selectedItems.put(position, true)
        this.notifyItemChanged(position)
    }

    fun clearSelection() {
        val selection = getSelectedItems()
        selectedItems.clear()
        for (i in selection) {
            notifyItemChanged(i)
        }
    }

    val selectedItemCount: Int
        get() = selectedItems.size()

    private fun getSelectedItems(): List<Int> {
        val items: MutableList<Int> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    companion object {
        private val TAG = SelectableAdapter::class.java.simpleName
    }

}