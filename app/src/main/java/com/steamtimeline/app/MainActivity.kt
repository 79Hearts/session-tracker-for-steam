package com.steamtimeline.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.steamtimeline.app.data.repository.PreferencesRepository
import com.steamtimeline.app.service.SteamMonitorService
import com.steamtimeline.app.ui.SteamTimelineNavHost
import com.steamtimeline.app.ui.theme.SteamTimelineTheme
import com.steamtimeline.app.worker.SteamPollingWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesRepository: PreferencesRepository

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        startServiceIfEnabled()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SteamPollingWorker.enqueue(this)
        ensureNotificationPermissionAndStartService()

        setContent {
            SteamTimelineTheme {
                SteamTimelineNavHost()
            }
        }
    }

    private fun ensureNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) startServiceIfEnabled()
            else notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startServiceIfEnabled()
        }
    }

    private fun startServiceIfEnabled() {
        lifecycleScope.launch {
            if (preferencesRepository.monitoringEnabled.first()) {
                SteamMonitorService.start(this@MainActivity)
            }
        }
    }
}
