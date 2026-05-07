package com.steamtimeline.app.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.steamtimeline.app.ui.home.gameColor
import com.steamtimeline.app.ui.home.steamHeaderUrl
import com.steamtimeline.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onBack: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SteamDarkSurface,
                    titleContentColor = SteamLightGray
                )
            )
        },
        containerColor = SteamNavy
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { RangeSelector(state.range) { viewModel.setRange(it) } }
            item { TotalsHeader(state.totalSeconds, state.totals.size, state.range) }
            item { StackedTimelineBar(state.totals, state.totalSeconds) }
            item {
                Text(
                    "Per-game totals",
                    style = MaterialTheme.typography.titleMedium,
                    color = SteamLightGray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (state.totals.isEmpty()) {
                item {
                    Text(
                        "No sessions in this range",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SteamLightGray.copy(alpha = 0.6f)
                    )
                }
            } else {
                val maxSeconds = state.totals.maxOf { it.totalSeconds }.coerceAtLeast(1)
                items(state.totals) { game ->
                    GameTotalRow(game = game, maxSeconds = maxSeconds)
                }
            }
        }
    }
}

@Composable
private fun RangeSelector(current: Range, onSelect: (Range) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Range.entries.forEach { r ->
                val selected = r == current
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(r) },
                    label = { Text(r.label) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SteamBlue,
                        selectedLabelColor = Color.White,
                        containerColor = SteamCharcoal,
                        labelColor = SteamLightGray
                    )
                )
            }
        }
    }
}

@Composable
private fun TotalsHeader(totalSeconds: Long, gameCount: Int, range: Range) {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                "Total time",
                if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
            )
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = SteamCharcoal)
            StatItem("Games", gameCount.toString())
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = SteamCharcoal)
            StatItem("Range", range.label)
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
private fun StackedTimelineBar(totals: List<GameTotal>, totalSeconds: Long) {
    if (totals.isEmpty() || totalSeconds == 0L) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Time share",
                style = MaterialTheme.typography.titleSmall,
                color = SteamLightGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Canvas(
                modifier = Modifier.fillMaxWidth().height(28.dp)
            ) {
                drawRoundRect(
                    color = SteamCharcoal,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(8f)
                )
                var cursor = 0f
                totals.forEach { game ->
                    val frac = game.totalSeconds.toFloat() / totalSeconds
                    val w = (frac * size.width).coerceAtLeast(2f)
                    drawRoundRect(
                        color = gameColor(game.appId),
                        topLeft = Offset(cursor, 2f),
                        size = Size(w, size.height - 4f),
                        cornerRadius = CornerRadius(6f)
                    )
                    cursor += w
                }
            }
        }
    }
}

@Composable
private fun GameTotalRow(game: GameTotal, maxSeconds: Long) {
    val accent = gameColor(game.appId)
    val hours = game.totalSeconds / 3600
    val minutes = (game.totalSeconds % 3600) / 60
    val durationText = when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
    val barFraction = (game.totalSeconds.toFloat() / maxSeconds).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = steamHeaderUrl(game.appId),
                    contentDescription = game.gameName,
                    modifier = Modifier.size(64.dp, 30.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        game.gameName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${game.sessionCount} ${if (game.sessionCount == 1) "session" else "sessions"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SteamLightGray.copy(alpha = 0.6f)
                    )
                }
                Text(
                    durationText,
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            // Per-game proportional bar (relative to top game)
            Box(
                modifier = Modifier.fillMaxWidth().height(6.dp)
                    .background(SteamCharcoal, shape = RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(barFraction)
                        .background(accent, shape = RoundedCornerShape(3.dp))
                )
            }
        }
    }
}
