package com.boardgameinventory.pagination

import com.boardgameinventory.data.Game
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Simple pagination integration test without complex dependencies
 */
class SimplePaginationTest {

    @Test
    fun `pagination test data should be consistent and filterable`() = runTest {
        // Create test data
        val testGames = createTestGames(100)
        
        // Test basic pagination concepts
        val pageSize = 20
        val firstPage = testGames.take(pageSize)
        val secondPage = testGames.drop(pageSize).take(pageSize)
        
        // Verify pagination works
        assertEquals(pageSize, firstPage.size)
        assertEquals(pageSize, secondPage.size)
        assertEquals("Game 1", firstPage.first().name)
        assertEquals("Game 20", firstPage.last().name)
        assertEquals("Game 21", secondPage.first().name)
        assertEquals("Game 40", secondPage.last().name)
        
        // Test filtering (simulating what the repository would do)
        val availableGames = testGames.filter { it.loanedTo == null }
        val loanedGames = testGames.filter { it.loanedTo != null }
        
        assertTrue("Should have available games", availableGames.isNotEmpty())
        assertTrue("Should have loaned games", loanedGames.isNotEmpty())
        assertEquals(100, availableGames.size + loanedGames.size)
        
        // Test search simulation
        val searchResults = testGames.filter { it.name.contains("1") }
        assertTrue("Should find games with '1' in name", searchResults.isNotEmpty())
        
        // Every game with "1" in the name should match our pattern
        searchResults.forEach { game ->
            assertTrue("Game name should contain '1': ${game.name}", 
                      game.name.contains("1"))
        }
    }
    
    @Test
    fun `empty pagination should be handled correctly`() = runTest {
        val emptyGames = emptyList<Game>()
        
        // Test pagination on empty list
        val pageSize = 20
        val firstPage = emptyGames.take(pageSize)
        val secondPage = emptyGames.drop(pageSize).take(pageSize)
        
        assertEquals(0, firstPage.size)
        assertEquals(0, secondPage.size)
        
        // Test filtering empty list
        val availableGames = emptyGames.filter { it.loanedTo == null }
        val loanedGames = emptyGames.filter { it.loanedTo != null }
        
        assertEquals(0, availableGames.size)
        assertEquals(0, loanedGames.size)
    }
    
    @Test
    fun `single page should work correctly`() = runTest {
        val smallGameList = createTestGames(5)
        val pageSize = 20
        
        val firstPage = smallGameList.take(pageSize)
        val secondPage = smallGameList.drop(pageSize).take(pageSize)
        
        assertEquals(5, firstPage.size)
        assertEquals(0, secondPage.size)
        
        assertEquals("Game 1", firstPage.first().name)
        assertEquals("Game 5", firstPage.last().name)
    }
    
    @Test
    fun `pagination should maintain data integrity`() = runTest {
        val originalGames = createTestGames(50)
        val pageSize = 10
        
        // Collect all pages
        val allPagesData = mutableListOf<Game>()
        var currentOffset = 0
        
        while (currentOffset < originalGames.size) {
            val page = originalGames.drop(currentOffset).take(pageSize)
            if (page.isEmpty()) break
            allPagesData.addAll(page)
            currentOffset += pageSize
        }
        
        // Verify we got all data back
        assertEquals(originalGames.size, allPagesData.size)
        
        // Verify order is maintained
        originalGames.forEachIndexed { index, originalGame ->
            assertEquals("Game order should be maintained at index $index",
                        originalGame.id, allPagesData[index].id)
        }
        
        // Verify no duplicates
        val uniqueIds = allPagesData.map { it.id }.distinct()
        assertEquals("Should have no duplicate IDs", 
                    allPagesData.size, uniqueIds.size)
    }
    
    @Test
    fun `search filtering should work with pagination`() = runTest {
        val allGames = createTestGames(100)
        
        // Simulate search for games with "1" in the name
        val searchTerm = "1"
        val filteredGames = allGames.filter { 
            it.name.contains(searchTerm, ignoreCase = true) 
        }
        
        // Apply pagination to filtered results
        val pageSize = 5
        val firstPageOfSearch = filteredGames.take(pageSize)
        
        assertTrue("Should have search results", filteredGames.isNotEmpty())
        assertTrue("First page should not exceed page size", 
                  firstPageOfSearch.size <= pageSize)
        
        // Verify all results match search criteria
        firstPageOfSearch.forEach { game ->
            assertTrue("Search result should match criteria: ${game.name}",
                      game.name.contains(searchTerm, ignoreCase = true))
        }
    }

    // Helper method to create test games
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
