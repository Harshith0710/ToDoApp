package com.practice.todo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val isDone: Boolean = false,
    @ColumnInfo(defaultValue = "")
    val description: String = "",
    val endTime: LocalDateTime? = null,
    @ColumnInfo(defaultValue = "normal")
    val importance: TaskImportance = TaskImportance.NORMAL
)
