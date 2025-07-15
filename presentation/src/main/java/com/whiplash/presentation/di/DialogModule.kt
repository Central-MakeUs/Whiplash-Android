package com.whiplash.presentation.di

import android.content.Context
import com.whiplash.presentation.dialog.DisableAlarmPopup
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object DialogModule {

    @Provides
    @ActivityScoped
    fun provideDisableAlarmPopup(@ActivityContext context: Context): DisableAlarmPopup =
        DisableAlarmPopup(context)

}