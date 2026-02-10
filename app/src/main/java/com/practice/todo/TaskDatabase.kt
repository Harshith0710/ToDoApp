package com.practice.todo

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Task::class, FocusSession::class],
    version = 3
)
@TypeConverters(DateTypeConverter::class, ImportanceTypeConverter::class, TimerModeTypeConverter::class)
abstract class TaskDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
}