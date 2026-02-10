package com.practice.todo.data.typeconverter

import androidx.room.TypeConverter
import com.practice.todo.model.TimerMode

class TimerModeTypeConverter {
    @TypeConverter
    fun timerModeToString(mode: TimerMode): String {
        return mode.name
    }

    @TypeConverter
    fun stringToTimerMode(value: String): TimerMode {
        return TimerMode.valueOf(value)
    }
}