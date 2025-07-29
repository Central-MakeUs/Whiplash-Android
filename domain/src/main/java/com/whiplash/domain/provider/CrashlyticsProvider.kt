package com.whiplash.domain.provider

interface CrashlyticsProvider {
    fun recordError(exception: Throwable)
    fun logError(message: String)
}