package com.example.fitness_tracking_app.features.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness_tracking_app.R
import com.example.fitness_tracking_app.base.base_components.BaseFragment
import com.example.fitness_tracking_app.databinding.FragLanguageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageFragment : BaseFragment<FragLanguageBinding>() {

    private val viewModel: LanguageViewModel by viewModels()
    private var selectedLanguage: String? = null

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragLanguageBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.languages.observe(viewLifecycleOwner) { languagesList ->
            binding.apply {
                rvLanguages.apply { layoutManager = LinearLayoutManager(this.context) }
                val languageAdapter = LanguageAdapter(
                    languageList = languagesList.sortedByDescending { it.language },
                    onSelected = {language ->
                        selectedLanguage = language.code
                        viewModel.changeLanguage(selectedLanguage ?: "en")
                    }
                )
                rvLanguages.adapter = languageAdapter
                rvLanguages.setHasFixedSize(true)
            }
        }
    }

    private fun initView() {
        binding.apply {
            header.setTitle(getString(R.string.language))
            header.setOnBackClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            btnLanguage.setOnClickListener {
                selectedLanguage?.let {language ->
                    viewModel.setLanguage(language)
                    updateLocale()
                }
            }
        }
    }

    private fun updateLocale() { requireActivity().recreate() }
}