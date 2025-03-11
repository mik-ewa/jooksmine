package com.example.fitness_tracking_app.features.start_tracking

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.databinding.FragBottomSheetNewRecordBinding
import com.example.fitness_tracking_app.features.tracking.TrackingActivity
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.common.DialogManager
import com.example.fitness_tracking_app.data.FriendshipData
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.showNotImplementedToast
import com.example.fitness_tracking_app.utils.visible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StartNewRecordBottomSheet : BottomSheetDialogFragment() {

    @Inject lateinit var dialogManager: DialogManager

    private val friendsActivityViewModel: StartNewRecordViewModel by viewModels()

    private val binding by lazy { FragBottomSheetNewRecordBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = binding.root

    override fun onStart() {
        super.onStart()
        setupBottomSheet()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initViewModel() {
        friendsActivityViewModel.friendsList.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.data.isNullOrEmpty()) {
                        binding.llNoFriends.visible()
                    } else {
                        showFriendsList(it.data)
                    }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    private fun initView() {
        val intent = Intent(this.requireContext(), TrackingActivity::class.java)
        binding.apply {
            tvWithFriend.setOnClickListener {
                expandSheetHeight()
            }
            tvAlone.setOnClickListener {
                startActivity(intent)
                dialog?.dismiss()
            }
            rvChallenges.apply {
                layoutManager = LinearLayoutManager(this.context)
            }
        }
    }

    private fun showFriendsList(list: List<FriendshipData>){
        val friendsList = list.sortedBy { it.friendNickname }
        val rvAdapter = ChallengeAdapter(
            context= requireContext(),
            friendList = friendsList,
            onClick = { name, _ ->
                showDialog(name = name, onClick = { showNotImplementedToast() })
            }
        )
        binding.rvChallenges.adapter = rvAdapter
    }

    private fun setupBottomSheet(){
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val llContainer = dialog?.findViewById<LinearLayout>(R.id.ll_container)

        llContainer?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                llContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val containerHeight = llContainer.height
                bottomSheet?.let {
                    val layoutParams = it.layoutParams
                    layoutParams.height = containerHeight
                    it.layoutParams = layoutParams

                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        })
    }

    private fun expandSheetHeight() {
        binding.tvWithFriend.gone()
        binding.tvTitle.text = getString(R.string.challenge_your_friend)
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val startHeight = (resources.displayMetrics.heightPixels * 0.20).toInt()
            val endHeight = (resources.displayMetrics.heightPixels * 0.90).toInt()

            val animator = ValueAnimator.ofInt(startHeight, endHeight)
            animator.duration = 300
            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                val layoutParams = it.layoutParams
                layoutParams.height = animatedValue
                it.layoutParams = layoutParams
            }
            animator.start()

            animator.doOnEnd { behavior.state = BottomSheetBehavior.STATE_EXPANDED }
        }
    }

    private fun showDialog(name: String, onClick: () -> Unit) {
        dialogManager.showSingleActionDialog(
            buttonText = getString(R.string.invite),
            onButtonClick = { onClick.invoke() },
            textQuestion = getString(R.string.do_you_want_to_invite_name, name)
        )
    }
}