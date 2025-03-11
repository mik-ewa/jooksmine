package com.example.fitness_tracking_app.features.chat

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness_tracking_app.data.ChatInfo
import com.example.fitness_tracking_app.data.FriendChatData
import com.example.fitness_tracking_app.databinding.ItemChatBinding
import com.example.fitness_tracking_app.utils.DateFormatter.timeMillisToMessageTime
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.invisible
import com.example.fitness_tracking_app.utils.loadOval
import com.example.fitness_tracking_app.utils.visible

class ChatAdapter(
    private val chatList: List<ChatInfo>,
    private val onClick: (FriendChatData) -> Unit,
    private val onRemove: (String) -> Unit
): RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    fun removeChat(position: Int) {
        onRemove.invoke(chatList[position].chatId)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = chatList[position]
        holder.binding.apply {
           tvNickname.text = currentItem.friendNickname
            tvDate.text = timeMillisToMessageTime(currentItem.date)
            tvLastMessage.text = currentItem.message
            ivProfilePhoto.loadOval(currentItem.friendPhoto  ?: "")
            clChat.setOnClickListener {
                onClick.invoke(FriendChatData(
                    friendId = currentItem.friendId,
                    chatId = currentItem.chatId,
                    friendNickname = currentItem.friendNickname!!,
                    friendPhoto = currentItem.friendPhoto!!,
                    isFriendActive = currentItem.friendActiveStatus
                ))
            }
            if (currentItem.isYourLastMsg) {
                iconIsRead.invisible()
                tvYou.visible()
            } else if (!currentItem.isRead) {
                tvYou.gone()
                iconIsRead.visible()
                tvLastMessage.setTypeface(tvLastMessage.typeface, Typeface.BOLD)
                tvNickname.setTypeface(tvNickname.typeface, Typeface.BOLD)
            } else {
                tvYou.gone()
                iconIsRead.invisible()
            }

            if (currentItem.friendActiveStatus) {
                iconIsActive.visible()
            } else {
                iconIsActive.gone()
            }
        }
    }

    override fun getItemCount(): Int = chatList.size
}