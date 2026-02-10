package com.practice.todo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.todo.model.TimerMode
import com.practice.todo.model.TimerState
import com.practice.todo.data.dao.FocusSessionDao
import com.practice.todo.data.entity.FocusSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class FocusTimerViewModel(
    private val focusSessionDao: FocusSessionDao
) : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private val pomodoroSeconds = 25 * 60L
    private var sessionStartTime: LocalDateTime? = null
    private var sessionStartSeconds: Long = 0

    fun setMode(mode: TimerMode) {
        pauseTimer()
        _timerState.value = TimerState(
            elapsedSeconds = if (mode == TimerMode.POMODORO) pomodoroSeconds else 0,
            isRunning = false,
            mode = mode
        )
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return

        if (sessionStartTime == null) {
            sessionStartTime = LocalDateTime.now()
            sessionStartSeconds = _timerState.value.elapsedSeconds
        }

        _timerState.value = _timerState.value.copy(isRunning = true)

        timerJob = viewModelScope.launch {
            while (_timerState.value.isRunning) {
                delay(1000L)

                when (_timerState.value.mode) {
                    TimerMode.FOCUS -> {
                        _timerState.value = _timerState.value.copy(
                            elapsedSeconds = _timerState.value.elapsedSeconds + 1
                        )
                    }
                    TimerMode.POMODORO -> {
                        val newSeconds = _timerState.value.elapsedSeconds - 1
                        if (newSeconds >= 0) {
                            _timerState.value = _timerState.value.copy(
                                elapsedSeconds = newSeconds
                            )
                        } else {
                            saveSession()
                            pauseTimer()
                        }
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isRunning = false)
        timerJob?.cancel()
    }

    fun resetTimer() {
        if (_timerState.value.mode == TimerMode.FOCUS && sessionStartTime != null) {
            saveSession()
        }

        pauseTimer()
        val resetSeconds = when (_timerState.value.mode) {
            TimerMode.FOCUS -> 0L
            TimerMode.POMODORO -> pomodoroSeconds
        }
        _timerState.value = _timerState.value.copy(
            elapsedSeconds = resetSeconds,
            isRunning = false
        )
        sessionStartTime = null
        sessionStartSeconds = 0
    }

    private fun saveSession() {
        sessionStartTime?.let { startTime ->
            val duration = when (_timerState.value.mode) {
                TimerMode.FOCUS -> _timerState.value.elapsedSeconds
                TimerMode.POMODORO -> pomodoroSeconds
            }

            if (duration >= 60) {
                viewModelScope.launch {
                    val session = FocusSession(
                        startTime = startTime,
                        endTime = LocalDateTime.now(),
                        durationSeconds = duration,
                        mode = _timerState.value.mode
                    )
                    focusSessionDao.insertSession(session)
                }
            }

            sessionStartTime = null
            sessionStartSeconds = 0
        }
    }

    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}