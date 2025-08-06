package com.whiplash.presentation.onboarding.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.whiplash.presentation.databinding.FragmentOnboardingSecondBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingSecondFragment : BaseOnboardingFragment<FragmentOnboardingSecondBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnboardingSecondBinding =
        FragmentOnboardingSecondBinding.inflate(inflater, container, false)

    companion object {
        fun newInstance() = OnboardingSecondFragment()
    }
}