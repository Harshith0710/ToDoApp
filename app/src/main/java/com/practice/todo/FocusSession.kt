package com.practice.todo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val durationSeconds: Long,
    val mode: TimerMode
)