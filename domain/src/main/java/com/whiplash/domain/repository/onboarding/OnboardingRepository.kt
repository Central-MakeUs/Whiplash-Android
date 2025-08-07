package com.whiplash.domain.repository.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val isOnboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted()
}