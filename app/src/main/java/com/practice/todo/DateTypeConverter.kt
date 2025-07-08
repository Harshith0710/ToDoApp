package com.practice.todo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTypeConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun dateToTimeStamp(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }
    @TypeConverter
    @RequiresApi(Build.VERSION_CODES.O)
    fun timeStampToDate(timeStamp: String?): LocalDateTime? {
        return timeStamp?.let{
            LocalDateTime.parse(it, formatter)
        }
    }
}