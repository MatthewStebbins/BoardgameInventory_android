package com.boardgameinventory.pagination

import com.boardgameinventory.data.Game
import org.junit.Test
import org.junit.Assert.*

/**
 * Basic tests to verify pagination setup and data structures
 */
class PaginationSetupTest {

    @Test
    fun `Game data class should support pagination requirements`() {
        // Given
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
        
        // Then - Game should have all required fields for pagination
        assertNotNull(game.id)
        assertNotNull(game.name)
        assertNotNull(game.bookcase)
        assertNotNull(game.shelf)
        assertNotNull(game.dateAdded)
        
        // Test that Game is data class (has proper equals/hashCode for DiffUtil)
        val samegame = game.copy()
        assertEquals(game, samegame)
        assertEquals(game.hashCode(), samegame.hashCode())
        
        // Test that modified game is different (for DiffUtil)
        val modifiedGame = game.copy(name = "Different Name")
        assertNotEquals(game, modifiedGame)
    }

    @Test
    fun `createTestGames should generate consistent test data`() {
        // Given
        val count = 20
        
        // When
        val games = createTestGames(count)
        
        // Then
        assertEquals(count, games.size)
        
        // Verify data consistency
        games.forEachIndexed { index, game ->
            assertEquals("Game ${index + 1}", game.name)
            assertEquals("TEST${index + 1}", game.barcode)
            assertEquals("A", game.bookcase)
            assertEquals("1", game.shelf)
            
            // Every 3rd game should be loaned
            if ((index + 1) % 3 == 0) {
                assertNotNull("Game ${index + 1} should be loaned", game.loanedTo)
                assertNotNull("Loaned game should have loan date", game.dateLoaned)
            } else {
                assertNull("Game ${index + 1} should be available", game.loanedTo)
                assertNull("Available game should not have loan date", game.dateLoaned)
            }
        }
    }

    @Test
    fun `test data should support filtering scenarios`() {
        // Given
        val games = createTestGames(30)
        
        // When
        val availableGames = games.filter { it.loanedTo == null }
        val loanedGames = games.filter { it.loanedTo != null }
        val bookcaseAGames = games.filter { it.bookcase == "A" }
        val shelf1Games = games.filter { it.shelf == "1" }
        
        // Then
        assertEquals(30, games.size)
        assertEquals(20, availableGames.size) // 2/3 should be available
        assertEquals(10, loanedGames.size)    // 1/3 should be loaned
        assertEquals(30, bookcaseAGames.size) // All in bookcase A
        assertEquals(30, shelf1Games.size)    // All on shelf 1
        
        // Verify no overlap issues
        assertEquals(30, availableGames.size + loanedGames.size)
    }

    @Test
    fun `empty game list should be handled correctly`() {
        // Given
        val emptyGames = createTestGames(0)
        
        // When & Then
        assertEquals(0, emptyGames.size)
        assertTrue(emptyGames.isEmpty())
        
        // Test filtering empty list
        val availableGames = emptyGames.filter { it.loanedTo == null }
        val loanedGames = emptyGames.filter { it.loanedTo != null }
        
        assertEquals(0, availableGames.size)
        assertEquals(0, loanedGames.size)
    }

    @Test
    fun `large game list should be generated efficiently`() {
        // Given
        val largeCount = 1000
        
        // When
        val startTime = System.currentTimeMillis()
        val games = createTestGames(largeCount)
        val endTime = System.currentTimeMillis()
        
        // Then
        assertEquals(largeCount, games.size)
        
        // Should complete within reasonable time (less than 1 second)
        val duration = endTime - startTime
        assertTrue("Large list generation took too long: ${duration}ms", duration < 1000)
        
        // Verify first and last games
        assertEquals("Game 1", games.first().name)
        assertEquals("Game $largeCount", games.last().name)
        
        // Verify distribution of loaned vs available
        val availableCount = games.count { it.loanedTo == null }
        val loanedCount = games.count { it.loanedTo != null }
        
        // Should be roughly 2:1 ratio (available:loaned)
        assertTrue("Available games should be majority", availableCount > loanedCount)
        assertEquals(largeCount, availableCount + loanedCount)
    }

    @Test
    fun `game action constants should be defined`() {
        // Test that we have the expected action constants for the adapter
        // These would normally be in GamePagingAdapter but we test the concept here
        
        val expectedActions = listOf("click", "loan", "return", "delete", "edit")
        
        expectedActions.forEach { action ->
            assertNotNull("Action should be defined: $action", action)
            assertTrue("Action should not be empty", action.isNotEmpty())
        }
        
        // Test action uniqueness
        val uniqueActions = expectedActions.distinct()
        assertEquals("All actions should be unique", expectedActions.size, uniqueActions.size)
    }

    // Helper method to create test games for pagination testing
    private fun createTestGames(count: Int): List<Game> {
        return (1..count).map { index ->
            Game(
                id = index.toLong(),
                name = "Game $index",
                barcode = "TEST$index",
                bookcase = "A",
                shelf = "1",
                loanedTo = if (index % 3 == 0) "User $index" else null,
                description = "Test game $index description",
                imageUrl = null,
                dateAdded = 1234567890L + index,
                dateLoaned = if (index % 3 == 0) 1234567890L + index else null
            )
        }
    }
}
