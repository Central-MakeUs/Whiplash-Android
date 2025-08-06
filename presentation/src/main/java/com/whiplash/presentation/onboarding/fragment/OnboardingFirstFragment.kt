package com.whiplash.presentation.onboarding.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.whiplash.presentation.databinding.FragmentOnboardingFirstBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFirstFragment : BaseOnboardingFragment<FragmentOnboardingFirstBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnboardingFirstBinding =
        FragmentOnboardingFirstBinding.inflate(inflater, container, false)

    companion object {
        fun newInstance() = OnboardingFirstFragment()
    }
}