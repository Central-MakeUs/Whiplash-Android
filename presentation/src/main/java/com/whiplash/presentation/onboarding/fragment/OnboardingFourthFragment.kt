package com.whiplash.presentation.onboarding.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.FragmentOnboardingFourthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFourthFragment : BaseOnboardingFragment<FragmentOnboardingFourthBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnboardingFourthBinding =
        FragmentOnboardingFourthBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTitle()
    }

    private fun setupTitle() {
        val fullText = getString(R.string.fourth_onboarding_title)
        val spannable = SpannableString(fullText)
        val lemonColor = ContextCompat.getColor(requireContext(), R.color.lemon_400)

        val startIndex = fullText.indexOf("눈떠")
        val endIndex = startIndex + "눈떠".length
        spannable.setSpan(
            ForegroundColorSpan(lemonColor),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvFourthOnboardingTitle.text = spannable
    }

    companion object {
        fun newInstance() = OnboardingFourthFragment()
    }
}