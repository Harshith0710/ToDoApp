package com.practice.todo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.practice.todo.model.TimerMode
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