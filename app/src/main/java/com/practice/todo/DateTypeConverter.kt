package com.practice.todo

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTypeConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    @TypeConverter
    fun dateToTimeStamp(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }
    @TypeConverter
    fun timeStampToDate(timeStamp: String?): LocalDateTime? {
        return timeStamp?.let{
            LocalDateTime.parse(it, formatter)
        }
    }
}