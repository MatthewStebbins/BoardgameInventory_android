package com.boardgameinventory.validation

object GameInputValidation {
    fun validateGameName(name: String): Boolean {
        // Basic validation: not blank, length between 2 and 100, no invalid characters
        if (name.isBlank()) return false
        if (name.length < 2 || name.length > 100) return false
        val regex = "^[a-zA-Z0-9 .,'\\-()!&]+$".toRegex() // Use double backslash for escaping
        return regex.matches(name)
    }

    // Add stubs for other validation functions if needed
}
