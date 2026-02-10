package com.practice.todo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface FocusSessionDao {
    @Insert
    suspend fun insertSession(session: FocusSession)

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startDate ORDER BY startTime DESC")
    fun getSessionsSince(startDate: LocalDateTime): Flow<List<FocusSession>>

    @Query("SELECT SUM(durationSeconds) FROM focus_sessions WHERE startTime >= :startDate")
    suspend fun getTotalDurationSince(startDate: LocalDateTime): Long?

    @Query("SELECT SUM(durationSeconds) FROM focus_sessions")
    suspend fun getTotalDurationAllTime(): Long?

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAllSessions()
}