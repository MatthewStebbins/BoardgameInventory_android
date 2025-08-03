package com.boardgameinventory.data

import com.boardgameinventory.validation.ValidationUtils

/**
 * Extension functions to add validation capabilities to the Game data class
 */

/**
 * Validates all fields of the game and returns validation results
 */
fun Game.validate(): List<ValidationUtils.ValidationResult> {
    return ValidationUtils.validateGame(
        name = name,
        barcode = barcode,
        bookcase = bookcase,
        shelf = shelf,
        description = description,
        loanedTo = loanedTo,
        imageUrl = imageUrl,
        dateAdded = dateAdded,
        dateLoaned = dateLoaned
    )
}

/**
 * Checks if all validation rules pass for this game
 */
fun Game.isValid(): Boolean {
    return ValidationUtils.isAllValid(validate())
}

/**
 * Gets the first validation error, if any
 */
fun Game.getFirstValidationError(): ValidationUtils.ValidationResult? {
    return ValidationUtils.getFirstError(validate())
}

/**
 * Returns a sanitized copy of this game with cleaned up string values
 */
fun Game.sanitized(): Game {
    return copy(
        name = name.trim(),
        barcode = barcode.trim().uppercase(),
        bookcase = bookcase.trim().uppercase(),
        shelf = shelf.trim(),
        loanedTo = loanedTo?.trim(),
        description = description?.trim(),
        imageUrl = imageUrl?.trim()
    )
}

/**
 * Checks if this game is currently loaned out
 */
fun Game.isLoaned(): Boolean {
    return !loanedTo.isNullOrBlank() && dateLoaned != null && dateLoaned!! > 0
}

/**
 * Gets the formatted location string for this game
 */
fun Game.getLocation(): String {
    return "$bookcase-$shelf"
}

/**
 * Companion object extensions for Game creation with validation
 */
object GameValidationHelper {
    /**
     * Creates a new Game with validation and sanitization
     */
    fun createValidated(
        name: String,
        barcode: String,
        bookcase: String,
        shelf: String,
        loanedTo: String? = null,
        description: String? = null,
        imageUrl: String? = null,
        dateAdded: Long = System.currentTimeMillis(),
        dateLoaned: Long? = null
    ): Pair<Game?, List<ValidationUtils.ValidationResult>> {
        
        val validationResults = ValidationUtils.validateGame(
            name, barcode, bookcase, shelf, description, 
            loanedTo, imageUrl, dateAdded, dateLoaned
        )
        
        return if (ValidationUtils.isAllValid(validationResults)) {
            val game = Game(
                name = name.trim(),
                barcode = barcode.trim().uppercase(),
                bookcase = bookcase.trim().uppercase(),
                shelf = shelf.trim(),
                loanedTo = loanedTo?.trim(),
                description = description?.trim(),
                imageUrl = imageUrl?.trim(),
                dateAdded = dateAdded,
                dateLoaned = dateLoaned
            )
            Pair(game, validationResults)
        } else {
            Pair(null, validationResults)
        }
    }
}
