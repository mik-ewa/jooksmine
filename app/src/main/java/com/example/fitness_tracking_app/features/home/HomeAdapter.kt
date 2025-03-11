package com.example.fitness_tracking_app.features.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.databinding.ItemMotivationBinding
import com.example.fitness_tracking_app.databinding.ItemWeatherBinding

class HomeAdapter(
    private val context: Context,
    private var items: List<HomeRecyclerItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    companion object {
        private const val ITEM_MOTIVATION = 0
        private const val ITEM_WEATHER = 1
        private const val ITEM_ACTIVITY = 2
    }

    inner class MotivationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemMotivationBinding = ItemMotivationBinding.bind(itemView)
        fun bind(item: HomeRecyclerItem.MotivationItem) {
            binding.textMotivation.text = item.motivation
        }
    }

    inner class WeatherHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemWeatherBinding = ItemWeatherBinding.bind(itemView)
        fun bind(item: HomeRecyclerItem.WeatherItem) {
            val weatherIcon = WeatherIcon.fromMain(item.main)
            binding.apply {
                ivWeatherIcon.setImageDrawable(ContextCompat.getDrawable(context, weatherIcon))
                ivWeatherIcon.setColorFilter(ContextCompat.getColor(context, R.color.white))
                tvDescription.text = item.description
                item.temp
                tvTemperature.text = context.getString(R.string.to_temperature, item.temp)
                tvDate.text = item.date
                item.city?.apply {tvCity.text = this}
                tvHumidity.text = context.getString(R.string.to_humidity, item.humidity)
                tvWind.text = context.getString(R.string.to_wind, item.wind)
            }
        }
    }

    inner class ActivitiesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: HomeRecyclerItem.ActivitiesItem) {}
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeRecyclerItem.MotivationItem -> ITEM_MOTIVATION
            is HomeRecyclerItem.WeatherItem -> ITEM_WEATHER
            is HomeRecyclerItem.ActivitiesItem -> ITEM_ACTIVITY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_MOTIVATION -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_motivation, parent, false)
                MotivationHolder(view)
            }

            ITEM_WEATHER -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_weather, parent, false)
                WeatherHolder(view)
            }

            ITEM_ACTIVITY -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false)
                ActivitiesHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeRecyclerItem.MotivationItem -> (holder as MotivationHolder).bind(item)
            is HomeRecyclerItem.WeatherItem -> (holder as WeatherHolder).bind(item)
            is HomeRecyclerItem.ActivitiesItem -> (holder as ActivitiesHolder).bind(item)
        }
    }

    fun updateData(newItems: List<HomeRecyclerItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
