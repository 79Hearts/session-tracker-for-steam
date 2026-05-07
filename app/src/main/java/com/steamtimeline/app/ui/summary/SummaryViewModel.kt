package com.steamtimeline.app.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steamtimeline.app.data.repository.SteamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class Range(val days: Int, val label: String) {
    DAYS_3(3, "3D"),
    DAYS_7(7, "7D"),
    DAYS_14(14, "14D"),
    DAYS_30(30, "30D")
}

data class GameTotal(
    val appId: Long,
    val gameName: String,
    val totalSeconds: Long,
    val sessionCount: Int
)

data class SummaryUiState(
    val range: Range = Range.DAYS_7,
    val totalSeconds: Long = 0,
    val totals: List<GameTotal> = emptyList()
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val steamRepository: SteamRepository
) : ViewModel() {

    private val _range = MutableStateFlow(Range.DAYS_7)
    val range: StateFlow<Range> = _range.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SummaryUiState> = _range.flatMapLatest { range ->
        steamRepository.getSessionsInRange(range.days - 1).map { sessions ->
            val grouped = sessions.groupBy { it.appId }
                .map { (appId, list) ->
                    GameTotal(
                        appId = appId,
                        gameName = list.first().gameName,
                        totalSeconds = list.sumOf { it.durationSeconds },
                        sessionCount = list.size
                    )
                }
                .sortedByDescending { it.totalSeconds }
            SummaryUiState(
                range = range,
                totalSeconds = grouped.sumOf { it.totalSeconds },
                totals = grouped
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SummaryUiState()
    )

    fun setRange(r: Range) { _range.value = r }
}
