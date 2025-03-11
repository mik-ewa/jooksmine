package com.example.fitness_tracking_app.utils

import android.animation.ValueAnimator
import android.content.res.Resources
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.fitness_tracking_app.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

fun Fragment.showNotImplementedToast() {
    val context = requireContext()
    Toast.makeText(context, context.getString(R.string.not_yet_implemented), Toast.LENGTH_SHORT).show()
}

fun Fragment.showGenericErrorToast() {
    Toast.makeText(
        requireContext(),
        requireContext().getString(R.string.error_generic),
        Toast.LENGTH_SHORT
    ).show()
}

fun LinearLayout.showItemContainer(targetHeight: Int) {animateContainerHeight(targetHeight.dpToPx()) }

fun LinearLayout.animateContainerHeight(targetHeight: Int) {
    val currentHeight = this.height
    val valueAnimator = ValueAnimator.ofInt(currentHeight, targetHeight)
    valueAnimator.addUpdateListener { animation ->
        val params = this.layoutParams
        params.height = animation.animatedValue as Int
        this.layoutParams = params
    }
    valueAnimator.duration = 300
    valueAnimator.start()
}

fun EditText.inputChanges(): Flow<CharSequence?> {
    return callbackFlow {
        val listener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                trySendBlocking(s)
            }
        }
        addTextChangedListener(listener)
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}

fun Float.trimZero(): String {
    return if (this == this.toLong().toFloat()) String.format("%d", this.toLong())
    else String.format("%s", this)
}

fun EditText.isWeightValid(): Boolean = this.length() >= 2

fun EditText.filterSpaces(): String {
    this.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
        if (source.contains(" ")) "" else null
    })
    return this.text.toString().replace(" ", "")
}

fun EditText.filterSpacesAndNumbers(): String {
    this.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
        if (source.matches(Regex("[^a-zA-Z]"))) { "" } else { null }
    })
    return this.text.toString().replace(" ", "")
}

fun String.isEmailCorrect(): Boolean {
    val pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(this).matches()
}

fun String.isPasswordCorrect(): Boolean = (this.length > 6)

fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.setGone(gone: Boolean) {
    this.visibility = if (gone) View.GONE else View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun ImageView.loadOval(url: String) {
    Glide.with(this).load(url).circleCrop().into(this)
}
