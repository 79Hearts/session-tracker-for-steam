package com.steamtimeline.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.steamtimeline.app.MainActivity
import com.steamtimeline.app.R
import com.steamtimeline.app.data.repository.PreferencesRepository
import com.steamtimeline.app.data.repository.SteamRepository
import com.steamtimeline.app.domain.SessionTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SteamMonitorService : LifecycleService() {

    @Inject lateinit var sessionTracker: SessionTracker
    @Inject lateinit var steamRepository: SteamRepository
    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent?.action == ACTION_STOP) {
            lifecycleScope.launch {
                preferencesRepository.setMonitoringEnabled(false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification("Monitoring Steam activity…"))

        lifecycleScope.launch {
            sessionTracker.cleanup()
            while (isActive) {
                runCatching { sessionTracker.poll() }
                val active = runCatching { steamRepository.getActiveSession() }.getOrNull()
                updateNotification(
                    if (active != null) "Now playing: ${active.gameName}"
                    else "Monitoring Steam activity…"
                )
                delay(POLL_INTERVAL_MS)
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Session Tracker for Steam",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks your active Steam game"
                setShowBadge(false)
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, SteamMonitorService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Session Tracker for Steam")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentIntent(openAppIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    companion object {
        const val NOTIFICATION_ID = 42
        const val CHANNEL_ID = "steam_monitor_channel"
        const val POLL_INTERVAL_MS = 60_000L
        const val ACTION_STOP = "com.sessiontracker.steam.STOP"

        fun start(context: Context) {
            val intent = Intent(context, SteamMonitorService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, SteamMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
