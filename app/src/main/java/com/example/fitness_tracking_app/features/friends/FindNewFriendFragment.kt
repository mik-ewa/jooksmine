package com.example.fitness_tracking_app.features.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.databinding.FragFindNewFriendsBinding
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.features.friend.FriendFragment
import com.example.fitness_tracking_app.features.my_profile.MyProfileFragment
import com.example.fitness_tracking_app.utils.BundleKeys
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.showToast
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindNewFriendFragment : BaseFragment<FragFindNewFriendsBinding>() {

    private val viewModel: FindNewFriendViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragFindNewFriendsBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.apply {
            iconClose.setOnClickListener {
                (requireActivity() as MainActivity).onBackPressedDispatcher.onBackPressed()
            }
            svFriends.queryHint = getString(R.string.find_new_friends_using_username)
            svFriends.isIconified = true

            svFriends.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.isYourNickname(query ?: "")
                    childFragmentManager.findFragmentById(R.id.fl_placeholder)?.let { fragment ->
                        childFragmentManager.beginTransaction().remove(fragment).commit()
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean = true

            })
        }
    }

    private fun initViewModel() {
        viewModel.friendId.observe(viewLifecycleOwner) { friendData ->
            binding.apply {
                when (friendData) {
                    is Resource.Success -> {
                        if (friendData.data.isNullOrEmpty()) {
                            tvNoFriend.visible()

                        } else {
                            tvNoFriend.gone()
                            val ft: FragmentTransaction = childFragmentManager.beginTransaction()
                            val dataBundle = Bundle().apply { putString(BundleKeys.FRIEND_ID, friendData.data) }
                            ft.replace(R.id.fl_placeholder, FriendFragment().apply { this.arguments = dataBundle })
                            ft.commit()
                        }
                    }

                    is Resource.Error -> {
                        showToast(friendData.message ?: GlobalStrings.ERROR_GENERIC)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
        viewModel.isMyNickname.observe(viewLifecycleOwner) {
            if (it) {
                val ft: FragmentTransaction = childFragmentManager.beginTransaction()
                ft.replace(R.id.fl_placeholder, MyProfileFragment())
                ft.commit()
            }
        }
    }
}