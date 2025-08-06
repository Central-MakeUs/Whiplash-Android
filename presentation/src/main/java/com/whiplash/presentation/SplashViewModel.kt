package com.whiplash.presentation

import androidx.lifecycle.ViewModel
import com.whiplash.domain.usecase.onboarding.GetOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase
) : ViewModel() {

    val isOnboardingCompleted: Flow<Boolean> = getOnboardingStatusUseCase()
}