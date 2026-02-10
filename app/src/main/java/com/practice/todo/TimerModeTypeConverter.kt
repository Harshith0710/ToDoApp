package com.practice.todo

import androidx.room.TypeConverter

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