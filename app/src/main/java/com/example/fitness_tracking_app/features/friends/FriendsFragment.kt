package com.example.fitness_tracking_app.features.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.databinding.FragFriendsBinding
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.features.friend.FriendFragment
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.data.FriendshipData
import com.example.fitness_tracking_app.utils.BundleKeys
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsFragment : BaseFragment<FragFriendsBinding>() {

    private val viewModel: FriendsViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragFriendsBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).showBottomNavBar(false)
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.apply {
            header.setTitle(getString(R.string.friends))
            header.setOnBackClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            icAddFriend.setOnClickListener {
                (requireActivity() as MainActivity).nextFragment(
                    FindNewFriendFragment(),
                    hasAnimation = false
                )
            }
            svFriends.queryHint = getString(R.string.find_your_friend)
            svFriends.isIconified = false
            rvFriends.apply {
                layoutManager = LinearLayoutManager(this.context)
            }
        }
    }

    private fun initViewModel() {
        viewModel.friendsList.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    is Resource.Success -> {
                        hideShimmer()
                        if (it.data.isNullOrEmpty()) {
                            llNoFriends.visible()
                            rvFriends.gone()
                            svFriends.gone()
                        } else {
                            llNoFriends.gone()
                            rvFriends.visible()
                            svFriends.visible()
                            showFriendsList(list = it.data)
                        }
                    }

                    is Resource.Error -> { hideShimmer() }
                    is Resource.Loading -> { shimmerLayout.startShimmer() }
                }
            }
        }
    }

    private fun showFriendsList(list: List<FriendshipData>){
        val friendsList = list.sortedBy { it.friendNickname }
        val rvAdapter =
            FriendsAdapter(friendsList, onClick = { friend ->
                val dataBundle = Bundle().apply { putString(BundleKeys.FRIEND_ID, friend) }
                (requireActivity() as MainActivity).nextFragment(FriendFragment(), dataBundle = dataBundle)
            })
        binding.rvFriends.adapter = rvAdapter
        binding.svFriends.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                rvAdapter.filter(newText)
                return false
            }
        })
    }

    private fun hideShimmer() {
       binding.shimmerLayout.stopShimmer()
       binding.shimmerLayout.gone()
    }
}
