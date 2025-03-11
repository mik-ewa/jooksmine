package com.example.fitness_tracking_app.features.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness_tracking_app.data.ProgressItemData
import com.example.fitness_tracking_app.databinding.ItemStatisticsBinding
import com.example.fitness_tracking_app.utils.trimZero

class ProgressAdapter(
    private val progressData: List<ProgressItemData>
) : RecyclerView.Adapter<ProgressAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemStatisticsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemStatisticsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = progressData[position]
        holder.binding.apply {
            tvValue.text = item.value.trimZero()
            tvDescription.text = item.description
        }
    }

    override fun getItemCount(): Int = progressData.size
}
