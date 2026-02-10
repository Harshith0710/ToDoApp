package com.practice.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

class StatsViewModel(
    private val focusSessionDao: FocusSessionDao
) : ViewModel() {

    private val _stats = MutableStateFlow(FocusStats())
    val stats = _stats.asStateFlow()

    private val _recentSessions = MutableStateFlow<List<FocusSession>>(emptyList())
    val recentSessions = _recentSessions.asStateFlow()

    private val _chartData = MutableStateFlow<List<ChartData>>(emptyList())
    val chartData = _chartData.asStateFlow()

    private val _selectedChartPeriod = MutableStateFlow(ChartPeriod.LAST_7_DAYS)
    val selectedChartPeriod = _selectedChartPeriod.asStateFlow()

    private var allSessions: List<FocusSession> = emptyList()

    init {
        loadStats()
        loadRecentSessions()
    }

    fun setChartPeriod(period: ChartPeriod) {
        _selectedChartPeriod.value = period
        updateChartData(allSessions, period)
    }

    private fun loadStats() {
        viewModelScope.launch {
            focusSessionDao.getAllSessions().collectLatest { sessions ->
                allSessions = sessions
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

                val sessionsToday = sessions.count { it.startTime >= startOfDay }

                val averageSeconds = if (sessions.isNotEmpty()) {
                    allTimeSeconds / sessions.size
                } else {
                    0L
                }

                val averageSessionsPerDay = calculateAverageSessionsPerDay(sessions)

                val streaks = calculateStreaks(sessions)

                _stats.value = FocusStats(
                    todaySeconds = todaySeconds,
                    thisWeekSeconds = thisWeekSeconds,
                    thisMonthSeconds = thisMonthSeconds,
                    allTimeSeconds = allTimeSeconds,
                    totalSessions = sessions.size,
                    averageSessionSeconds = averageSeconds,
                    sessionsToday = sessionsToday,
                    averageSessionsPerDay = averageSessionsPerDay,
                    currentStreak = streaks.first,
                    longestStreak = streaks.second
                )

                updateChartData(sessions, _selectedChartPeriod.value)
            }
        }
    }

    private fun calculateAverageSessionsPerDay(sessions: List<FocusSession>): Double {
        if (sessions.isEmpty()) return 0.0

        val firstSessionDate = sessions.minByOrNull { it.startTime }?.startTime?.toLocalDate()
            ?: return 0.0
        val today = LocalDate.now()
        val daysSinceFirst = ChronoUnit.DAYS.between(firstSessionDate, today) + 1

        return if (daysSinceFirst > 0) {
            sessions.size.toDouble() / daysSinceFirst.toDouble()
        } else {
            0.0
        }
    }

    private fun calculateStreaks(sessions: List<FocusSession>): Pair<Int, Int> {
        if (sessions.isEmpty()) return Pair(0, 0)

        val sessionsByDate = sessions
            .groupBy { it.startTime.toLocalDate() }
            .mapValues { it.value.size }

        val uniqueDates = sessionsByDate.keys
        if (uniqueDates.isEmpty()) return Pair(0, 0)

        var currentStreak: Int
        var longestStreak = 0

        val today = LocalDate.now()
        var lastSessionDate: LocalDate? = null

        for (date in uniqueDates) {
            val previousDate = date.minusDays(1)
            if(!uniqueDates.contains(previousDate)){
                var tempStreak = 1
                var expectedDate = date.plusDays(1)
                while(uniqueDates.contains(expectedDate)){
                    tempStreak++
                    expectedDate = expectedDate.plusDays(1)
                }
                longestStreak = max(longestStreak, tempStreak)
            }
            if (lastSessionDate == null || date.isAfter(lastSessionDate)) {
                lastSessionDate = date
            }
        }

        val daysSinceLastSession = ChronoUnit.DAYS.between(lastSessionDate, today)

        when (daysSinceLastSession) {
            0L, 1L -> {
                currentStreak = 1
                var checkDate = lastSessionDate?.minusDays(1)
                while (uniqueDates.contains(checkDate)) {
                    currentStreak++
                    checkDate = checkDate?.minusDays(1)
                }
            }
            else -> {
                currentStreak = 0
            }
        }

        return Pair(currentStreak, longestStreak)
    }

    private fun updateChartData(sessions: List<FocusSession>, period: ChartPeriod) {
        val data = when (period) {
            ChartPeriod.LAST_24_HOURS -> generateHourlyData(sessions)
            ChartPeriod.LAST_7_DAYS -> generateDailyData(sessions)
            ChartPeriod.LAST_12_MONTHS -> generateMonthlyData(sessions)
        }
        _chartData.value = data
    }

    private fun generateHourlyData(sessions: List<FocusSession>): List<ChartData> {
        val now = LocalDateTime.now()
        val last24Hours = now.minusHours(24)

        val hourlyMap = mutableMapOf<Int, Long>()
        for (i in 0..23) {
            hourlyMap[i] = 0L
        }

        sessions
            .filter { it.startTime >= last24Hours }
            .forEach { session ->
                val hour = session.startTime.hour
                hourlyMap[hour] = (hourlyMap[hour] ?: 0L) + session.durationSeconds
            }

        return (0..23).map { hour ->
            val value = hourlyMap[hour] ?: 0L
            ChartData(
                label = String.format("%02d:00", hour),
                value = value,
                displayValue = formatDuration(value)
            )
        }
    }

    private fun generateDailyData(sessions: List<FocusSession>): List<ChartData> {
        val today = LocalDate.now()
        val dailyMap = mutableMapOf<LocalDate, Long>()

        for (i in 0..6) {
            val date = today.minusDays(6 - i.toLong())
            dailyMap[date] = 0L
        }

        sessions
            .filter {
                val sessionDate = it.startTime.toLocalDate()
                sessionDate >= today.minusDays(6) && sessionDate <= today
            }
            .forEach { session ->
                val date = session.startTime.toLocalDate()
                dailyMap[date] = (dailyMap[date] ?: 0L) + session.durationSeconds
            }

        val formatter = DateTimeFormatter.ofPattern("EEE")
        return (0..6).map { i ->
            val date = today.minusDays(6 - i.toLong())
            val value = dailyMap[date] ?: 0L
            ChartData(
                label = date.format(formatter),
                value = value,
                displayValue = formatDuration(value)
            )
        }
    }

    private fun generateMonthlyData(sessions: List<FocusSession>): List<ChartData> {
        val today = LocalDate.now()
        val monthlyMap = mutableMapOf<String, Long>()

        for (i in 0..11) {
            val date = today.minusMonths(11 - i.toLong())
            val key = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            monthlyMap[key] = 0L
        }

        sessions
            .filter {
                val sessionDate = it.startTime.toLocalDate()
                sessionDate >= today.minusMonths(11).withDayOfMonth(1)
            }
            .forEach { session ->
                val key = session.startTime.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                monthlyMap[key] = (monthlyMap[key] ?: 0L) + session.durationSeconds
            }

        val labelFormatter = DateTimeFormatter.ofPattern("MMM")
        return (0..11).map { i ->
            val date = today.minusMonths(11 - i.toLong())
            val key = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val value = monthlyMap[key] ?: 0L
            ChartData(
                label = date.format(labelFormatter),
                value = value,
                displayValue = formatDuration(value)
            )
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

    fun formatDecimal(value: Double): String {
        return String.format("%.1f", value)
    }
}