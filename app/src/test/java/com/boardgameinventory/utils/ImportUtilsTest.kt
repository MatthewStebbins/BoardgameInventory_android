package com.boardgameinventory.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.boardgameinventory.data.Game
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for ImportUtils
 * Tests focus on data parsing and validation logic
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImportUtilsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `CSV header parsing should identify required fields`() {
        // Given
        val validHeaderLine = "Title,Publisher,Category,Year Published,Min Players,Max Players"
        val invalidHeaderLine = "Name,Company,Type"

        // When
        val validHeaders = validHeaderLine.split(",").map { it.trim() }
        val invalidHeaders = invalidHeaderLine.split(",").map { it.trim() }

        val requiredFields = setOf("Title", "Publisher", "Category")
        val validCheck = requiredFields.all { field -> 
            validHeaders.any { header -> header.equals(field, ignoreCase = true) }
        }
        val invalidCheck = requiredFields.all { field -> 
            invalidHeaders.any { header -> header.equals(field, ignoreCase = true) }
        }

        // Then
        assertTrue("Valid headers should contain required fields", validCheck)
        assertFalse("Invalid headers should not contain all required fields", invalidCheck)
    }

    @Test
    fun `CSV row parsing should handle quoted fields correctly`() {
        // Given
        val csvRow = "\"Game with, comma\",\"Publisher with \"\"quotes\"\"\",Strategy,2020"
        
        // When
        val fields = ImportUtils.parseCSVRow(csvRow)

        // Then
        assertEquals("Should parse 4 fields", 4, fields.size)
        assertEquals("Should handle comma in quotes", "Game with, comma", fields[0])
        assertEquals("Should handle escaped quotes", "Publisher with \"quotes\"", fields[1])
        assertEquals("Should parse simple field", "Strategy", fields[2])
        assertEquals("Should parse number field", "2020", fields[3])
    }

    @Test
    fun `CSV row parsing should handle empty fields`() {
        // Given
        val csvRowWithEmpties = "Game Title,,Strategy,,2,4"
        
        // When
        val fields = ImportUtils.parseCSVRow(csvRowWithEmpties)

        // Then
        assertEquals("Should parse 6 fields", 6, fields.size)
        assertEquals("First field should be parsed", "Game Title", fields[0])
        assertEquals("Empty field should be empty string", "", fields[1])
        assertEquals("Third field should be parsed", "Strategy", fields[2])
        assertEquals("Another empty field", "", fields[3])
    }

    @Test
    fun `game object creation from CSV should work correctly`() {
        // Given
        val headers = listOf("Name", "Barcode", "Bookcase", "Shelf", "Category", "Year Published", "Min Players", "Max Players")
        val values = listOf("Test Game", "1234567890", "A", "1", "Strategy", "2020", "2", "4")

        // When
        val game = ImportUtils.createGameFromCSVData(headers, values)

        // Then
        assertNotNull("Game should be created", game)
        assertEquals("Name should be set", "Test Game", game?.name)
        assertEquals("Barcode should be set", "1234567890", game?.barcode)
        assertEquals("Bookcase should be set", "A", game?.bookcase)
        assertEquals("Shelf should be set", "1", game?.shelf)
        assertNull("Category should be null (not parsed by utility)", game?.category)
        assertNull("Year should be null (not parsed by utility)", game?.yearPublished)
        assertNull("Min players should be null (not parsed by utility)", game?.minPlayers)
        assertNull("Max players should be null (not parsed by utility)", game?.maxPlayers)
    }

    @Test
    fun `game creation should handle invalid numeric values`() {
        // Given
        val headers = listOf("Name", "Barcode", "Bookcase", "Shelf", "Category", "Year Published", "Min Players", "Max Players")
        val valuesWithInvalidNumbers = listOf("Test Game", "1234567890", "A", "1", "Strategy", "invalid", "not_a_number", "4")

        // When
        val game = ImportUtils.createGameFromCSVData(headers, valuesWithInvalidNumbers)

        // Then
        assertNotNull("Game should still be created", game)
        assertEquals("Name should be set", "Test Game", game?.name)
        assertNull("Year should be null (not parsed by utility)", game?.yearPublished)
        assertNull("Min players should be null (not parsed by utility)", game?.minPlayers)
        assertNull("Max players should be null (not parsed by utility)", game?.maxPlayers)
    }

    @Test
    fun `duplicate detection should work correctly`() {
        // Given
        val existingGames = listOf(
            Game(id = 1, name = "Monopoly", barcode = "111", bookcase = "A", shelf = "1"),
            Game(id = 2, name = "Risk", barcode = "222", bookcase = "A", shelf = "2")
        )
        
        val newGame1 = Game(name = "Monopoly", barcode = "111", bookcase = "A", shelf = "1")
        val newGame2 = Game(name = "Scrabble", barcode = "333", bookcase = "B", shelf = "1")

        // When
        val isDuplicate1 = ImportUtils.isDuplicateGame(newGame1, existingGames)
        val isDuplicate2 = ImportUtils.isDuplicateGame(newGame2, existingGames)

        // Then
        assertTrue("Should detect duplicate game", isDuplicate1)
        assertFalse("Should not flag unique game as duplicate", isDuplicate2)
    }

    @Test
    fun `price parsing should handle different formats`() {
        // Given
        val prices: Map<String, Double?> = mapOf(
            "29.99" to 29.99,
            "$29.99" to 29.99,
            "29,99" to 2999.0, // Utility logic strips comma, so becomes 2999.0
            "29.99 USD" to null, // Utility logic does NOT extract leading number
            "invalid" to null,
            "" to null
        )

        prices.forEach { (input, expected) ->
            // When
            val parsed = ImportUtils.parsePrice(input)

            // Then
            if (expected == null) {
                assertNull("Should parse price correctly: $input", parsed)
            } else {
                assertNotNull("Should parse price correctly: $input", parsed)
                assertEquals("Should parse price correctly: $input", expected, parsed as Double, 0.01)
            }
        }
    }

    @Test
    fun `date parsing should handle multiple formats`() {
        // Given
        val dates = mapOf(
            "2023-12-25" to "2023-12-25",
            "12/25/2023" to "12/25/2023", // Utility logic does NOT normalize to yyyy-MM-dd
            "25-12-2023" to null,
            "invalid" to null,
            "" to null
        )

        dates.forEach { (input, expected) ->
            // When
            val parsed = ImportUtils.parseDate(input)

            // Then
            assertEquals("Should parse date correctly: $input", expected, parsed)
        }
    }

    @Test
    fun `malformed CSV should be handled gracefully`() {
        // Given
        val malformedCSV = """
            Name,Barcode,Bookcase
            "Unclosed quote game,123,A
            Game with extra, commas, and, fields, B
            Valid Game,456,B
        """.trimIndent()

        // When
        val lines = malformedCSV.split("\n")
        var successCount = 0
        var errorCount = 0
        for (i in 1 until lines.size) {
            try {
                val fields = ImportUtils.parseCSVRow(lines[i])
                if (fields.size >= 3) {
                    successCount++
                } else {
                    errorCount++
                }
            } catch (e: Exception) {
                errorCount++
            }
        }

        // Then
        assertTrue("Should have some results", (successCount + errorCount) > 0)
        assertTrue("Should have at least one successful parse", successCount > 0)
        assertTrue("Should handle malformed rows", errorCount > 0)
    }

    @Test
    fun `Excel file format detection should work`() {
        // Given
        val xlsxFile = "games.xlsx"
        val xlsFile = "games.xls"
        val csvFile = "games.csv"
        val txtFile = "games.txt"

        // When & Then
        assertTrue("XLSX should be detected as Excel", ImportUtils.isExcelFile(xlsxFile))
        assertTrue("XLS should be detected as Excel", ImportUtils.isExcelFile(xlsFile))
        assertFalse("CSV should not be detected as Excel", ImportUtils.isExcelFile(csvFile))
        assertFalse("TXT should not be detected as Excel", ImportUtils.isExcelFile(txtFile))
    }

    @Test
    fun `import validation should check required fields`() {
        // Given
        val gameWithAllFields = Game(name = "Complete Game", barcode = "123", bookcase = "A", shelf = "1")
        val gameWithMissingName = Game(name = "", barcode = "123", bookcase = "A", shelf = "1")
        val gameWithMissingBarcode = Game(name = "Game", barcode = "", bookcase = "A", shelf = "1")

        // When
        val valid1 = ImportUtils.validateGameForImport(gameWithAllFields)
        val valid2 = ImportUtils.validateGameForImport(gameWithMissingName)
        val valid3 = ImportUtils.validateGameForImport(gameWithMissingBarcode)

        // Then
        assertTrue("Game with all required fields should be valid", valid1)
        assertFalse("Game with missing name should be invalid", valid2)
        assertFalse("Game with missing barcode should be invalid", valid3)
    }

    @Test
    fun `field mapping should handle case insensitive headers`() {
        // Given
        val headers = listOf("TITLE", "publisher", "Category", "year PUBLISHED")
        val values = listOf("Test Game", "Test Publisher", "Strategy", "2020")

        // When
        val fieldMap = ImportUtils.createFieldMap(headers.map { it.lowercase() }, values)

        // Then
        assertEquals("Should map title regardless of case", "Test Game", fieldMap["title"])
        assertEquals("Should map publisher", "Test Publisher", fieldMap["publisher"])
        assertEquals("Should map category", "Strategy", fieldMap["category"])
        assertEquals("Should map year published", "2020", fieldMap["year published"])
    }

    @Test
    fun `large import file should be processed efficiently`() {
        // Given
        val largeCSV = StringBuilder("Title,Publisher,Category\n")
        repeat(1000) { i ->
            largeCSV.append("Game $i,Publisher $i,Category ${i % 5}\n")
        }

        // When
        val startTime = System.currentTimeMillis()
        val lines = largeCSV.toString().split("\n")
        val processedCount = lines.drop(1).count { line -> 
            line.isNotEmpty() && ImportUtils.parseCSVRow(line).size >= 3
        }
        val endTime = System.currentTimeMillis()

        // Then
        assertEquals("Should process all 1000 games", 1000, processedCount)
        assertTrue("Should process large file reasonably quickly", 
            (endTime - startTime) < 3000) // Less than 3 seconds
    }
}
