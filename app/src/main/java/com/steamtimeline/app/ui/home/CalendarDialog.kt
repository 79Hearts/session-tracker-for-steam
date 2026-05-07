package com.steamtimeline.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.steamtimeline.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDialog(
    today: LocalDate,
    minDate: LocalDate,
    selectedDate: LocalDate,
    onDaySelected: (daysAgo: Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SteamNavy
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Select a day", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SteamDarkSurface,
                        titleContentColor = SteamLightGray,
                        navigationIconContentColor = SteamLightGray
                    )
                )

                Text(
                    "Showing the last 30 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = SteamLightGray.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(monthsBetween(minDate, today)) { month ->
                        MonthGrid(
                            yearMonth = month,
                            today = today,
                            minDate = minDate,
                            selectedDate = selectedDate,
                            onDayClick = { date ->
                                val daysAgo = ChronoUnit.DAYS.between(date, today).toInt()
                                onDaySelected(daysAgo)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    today: LocalDate,
    minDate: LocalDate,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val firstOfMonth = yearMonth.atDay(1)
    val leadingBlanks = ((firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7)
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = ((leadingBlanks + daysInMonth + 6) / 7) * 7

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = SteamLightGray,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in 0 until 7) {
                val dow = firstDayOfWeek.plus(i.toLong())
                Text(
                    dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                    color = SteamLightGray.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        var cell = 0
        while (cell < totalCells) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val pos = cell
                    if (pos < leadingBlanks || pos >= leadingBlanks + daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val day = pos - leadingBlanks + 1
                        val date = yearMonth.atDay(day)
                        val inRange = !date.isBefore(minDate) && !date.isAfter(today)
                        DayCell(
                            date = date,
                            isToday = date == today,
                            isSelected = date == selectedDate && date != today,
                            isEnabled = inRange,
                            onClick = { onDayClick(date) },
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                    }
                    cell++
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        isToday -> SteamBlue
        isSelected -> SteamCharcoal
        else -> Color.Transparent
    }
    val textColor = when {
        isToday -> Color.White
        !isEnabled -> SteamLightGray.copy(alpha = 0.25f)
        else -> SteamLightGray
    }
    Box(
        modifier = modifier
            .padding(3.dp)
            .clip(CircleShape)
            .background(bg)
            .let { m ->
                if (isSelected && !isToday) m.border(1.5.dp, SteamBlue, CircleShape) else m
            }
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            date.dayOfMonth.toString(),
            color = textColor,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun monthsBetween(minDate: LocalDate, maxDate: LocalDate): List<YearMonth> {
    val list = mutableListOf<YearMonth>()
    var cursor = YearMonth.from(minDate)
    val end = YearMonth.from(maxDate)
    while (!cursor.isAfter(end)) {
        list.add(cursor)
        cursor = cursor.plusMonths(1)
    }
    return list
}
