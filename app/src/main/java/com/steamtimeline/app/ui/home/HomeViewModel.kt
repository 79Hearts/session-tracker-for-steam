package com.steamtimeline.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steamtimeline.app.data.local.entity.GameSession
import com.steamtimeline.app.data.repository.PreferencesRepository
import com.steamtimeline.app.data.repository.SteamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class DayUiState(
    val date: LocalDate = LocalDate.now(),
    val daysAgo: Int = 0,
    val sessions: List<GameSession> = emptyList(),
    val totalPlaytimeSeconds: Long = 0,
    val distinctGamesPlayed: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val steamRepository: SteamRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val activeSession: StateFlow<GameSession?> = steamRepository.observeActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val credentialsConfigured: StateFlow<Boolean> = combine(
        preferencesRepository.apiKey,
        preferencesRepository.steamId
    ) { key, id -> key.isNotBlank() && id.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun dayStateFlow(daysAgo: Int): Flow<DayUiState> {
        val date = LocalDate.now().minusDays(daysAgo.toLong())
        return combine(
            steamRepository.getSessionsForDate(date),
            steamRepository.getTotalPlaytimeForDate(date),
            steamRepository.getDistinctGamesForDate(date)
        ) { sessions, total, distinct ->
            DayUiState(
                date = date,
                daysAgo = daysAgo,
                sessions = sessions,
                totalPlaytimeSeconds = total ?: 0L,
                distinctGamesPlayed = distinct
            )
        }
    }
}
