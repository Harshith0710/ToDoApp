package com.practice.todo

data class FocusStats(
    val todaySeconds: Long = 0,
    val thisWeekSeconds: Long = 0,
    val thisMonthSeconds: Long = 0,
    val allTimeSeconds: Long = 0,
    val totalSessions: Int = 0,
    val averageSessionSeconds: Long = 0
)