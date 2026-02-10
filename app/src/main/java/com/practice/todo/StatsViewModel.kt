package com.practice.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class StatsViewModel(
    private val focusSessionDao: FocusSessionDao
) : ViewModel() {

    private val _stats = MutableStateFlow(FocusStats())
    val stats = _stats.asStateFlow()

    private val _recentSessions = MutableStateFlow<List<FocusSession>>(emptyList())
    val recentSessions = _recentSessions.asStateFlow()

    init {
        loadStats()
        loadRecentSessions()
    }

    private fun loadStats() {
        viewModelScope.launch {
            focusSessionDao.getAllSessions().collectLatest { sessions ->
                val now = LocalDateTime.now()
                val startOfDay = now.truncatedTo(ChronoUnit.DAYS)
                val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1).truncatedTo(ChronoUnit.DAYS)
                val startOfMonth = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)

                val todaySeconds = sessions
                    .filter { it.startTime >= startOfDay }
                    .sumOf { it.durationSeconds }

                val thisWeekSeconds = sessions
                    .filter { it.startTime >= startOfWeek }
                    .sumOf { it.durationSeconds }

                val thisMonthSeconds = sessions
                    .filter { it.startTime >= startOfMonth }
                    .sumOf { it.durationSeconds }

                val allTimeSeconds = sessions.sumOf { it.durationSeconds }

                val averageSeconds = if (sessions.isNotEmpty()) {
                    allTimeSeconds / sessions.size
                } else {
                    0L
                }

                _stats.value = FocusStats(
                    todaySeconds = todaySeconds,
                    thisWeekSeconds = thisWeekSeconds,
                    thisMonthSeconds = thisMonthSeconds,
                    allTimeSeconds = allTimeSeconds,
                    totalSessions = sessions.size,
                    averageSessionSeconds = averageSeconds
                )
            }
        }
    }

    private fun loadRecentSessions() {
        viewModelScope.launch {
            focusSessionDao.getAllSessions().collectLatest { sessions ->
                _recentSessions.value = sessions.take(10)
            }
        }
    }

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }

    fun formatDetailedDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return buildString {
            if (hours > 0) append("${hours}h ")
            if (minutes > 0 || hours > 0) append("${minutes}m ")
            append("${secs}s")
        }.trim()
    }
}