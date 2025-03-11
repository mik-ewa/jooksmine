package com.example.fitness_tracking_app.features.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.common.ItemConfigurator
import com.example.fitness_tracking_app.databinding.FragSettingsBinding
import com.example.fitness_tracking_app.features.activity_main.MainActivity
import com.example.fitness_tracking_app.features.delete_account.DeleteAccountFragment
import com.example.fitness_tracking_app.features.language.LanguageFragment
import com.example.fitness_tracking_app.utils.showItemContainer
import com.example.fitness_tracking_app.utils.showNotImplementedToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragSettingsBinding>() {

    @Inject lateinit var itemConfigurator: ItemConfigurator

    private val viewModel: SettingsViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragSettingsBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.apply {
            header.setTitle(getString(R.string.settings))
            header.setOnBackClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            itemConfigurator.applyContentForItemEdit(
                item = itemLanguage,
                text = R.string.language,
                icon = R.drawable.icon_language,
                action = {
                    (requireActivity() as MainActivity).nextFragment(LanguageFragment())
                }
            )
            itemConfigurator.applyContentForItemEdit(
                item = itemDarkMode,
                text = R.string.dark_mode,
                icon = R.drawable.icon_dark_mode,
                action = {
                    showNotImplementedToast()
                    containerSwitchMode.showItemContainer(40)
                }
            )
            itemConfigurator.applyContentForItemEdit(
                item = itemDeleteAccount,
                text = R.string.delete_account,
                icon = R.drawable.icon_delete_account,
                action = {
                    deleteAccountDialog {
                        (requireActivity() as MainActivity).nextFragment(DeleteAccountFragment())
                    }
                },
                isDelete = true
            )
        }
    }

    private fun deleteAccountDialog(onDelete: () -> Unit) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_account_title))
            .setMessage(getString(R.string.delete_account_text))
            .setPositiveButton(getString(R.string.delete_my_account)) { _, _ -> onDelete.invoke() }
            .setNegativeButton(getString(R.string.close)) { _, _ -> }
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_red))

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

    }
}