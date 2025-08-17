package com.boardgameinventory.validation

import android.util.Patterns
import androidx.annotation.StringRes
import com.boardgameinventory.R

/**
 * Data validation utilities for the BoardGame Inventory app
 * Provides comprehensive input validation with localized error messages
 */
object ValidationUtils {

    // Constants for validation rules
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 100
    const val MIN_BARCODE_LENGTH = 4
    const val MAX_BARCODE_LENGTH = 20
    const val MIN_BOOKCASE_LENGTH = 1
    const val MAX_BOOKCASE_LENGTH = 10
    const val MIN_SHELF_LENGTH = 1
    const val MAX_SHELF_LENGTH = 10
    const val MAX_DESCRIPTION_LENGTH = 500
    const val MAX_LOANED_TO_LENGTH = 50

    /**
     * Validation result containing success status and error message resource
     */
    data class ValidationResult(
        val isValid: Boolean,
        @StringRes val errorMessageRes: Int? = null,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success() = ValidationResult(true)
            fun error(@StringRes messageRes: Int) = ValidationResult(false, errorMessageRes = messageRes)
            fun error(message: String) = ValidationResult(false, errorMessage = message)
        }
    }

    /**
     * Validates game name
     */
    fun validateGameName(name: String?): ValidationResult {
        return when {
            name.isNullOrBlank() -> ValidationResult.error(R.string.error_name_required)
            name.trim().length < MIN_NAME_LENGTH -> ValidationResult.error(R.string.error_name_too_short)
            name.trim().length > MAX_NAME_LENGTH -> ValidationResult.error(R.string.error_name_too_long)
            !isValidTextInput(name) -> ValidationResult.error(R.string.error_name_invalid_characters)
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates barcode
     */
    fun validateBarcode(barcode: String?): ValidationResult {
        return when {
            barcode.isNullOrBlank() -> ValidationResult.error(R.string.error_barcode_required)
            barcode.trim().length < MIN_BARCODE_LENGTH -> ValidationResult.error(R.string.error_barcode_too_short)
            barcode.trim().length > MAX_BARCODE_LENGTH -> ValidationResult.error(R.string.error_barcode_too_long)
            !isValidBarcode(barcode) -> ValidationResult.error(R.string.error_barcode_invalid)
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates bookcase identifier
     */
    fun validateBookcase(bookcase: String?): ValidationResult {
        return when {
            bookcase.isNullOrBlank() -> ValidationResult.error(R.string.error_bookcase_required)
            bookcase.trim().length < MIN_BOOKCASE_LENGTH -> ValidationResult.error(R.string.error_bookcase_too_short)
            bookcase.trim().length > MAX_BOOKCASE_LENGTH -> ValidationResult.error(R.string.error_bookcase_too_long)
            !isValidLocationInput(bookcase) -> ValidationResult.error(R.string.error_bookcase_invalid)
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates shelf identifier
     */
    fun validateShelf(shelf: String?): ValidationResult {
        return when {
            shelf.isNullOrBlank() -> ValidationResult.error(R.string.error_shelf_required)
            shelf.trim().length < MIN_SHELF_LENGTH -> ValidationResult.error(R.string.error_shelf_too_short)
            shelf.trim().length > MAX_SHELF_LENGTH -> ValidationResult.error(R.string.error_shelf_too_long)
            !isValidLocationInput(shelf) -> ValidationResult.error(R.string.error_shelf_invalid)
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates description (optional field)
     */
    fun validateDescription(description: String?): ValidationResult {
        return when {
            description == null -> ValidationResult.success() // Optional field
            description.length > MAX_DESCRIPTION_LENGTH -> ValidationResult.error(R.string.error_description_too_long)
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates loaned to field (optional)
     */
    fun validateLoanedTo(loanedTo: String?): ValidationResult {
        return when {
            loanedTo == null -> ValidationResult.success() // Optional field
            loanedTo.isBlank() -> ValidationResult.success() // Empty is valid for optional field
            loanedTo.trim().length > MAX_LOANED_TO_LENGTH -> ValidationResult.error(R.string.error_loaned_to_too_long)
            !isValidPersonName(loanedTo) -> ValidationResult.error(R.string.error_loaned_to_invalid)
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates URL format (optional)
     */
    fun validateImageUrl(url: String?): ValidationResult {
        return when {
            url.isNullOrBlank() -> ValidationResult.success() // Optional field
            !isValidUrl(url) -> ValidationResult.error(R.string.error_invalid_url)
            !isValidImageUrl(url) -> ValidationResult.error(R.string.error_invalid_image_url)
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates date (must be in the past or present)
     */
    fun validateDate(timestamp: Long?): ValidationResult {
        return when {
            timestamp == null -> ValidationResult.success()
            timestamp > System.currentTimeMillis() -> ValidationResult.error(R.string.error_date_future)
            timestamp < 0 -> ValidationResult.error(R.string.error_date_invalid)
            else -> ValidationResult.success()
        }
    }

    /**
     * Comprehensive game validation
     */
    fun validateGame(
        name: String?,
        barcode: String?,
        bookcase: String?,
        shelf: String?,
        description: String? = null,
        loanedTo: String? = null,
        imageUrl: String? = null,
        dateAdded: Long? = null,
        dateLoaned: Long? = null
    ): List<ValidationResult> {
        return listOf(
            validateGameName(name),
            validateBarcode(barcode),
            validateBookcase(bookcase),
            validateShelf(shelf),
            validateDescription(description),
            validateLoanedTo(loanedTo),
            validateImageUrl(imageUrl),
            validateDate(dateAdded),
            validateDate(dateLoaned)
        )
    }

    /**
     * Checks if all validation results are valid
     */
    fun isAllValid(results: List<ValidationResult>): Boolean {
        return results.all { it.isValid }
    }

    /**
     * Gets first error from validation results
     */
    fun getFirstError(results: List<ValidationResult>): ValidationResult? {
        return results.firstOrNull { !it.isValid }
    }

    // Private helper methods

    /**
     * Validates text input for names and descriptions
     */
    private fun isValidTextInput(text: String): Boolean {
        // Allow letters, numbers, spaces, and common punctuation including colons
        val pattern = "^[a-zA-Z0-9\\s\\-_.,!?()&'\":]+$".toRegex()
        return pattern.matches(text.trim())
    }

    /**
     * Validates barcode format
     */
    private fun isValidBarcode(barcode: String): Boolean {
        // Allow only alphanumeric characters for barcodes
        val pattern = "^[a-zA-Z0-9]+$".toRegex()
        return pattern.matches(barcode.trim())
    }

    /**
     * Validates location input (bookcase/shelf)
     */
    private fun isValidLocationInput(location: String): Boolean {
        // Allow letters, numbers, and hyphens for location identifiers
        val pattern = "^[a-zA-Z0-9\\-]+$".toRegex()
        return pattern.matches(location.trim())
    }

    /**
     * Validates person name
     */
    private fun isValidPersonName(name: String): Boolean {
        // Allow letters, spaces, apostrophes, and hyphens for names
        val pattern = "^[a-zA-Z\\s'\\-]+$".toRegex()
        return pattern.matches(name.trim())
    }

    /**
     * Validates URL format - safe for unit tests
     */
    private fun isValidUrl(url: String): Boolean {
        return try {
            // Try to use Android Patterns if available (runtime)
            Patterns.WEB_URL?.matcher(url)?.matches() ?: run {
                // Fallback for unit tests where Android classes aren't available
                val trimmedUrl = url.trim()
                (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) && 
                trimmedUrl.length > 7 && trimmedUrl.contains(".")
            }
        } catch (_: Exception) {
            // Fallback for unit tests where Android classes aren't available
            val trimmedUrl = url.trim()
            (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) && 
            trimmedUrl.length > 7 && trimmedUrl.contains(".")
        }
    }

    /**
     * Validates image URL format
     */
    private fun isValidImageUrl(url: String): Boolean {
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")
        val lowerUrl = url.lowercase()
        return imageExtensions.any { lowerUrl.contains(it) } || 
               lowerUrl.contains("image") || 
               lowerUrl.contains("img")
    }
}
