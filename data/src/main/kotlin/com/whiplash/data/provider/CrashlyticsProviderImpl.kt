package com.whiplash.data.provider

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.whiplash.domain.provider.CrashlyticsProvider
import javax.inject.Inject

class CrashlyticsProviderImpl @Inject constructor(): CrashlyticsProvider {
    override fun recordError(exception: Throwable) =
        FirebaseCrashlytics.getInstance().recordException(exception)

    override fun logError(message: String) =
        FirebaseCrashlytics.getInstance().log(message)
}