package com.example.fitness_tracking_app.features.start_tracking

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.data.FriendshipData
import com.example.fitness_tracking_app.databinding.ItemChallengeFriendBinding
import com.example.fitness_tracking_app.features.friends.FriendsAdapter

class ChallengeAdapter(
    private val context: Context,
    private val friendList: List<FriendshipData>,
    private val onClick: (name: String, id: String) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ViewHolder>(){

    class ViewHolder(val binding: ItemChallengeFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemChallengeFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val currentItem = friendList[position]
        holder.binding.apply {
            Glide.with(context).load(currentItem.friendImage).placeholder(R.drawable.placeholder_profile_photo).into(ivProfilePhoto)
            tvNickname.text = currentItem.friendNickname
            ivChallenge.setOnClickListener {
                onClick.invoke(
                    currentItem.friendNickname,
                    currentItem.friendID
                )
            }
        }
    }

    override fun getItemCount(): Int = friendList.size
}
