package com.boardgameinventory.ui

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boardgameinventory.data.Game
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for pagination functionality using Android Test framework
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PaginationIntegrationTest {

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
        
        // When
        val pagingData = PagingData.from(testGames)
        val snapshot = pagingData.asSnapshot()
        
        // Then
        assertEquals(50, snapshot.size)
        assertEquals("Game 1", snapshot[0].name)
        assertEquals("Game 50", snapshot[49].name)
        
        // Verify filtering logic
        val availableGames = snapshot.filter { it.loanedTo == null }
        val loanedGames = snapshot.filter { it.loanedTo != null }
        
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
        
        // When
        val pagingData = PagingData.from(testGames)
        val snapshot = pagingData.asSnapshot()
        
        // Then - Test various filters
        val bookcaseAGames = snapshot.filter { it.bookcase == "A" }
        assertEquals(3, bookcaseAGames.size)
        
        val shelf1Games = snapshot.filter { it.shelf == "1" }
        assertEquals(2, shelf1Games.size)
        
        val availableGames = snapshot.filter { it.loanedTo == null }
        assertEquals(3, availableGames.size)
        
        val loanedGames = snapshot.filter { it.loanedTo != null }
        assertEquals(2, loanedGames.size)
        
        val chessGames = snapshot.filter { it.name.contains("Chess", ignoreCase = true) }
        assertEquals(1, chessGames.size)
    }

    @Test
    fun testEmptyPagingData() = runTest {
        // Given
        val emptyPagingData = PagingData.from(emptyList<Game>())
        
        // When
        val snapshot = emptyPagingData.asSnapshot()
        
        // Then
        assertEquals(0, snapshot.size)
        assertTrue(snapshot.isEmpty())
    }

    @Test
    fun testSingleItemPagingData() = runTest {
        // Given
        val singleGame = createGame(1L, "Solo Game", "A", "1", null)
        val pagingData = PagingData.from(listOf(singleGame))
        
        // When
        val snapshot = pagingData.asSnapshot()
        
        // Then
        assertEquals(1, snapshot.size)
        assertEquals("Solo Game", snapshot[0].name)
        assertEquals("A", snapshot[0].bookcase)
        assertNull(snapshot[0].loanedTo)
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
        
        // When
        val pagingData = PagingData.from(largeGameList)
        val snapshot = pagingData.asSnapshot()
        
        // Then
        assertEquals(1000, snapshot.size)
        assertEquals("Game 1", snapshot[0].name)
        assertEquals("Game 1000", snapshot[999].name)
        
        // Verify data integrity
        val availableCount = snapshot.count { it.loanedTo == null }
        val loanedCount = snapshot.count { it.loanedTo != null }
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
        
        // When
        val pagingData = PagingData.from(testGames)
        val snapshot = pagingData.asSnapshot()
        
        // Then - Verify data consistency
        snapshot.forEach { game ->
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
