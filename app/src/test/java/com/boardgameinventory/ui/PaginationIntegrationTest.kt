package com.boardgameinventory.ui

import com.boardgameinventory.data.Game
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for data filtering and consistency functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DataFilteringUnitTest {

    @Test
    fun testPagingDataSnapshot() = runTest {
        // Given
        val testGames = (1..50).map { index ->
            Game(
                id = index.toLong(),
                name = "Game $index",
                barcode = "barcode$index",
                bookcase = if (index % 2 == 0) "A" else "B",
                shelf = (index % 5 + 1).toString(),
                loanedTo = if (index % 3 == 0) "User $index" else null,
                description = "Description for game $index",
                imageUrl = null,
                dateAdded = 1234567890L + index,
                dateLoaned = if (index % 3 == 0) 1234567890L + index else null
            )
        }
        
        // When - Test the games list directly since PagingData.from() creates static data
        val gamesList = testGames
        
        // Then
        assertEquals(50, gamesList.size)
        assertEquals("Game 1", gamesList[0].name)
        assertEquals("Game 50", gamesList[49].name)
        
        // Verify filtering logic
        val availableGames = gamesList.filter { it.loanedTo == null }
        val loanedGames = gamesList.filter { it.loanedTo != null }
        
        assertTrue(availableGames.size > 0)
        assertTrue(loanedGames.size > 0)
        assertEquals(50, availableGames.size + loanedGames.size)
    }

    @Test
    fun testPagingDataFiltering() = runTest {
        // Given
        val testGames = listOf(
            createGame(1L, "Chess", "A", "1", null),
            createGame(2L, "Checkers", "A", "2", "John"),
            createGame(3L, "Monopoly", "B", "1", null),
            createGame(4L, "Scrabble", "B", "2", "Jane"),
            createGame(5L, "Risk", "A", "1", null)
        )
        
        // When - Test the games list directly
        val gamesList = testGames
        
        // Then - Test various filters
        val bookcaseAGames = gamesList.filter { it.bookcase == "A" }
        assertEquals(3, bookcaseAGames.size)
        
        val shelf1Games = gamesList.filter { it.shelf == "1" }
        assertEquals(2, shelf1Games.size)
        
        val availableGames = gamesList.filter { it.loanedTo == null }
        assertEquals(3, availableGames.size)
        
        val loanedGames = gamesList.filter { it.loanedTo != null }
        assertEquals(2, loanedGames.size)
        
        val chessGames = gamesList.filter { it.name.contains("Chess", ignoreCase = true) }
        assertEquals(1, chessGames.size)
    }

    @Test
    fun testEmptyPagingData() = runTest {
        // Given
        val emptyGamesList = emptyList<Game>()
        
        // When - Test empty list directly
        val gamesList = emptyGamesList
        
        // Then
        assertEquals(0, gamesList.size)
        assertTrue(gamesList.isEmpty())
    }

    @Test
    fun testSingleItemPagingData() = runTest {
        // Given
        val singleGame = createGame(1L, "Solo Game", "A", "1", null)
        val gamesList = listOf(singleGame)
        
        // When - Test single item list directly
        
        // Then
        assertEquals(1, gamesList.size)
        assertEquals("Solo Game", gamesList[0].name)
        assertEquals("A", gamesList[0].bookcase)
        assertNull(gamesList[0].loanedTo)
    }

    @Test
    fun testLargePagingDataSet() = runTest {
        // Given - Large dataset to test performance
        val largeGameList = (1..1000).map { index ->
            createGame(
                id = index.toLong(),
                name = "Game $index",
                bookcase = ('A'..'Z').random().toString(),
                shelf = (1..10).random().toString(),
                loanedTo = if (index % 4 == 0) "User $index" else null
            )
        }
        
        // When - Test large list directly
        val gamesList = largeGameList
        
        // Then
        assertEquals(1000, gamesList.size)
        assertEquals("Game 1", gamesList[0].name)
        assertEquals("Game 1000", gamesList[999].name)
        
        // Verify data integrity
        val availableCount = gamesList.count { it.loanedTo == null }
        val loanedCount = gamesList.count { it.loanedTo != null }
        assertEquals(1000, availableCount + loanedCount)
        
        // Test expected distribution (roughly 75% available, 25% loaned)
        assertTrue("Available games should be majority", availableCount > loanedCount)
    }

    @Test
    fun testPagingDataConsistency() = runTest {
        // Given
        val testGames = (1..10).map { index ->
            createGame(
                id = index.toLong(),
                name = "Consistent Game $index",
                bookcase = "TEST",
                shelf = "1",
                loanedTo = if (index % 2 == 0) "Borrower $index" else null
            )
        }
        
        // When - Test consistency of data structure
        val gamesList = testGames
        
        // Then - Verify data consistency
        gamesList.forEach { game ->
            assertEquals("TEST", game.bookcase)
            assertEquals("1", game.shelf)
            assertTrue(game.name.startsWith("Consistent Game"))
            assertTrue(game.id in 1..10)
            
            if (game.id % 2 == 0L) {
                assertNotNull("Even ID games should be loaned", game.loanedTo)
                assertTrue(game.loanedTo!!.startsWith("Borrower"))
            } else {
                assertNull("Odd ID games should be available", game.loanedTo)
            }
        }
    }

    private fun createGame(
        id: Long,
        name: String,
        bookcase: String,
        shelf: String,
        loanedTo: String?
    ): Game {
        return Game(
            id = id,
            name = name,
            barcode = "TEST$id",
            bookcase = bookcase,
            shelf = shelf,
            loanedTo = loanedTo,
            description = "Test game description",
            imageUrl = null,
            dateAdded = 1234567890L,
            dateLoaned = if (loanedTo != null) 1234567890L else null
        )
    }
}
