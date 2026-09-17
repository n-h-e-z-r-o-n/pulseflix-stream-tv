package com.example.onyx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class LiveChannelAdapter(
    private val onChannelClick: (IptvChannel) -> Unit
) : ListAdapter<IptvChannel, LiveChannelAdapter.ViewHolder>(ChannelDiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val channelLogo: ImageView = view.findViewById(R.id.channelLogo)
        val channelName: TextView = view.findViewById(R.id.channelName)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChannelClick(getItem(position))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_live_channel_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channel = getItem(position)
        holder.channelName.text = channel.name

        // Remove tint when loading image, keeping it only for the placeholder icon if load fails
        holder.channelLogo.imageTintList = null

        if (channel.logo.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(channel.logo)
                .error(R.drawable.ic_tv)
                .into(holder.channelLogo)
        } else {
            holder.channelLogo.setImageResource(R.drawable.ic_tv)
            // Re-apply tint for the default icon to match theme if desired, 
            // though keeping it simple here.
        }
    }
}

class ChannelDiffCallback : DiffUtil.ItemCallback<IptvChannel>() {
    override fun areItemsTheSame(oldItem: IptvChannel, newItem: IptvChannel) = oldItem.url == newItem.url
    override fun areContentsTheSame(oldItem: IptvChannel, newItem: IptvChannel) = oldItem == newItem
}
