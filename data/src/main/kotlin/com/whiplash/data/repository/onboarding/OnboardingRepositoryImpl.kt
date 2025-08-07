package com.whiplash.data.repository.onboarding

import com.whiplash.data.datastore.OnboardingDataStore
import com.whiplash.domain.repository.onboarding.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val onboardingDataStore: OnboardingDataStore
) : OnboardingRepository {

    override val isOnboardingCompleted: Flow<Boolean> = onboardingDataStore.isOnboardingCompleted

    override suspend fun setOnboardingCompleted() =
        onboardingDataStore.setOnboardingCompleted()
}