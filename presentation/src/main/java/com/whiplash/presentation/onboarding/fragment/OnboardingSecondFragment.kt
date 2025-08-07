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
import com.whiplash.presentation.databinding.FragmentOnboardingSecondBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingSecondFragment : BaseOnboardingFragment<FragmentOnboardingSecondBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnboardingSecondBinding =
        FragmentOnboardingSecondBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTitle()
    }

    private fun setupTitle() {
        val fullText = "목표 장소, 목표 시간을"
        val spannable = SpannableString(fullText)

        val lemonColor = ContextCompat.getColor(requireContext(), R.color.lemon_400)

        // '장소' 색상 적용
        val placeStartIndex = fullText.indexOf("장소")
        val placeEndIndex = placeStartIndex + "장소".length
        spannable.setSpan(
            ForegroundColorSpan(lemonColor),
            placeStartIndex,
            placeEndIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // '시간' 색상 적용
        val timeStartIndex = fullText.indexOf("시간")
        val timeEndIndex = timeStartIndex + "시간".length
        spannable.setSpan(
            ForegroundColorSpan(lemonColor),
            timeStartIndex,
            timeEndIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvSecondOnboardingTitle.text = spannable
    }

    companion object {
        fun newInstance() = OnboardingSecondFragment()
    }
}