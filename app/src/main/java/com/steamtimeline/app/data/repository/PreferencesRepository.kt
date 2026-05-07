package com.steamtimeline.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "steam_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val apiKeyPref = stringPreferencesKey("api_key")
    private val steamIdPref = stringPreferencesKey("steam_id")
    private val monitoringEnabledPref = booleanPreferencesKey("monitoring_enabled")

    val apiKey: Flow<String> = context.dataStore.data.map { it[apiKeyPref] ?: "" }
    val steamId: Flow<String> = context.dataStore.data.map { it[steamIdPref] ?: "" }
    val monitoringEnabled: Flow<Boolean> = context.dataStore.data.map { it[monitoringEnabledPref] ?: true }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { it[apiKeyPref] = key }
    }

    suspend fun saveSteamId(id: String) {
        context.dataStore.edit { it[steamIdPref] = id }
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { it[monitoringEnabledPref] = enabled }
    }
}
