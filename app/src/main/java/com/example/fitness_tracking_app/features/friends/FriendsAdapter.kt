package com.example.fitness_tracking_app.features.friends

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.data.FriendshipData
import com.example.fitness_tracking_app.databinding.ItemFriendBinding
import com.example.fitness_tracking_app.utils.loadOval

class FriendsAdapter(
    private val friendsList: List<FriendshipData>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    private var filteredFriends : List<FriendshipData> = friendsList

    class ViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = filteredFriends.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = filteredFriends[position]
        holder.binding.apply {
            tvName.text = currentItem.friendName
            tvNickname.text = currentItem.friendNickname
            ivProfilePhoto.loadOval(currentItem.friendImage)
            iconGo.setOnClickListener{
                onClick.invoke(currentItem.friendID)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String?) {
        filteredFriends = if (query.isNullOrEmpty()) {
            friendsList
        } else {
            friendsList.filter {
                it.friendName.contains(query, true) || it.friendNickname.contains(query, true)
            }
        }
        notifyDataSetChanged()
    }
}
