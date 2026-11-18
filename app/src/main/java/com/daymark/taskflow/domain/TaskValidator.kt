package com.daymark.taskflow.domain

data class ValidationResult(
    val isValid: Boolean,
    val normalizedValue: String,
    val message: String? = null,
)

object TaskValidator {
    const val MAX_TITLE_LENGTH = 80

    fun validateTitle(value: String): ValidationResult {
        val normalized = value.trim().replace(Regex("\\s+"), " ")
        return when {
            normalized.isEmpty() -> ValidationResult(false, normalized, "Enter a task name")
            normalized.length > MAX_TITLE_LENGTH -> ValidationResult(false, normalized, "Use 80 characters or fewer")
            else -> ValidationResult(true, normalized)
        }
    }

    fun normalizeNote(value: String): String = value.trim().take(500)
}
