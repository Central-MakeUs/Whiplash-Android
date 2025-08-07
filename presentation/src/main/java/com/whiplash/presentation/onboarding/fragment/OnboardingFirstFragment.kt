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
import com.whiplash.presentation.databinding.FragmentOnboardingFirstBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFirstFragment : BaseOnboardingFragment<FragmentOnboardingFirstBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnboardingFirstBinding =
        FragmentOnboardingFirstBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSubtitle()
    }

    // "목표 장소에 도착 해야만 꺼지는 알람!" 중 '도착 해야만'은 lemon_400 색으로 표시
    private fun setupSubtitle() {
        val fullText = getString(R.string.first_onboarding_subtitle)
        val spannable = SpannableString(fullText)

        val startIndex = fullText.indexOf("도착 해야만")
        val endIndex = startIndex + "도착 해야만".length

        val lemonColor = ContextCompat.getColor(requireContext(), R.color.lemon_400)
        spannable.setSpan(
            ForegroundColorSpan(lemonColor),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvFirstOnboardingSubTitle.text = spannable
    }

    companion object {
        fun newInstance() = OnboardingFirstFragment()
    }
}