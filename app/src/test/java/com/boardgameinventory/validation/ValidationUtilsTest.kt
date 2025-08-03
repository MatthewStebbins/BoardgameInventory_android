package com.boardgameinventory.validation

import org.junit.Test
import org.junit.Assert.*
import com.boardgameinventory.R

/**
 * Unit tests for ValidationUtils
 */
class ValidationUtilsTest {

    @Test
    fun `validateGameName should pass for valid names`() {
        val validNames = listOf(
            "Settlers of Catan",
            "Ticket to Ride",
            "7 Wonders",
            "Azul",
            "Wingspan",
            "Gloomhaven: Jaws of the Lion"
        )
        
        validNames.forEach { name ->
            val result = ValidationUtils.validateGameName(name)
            assertTrue("Name '$name' should be valid", result.isValid)
        }
    }

    @Test
    fun `validateGameName should fail for invalid names`() {
        val invalidNames = mapOf(
            null to R.string.error_name_required,
            "" to R.string.error_name_required,
            "   " to R.string.error_name_required,
            "A" to R.string.error_name_too_short,
            "A".repeat(101) to R.string.error_name_too_long,
            "Game<script>" to R.string.error_name_invalid_characters
        )
        
        invalidNames.forEach { (name, expectedError) ->
            val result = ValidationUtils.validateGameName(name)
            assertFalse("Name '$name' should be invalid", result.isValid)
            assertEquals("Wrong error for name '$name'", expectedError, result.errorMessageRes)
        }
    }

    @Test
    fun `validateBarcode should pass for valid barcodes`() {
        val validBarcodes = listOf(
            "1234567890",
            "ABC123",
            "123ABC456",
            "UPC123456789"
        )
        
        validBarcodes.forEach { barcode ->
            val result = ValidationUtils.validateBarcode(barcode)
            assertTrue("Barcode '$barcode' should be valid", result.isValid)
        }
    }

    @Test
    fun `validateBarcode should fail for invalid barcodes`() {
        val invalidBarcodes = mapOf(
            null to R.string.error_barcode_required,
            "" to R.string.error_barcode_required,
            "123" to R.string.error_barcode_too_short,
            "A".repeat(21) to R.string.error_barcode_too_long,
            "123-456" to R.string.error_barcode_invalid,
            "123 456" to R.string.error_barcode_invalid
        )
        
        invalidBarcodes.forEach { (barcode, expectedError) ->
            val result = ValidationUtils.validateBarcode(barcode)
            assertFalse("Barcode '$barcode' should be invalid", result.isValid)
            assertEquals("Wrong error for barcode '$barcode'", expectedError, result.errorMessageRes)
        }
    }

    @Test
    fun `validateBookcase should pass for valid bookcases`() {
        val validBookcases = listOf(
            "A",
            "B1",
            "SHELF-A",
            "123",
            "A-1"
        )
        
        validBookcases.forEach { bookcase ->
            val result = ValidationUtils.validateBookcase(bookcase)
            assertTrue("Bookcase '$bookcase' should be valid", result.isValid)
        }
    }

    @Test
    fun `validateBookcase should fail for invalid bookcases`() {
        val invalidBookcases = mapOf(
            null to R.string.error_bookcase_required,
            "" to R.string.error_bookcase_required,
            "A".repeat(11) to R.string.error_bookcase_too_long,
            "A B" to R.string.error_bookcase_invalid,
            "A.B" to R.string.error_bookcase_invalid
        )
        
        invalidBookcases.forEach { (bookcase, expectedError) ->
            val result = ValidationUtils.validateBookcase(bookcase)
            assertFalse("Bookcase '$bookcase' should be invalid", result.isValid)
            assertEquals("Wrong error for bookcase '$bookcase'", expectedError, result.errorMessageRes)
        }
    }

    @Test
    fun `validateShelf should pass for valid shelves`() {
        val validShelves = listOf(
            "1",
            "A",
            "TOP",
            "1-A",
            "BOTTOM-2"
        )
        
        validShelves.forEach { shelf ->
            val result = ValidationUtils.validateShelf(shelf)
            assertTrue("Shelf '$shelf' should be valid", result.isValid)
        }
    }

    @Test
    fun `validateDescription should pass for valid descriptions`() {
        val validDescriptions = listOf(
            null,
            "",
            "A great strategy game",
            "Fun for the whole family!",
            "A".repeat(500) // Max length
        )
        
        validDescriptions.forEach { description ->
            val result = ValidationUtils.validateDescription(description)
            assertTrue("Description '$description' should be valid", result.isValid)
        }
    }

    @Test
    fun `validateDescription should fail for too long descriptions`() {
        val tooLongDescription = "A".repeat(501)
        val result = ValidationUtils.validateDescription(tooLongDescription)
        assertFalse("Too long description should be invalid", result.isValid)
        assertEquals(R.string.error_description_too_long, result.errorMessageRes)
    }

    @Test
    fun `validateLoanedTo should pass for valid names`() {
        val validNames = listOf(
            null,
            "",
            "John Doe",
            "Mary Smith",
            "Jean-Claude Van Damme",
            "O'Connor"
        )
        
        validNames.forEach { name ->
            val result = ValidationUtils.validateLoanedTo(name)
            assertTrue("Name '$name' should be valid", result.isValid)
        }
    }

    @Test
    fun `validateLoanedTo should fail for invalid names`() {
        val invalidNames = mapOf(
            "A".repeat(51) to R.string.error_loaned_to_too_long,
            "John123" to R.string.error_loaned_to_invalid,
            "John@Smith" to R.string.error_loaned_to_invalid
        )
        
        invalidNames.forEach { (name, expectedError) ->
            val result = ValidationUtils.validateLoanedTo(name)
            assertFalse("Name '$name' should be invalid", result.isValid)
            assertEquals("Wrong error for name '$name'", expectedError, result.errorMessageRes)
        }
    }

    @Test
    fun `validateImageUrl should pass for valid URLs`() {
        val validUrls = listOf(
            null,
            "",
            "https://example.com/image.jpg",
            "http://example.com/photo.png",
            "https://cdn.example.com/images/game.jpeg",
            "https://example.com/api/image/123"
        )
        
        validUrls.forEach { url ->
            val result = ValidationUtils.validateImageUrl(url)
            assertTrue("URL '$url' should be valid", result.isValid)
        }
    }

    @Test
    fun `validateImageUrl should fail for invalid URLs`() {
        val invalidUrls = mapOf(
            "not-a-url" to R.string.error_invalid_url,
            "ftp://example.com/file.txt" to R.string.error_invalid_url,
            "https://example.com/document.pdf" to R.string.error_invalid_image_url
        )
        
        invalidUrls.forEach { (url, expectedError) ->
            val result = ValidationUtils.validateImageUrl(url)
            assertFalse("URL '$url' should be invalid", result.isValid)
            assertNotNull("Error message resource should not be null for '$url'", result.errorMessageRes)
            assertEquals("Wrong error for URL '$url'", expectedError, result.errorMessageRes)
        }
    }

    @Test
    fun `validateDate should pass for valid dates`() {
        val validDates = listOf(
            null,
            System.currentTimeMillis(),
            System.currentTimeMillis() - 86400000, // Yesterday
            0L
        )
        
        validDates.forEach { date ->
            val result = ValidationUtils.validateDate(date)
            assertTrue("Date '$date' should be valid", result.isValid)
        }
    }

    @Test
    fun `validateDate should fail for invalid dates`() {
        val invalidDates = mapOf(
            System.currentTimeMillis() + 86400000 to R.string.error_date_future, // Tomorrow
            -1L to R.string.error_date_invalid
        )
        
        invalidDates.forEach { (date, expectedError) ->
            val result = ValidationUtils.validateDate(date)
            assertFalse("Date '$date' should be invalid", result.isValid)
            assertEquals("Wrong error for date '$date'", expectedError, result.errorMessageRes)
        }
    }

    @Test
    fun `validateGame should validate all fields`() {
        val results = ValidationUtils.validateGame(
            name = "Valid Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1",
            description = "A great game",
            loanedTo = "John Doe",
            imageUrl = "https://example.com/image.jpg",
            dateAdded = System.currentTimeMillis(),
            dateLoaned = System.currentTimeMillis()
        )
        
        assertEquals("Should have 9 validation results", 9, results.size)
        assertTrue("All validations should pass", ValidationUtils.isAllValid(results))
    }

    @Test
    fun `validateGame should fail with invalid fields`() {
        val results = ValidationUtils.validateGame(
            name = "", // Invalid
            barcode = "123", // Too short
            bookcase = "A B", // Invalid characters
            shelf = "", // Required
            description = "A".repeat(501), // Too long
            loanedTo = "John123", // Invalid characters
            imageUrl = "not-a-url", // Invalid URL
            dateAdded = System.currentTimeMillis() + 86400000, // Future
            dateLoaned = -1L // Invalid
        )
        
        assertEquals("Should have 9 validation results", 9, results.size)
        assertFalse("Validations should fail", ValidationUtils.isAllValid(results))
        
        val firstError = ValidationUtils.getFirstError(results)
        assertNotNull("Should have errors", firstError)
        assertFalse("First error should be invalid", firstError!!.isValid)
    }

    @Test
    fun `isAllValid should return correct results`() {
        val allValidResults = listOf(
            ValidationUtils.ValidationResult.success(),
            ValidationUtils.ValidationResult.success(),
            ValidationUtils.ValidationResult.success()
        )
        assertTrue(ValidationUtils.isAllValid(allValidResults))
        
        val someInvalidResults = listOf(
            ValidationUtils.ValidationResult.success(),
            ValidationUtils.ValidationResult.error(R.string.error_name_required),
            ValidationUtils.ValidationResult.success()
        )
        assertFalse(ValidationUtils.isAllValid(someInvalidResults))
    }

    @Test
    fun `getFirstError should return first error or null`() {
        val allValidResults = listOf(
            ValidationUtils.ValidationResult.success(),
            ValidationUtils.ValidationResult.success()
        )
        assertNull(ValidationUtils.getFirstError(allValidResults))
        
        val resultsWithError = listOf(
            ValidationUtils.ValidationResult.success(),
            ValidationUtils.ValidationResult.error(R.string.error_name_required),
            ValidationUtils.ValidationResult.error(R.string.error_barcode_required)
        )
        val firstError = ValidationUtils.getFirstError(resultsWithError)
        assertNotNull(firstError)
        assertEquals(R.string.error_name_required, firstError!!.errorMessageRes)
    }
}
