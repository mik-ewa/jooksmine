package com.example.fitness_tracking_app.common

import androidx.core.content.ContextCompat
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.databinding.ItemAboutBinding
import com.example.fitness_tracking_app.databinding.ItemEditBinding
import com.example.fitness_tracking_app.utils.invisible
import com.example.fitness_tracking_app.utils.visible
import javax.inject.Inject

class ItemConfigurator @Inject constructor() {

    fun applyContentForItemAbout(
        item: ItemAboutBinding,
        icon: Int,
        text: Int,
        isViewLineVisible: Boolean = true,
        action: () -> Unit
    ) {
        val context = item.root.context
        item.iconItem.setImageDrawable(ContextCompat.getDrawable(context, icon))
        item.textItem.text = context.getString(text)
        item.buttonArrow.setOnClickListener {
            action.invoke()
        }
        if (isViewLineVisible) item.viewLine.visible() else item.viewLine.invisible()
    }

    fun applyContentForItemEdit(
        item: ItemEditBinding,
        icon: Int,
        text: Int,
        action: () -> Unit,
        isDelete: Boolean = false
    ) {
        val context = item.root.context
        item.iconItem.setImageDrawable(ContextCompat.getDrawable(context, icon))
        item.textItem.text = context.getString(text)
        item.buttonEdit.setOnClickListener {
            action.invoke()
        }
        if (isDelete) {
            item.iconItem.setColorFilter(ContextCompat.getColor(context, R.color.dark_red))
            item.textItem.setTextColor(ContextCompat.getColor(context, R.color.dark_red))
            item.buttonEdit.setColorFilter(ContextCompat.getColor(context, R.color.dark_red))
            item.buttonEdit.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.icon_bin
                )
            )
        }
    }
}
