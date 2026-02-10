package com.practice.todo.model

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
