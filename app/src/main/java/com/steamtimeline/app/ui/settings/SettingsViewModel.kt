package com.steamtimeline.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steamtimeline.app.data.repository.PreferencesRepository
import com.steamtimeline.app.data.repository.SteamRepository
import com.steamtimeline.app.service.SteamMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TestStatus {
    data object Idle : TestStatus
    data object Testing : TestStatus
    data class Success(val personaName: String) : TestStatus
    data class Failure(val reason: String) : TestStatus
}

data class SettingsUiState(
    val apiKey: String = "",
    val steamId: String = "",
    val monitoringEnabled: Boolean = true,
    val pollIntervalSeconds: Int = 60,
    val saved: Boolean = false,
    val testStatus: TestStatus = TestStatus.Idle
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val preferencesRepository: PreferencesRepository,
    private val steamRepository: SteamRepository
) : ViewModel() {

    private val _saved = MutableStateFlow(false)
    private val _testStatus = MutableStateFlow<TestStatus>(TestStatus.Idle)

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            preferencesRepository.apiKey,
            preferencesRepository.steamId,
            preferencesRepository.monitoringEnabled,
            preferencesRepository.pollIntervalSeconds
        ) { apiKey, steamId, monitoring, pollInterval ->
            Triple(apiKey, steamId, Pair(monitoring, pollInterval))
        },
        _saved,
        _testStatus
    ) { base, saved, testStatus ->
        SettingsUiState(
            apiKey = base.first,
            steamId = base.second,
            monitoringEnabled = base.third.first,
            pollIntervalSeconds = base.third.second,
            saved = saved,
            testStatus = testStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    /** Resolves the steam-id-or-URL field, then saves. Returns once persisted. */
    fun save(apiKey: String, steamIdOrUrl: String) {
        viewModelScope.launch {
            val cleanKey = apiKey.trim()
            val resolved = steamRepository.resolveSteamId(cleanKey, steamIdOrUrl)
                ?: steamIdOrUrl.trim() // fall back to whatever was typed

            preferencesRepository.saveApiKey(cleanKey)
            preferencesRepository.saveSteamId(resolved)
            _saved.value = true
        }
    }

    fun testConnection(apiKey: String, steamIdOrUrl: String) {
        viewModelScope.launch {
            _testStatus.value = TestStatus.Testing
            val cleanKey = apiKey.trim()
            val resolved = steamRepository.resolveSteamId(cleanKey, steamIdOrUrl)
            if (resolved == null) {
                _testStatus.value = TestStatus.Failure("Could not resolve Steam ID")
                return@launch
            }
            val name = steamRepository.testConnection(cleanKey, resolved)
            _testStatus.value = if (name != null) TestStatus.Success(name)
            else TestStatus.Failure("API call failed — check key & profile privacy")
        }
    }

    fun setMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setMonitoringEnabled(enabled)
            if (enabled) SteamMonitorService.start(appContext)
            else SteamMonitorService.stop(appContext)
        }
    }

    fun updatePollInterval(seconds: Int) {
        viewModelScope.launch {
            preferencesRepository.setPollIntervalSeconds(seconds)
        }
    }

    fun clearSaved() { _saved.value = false }
    fun clearTestStatus() { _testStatus.value = TestStatus.Idle }
}
