package com.steamtimeline.app.ui.gamehistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.steamtimeline.app.data.local.dao.GameSessionDao
import com.steamtimeline.app.data.local.entity.GameSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class GameHistoryUiState(
    val gameName: String = "",
    val sessions: List<GameSession> = emptyList(),
    val totalPlaytimeSeconds: Long = 0L,
    val sessionCount: Int = 0,
    val avgSessionSeconds: Long = 0L,
    val appId: Long = 0L
)

@HiltViewModel
class GameHistoryViewModel @Inject constructor(
    private val dao: GameSessionDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gameName: String = checkNotNull(savedStateHandle["gameName"])

    val uiState: Flow<GameHistoryUiState> = dao.getSessionsForGame(gameName).map { sessions ->
        val completed = sessions.filter { it.endTime != null }
        val total = completed.sumOf { it.durationSeconds }
        val avg = if (completed.isNotEmpty()) total / completed.size else 0L
        GameHistoryUiState(
            gameName = gameName,
            sessions = sessions,
            totalPlaytimeSeconds = total,
            sessionCount = sessions.size,
            avgSessionSeconds = avg,
            appId = sessions.firstOrNull()?.appId ?: 0L
        )
    }
}
