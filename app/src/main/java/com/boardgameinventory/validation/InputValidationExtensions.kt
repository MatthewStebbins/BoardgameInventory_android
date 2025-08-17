package com.boardgameinventory.validation

import android.content.Context
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import com.boardgameinventory.validation.ValidationUtils.ValidationResult

/**
 * Extension functions for real-time input validation on Android UI components
 */

/**
 * Sets up real-time validation for a TextInputLayout
 */
fun TextInputLayout.setupValidation(
    context: Context,
    validationFunction: (String) -> ValidationResult,
    debounceDelayMs: Long = 500L
) {
    val editText = editText ?: return
    var validationRunnable: Runnable? = null
    val handler = android.os.Handler(android.os.Looper.getMainLooper()) // Initialize Handler

    editText.addTextChangedListener { editable ->
        // Clear previous validation
        validationRunnable?.let { handler.removeCallbacks(it) }
        
        // Schedule new validation
        validationRunnable = Runnable {
            val text = editable?.toString() ?: ""
            val result = validationFunction(text)
            
            if (result.isValid) {
                error = null
                isErrorEnabled = false
            } else {
                val errorMessage = when {
                    result.errorMessageRes != null -> context.getString(result.errorMessageRes)
                    result.errorMessage != null -> result.errorMessage
                    else -> "Invalid input"
                }
                error = errorMessage
                isErrorEnabled = true
            }
        }
        
        handler.postDelayed(validationRunnable, debounceDelayMs)
    }
}

/**
 * Validates the current value in the TextInputLayout
 */
fun TextInputLayout.validateInput(
    context: Context,
    validationFunction: (String) -> ValidationResult
): ValidationResult {
    val text = editText?.text?.toString() ?: ""
    val result = validationFunction(text)
    
    if (result.isValid) {
        error = null
        isErrorEnabled = false
    } else {
        val errorMessage = when {
            result.errorMessageRes != null -> context.getString(result.errorMessageRes)
            result.errorMessage != null -> result.errorMessage
            else -> "Invalid input"
        }
        error = errorMessage
        isErrorEnabled = true
    }
    
    return result
}

/**
 * Sets up character counter with validation
 */
fun TextInputLayout.setupCharacterCounterWithValidation(
    maxLength: Int,
    context: Context,
    validationFunction: (String) -> ValidationResult
) {
    isCounterEnabled = true
    counterMaxLength = maxLength
    
    setupValidation(context, validationFunction)
}

/**
 * Validation helper for common game input fields
 */
object GameInputValidation {
    
    fun setupGameNameValidation(textInputLayout: TextInputLayout, context: Context) {
        textInputLayout.setupCharacterCounterWithValidation(
            ValidationUtils.MAX_NAME_LENGTH,
            context,
            ValidationUtils::validateGameName
        )
    }
    
    fun setupBarcodeValidation(textInputLayout: TextInputLayout, context: Context) {
        textInputLayout.setupCharacterCounterWithValidation(
            ValidationUtils.MAX_BARCODE_LENGTH,
            context,
            ValidationUtils::validateBarcode
        )
    }
    
    fun setupBookcaseValidation(textInputLayout: TextInputLayout, context: Context) {
        textInputLayout.setupCharacterCounterWithValidation(
            ValidationUtils.MAX_BOOKCASE_LENGTH,
            context,
            ValidationUtils::validateBookcase
        )
    }
    
    fun setupShelfValidation(textInputLayout: TextInputLayout, context: Context) {
        textInputLayout.setupCharacterCounterWithValidation(
            ValidationUtils.MAX_SHELF_LENGTH,
            context,
            ValidationUtils::validateShelf
        )
    }
    
    fun setupDescriptionValidation(textInputLayout: TextInputLayout, context: Context) {
        textInputLayout.setupCharacterCounterWithValidation(
            ValidationUtils.MAX_DESCRIPTION_LENGTH,
            context,
            ValidationUtils::validateDescription
        )
    }

    fun validateGameName(name: String): Boolean {
        // Basic validation: not blank, length between 2 and 100, no invalid characters
        if (name.isBlank()) return false
        if (name.length < 2 || name.length > 100) return false
        val regex = "^[a-zA-Z0-9 .,'\\-()!&]+$".toRegex()
        return regex.matches(name)
    }
}

/**
 * Validates multiple TextInputLayouts and returns results
 */
fun validateMultipleInputs(
    context: Context,
    vararg inputs: Pair<TextInputLayout, (String) -> ValidationResult>
): List<ValidationResult> {
    return inputs.map { (layout, validator) ->
        layout.validateInput(context, validator)
    }
}

/**
 * Checks if all inputs in a collection are valid
 */
fun areAllInputsValid(validationResults: List<ValidationResult>): Boolean {
    return validationResults.all { it.isValid }
}
