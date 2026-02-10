package com.practice.todo

data class TimerState(
    val elapsedSeconds: Long = 0,
    val isRunning: Boolean = false,
    val mode: TimerMode = TimerMode.FOCUS
)