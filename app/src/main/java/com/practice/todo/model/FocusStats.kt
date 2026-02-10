package com.practice.todo.model

data class FocusStats(
    val todaySeconds: Long = 0,
    val thisWeekSeconds: Long = 0,
    val thisMonthSeconds: Long = 0,
    val allTimeSeconds: Long = 0,
    val totalSessions: Int = 0,
    val averageSessionSeconds: Long = 0,
    val sessionsToday: Int = 0,
    val averageSessionsPerDay: Double = 0.0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)