package com.steamtimeline.app.domain

import android.util.Log
import com.steamtimeline.app.data.repository.PreferencesRepository
import com.steamtimeline.app.data.repository.SteamRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTracker @Inject constructor(
    private val steamRepository: SteamRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend fun cleanup() {
        runCatching { steamRepository.cleanupOldSessions(retentionDays = 30) }
            .onFailure { Log.e("SessionTracker", "Cleanup failed", it) }
    }

    suspend fun poll() {
        val apiKey = preferencesRepository.apiKey.first()
        val steamId = preferencesRepository.steamId.first()

        if (apiKey.isBlank() || steamId.isBlank()) {
            Log.d("SessionTracker", "API key or Steam ID not configured")
            return
        }

        try {
            val currentGame = steamRepository.getCurrentlyPlayingGame(apiKey, steamId)
            val activeSession = steamRepository.getActiveSession()

            when {
                // Was playing something, now playing something different
                currentGame != null && activeSession != null && activeSession.appId != currentGame.first -> {
                    steamRepository.closeSession(activeSession.id)
                    steamRepository.startSession(currentGame.first, currentGame.second)
                    Log.d("SessionTracker", "Switched game to ${currentGame.second}")
                }

                // Started playing
                currentGame != null && activeSession == null -> {
                    steamRepository.startSession(currentGame.first, currentGame.second)
                    Log.d("SessionTracker", "Started session: ${currentGame.second}")
                }

                // Stopped playing
                currentGame == null && activeSession != null -> {
                    steamRepository.closeSession(activeSession.id)
                    Log.d("SessionTracker", "Ended session for ${activeSession.gameName}")
                }

                // No change
                else -> Log.d("SessionTracker", "No change detected")
            }
        } catch (e: Exception) {
            Log.e("SessionTracker", "Poll failed", e)
        }
    }
}
