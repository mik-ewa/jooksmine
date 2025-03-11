package com.example.fitness_tracking_app.base.base_components

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.fitness_tracking_app.R

class CustomHeader(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var arrowBack : ImageView
    private var titleHeader : TextView
    private var onBackClickListener: (() -> Unit)? = null

    init {
        inflate(context, R.layout.header_fragments, this)
        arrowBack = findViewById(R.id.arrow_back)
        titleHeader = findViewById(R.id.header_title)

        setupViews()
    }

    private fun setupViews() {
        arrowBack.setOnClickListener {
            onBackClickListener?.invoke()
        }
    }

    fun setOnBackClickListener(listener: () -> Unit) {
        onBackClickListener = listener
    }

    fun setTitle(title: String) {
        titleHeader.text = title
    }
}
