package com.example.onyx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class LiveCategoryAdapter(
    private val onCategorySelected: (String) -> Unit
) : ListAdapter<String, LiveCategoryAdapter.ViewHolder>(CategoryDiffCallback()) {

    private var selectedCategory: String = "All"

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chipText: TextView = view.findViewById(R.id.chipText)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val category = getItem(position)
                    selectedCategory = category
                    notifyDataSetChanged() // Lazy way to update all chips' selection state
                    onCategorySelected(category)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_live_category_chip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = getItem(position)
        holder.chipText.text = category
        
        // Highlight selected category
        if (category == selectedCategory) {
            holder.chipText.alpha = 1.0f
            // Option to change background tint here
        } else {
            holder.chipText.alpha = 0.5f
        }
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
}
