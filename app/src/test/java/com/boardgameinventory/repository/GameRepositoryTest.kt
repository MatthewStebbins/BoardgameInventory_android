package com.boardgameinventory.repository

import com.boardgameinventory.data.Game
import com.boardgameinventory.data.GameDao
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*

/**
 * Unit tests for the GameRepository class
 * These tests focus on the repository's interface and basic functionality
 */
class GameRepositoryTest {

    @Test
    fun `GameRepository should be instantiable with GameDao`() {
        // Given
        val mockGameDao = mock(GameDao::class.java)
        
        // When
        val repository = GameRepository(mockGameDao)
        
        // Then
        assertNotNull(repository)
    }

    @Test
    fun `Sample Game object should have correct properties`() {
        // Test the Game data class structure
        val game = Game(
            id = 1L,
            name = "Test Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1",
            loanedTo = null,
            description = "Test description",
            imageUrl = null,
            dateAdded = 1234567890L,
            dateLoaned = null
        )
        
        assertEquals(1L, game.id)
        assertEquals("Test Game", game.name)
        assertEquals("123456789", game.barcode)
        assertEquals("A", game.bookcase)
        assertEquals("1", game.shelf)
        assertNull(game.loanedTo)
        assertEquals("Test description", game.description)
        assertNull(game.imageUrl)
        assertEquals(1234567890L, game.dateAdded)
        assertNull(game.dateLoaned)
    }

    @Test
    fun `Game copy function should work correctly`() {
        val originalGame = Game(
            id = 1L,
            name = "Original Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1",
            loanedTo = null,
            description = "Original description",
            imageUrl = null,
            dateAdded = 1234567890L,
            dateLoaned = null
        )
        
        val copiedGame = originalGame.copy(
            name = "Updated Game",
            loanedTo = "John Doe",
            dateLoaned = 1234567999L
        )
        
        assertEquals(1L, copiedGame.id) // ID should remain the same
        assertEquals("Updated Game", copiedGame.name) // Name should be updated
        assertEquals("John Doe", copiedGame.loanedTo) // loanedTo should be updated
        assertEquals(1234567999L, copiedGame.dateLoaned) // dateLoaned should be updated
        assertEquals("123456789", copiedGame.barcode) // Other fields should remain the same
        assertEquals("A", copiedGame.bookcase)
        assertEquals("Original description", copiedGame.description)
        
        // Original should remain unchanged
        assertEquals("Original Game", originalGame.name)
        assertNull(originalGame.loanedTo)
        assertNull(originalGame.dateLoaned)
    }

    @Test
    fun `Game toString should contain key information`() {
        val game = Game(
            id = 42L,
            name = "Awesome Game",
            barcode = "987654321",
            bookcase = "B",
            shelf = "3",
            loanedTo = "Jane Smith",
            description = "Great game!",
            imageUrl = "http://example.com/image.jpg",
            dateAdded = 1234567890L,
            dateLoaned = 1234567999L
        )
        
        val toString = game.toString()
        assertNotNull(toString)
        assertTrue(toString.contains("Awesome Game"))
        assertTrue(toString.contains("987654321"))
    }
}
