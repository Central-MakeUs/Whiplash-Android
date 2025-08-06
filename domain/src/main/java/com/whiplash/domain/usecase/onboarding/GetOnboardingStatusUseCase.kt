package com.whiplash.domain.usecase.onboarding

import com.whiplash.domain.repository.onboarding.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class GetOnboardingStatusUseCase(
    private val repository: OnboardingRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isOnboardingCompleted
}