package com.practice.todo.data.typeconverter

import androidx.room.TypeConverter
import com.practice.todo.model.TaskImportance

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