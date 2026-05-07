package com.steamtimeline.app.data.repository

import com.steamtimeline.app.data.local.dao.GameSessionDao
import com.steamtimeline.app.data.local.entity.GameSession
import com.steamtimeline.app.data.remote.SteamApiService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SteamRepository @Inject constructor(
    private val api: SteamApiService,
    private val dao: GameSessionDao
) {
    suspend fun getCurrentlyPlayingGame(apiKey: String, steamId: String): Pair<Long, String>? {
        val response = api.getPlayerSummaries(apiKey, steamId)
        val player = response.response.players.firstOrNull() ?: return null
        val gameId = player.gameid?.toLongOrNull() ?: return null
        val gameName = player.gameextrainfo ?: "Unknown Game"
        return Pair(gameId, gameName)
    }

    suspend fun resolveSteamId(apiKey: String, input: String): String? {
        val trimmed = input.trim().trimEnd('/')
        if (trimmed.isEmpty()) return null
        if (trimmed.all { it.isDigit() } && trimmed.length == 17) return trimmed
        Regex("""profiles/(\d{17})""").find(trimmed)?.let { return it.groupValues[1] }
        val vanity = Regex("""(?:^|/)id/([^/]+)""").find(trimmed)?.groupValues?.get(1)
            ?: trimmed.substringAfterLast('/')
        return runCatching {
            val response = api.resolveVanityUrl(apiKey, vanity)
            if (response.response.success == 1) response.response.steamid else null
        }.getOrNull()
    }

    suspend fun testConnection(apiKey: String, steamId: String): String? = runCatching {
        api.getPlayerSummaries(apiKey, steamId).response.players.firstOrNull()?.personaname
    }.getOrNull()

    suspend fun getActiveSession(): GameSession? = dao.getActiveSession()
    fun observeActiveSession(): Flow<GameSession?> = dao.observeActiveSession()

    suspend fun startSession(appId: Long, gameName: String): Long {
        val session = GameSession(
            appId = appId,
            gameName = gameName,
            startTime = System.currentTimeMillis(),
            endTime = null,
            durationSeconds = 0
        )
        return dao.insert(session)
    }

    suspend fun closeSession(id: Long) {
        val endTime = System.currentTimeMillis()
        val session = dao.getActiveSession() ?: return
        val duration = (endTime - session.startTime) / 1000
        dao.closeSession(id, endTime, duration)
    }

    fun getSessionsForDate(date: LocalDate): Flow<List<GameSession>> {
        val (start, end) = dayBounds(date)
        return dao.getSessionsBetween(start, end)
    }

    fun getTotalPlaytimeForDate(date: LocalDate): Flow<Long?> {
        val (start, end) = dayBounds(date)
        return dao.getTotalPlaytimeBetween(start, end)
    }

    fun getDistinctGamesForDate(date: LocalDate): Flow<Int> {
        val (start, end) = dayBounds(date)
        return dao.getDistinctGamesBetween(start, end)
    }

    fun getSessionsInRange(fromDaysAgo: Int): Flow<List<GameSession>> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().minusDays(fromDaysAgo.toLong())
            .atStartOfDay(zone).toInstant().toEpochMilli()
        val end = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return dao.getSessionsBetween(start, end)
    }

    fun getAllSessions(): Flow<List<GameSession>> = dao.getAllSessions()

    suspend fun cleanupOldSessions(retentionDays: Int = 30) {
        val cutoff = LocalDate.now().minusDays(retentionDays.toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        dao.deleteSessionsOlderThan(cutoff)
    }

    private fun dayBounds(date: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }
}
