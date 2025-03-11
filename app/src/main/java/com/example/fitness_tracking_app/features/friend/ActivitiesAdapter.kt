package com.example.fitness_tracking_app.features.friend

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.data.RunActivitiesList
import com.example.fitness_tracking_app.databinding.ItemActivityBinding
import com.example.fitness_tracking_app.utils.CalculationsUtils
import com.example.fitness_tracking_app.utils.Converters
import com.example.fitness_tracking_app.utils.DateFormatter
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.loadOval
import com.example.fitness_tracking_app.utils.visible

class ActivitiesAdapter(
    private val context: Context,
    private val runList: RunActivitiesList
): RecyclerView.Adapter<ActivitiesAdapter.ViewHolder>(){

    class ViewHolder(val binding: ItemActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent,false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = runList.activitiesList?.get(position)
        holder.binding.apply {
            tvWhere.text = currentItem?.place
            tvDate.text = DateFormatter.timeMillisToDate(currentItem?.startTime!!)
            tvDistance.text = context.getString(R.string.to_distance, currentItem.distance)
            tvPace.text = CalculationsUtils.formatPace(currentItem.pace)
            tvDuration.text = DateFormatter.secondsToTime(currentItem.duration)
            ivProfileFriend.loadOval(runList.userPhoto ?: "")
            Glide.with(context).load(Converters.base64ToBitmap(currentItem.image ?: "")).into(ivMap)
            if (currentItem.calories != null) {
                tvCalories.text = currentItem.calories.toString()
                llCalories.visible()
            } else {
                llCalories.gone()
            }
        }
    }

    override fun getItemCount(): Int = runList.activitiesList?.size ?: 0
}