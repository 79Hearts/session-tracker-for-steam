package com.steamtimeline.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Steam-inspired dark navy/charcoal palette
val SteamNavy = Color(0xFF1B2838)
val SteamCharcoal = Color(0xFF2A475E)
val SteamBlue = Color(0xFF1A9FFF)
val SteamGreen = Color(0xFF4CAF50)
val SteamLightGray = Color(0xFFC7D5E0)
val SteamDarkSurface = Color(0xFF171A21)
val SteamCardBg = Color(0xFF213247)

private val DarkColorScheme = darkColorScheme(
    primary = SteamBlue,
    onPrimary = Color.White,
    primaryContainer = SteamCharcoal,
    onPrimaryContainer = SteamLightGray,
    secondary = SteamGreen,
    onSecondary = Color.White,
    background = SteamNavy,
    onBackground = SteamLightGray,
    surface = SteamDarkSurface,
    onSurface = SteamLightGray,
    surfaceVariant = SteamCardBg,
    onSurfaceVariant = SteamLightGray,
    outline = SteamCharcoal
)

@Composable
fun SteamTimelineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
