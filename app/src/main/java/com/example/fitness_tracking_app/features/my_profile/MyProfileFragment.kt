package com.example.fitness_tracking_app.features.my_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.databinding.FragMyProfileBinding
import com.example.fitness_tracking_app.features.friend.ActivitiesAdapter
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.example.fitness_tracking_app.utils.Resource
import com.example.fitness_tracking_app.utils.gone
import com.example.fitness_tracking_app.utils.loadOval
import com.example.fitness_tracking_app.utils.showToast
import com.example.fitness_tracking_app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyProfileFragment : BaseFragment<FragMyProfileBinding>() {

    private val viewModel: MyProfileViewModel by viewModels()
    private var currentPhotoType: PhotoType? = null

    private val pickProfilePhotoMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                when (currentPhotoType) {
                    PhotoType.PROFILE_PHOTO -> viewModel.saveProfilePhoto(uri)
                    PhotoType.BACKGROUND_PHOTO -> viewModel.saveBackgroundPhoto(uri)
                    null -> showToast(GlobalStrings.ERROR_FAILED_UPLOAD_DATA)
                }
            }
        }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragMyProfileBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.apply {
            ivEditProfilePicture.setOnClickListener { pickPhoto(PhotoType.PROFILE_PHOTO) }
            ivEditBackgroundPicture.setOnClickListener { pickPhoto(PhotoType.BACKGROUND_PHOTO) }
        }
    }

    private fun initViewModel() {
        binding.apply {
            viewModel.userResult.observe(viewLifecycleOwner) { user ->
                when (user) {
                    is Resource.Success -> {
                        tvNickname.text = user.data?.username
                        loadProfilePhoto(user.data?.profilePhoto)
                        loadBackgroundPhoto(user.data?.backgroundPhoto)
                    }
                    is Resource.Error -> TODO()
                    is Resource.Loading -> TODO()
                }
            }
            viewModel.runList.observe(viewLifecycleOwner) { result ->
                if (result is Resource.Loading) {
                    lottieRunList.visible()
                } else {
                    lottieRunList.gone()
                    result.data?.let { runs ->
                        rvActivities.apply { setHasFixedSize(true)
                            layoutManager = LinearLayoutManager(this.context)
                        }
                        val activityAdapter = ActivitiesAdapter(requireContext(), runs)
                        rvActivities.adapter = activityAdapter
                    }
                }
            }
            viewModel.photoResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Success -> {
                        loadProfilePhoto(result.data)
                        lottieIv.gone()
                    }

                    is Resource.Error -> {
                        showToast(result.message ?: GlobalStrings.ERROR_FAILED_UPLOAD_DATA)
                        lottieIv.gone()
                    }

                    is Resource.Loading -> {
                        lottieIv.visible()
                    }
                }
            }
            viewModel.backgroundPhotoResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Success -> {
                        loadBackgroundPhoto(result.data)
                        lottieBackgroundIv.gone()
                    }

                    is Resource.Error -> {
                        showToast(result.message ?: GlobalStrings.ERROR_FAILED_UPLOAD_DATA)
                        lottieBackgroundIv.gone()
                    }

                    is Resource.Loading -> {
                        lottieBackgroundIv.visible()
                    }
                }
            }
        }
    }

    private fun loadProfilePhoto(photo: String?) {
        binding.ivPhoto.loadOval(photo ?: "")
    }

    private fun loadBackgroundPhoto(photo: String?) {
        Glide.with(requireContext()).load(photo).into(binding.ivBackground)
    }

    private fun pickPhoto(photoType: PhotoType) {
        currentPhotoType = photoType
        pickProfilePhotoMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

enum class PhotoType {
    PROFILE_PHOTO,
    BACKGROUND_PHOTO
}
