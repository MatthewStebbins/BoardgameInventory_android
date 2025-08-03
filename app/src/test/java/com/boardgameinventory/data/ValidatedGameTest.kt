package com.boardgameinventory.data

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ValidatedGame functionality
 */
class ValidatedGameTest {

    @Test
    fun `Game validate should return validation results`() {
        val validGame = Game(
            id = 1L,
            name = "Valid Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1",
            loanedTo = null,
            description = "A great game",
            imageUrl = null,
            dateAdded = System.currentTimeMillis(),
            dateLoaned = null
        )
        
        val results = validGame.validate()
        assertEquals("Should have 9 validation results", 9, results.size)
        assertTrue("Valid game should pass validation", validGame.isValid())
        assertNull("Valid game should have no errors", validGame.getFirstValidationError())
    }

    @Test
    fun `Game validate should fail for invalid data`() {
        val invalidGame = Game(
            id = 1L,
            name = "", // Invalid - empty
            barcode = "123", // Invalid - too short
            bookcase = "A B", // Invalid - contains space
            shelf = "", // Invalid - empty
            loanedTo = "John123", // Invalid - contains numbers
            description = "A".repeat(501), // Invalid - too long
            imageUrl = "not-a-url", // Invalid - not a URL
            dateAdded = System.currentTimeMillis() + 86400000, // Invalid - future date
            dateLoaned = -1L // Invalid - negative
        )
        
        val results = invalidGame.validate()
        assertEquals("Should have 9 validation results", 9, results.size)
        assertFalse("Invalid game should fail validation", invalidGame.isValid())
        
        val firstError = invalidGame.getFirstValidationError()
        assertNotNull("Invalid game should have errors", firstError)
        assertFalse("First error should be invalid", firstError!!.isValid)
    }

    @Test
    fun `Game sanitized should clean up string values`() {
        val game = Game(
            id = 1L,
            name = "  Test Game  ",
            barcode = "  abc123  ",
            bookcase = "  section-a  ",
            shelf = "  1  ",
            loanedTo = "  John Doe  ",
            description = "  Great game  ",
            imageUrl = "  https://example.com/image.jpg  ",
            dateAdded = System.currentTimeMillis(),
            dateLoaned = null
        )
        
        val sanitized = game.sanitized()
        
        assertEquals("Test Game", sanitized.name)
        assertEquals("ABC123", sanitized.barcode) // Should be uppercase
        assertEquals("SECTION-A", sanitized.bookcase) // Should be uppercase
        assertEquals("1", sanitized.shelf)
        assertEquals("John Doe", sanitized.loanedTo)
        assertEquals("Great game", sanitized.description)
        assertEquals("https://example.com/image.jpg", sanitized.imageUrl)
    }

    @Test
    fun `Game isLoaned should return correct status`() {
        val notLoanedGame = Game(
            id = 1L,
            name = "Test Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1"
        )
        assertFalse("Game without loan info should not be loaned", notLoanedGame.isLoaned())
        
        val loanedGame = Game(
            id = 1L,
            name = "Test Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1",
            loanedTo = "John Doe",
            dateLoaned = System.currentTimeMillis()
        )
        assertTrue("Game with loan info should be loaned", loanedGame.isLoaned())
        
        val incompleteLoanGame = Game(
            id = 1L,
            name = "Test Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1",
            loanedTo = "John Doe"
            // Missing dateLoaned
        )
        assertFalse("Game with only loanedTo should not be loaned", incompleteLoanGame.isLoaned())
    }

    @Test
    fun `Game getLocation should format correctly`() {
        val game = Game(
            id = 1L,
            name = "Test Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1"
        )
        
        assertEquals("A-1", game.getLocation())
    }

    @Test
    fun `GameValidationHelper createValidated should create valid games`() {
        val (game, results) = GameValidationHelper.createValidated(
            name = "Valid Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1",
            description = "A great game"
        )
        
        assertNotNull("Valid input should create game", game)
        assertTrue("Validation should pass", results.all { result -> result.isValid })
        
        // Check sanitization
        assertEquals("Valid Game", game!!.name)
        assertEquals("123456789", game.barcode)
        assertEquals("A", game.bookcase)
        assertEquals("1", game.shelf)
        assertEquals("A great game", game.description)
    }

    @Test
    fun `GameValidationHelper createValidated should fail for invalid input`() {
        val (game, results) = GameValidationHelper.createValidated(
            name = "", // Invalid
            barcode = "123", // Too short
            bookcase = "", // Invalid
            shelf = "", // Invalid
            description = "A".repeat(501) // Too long
        )
        
        assertNull("Invalid input should not create game", game)
        assertFalse("Validation should fail", results.all { result -> result.isValid })
        
        val failedResults = results.filter { !it.isValid }
        assertTrue("Should have multiple failed validations", failedResults.size > 1)
    }

    @Test
    fun `GameValidationHelper createValidated should sanitize input values`() {
        val (game, results) = GameValidationHelper.createValidated(
            name = "  Test Game  ",
            barcode = "  abc123def  ",
            bookcase = "  section-a  ",
            shelf = "  top  ",
            loanedTo = "  John Doe  ",
            description = "  Great strategy game  "
        )
        
        assertNotNull("Valid input should create game", game)
        assertTrue("Validation should pass", results.all { result -> result.isValid })
        
        assertEquals("Test Game", game!!.name)
        assertEquals("ABC123DEF", game.barcode)
        assertEquals("SECTION-A", game.bookcase)
        assertEquals("top", game.shelf)
        assertEquals("John Doe", game.loanedTo)
        assertEquals("Great strategy game", game.description)
    }
}
