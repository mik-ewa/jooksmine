package com.example.fitness_tracking_app.features.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness_tracking_app.data.LanguageData
import com.example.fitness_tracking_app.databinding.ItemLanguageBinding
import com.example.fitness_tracking_app.utils.invisible
import com.example.fitness_tracking_app.utils.loadOval
import com.example.fitness_tracking_app.utils.visible

class LanguageAdapter(
    private val languageList: List<LanguageData>,
    private val onSelected: (LanguageData) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = languageList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = languageList[position]
        holder.binding.apply {
            if (current.isSelected) ivIsSelected.visible() else ivIsSelected.invisible()
            tvCountry.text = current.language
            ivFlag.loadOval(current.flag)
            root.setOnClickListener {
                current.isSelected = !current.isSelected
                onSelected.invoke(current)
            }
        }
    }
}