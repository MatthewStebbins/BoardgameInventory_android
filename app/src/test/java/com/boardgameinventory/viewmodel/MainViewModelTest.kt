package com.boardgameinventory.viewmodel

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the MainViewModel package components
 * These tests focus on testable components without Android dependencies
 */
class MainViewModelTest {

    @Test
    fun `GameStats data class should have correct default values`() {
        val gameStats = GameStats()
        
        assertEquals(0, gameStats.totalGames)
        assertEquals(0, gameStats.loanedGames)
        assertEquals(0, gameStats.availableGames)
    }

    @Test
    fun `GameStats data class should accept custom values`() {
        val gameStats = GameStats(
            totalGames = 10,
            loanedGames = 3,
            availableGames = 7
        )
        
        assertEquals(10, gameStats.totalGames)
        assertEquals(3, gameStats.loanedGames)
        assertEquals(7, gameStats.availableGames)
    }

    @Test
    fun `GameStats should support equality comparison`() {
        val stats1 = GameStats(totalGames = 5, loanedGames = 2, availableGames = 3)
        val stats2 = GameStats(totalGames = 5, loanedGames = 2, availableGames = 3)
        val stats3 = GameStats(totalGames = 6, loanedGames = 2, availableGames = 3)
        
        assertEquals(stats1, stats2)
        assertNotEquals(stats1, stats3)
    }

    @Test
    fun `GameStats toString should work correctly`() {
        val gameStats = GameStats(totalGames = 15, loanedGames = 5, availableGames = 10)
        val toString = gameStats.toString()
        
        assertNotNull(toString)
        assertTrue(toString.contains("15"))
        assertTrue(toString.contains("5"))
        assertTrue(toString.contains("10"))
    }

    @Test
    fun `GameStats copy should work correctly`() {
        val original = GameStats(totalGames = 20, loanedGames = 8, availableGames = 12)
        val copied = original.copy(loanedGames = 10, availableGames = 10)
        
        assertEquals(20, copied.totalGames)
        assertEquals(10, copied.loanedGames)
        assertEquals(10, copied.availableGames)
        
        // Original should remain unchanged
        assertEquals(8, original.loanedGames)
        assertEquals(12, original.availableGames)
    }
}
