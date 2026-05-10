package com.steamtimeline.app.ui.gamehistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.steamtimeline.app.data.local.entity.GameSession
import com.steamtimeline.app.ui.home.formatDuration
import com.steamtimeline.app.ui.home.formatTime
import com.steamtimeline.app.ui.home.gameColor
import com.steamtimeline.app.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHistoryScreen(
    onBack: () -> Unit,
    viewModel: GameHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle(initialValue = GameHistoryUiState())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.gameName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SteamDarkSurface,
                    titleContentColor = SteamLightGray,
                    navigationIconContentColor = SteamLightGray
                )
            )
        },
        containerColor = SteamNavy
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GameBanner(appId = state.appId, gameName = state.gameName)
            }
            item {
                GameStatsSummary(
                    totalSeconds = state.totalPlaytimeSeconds,
                    sessionCount = state.sessionCount,
                    avgSeconds = state.avgSessionSeconds
                )
            }
            item {
                Text(
                    "All Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    color = SteamLightGray,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (state.sessions.isEmpty()) {
                item {
                    Text(
                        "No sessions recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SteamLightGray.copy(alpha = 0.6f)
                    )
                }
            } else {
                items(state.sessions) { session ->
                    GameHistorySessionRow(session = session)
                }
            }
        }
    }
}

@Composable
private fun GameBanner(appId: Long, gameName: String) {
    if (appId == 0L) return
    AsyncImage(
        model = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/$appId/library_hero.jpg",
        contentDescription = gameName,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun GameStatsSummary(totalSeconds: Long, sessionCount: Int, avgSeconds: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "Total Time", value = formatDuration(totalSeconds))
            VerticalDivider(modifier = Modifier.height(40.dp), color = SteamCharcoal)
            StatItem(label = "Sessions", value = sessionCount.toString())
            VerticalDivider(modifier = Modifier.height(40.dp), color = SteamCharcoal)
            StatItem(label = "Avg Session", value = formatDuration(avgSeconds))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = SteamBlue, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = SteamLightGray.copy(alpha = 0.6f))
    }
}

@Composable
private fun GameHistorySessionRow(session: GameSession) {
    val accent = gameColor(session.appId)
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()
    val dateLabel = formatSessionDate(date)
    val duration = formatDuration(session.durationSeconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(color = accent, shape = RoundedCornerShape(2.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dateLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${formatTime(session.startTime)} – ${session.endTime?.let { formatTime(it) } ?: "In progress"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SteamLightGray.copy(alpha = 0.6f)
                )
            }

            Text(
                duration,
                style = MaterialTheme.typography.bodyMedium,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private val sessionDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

private fun formatSessionDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        else -> date.format(sessionDateFormatter)
    }
}
