package com.whiplash.domain.usecase.onboarding

import com.whiplash.domain.repository.onboarding.OnboardingRepository

class SetOnboardingCompletedUseCase(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke() = repository.setOnboardingCompleted()
}