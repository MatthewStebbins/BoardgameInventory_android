package com.boardgameinventory.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit tests for the Utils class
 */
@RunWith(RobolectricTestRunner::class)
class UtilsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `validateLocationBarcode with valid format should return bookcase and shelf`() {
        // Given
        val validBarcode = "A-1"

        // When
        val result = Utils.validateLocationBarcode(validBarcode)

        // Then
        assertEquals("A", result.first)
        assertEquals("1", result.second)
    }

    @Test
    fun `validateLocationBarcode with valid format and whitespace should trim and return correct values`() {
        // Given
        val barcodeWithSpaces = " B - 2 "

        // When
        val result = Utils.validateLocationBarcode(barcodeWithSpaces)

        // Then
        assertEquals("B", result.first)
        assertEquals("2", result.second)
    }

    @Test
    fun `validateLocationBarcode with no dash should return null values`() {
        // Given
        val invalidBarcode = "A1"

        // When
        val result = Utils.validateLocationBarcode(invalidBarcode)

        // Then
        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `validateLocationBarcode with empty string should return null values`() {
        // Given
        val emptyBarcode = ""

        // When
        val result = Utils.validateLocationBarcode(emptyBarcode)

        // Then
        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `validateLocationBarcode with blank string should return null values`() {
        // Given
        val blankBarcode = "   "

        // When
        val result = Utils.validateLocationBarcode(blankBarcode)

        // Then
        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `validateLocationBarcode with multiple dashes should return null values`() {
        // Given
        val multiDashBarcode = "A-B-1"

        // When
        val result = Utils.validateLocationBarcode(multiDashBarcode)

        // Then
        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `validateLocationBarcode with only dash should return null values`() {
        // Given
        val onlyDashBarcode = "-"

        // When
        val result = Utils.validateLocationBarcode(onlyDashBarcode)

        // Then
        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `validateLocationBarcode with numeric bookcase and shelf should work`() {
        // Given
        val numericBarcode = "1-2"

        // When
        val result = Utils.validateLocationBarcode(numericBarcode)

        // Then
        assertEquals("1", result.first)
        assertEquals("2", result.second)
    }

    @Test
    fun `validateLocationBarcode with alphanumeric values should work`() {
        // Given
        val alphanumericBarcode = "A1-B2"

        // When
        val result = Utils.validateLocationBarcode(alphanumericBarcode)

        // Then
        assertEquals("A1", result.first)
        assertEquals("B2", result.second)
    }

    @Test
    fun `formatDate should return correct format`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.DECEMBER, 25, 10, 30, 0) // Dec 25, 2023, 10:30 AM
        val timestamp = calendar.timeInMillis

        // When
        val formattedDate = Utils.formatDate(timestamp)

        // Then
        val expectedFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val expectedDate = expectedFormat.format(Date(timestamp))
        assertEquals(expectedDate, formattedDate)
    }

    @Test
    fun `formatDate with zero timestamp should handle gracefully`() {
        // Given
        val zeroTimestamp = 0L

        // When
        val formattedDate = Utils.formatDate(zeroTimestamp)

        // Then
        assertNotNull(formattedDate)
        assertTrue(formattedDate.isNotEmpty())
    }

    @Test
    fun `formatDate with current timestamp should not be null or empty`() {
        // Given
        val currentTimestamp = System.currentTimeMillis()

        // When
        val formattedDate = Utils.formatDate(currentTimestamp)

        // Then
        assertNotNull(formattedDate)
        assertTrue(formattedDate.isNotEmpty())
        assertTrue(formattedDate.contains("2025")) // Should contain current year
    }

    @Test
    fun `formatDateTime should return correct format`() {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.DECEMBER, 25, 10, 30, 0) // Dec 25, 2023, 10:30 AM
        val timestamp = calendar.timeInMillis

        // When
        val formattedDateTime = Utils.formatDateTime(timestamp)

        // Then
        assertTrue(formattedDateTime.contains("Dec"))
        assertTrue(formattedDateTime.contains("25"))
        assertTrue(formattedDateTime.contains("2023"))
        assertTrue(formattedDateTime.contains("10:30"))
    }

    @Test
    fun `showToast should not throw exception`() {
        // Given/When/Then - Should not throw any exception
        assertDoesNotThrow {
            Utils.showToast(context, "Test message")
        }
    }

    @Test
    fun `showLongToast should not throw exception`() {
        // Given/When/Then - Should not throw any exception
        assertDoesNotThrow {
            Utils.showLongToast(context, "Test long message")
        }
    }

    private fun assertDoesNotThrow(action: () -> Unit) {
        try {
            action()
            // If we reach here, no exception was thrown
            assertTrue(true)
        } catch (e: Exception) {
            fail("Expected no exception, but got: ${e.message}")
        }
    }
}
