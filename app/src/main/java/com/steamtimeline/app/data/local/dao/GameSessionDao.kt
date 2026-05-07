package com.steamtimeline.app.data.local.dao

import androidx.room.*
import com.steamtimeline.app.data.local.entity.GameSession
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    @Insert
    suspend fun insert(session: GameSession): Long

    @Update
    suspend fun update(session: GameSession)

    @Query("SELECT * FROM game_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): GameSession?

    @Query("SELECT * FROM game_sessions WHERE endTime IS NULL LIMIT 1")
    fun observeActiveSession(): Flow<GameSession?>

    @Query("SELECT * FROM game_sessions WHERE startTime >= :startMs AND startTime < :endMs ORDER BY startTime ASC")
    fun getSessionsBetween(startMs: Long, endMs: Long): Flow<List<GameSession>>

    @Query("SELECT SUM(durationSeconds) FROM game_sessions WHERE startTime >= :startMs AND startTime < :endMs")
    fun getTotalPlaytimeBetween(startMs: Long, endMs: Long): Flow<Long?>

    @Query("SELECT COUNT(DISTINCT appId) FROM game_sessions WHERE startTime >= :startMs AND startTime < :endMs")
    fun getDistinctGamesBetween(startMs: Long, endMs: Long): Flow<Int>

    @Query("SELECT * FROM game_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<GameSession>>

    @Query("UPDATE game_sessions SET endTime = :endTime, durationSeconds = :duration WHERE id = :id")
    suspend fun closeSession(id: Long, endTime: Long, duration: Long)

    @Query("DELETE FROM game_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("DELETE FROM game_sessions WHERE startTime < :cutoffMs AND endTime IS NOT NULL")
    suspend fun deleteSessionsOlderThan(cutoffMs: Long)
}
