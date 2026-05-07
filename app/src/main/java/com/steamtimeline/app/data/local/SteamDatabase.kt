package com.steamtimeline.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.steamtimeline.app.data.local.dao.GameSessionDao
import com.steamtimeline.app.data.local.entity.GameSession

@Database(
    entities = [GameSession::class],
    version = 1,
    exportSchema = false
)
abstract class SteamDatabase : RoomDatabase() {
    abstract fun gameSessionDao(): GameSessionDao
}
