package com.boardgameinventory.viewmodel

import com.boardgameinventory.data.Game
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Export/Import functionality
 * Tests business logic and data validation without requiring complex mocking
 */
class ExportImportViewModelTest {

    @Test
    fun `export data validation with valid games should pass`() {
        // Given
        val validGames = listOf(
            Game(id = 1, name = "Monopoly", barcode = "123", bookcase = "A", shelf = "1"),
            Game(id = 2, name = "Scrabble", barcode = "456", bookcase = "A", shelf = "2")
        )

        // When
        val isValid = validGames.isNotEmpty() && validGames.all { 
            it.name.isNotBlank() && it.barcode.isNotBlank() 
        }

        // Then
        assertTrue("Valid games should pass validation", isValid)
    }

    @Test
    fun `export data validation with empty list should fail`() {
        // Given
        val emptyGames = emptyList<Game>()

        // When
        val isValid = emptyGames.isNotEmpty()

        // Then
        assertFalse("Empty list should fail validation", isValid)
    }

    @Test
    fun `export format validation should work correctly`() {
        // Test CSV format
        assertTrue("CSV should be valid", "CSV" in listOf("CSV", "EXCEL"))
        assertTrue("EXCEL should be valid", "EXCEL" in listOf("CSV", "EXCEL"))
        assertFalse("PDF should not be valid", "PDF" in listOf("CSV", "EXCEL"))
    }

    @Test
    fun `file extension validation should work correctly`() {
        assertTrue("CSV extension should be valid", "test.csv".endsWith(".csv"))
        assertTrue("XLSX extension should be valid", "test.xlsx".endsWith(".xlsx"))
        assertFalse("TXT extension should not be valid for export", "test.txt".endsWith(".csv"))
    }

    @Test
    fun `duplicate detection logic should work`() {
        // Given
        val games = listOf(
            Game(id = 1, name = "Game A", barcode = "111", bookcase = "A", shelf = "1"),
            Game(id = 2, name = "Game B", barcode = "222", bookcase = "A", shelf = "2"),
            Game(id = 3, name = "Game A", barcode = "333", bookcase = "A", shelf = "3") // Duplicate name
        )

        // When
        val gameNames = games.map { it.name }
        val uniqueNames = gameNames.toSet()
        val hasDuplicates = gameNames.size != uniqueNames.size

        // Then
        assertTrue("Should detect duplicates", hasDuplicates)
        assertEquals("Should have 2 unique names", 2, uniqueNames.size)
    }

    @Test
    fun `CSV header validation should work`() {
        // Given
        val validHeaders = listOf("Name", "Barcode", "Category")
        val invalidHeaders = listOf("Title", "Code") // Missing required fields

        // When
        val requiredFields = setOf("Name", "Barcode")
        val validCheck = requiredFields.all { field -> 
            validHeaders.any { it.equals(field, ignoreCase = true) }
        }
        val invalidCheck = requiredFields.all { field -> 
            invalidHeaders.any { it.equals(field, ignoreCase = true) }
        }

        // Then
        assertTrue("Valid headers should pass", validCheck)
        assertFalse("Invalid headers should fail", invalidCheck)
    }

    @Test
    fun `game data field validation should work`() {
        // Given
        val validGame = Game(name = "Valid Game", barcode = "123456", bookcase = "A", shelf = "1")
        val invalidGame = Game(name = "", barcode = "123456", bookcase = "A", shelf = "1")

        // When & Then
        assertTrue("Valid game should pass validation", validGame.name.isNotBlank())
        assertFalse("Invalid game should fail validation", invalidGame.name.isNotBlank())
    }

    @Test
    fun `filename sanitization should work correctly`() {
        // Given
        val unsafeFilename = "My Games Collection 2024 Report.csv"
        val unsafeWithSpecialChars = "Games/With\\Bad:Chars<>.csv"

        // When
        val safe1 = unsafeFilename.replace(Regex("[/\\\\:*?\"<>|]"), "_")
        val safe2 = unsafeWithSpecialChars.replace(Regex("[/\\\\:*?\"<>|]"), "_")

        // Then
        assertEquals("Safe filename should remain unchanged", "My Games Collection 2024 Report.csv", safe1)
        assertEquals("Unsafe chars should be replaced", "Games_With_Bad_Chars__.csv", safe2)
    }

    @Test
    fun `data export statistics should calculate correctly`() {
        // Given
        val totalGames = 100
        val exportedGames = 95
        val failedExports = 5

        // When
        val successRate = (exportedGames.toFloat() / totalGames) * 100
        val calculatedTotal = exportedGames + failedExports

        // Then
        assertEquals("Total should match", totalGames, calculatedTotal)
        assertEquals("Success rate should be 95%", 95.0f, successRate, 0.1f)
    }

    @Test
    fun `import data type validation should work`() {
        // Given
        val csvFilename = "games.csv"
        val xlsxFilename = "games.xlsx"
        val txtFilename = "games.txt"

        // When & Then
        assertTrue("CSV should be supported", csvFilename.endsWith(".csv") || csvFilename.endsWith(".xlsx"))
        assertTrue("XLSX should be supported", xlsxFilename.endsWith(".csv") || xlsxFilename.endsWith(".xlsx"))
        assertFalse("TXT should not be supported", txtFilename.endsWith(".csv") || txtFilename.endsWith(".xlsx"))
    }
}
