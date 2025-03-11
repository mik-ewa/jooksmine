package com.example.fitness_tracking_app.features.body_measurement_fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.data.Weights
import com.example.fitness_tracking_app.databinding.ItemHistoryMeasurementsBinding
import com.example.fitness_tracking_app.utils.DateFormatter.timeMillisToDate

class BodyMeasurementAdapter(private val historyList: List<Weights>, private val delete: (Weights) -> Unit) : RecyclerView.Adapter<BodyMeasurementAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHistoryMeasurementsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemHistoryMeasurementsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = historyList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = historyList[position]
        holder.apply {
            binding.tvDate.text = currentItem.date?.let { timeMillisToDate(it) }
            binding.tvWeight.text = holder.itemView.context.getString(R.string.w_kg, currentItem.weight.toString())
            binding.ivDelete.setOnClickListener {
                delete.invoke(currentItem)
            }
        }
    }
}
