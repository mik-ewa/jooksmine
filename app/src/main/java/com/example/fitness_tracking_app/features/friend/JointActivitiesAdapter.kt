package com.example.fitness_tracking_app.features.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness_tracking_app.data.JointActivityAdapterData
import com.example.fitness_tracking_app.databinding.ItemJointActivityBinding
import com.example.fitness_tracking_app.utils.loadOval

class JointActivitiesAdapter(
    private val jointActivityAdapterData: JointActivityAdapterData?,
) : RecyclerView.Adapter<JointActivitiesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemJointActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemJointActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = jointActivityAdapterData?.jointActivityList ?: emptyList()
        val myName = jointActivityAdapterData?.myName
        val friendsName = jointActivityAdapterData?.friendsName
        val myPhoto = jointActivityAdapterData?.myPhoto
        val friendsPhoto = jointActivityAdapterData?.friendsPhoto
        if (item.isNotEmpty()) {
            val currentItem = item[position]
            holder.binding.apply {
                tvYourName.text = myName
                tvFriendName.text = friendsName
                ivProfileSecond.loadOval(myPhoto ?: "")
                ivProfileFirst.loadOval(friendsPhoto ?: "")
            }
        }
    }

    override fun getItemCount(): Int = jointActivityAdapterData?.jointActivityList?.size ?: 0
}
