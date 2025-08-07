package com.whiplash.presentation.onboarding.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.whiplash.presentation.databinding.FragmentOnboardingThirdBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingThirdFragment : BaseOnboardingFragment<FragmentOnboardingThirdBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnboardingThirdBinding =
        FragmentOnboardingThirdBinding.inflate(inflater, container, false)

    companion object {
        fun newInstance() = OnboardingThirdFragment()
    }
}