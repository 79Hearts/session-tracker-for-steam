package com.steamtimeline.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.steamtimeline.app.data.local.entity.GameSession
import com.steamtimeline.app.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val PAGE_COUNT = 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToGameHistory: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val credentialsConfigured by viewModel.credentialsConfigured.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = PAGE_COUNT - 1, pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    var showCalendar by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    val minDate = remember { today.minusDays((PAGE_COUNT - 1).toLong()) }
    val currentDaysAgo = (PAGE_COUNT - 1) - pagerState.currentPage
    val selectedDate = today.minusDays(currentDaysAgo.toLong())

    if (showCalendar) {
        CalendarDialog(
            today = today,
            minDate = minDate,
            selectedDate = selectedDate,
            onDaySelected = { daysAgo ->
                showCalendar = false
                scope.launch { pagerState.animateScrollToPage(PAGE_COUNT - 1 - daysAgo) }
            },
            onDismiss = { showCalendar = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Tracker for Steam", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showCalendar = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Open calendar")
                    }
                    IconButton(onClick = onNavigateToSummary) {
                        Icon(Icons.Default.BarChart, contentDescription = "Summary")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.padding(16.dp)) {
                if (!credentialsConfigured) {
                    OnboardingCard(onGetStarted = onNavigateToSettings)
                } else {
                    NowPlayingCard(activeSession)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                // page 0 = oldest (29 days ago), page (PAGE_COUNT-1) = today.
                // Swiping right decreases page index → reaches older days.
                val daysAgo = (PAGE_COUNT - 1) - page
                DayPage(
                    daysAgo = daysAgo,
                    viewModel = viewModel,
                    onGameClick = onNavigateToGameHistory
                )
            }
        }
    }
}

@Composable
private fun DayPage(daysAgo: Int, viewModel: HomeViewModel, onGameClick: (String) -> Unit = {}) {
    val flow = remember(daysAgo) { viewModel.dayStateFlow(daysAgo) }
    val state by flow.collectAsStateWithLifecycle(initialValue = DayUiState(daysAgo = daysAgo))

    val zone = ZoneId.systemDefault()
    val date = remember(daysAgo) { LocalDate.now().minusDays(daysAgo.toLong()) }
    val dayStart = remember(daysAgo) { date.atStartOfDay(zone).toInstant().toEpochMilli() }
    val dayEnd = remember(daysAgo) { date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { DayHeader(state.date, daysAgo) }
        item { TimelineBar(sessions = state.sessions, daysAgo = daysAgo) }
        item {
            Text(
                "Sessions",
                style = MaterialTheme.typography.titleMedium,
                color = SteamLightGray,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (state.sessions.isEmpty()) {
            item {
                Text(
                    "No sessions on this day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SteamLightGray.copy(alpha = 0.6f)
                )
            }
        } else {
            items(state.sessions.sortedByDescending { it.startTime }) { session ->
                SessionCard(
                    session = session,
                    dayStart = dayStart,
                    dayEnd = dayEnd,
                    onClick = { onGameClick(session.gameName) }
                )
            }
        }
        item {
            FooterStats(
                totalSeconds = state.totalPlaytimeSeconds,
                distinctGames = state.distinctGamesPlayed
            )
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate, daysAgo: Int) {
    val label = when (daysAgo) {
        0 -> "Today"
        1 -> "Yesterday"
        in 2..6 -> date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
        else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    }
    val sub = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))

    Column {
        Text(label, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text(sub, style = MaterialTheme.typography.bodySmall, color = SteamLightGray.copy(alpha = 0.6f))
        if (daysAgo < PAGE_COUNT - 1) {
            Text(
                "swipe right for older days →",
                style = MaterialTheme.typography.labelSmall,
                color = SteamLightGray.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun NowPlayingCard(activeSession: GameSession?) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeSession != null) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color = SteamGreen.copy(alpha = pulseAlpha), shape = CircleShape)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "NOW PLAYING",
                        fontSize = 10.sp,
                        color = SteamGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        activeSession.gameName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Since ${formatTime(activeSession.startTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SteamLightGray.copy(alpha = 0.7f)
                    )
                }
                AsyncImage(
                    model = steamHeaderUrl(activeSession.appId),
                    contentDescription = activeSession.gameName,
                    modifier = Modifier.size(80.dp, 37.dp).clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(12.dp)
                        .background(color = Color.Gray, shape = CircleShape)
                )
                Text(
                    "Not playing anything",
                    style = MaterialTheme.typography.titleMedium,
                    color = SteamLightGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun TimelineBar(sessions: List<GameSession>, daysAgo: Int) {
    val zone = ZoneId.systemDefault()
    val date = LocalDate.now().minusDays(daysAgo.toLong())
    val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
    val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val now = System.currentTimeMillis()
    val rightEdge = if (daysAgo == 0) now else dayEnd
    val totalDayMs = (rightEdge - dayStart).coerceAtLeast(1L)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Timeline",
                style = MaterialTheme.typography.titleSmall,
                color = SteamLightGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            val animProgress = remember(daysAgo, sessions.size) { Animatable(0f) }
            LaunchedEffect(daysAgo, sessions.size) {
                animProgress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                drawRoundRect(
                    color = SteamCharcoal,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(8f)
                )
                sessions.forEach { session ->
                    val effectiveEnd = (session.endTime ?: now).coerceAtMost(rightEdge)
                    val segStart = ((session.startTime - dayStart).coerceAtLeast(0L)).toFloat() / totalDayMs
                    val segEnd = ((effectiveEnd - dayStart).coerceAtLeast(0L)).toFloat() / totalDayMs
                    if (segEnd > segStart) {
                        drawTimelineSegment(
                            startFraction = segStart,
                            endFraction = segStart + (segEnd - segStart) * animProgress.value,
                            color = gameColor(session.appId),
                            canvasWidth = size.width,
                            canvasHeight = size.height
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("12:00 AM", fontSize = 10.sp, color = SteamLightGray.copy(alpha = 0.5f))
                Text(
                    if (daysAgo == 0) formatTime(now) else "11:59 PM",
                    fontSize = 10.sp,
                    color = SteamLightGray.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun DrawScope.drawTimelineSegment(
    startFraction: Float,
    endFraction: Float,
    color: Color,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val left = startFraction * canvasWidth
    val right = endFraction * canvasWidth
    val width = (right - left).coerceAtLeast(4f)
    drawRoundRect(
        color = color,
        topLeft = Offset(left, 2f),
        size = Size(width, canvasHeight - 4f),
        cornerRadius = CornerRadius(6f)
    )
}

@Composable
private fun SessionCard(
    session: GameSession,
    dayStart: Long = 0L,
    dayEnd: Long = Long.MAX_VALUE,
    onClick: () -> Unit = {}
) {
    val accent = gameColor(session.appId)

    // Rollover detection
    val isRolloverStart = session.endTime != null && session.endTime >= dayEnd   // Day 1: started today, ends tomorrow
    val isRolloverEnd = session.startTime < dayStart                              // Day 2: started yesterday, ends today
    val rolloverColor = Color(0xFFFFB300)

    // Clipped display times
    val displayStart = if (isRolloverEnd) "12:00 AM" else formatTime(session.startTime)
    val displayEnd = when {
        isRolloverStart -> "11:59 PM"
        session.endTime != null -> formatTime(session.endTime)
        else -> "Now"
    }

    // Clipped duration (only the portion within this day)
    val displayDurationSeconds = when {
        isRolloverEnd -> ((session.endTime ?: System.currentTimeMillis()) - dayStart) / 1000
        isRolloverStart -> (dayEnd - session.startTime) / 1000
        else -> session.durationSeconds
    }
    val duration = formatDuration(displayDurationSeconds)

    // Partial border modifier for rollover cards
    val cardModifier = when {
        isRolloverStart -> Modifier.fillMaxWidth()
            .partialBorder(rolloverColor, 2.dp, drawTop = true, drawBottom = true, drawStart = true, drawEnd = false)
        isRolloverEnd -> Modifier.fillMaxWidth()
            .partialBorder(rolloverColor, 2.dp, drawTop = true, drawBottom = true, drawStart = false, drawEnd = true)
        else -> Modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .background(color = accent, shape = RoundedCornerShape(2.dp))
                )

                AsyncImage(
                    model = steamHeaderUrl(session.appId),
                    contentDescription = session.gameName,
                    modifier = Modifier.size(64.dp, 30.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.gameName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "$displayStart – $displayEnd",
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

            if (isRolloverStart || isRolloverEnd) {
                Text(
                    text = if (isRolloverStart) "midnight rollover →" else "← midnight rollover",
                    modifier = Modifier.padding(start = 28.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = rolloverColor
                )
            }
        }
    }
}

@Composable
private fun FooterStats(totalSeconds: Long, distinctGames: Int) {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Total", if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m")
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = SteamCharcoal)
            StatItem("Games", distinctGames.toString())
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = SteamBlue, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = SteamLightGray.copy(alpha = 0.6f))
    }
}

@Composable
private fun OnboardingCard(onGetStarted: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SteamCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Welcome to Session Tracker for Steam",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Enter your Steam API key and Steam64 ID to start tracking your sessions.",
                style = MaterialTheme.typography.bodyMedium,
                color = SteamLightGray.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(containerColor = SteamBlue)
            ) {
                Text("Get Started", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Utilities ---

fun steamHeaderUrl(appId: Long) =
    "https://cdn.cloudflare.steamstatic.com/steam/apps/$appId/header.jpg"

fun gameColor(appId: Long): Color {
    val hash = appId.hashCode()
    val hue = ((hash and 0xFFFFFF).toLong() % 360).toFloat().let { if (it < 0) it + 360f else it }
    return Color.hsl(hue = hue, saturation = 0.65f, lightness = 0.55f)
}

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

fun formatTime(epochMs: Long): String =
    Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).format(timeFormatter)

fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return when {
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m ${s}s"
        else -> "${s}s"
    }
}

private fun Modifier.partialBorder(
    color: Color,
    strokeWidth: Dp,
    drawTop: Boolean = false,
    drawBottom: Boolean = false,
    drawStart: Boolean = false,
    drawEnd: Boolean = false
): Modifier = this.drawBehind {
    val stroke = strokeWidth.toPx()
    val half = stroke / 2f
    if (drawTop) drawLine(color, Offset(0f, half), Offset(size.width, half), stroke)
    if (drawBottom) drawLine(color, Offset(0f, size.height - half), Offset(size.width, size.height - half), stroke)
    if (drawStart) drawLine(color, Offset(half, 0f), Offset(half, size.height), stroke)
    if (drawEnd) drawLine(color, Offset(size.width - half, 0f), Offset(size.width - half, size.height), stroke)
}
