package com.practice.todo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.practice.todo.data.typeconverter.DateTypeConverter
import com.practice.todo.data.typeconverter.ImportanceTypeConverter
import com.practice.todo.data.typeconverter.TimerModeTypeConverter
import com.practice.todo.data.dao.FocusSessionDao
import com.practice.todo.data.dao.TaskDao
import com.practice.todo.data.entity.FocusSession
import com.practice.todo.data.entity.Task

@Database(
    entities = [Task::class, FocusSession::class],
    version = 3
)
@TypeConverters(DateTypeConverter::class, ImportanceTypeConverter::class, TimerModeTypeConverter::class)
abstract class TaskDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
}