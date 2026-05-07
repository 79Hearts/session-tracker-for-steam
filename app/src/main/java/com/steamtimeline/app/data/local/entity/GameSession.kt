package com.steamtimeline.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appId: Long,
    val gameName: String,
    val startTime: Long,
    val endTime: Long?,
    val durationSeconds: Long
)
