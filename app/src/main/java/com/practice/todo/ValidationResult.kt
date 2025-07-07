package com.practice.todo

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
