package com.practice.todo

import androidx.room.TypeConverter

class ImportanceTypeConverter {
    @TypeConverter
    fun importanceToString(importance: TaskImportance): String {
        return importance.value
    }
    @TypeConverter
    fun stringToImportance(value: String): TaskImportance {
        return TaskImportance.entries.first {
            it.value == value
        }
    }
}