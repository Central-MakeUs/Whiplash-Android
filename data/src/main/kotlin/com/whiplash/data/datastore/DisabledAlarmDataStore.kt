package com.whiplash.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.disabledAlarmDataStore: DataStore<Preferences> by preferencesDataStore(name = "disabled_alarms")

@Singleton
class DisabledAlarmDataStore @Inject constructor(
    private val context: Context
) {
    suspend fun setAlarmDisabled(alarmId: Long, dayOfWeek: Int, isDisabled: Boolean) {
        val key = booleanPreferencesKey("${alarmId}_${dayOfWeek}")
        context.disabledAlarmDataStore.edit { preferences ->
            preferences[key] = isDisabled
        }
    }

    suspend fun isAlarmDisabled(alarmId: Long, dayOfWeek: Int): Boolean {
        val key = booleanPreferencesKey("${alarmId}_${dayOfWeek}")
        return context.disabledAlarmDataStore.data.map { preferences ->
            preferences[key] ?: false
        }.first()
    }
}
