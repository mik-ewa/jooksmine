package com.example.fitness_tracking_app.features.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.data.MessageDataSender
import com.example.fitness_tracking_app.databinding.ReceiveMessageBinding
import com.example.fitness_tracking_app.databinding.SendMessageBinding
import com.example.fitness_tracking_app.utils.DateFormatter
import com.example.fitness_tracking_app.utils.visible

class MessagesAdapter(
    private val messages: List<MessageDataSender>,
    private val lastRead: Long?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>()   {

    private val ITEM_SENT = 1
    private val ITEM_RECEIVE = 2
    private var lastMessageRead : Boolean? = false

    inner class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMessageBinding = SendMessageBinding.bind(itemView)
    }

    inner class ReceiveMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ReceiveMessageBinding = ReceiveMessageBinding.bind(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        val messages = messages[position]
        return if (messages.sender==true) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.send_message, parent, false)
            SentMessageHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.receive_message, parent, false)
            ReceiveMessageHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val lastMsgPosition = messages.size-1
        if (holder.javaClass == SentMessageHolder::class.java) {
            val viewHolder = holder as SentMessageHolder
            viewHolder.binding.apply {
                tvMessage.text = message.message
                if (lastRead!= null && lastRead > message.timeStamp && lastMessageRead == false && messages[lastMsgPosition].sender == true) {
                    tvLastRead.text = DateFormatter.timeMillisToMessageTime(lastRead)
                    llLastRead.visible()
                    lastMessageRead = true
                }
            }
        }
        else {
            val viewHolder = holder as ReceiveMessageHolder
            viewHolder.binding.apply {
                tvMessage.text = message.message
            }
        }
    }

    override fun getItemCount(): Int = messages.size
}