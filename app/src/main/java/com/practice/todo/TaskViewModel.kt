package com.practice.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskDao: TaskDao
): ViewModel() {
    val tasks = taskDao.getAllTasks()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    fun upsertTask(task: Task){
        viewModelScope.launch{
            taskDao.upsertTask(task)
        }
    }
    fun deleteTask(task: Task){
        viewModelScope.launch{
            taskDao.deleteTask(task)
        }
    }
    fun isValidTaskText(
        newText: String,
        originalText: String? = null
    ): Boolean {
        return validateTaskText(newText, originalText).isValid
    }
    fun validateTaskText(
        newText: String,
        originalText: String? = null
    ): ValidationResult {
        if (newText.isBlank()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Task cannot be empty or contain only spaces"
            )
        }

        originalText?.let { original ->
            if (newText.trim() == original.trim()) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Task text is unchanged"
                )
            }
        }

        return ValidationResult(isValid = true)
    }
}