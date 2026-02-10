package com.practice.todo

enum class ChartPeriod {
    LAST_24_HOURS,
    LAST_7_DAYS,
    LAST_12_MONTHS
}

data class ChartData(
    val label: String,
    val value: Long,
    val displayValue: String
)