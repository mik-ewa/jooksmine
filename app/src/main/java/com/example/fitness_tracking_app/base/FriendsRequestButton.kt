package com.example.fitness_tracking_app.base

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.data.FriendshipStatus
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.visible

class FriendsRequestButton(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {

    private var buttonFriends: LinearLayout
    private var buttonUnfriend: LinearLayout
    private var textTitle: TextView
    private var icon: ImageView
    private var lottieAnimation: LottieAnimationView
    private var status: FriendshipStatus? = null

    init {
        inflate(context, R.layout.button_friends, this)
        textTitle = findViewById(R.id.button_text)
        buttonFriends = findViewById(R.id.friendsButton)
        buttonUnfriend = findViewById(R.id.friendsRemoveButton)
        icon = findViewById(R.id.icon_status)
        lottieAnimation = findViewById(R.id.lottie_animation)
        initAttrs(attrs)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        val listener = OnClickListener { view ->
            when (view.id) {
                R.id.friendsButton -> {
                    l?.onClick(view)
                }

                R.id.friendsRemoveButton -> {
                    l?.onClick(view)
                }
            }
        }
        buttonFriends.setOnClickListener(listener)
        buttonUnfriend.setOnClickListener(listener)
    }

    private fun initAttrs(set: AttributeSet?) {
        set?.let {
            context.obtainStyledAttributes(it, R.styleable.CustomButton).run {
                textTitle.text = getString(R.styleable.CustomButton_text)
                textTitle.isAllCaps = getBoolean(R.styleable.CustomButton_textAllCaps, false)
                setState()
                recycle()
            }
        }
    }

    fun setStatus(newStatus: FriendshipStatus?) {
        status = newStatus
        setState()
    }

    private fun setState() {
        when (status) {
            FriendshipStatus.PENDING -> setPending()
            FriendshipStatus.ACCEPTED -> setFriends()
            FriendshipStatus.REQUEST -> setFriendRequest()
            else -> setNone()
        }
    }

    private fun setFriends() {
        buttonFriends.apply {
            setBackgroundResource(R.drawable.bg_button_orange)
            isClickable = true
            isEnabled = true
        }
        icon.visible()
        lottieAnimation.gone()
        textTitle.text = context.getString(R.string.friend)
        textTitle.setTextColor(resources.getColor(R.color.white))
        icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_friends_white))
        buttonUnfriend.gone()
    }

    private fun setNone() {
        buttonFriends.apply {
            setBackgroundResource(R.drawable.bg_button_orange)
            isClickable = true
            isEnabled = true
        }
        icon.visible()
        lottieAnimation.gone()
        buttonUnfriend.gone()
        textTitle.text = context.getString(R.string.add)
        textTitle.setTextColor(resources.getColor(R.color.white))
        icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_add_user))
    }

    private fun setFriendRequest() {
        buttonFriends.apply {
            setBackgroundResource(R.drawable.bg_button_orange)
            isClickable = true
            isEnabled = true
        }
        icon.visible()
        lottieAnimation.gone()
        textTitle.text = context.getString(R.string.accept)
        textTitle.setTextColor(resources.getColor(R.color.white))
        icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_accept_friend))
        buttonUnfriend.apply {
            setBackgroundResource(R.drawable.bg_button_grey)
            isClickable = true
            isEnabled = true
        }
        buttonUnfriend.visible()
    }

    private fun setPending() {
        buttonFriends.apply {
            setBackgroundResource(R.drawable.bg_button_grey)
            isClickable = true
            isEnabled = true
        }
        icon.visible()
        lottieAnimation.gone()
        textTitle.text = context.getString(R.string.cancel)
        textTitle.setTextColor(resources.getColor(R.color.black))
        icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_remove_user))
        buttonUnfriend.gone()
    }

    fun setLoading() {
        buttonFriends.apply {
            setBackgroundResource(R.drawable.bg_button_grey)
            isClickable = false
            isEnabled = false
        }
        textTitle.text = ""
        icon.gone()
        lottieAnimation.visible()
    }
}