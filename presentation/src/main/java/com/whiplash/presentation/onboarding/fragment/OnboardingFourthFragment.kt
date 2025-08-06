package com.whiplash.presentation.onboarding.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.whiplash.presentation.databinding.FragmentOnboardingFourthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFourthFragment : BaseOnboardingFragment<FragmentOnboardingFourthBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnboardingFourthBinding =
        FragmentOnboardingFourthBinding.inflate(inflater, container, false)

    companion object {
        fun newInstance() = OnboardingFourthFragment()
    }
}