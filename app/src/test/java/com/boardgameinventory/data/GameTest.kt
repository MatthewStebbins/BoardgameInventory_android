package com.boardgameinventory.data

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Game data class
 */
class GameTest {

    @Test
    fun `game creation with all fields should work correctly`() {
        // Given
        val id = 1L
        val name = "Catan"
        val barcode = "123456789012"
        val bookcase = "A"
        val shelf = "1"
        val loanedTo = "John Doe"
        val description = "A strategy game"
        val imageUrl = "https://example.com/image.jpg"
        val dateAdded = System.currentTimeMillis()
        val dateLoaned = System.currentTimeMillis()

        // When
        val game = Game(
            id = id,
            name = name,
            barcode = barcode,
            bookcase = bookcase,
            shelf = shelf,
            loanedTo = loanedTo,
            description = description,
            imageUrl = imageUrl,
            dateAdded = dateAdded,
            dateLoaned = dateLoaned
        )

        // Then
        assertEquals(id, game.id)
        assertEquals(name, game.name)
        assertEquals(barcode, game.barcode)
        assertEquals(bookcase, game.bookcase)
        assertEquals(shelf, game.shelf)
        assertEquals(loanedTo, game.loanedTo)
        assertEquals(description, game.description)
        assertEquals(imageUrl, game.imageUrl)
        assertEquals(dateAdded, game.dateAdded)
        assertEquals(dateLoaned, game.dateLoaned)
    }

    @Test
    fun `game creation with minimal fields should work correctly`() {
        // Given
        val name = "Monopoly"
        val barcode = "987654321098"
        val bookcase = "B"
        val shelf = "2"

        // When
        val game = Game(
            name = name,
            barcode = barcode,
            bookcase = bookcase,
            shelf = shelf
        )

        // Then
        assertEquals(0L, game.id) // Default auto-generated ID
        assertEquals(name, game.name)
        assertEquals(barcode, game.barcode)
        assertEquals(bookcase, game.bookcase)
        assertEquals(shelf, game.shelf)
        assertNull(game.loanedTo)
        assertNull(game.description)
        assertNull(game.imageUrl)
        assertTrue(game.dateAdded > 0) // Should have current timestamp
        assertNull(game.dateLoaned)
    }

    @Test
    fun `game with null optional fields should work correctly`() {
        // Given/When
        val game = Game(
            name = "Risk",
            barcode = "111222333444",
            bookcase = "C",
            shelf = "3",
            loanedTo = null,
            description = null,
            imageUrl = null,
            dateLoaned = null
        )

        // Then
        assertNotNull(game.name)
        assertNotNull(game.barcode)
        assertNotNull(game.bookcase)
        assertNotNull(game.shelf)
        assertNull(game.loanedTo)
        assertNull(game.description)
        assertNull(game.imageUrl)
        assertNull(game.dateLoaned)
    }

    @Test
    fun `game equality should work based on all fields`() {
        // Given
        val game1 = Game(
            id = 1L,
            name = "Scrabble",
            barcode = "555666777888",
            bookcase = "D",
            shelf = "4"
        )
        
        val game2 = Game(
            id = 1L,
            name = "Scrabble",
            barcode = "555666777888",
            bookcase = "D",
            shelf = "4",
            dateAdded = game1.dateAdded // Use same timestamp
        )

        // Then
        assertEquals(game1, game2)
        assertEquals(game1.hashCode(), game2.hashCode())
    }

    @Test
    fun `games with different IDs should not be equal`() {
        // Given
        val game1 = Game(
            id = 1L,
            name = "Chess",
            barcode = "999888777666",
            bookcase = "E",
            shelf = "5"
        )
        
        val game2 = game1.copy(id = 2L)

        // Then
        assertNotEquals(game1, game2)
    }

    @Test
    fun `game toString should contain key information`() {
        // Given
        val game = Game(
            name = "Checkers",
            barcode = "123123123123",
            bookcase = "F",
            shelf = "6"
        )

        // When
        val stringRepresentation = game.toString()

        // Then
        assertTrue(stringRepresentation.contains("Checkers"))
        assertTrue(stringRepresentation.contains("123123123123"))
        assertTrue(stringRepresentation.contains("F"))
        assertTrue(stringRepresentation.contains("6"))
    }
}
