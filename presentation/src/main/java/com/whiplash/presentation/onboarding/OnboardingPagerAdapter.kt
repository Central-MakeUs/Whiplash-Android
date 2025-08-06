package com.whiplash.presentation.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.whiplash.presentation.onboarding.fragment.OnboardingFirstFragment
import com.whiplash.presentation.onboarding.fragment.OnboardingSecondFragment
import com.whiplash.presentation.onboarding.fragment.OnboardingThirdFragment
import com.whiplash.presentation.onboarding.fragment.OnboardingFourthFragment

class OnboardingPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> OnboardingFirstFragment.newInstance()
        1 -> OnboardingSecondFragment.newInstance()
        2 -> OnboardingThirdFragment.newInstance()
        3 -> OnboardingFourthFragment.newInstance()
        else -> OnboardingFirstFragment.newInstance()
    }
}