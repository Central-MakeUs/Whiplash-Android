package com.whiplash.presentation.onboarding

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityOnBoardingBinding
import com.whiplash.presentation.main.MainActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding
    private lateinit var onboardingPagerAdapter: OnboardingPagerAdapter

    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboarding_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewPager()
        setupListeners()
        observeOnboardingViewModel()
    }

    private fun observeOnboardingViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onboardingViewModel.navigationEvent.collect { event ->
                    when (event) {
                        is OnboardingViewModel.NavigationEvent.NavigateToMain -> navigateToMain()
                    }
                }
            }
        }
    }

    private fun setupViewPager() {
        onboardingPagerAdapter = OnboardingPagerAdapter(this)
        with(binding) {
            vpOnboarding.adapter = onboardingPagerAdapter
            vpOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onboardingIndicator.setCurrentPosition(position)
                    updateButtonsVisibility(position)
                }
            })
            onboardingIndicator.setCurrentPosition(0)
        }
    }

    private fun updateButtonsVisibility(position: Int) {
        val isLastPage = (position == onboardingPagerAdapter.itemCount - 1)
        with(binding) {
            btnSkip.isVisible = !isLastPage
            btnNextOnboarding.isVisible = !isLastPage
            btnStartApp.isVisible = isLastPage
        }
    }

    private fun setupListeners() {
        with(binding) {
            btnSkip.setOnClickListener { completeOnboarding() }

            btnNextOnboarding.setOnClickListener {
                val currentItem = vpOnboarding.currentItem
                if (currentItem < onboardingPagerAdapter.itemCount - 1) {
                    vpOnboarding.currentItem = currentItem + 1
                }
            }

            btnStartApp.setOnClickListener { completeOnboarding() }
        }
    }

    private fun completeOnboarding() = onboardingViewModel.completeOnboarding()

    private fun navigateToMain() {
        navigateTo<MainActivity> {
            finishCurrentActivity()
        }
    }

}