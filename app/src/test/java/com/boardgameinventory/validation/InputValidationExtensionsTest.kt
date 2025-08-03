package com.boardgameinventory.validation

import org.junit.Test
import org.junit.Assert.*
import com.boardgameinventory.R

/**
 * Unit tests for InputValidationExtensions
 * Tests the validation framework utility functions
 */
class InputValidationExtensionsTest {

    @Test
    fun `areAllInputsValid should return true when all inputs are valid`() {
        // Given
        val validResults = listOf(
            ValidationUtils.ValidationResult.success(),
            ValidationUtils.ValidationResult.success(),
            ValidationUtils.ValidationResult.success()
        )
        
        // When
        val result = areAllInputsValid(validResults)
        
        // Then
        assertTrue("All valid inputs should return true", result)
    }

    @Test
    fun `areAllInputsValid should return false when any input is invalid`() {
        // Given
        val mixedResults = listOf(
            ValidationUtils.ValidationResult.success(),
            ValidationUtils.ValidationResult.error(R.string.error_name_required),
            ValidationUtils.ValidationResult.success()
        )
        
        // When
        val result = areAllInputsValid(mixedResults)
        
        // Then
        assertFalse("Mixed results should return false", result)
    }

    @Test
    fun `areAllInputsValid should return true for empty list`() {
        // Given
        val emptyResults = emptyList<ValidationUtils.ValidationResult>()
        
        // When
        val result = areAllInputsValid(emptyResults)
        
        // Then
        assertTrue("Empty list should return true", result)
    }

    @Test
    fun `areAllInputsValid should return false when all inputs are invalid`() {
        // Given
        val invalidResults = listOf(
            ValidationUtils.ValidationResult.error(R.string.error_name_required),
            ValidationUtils.ValidationResult.error(R.string.error_barcode_required),
            ValidationUtils.ValidationResult.error(R.string.error_bookcase_required)
        )
        
        // When
        val result = areAllInputsValid(invalidResults)
        
        // Then
        assertFalse("All invalid inputs should return false", result)
    }

    @Test
    fun `ValidationResult success should create valid result`() {
        // When
        val result = ValidationUtils.ValidationResult.success()
        
        // Then
        assertTrue("Success should be valid", result.isValid)
        assertNull("Success should have no error message resource", result.errorMessageRes)
        assertNull("Success should have no error message", result.errorMessage)
    }

    @Test
    fun `ValidationResult error with resource should create invalid result`() {
        // When
        val result = ValidationUtils.ValidationResult.error(R.string.error_name_required)
        
        // Then
        assertFalse("Error should be invalid", result.isValid)
        assertEquals("Error should have correct resource", R.string.error_name_required, result.errorMessageRes)
        assertNull("Error should have no custom message", result.errorMessage)
    }

    @Test
    fun `ValidationResult error with message should create invalid result`() {
        // Given
        val errorMessage = "Custom error message"
        
        // When
        val result = ValidationUtils.ValidationResult.error(errorMessage)
        
        // Then
        assertFalse("Error should be invalid", result.isValid)
        assertNull("Error should have no resource", result.errorMessageRes)
        assertEquals("Error should have correct message", errorMessage, result.errorMessage)
    }

    @Test
    fun `validation constants should have reasonable values`() {
        // Then
        assertTrue("Min name length should be positive", ValidationUtils.MIN_NAME_LENGTH > 0)
        assertTrue("Max name length should be reasonable", ValidationUtils.MAX_NAME_LENGTH >= 10)
        assertTrue("Min barcode length should be positive", ValidationUtils.MIN_BARCODE_LENGTH > 0)
        assertTrue("Max barcode length should be reasonable", ValidationUtils.MAX_BARCODE_LENGTH >= 4)
        assertTrue("Max description length should be generous", ValidationUtils.MAX_DESCRIPTION_LENGTH >= 100)
        assertTrue("Max loaned to length should be reasonable", ValidationUtils.MAX_LOANED_TO_LENGTH >= 10)
        
        // Verify logical relationships
        assertTrue("Max name length should be greater than min", 
            ValidationUtils.MAX_NAME_LENGTH > ValidationUtils.MIN_NAME_LENGTH)
        assertTrue("Max barcode length should be greater than min", 
            ValidationUtils.MAX_BARCODE_LENGTH > ValidationUtils.MIN_BARCODE_LENGTH)
    }
}
