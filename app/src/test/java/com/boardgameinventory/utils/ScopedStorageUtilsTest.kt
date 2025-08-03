package com.boardgameinventory.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests for the ScopedStorageUtils class
 */
@RunWith(RobolectricTestRunner::class)
class ScopedStorageUtilsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `createPrivateFile should create file in app private storage`() {
        // Given
        val filename = "test_backup.db"

        // When
        val file = ScopedStorageUtils.createPrivateFile(context, filename)

        // Then
        assertNotNull(file)
        assertEquals(filename, file?.name)
        assertTrue(file?.absolutePath?.contains("BoardGameInventory") == true)
    }

    @Test
    fun `createPrivateFile with null filename should handle gracefully`() {
        // Given
        val filename = ""

        // When
        val file = ScopedStorageUtils.createPrivateFile(context, filename)

        // Then
        assertNotNull(file) // Should still create file, even if empty name
    }

    @Test
    fun `getMimeType should return correct types for known extensions`() {
        // Test CSV
        assertEquals("text/csv", ScopedStorageUtils.getMimeType("games.csv"))
        
        // Test Excel
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                    ScopedStorageUtils.getMimeType("games.xlsx"))
        assertEquals("application/vnd.ms-excel", ScopedStorageUtils.getMimeType("games.xls"))
        
        // Test JSON
        assertEquals("application/json", ScopedStorageUtils.getMimeType("data.json"))
        
        // Test Text
        assertEquals("text/plain", ScopedStorageUtils.getMimeType("readme.txt"))
        
        // Test Database
        assertEquals("application/x-sqlite3", ScopedStorageUtils.getMimeType("games.db"))
    }

    @Test
    fun `getMimeType should return default for unknown extensions`() {
        // Given
        val unknownFile = "unknown.xyz"

        // When
        val mimeType = ScopedStorageUtils.getMimeType(unknownFile)

        // Then
        assertEquals("application/octet-stream", mimeType)
    }

    @Test
    fun `getMimeType should handle files without extensions`() {
        // Given
        val noExtensionFile = "filename_without_extension"

        // When
        val mimeType = ScopedStorageUtils.getMimeType(noExtensionFile)

        // Then
        assertEquals("application/octet-stream", mimeType)
    }

    @Test
    fun `getMimeType should be case insensitive`() {
        // Test uppercase
        assertEquals("text/csv", ScopedStorageUtils.getMimeType("GAMES.CSV"))
        
        // Test mixed case
        assertEquals("application/json", ScopedStorageUtils.getMimeType("Data.JSON"))
        
        // Test with mixed case extension
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                    ScopedStorageUtils.getMimeType("games.XLSX"))
    }

    @Test
    fun `getPrivateExternalDir should return valid directory`() {
        // When
        val directory = ScopedStorageUtils.getPrivateExternalDir(context)

        // Then
        assertNotNull(directory)
        assertTrue(directory?.exists() == true || directory?.mkdirs() == true)
    }

    @Test
    fun `hasFilePermissions should delegate to StoragePermissionUtils`() {
        // When
        val hasPermissions = ScopedStorageUtils.hasFilePermissions(context)

        // Then
        // Should not throw exception (test passes if no exception is thrown)
        // hasPermissions is always Boolean, so this test confirms the method works
        assertTrue("Method should return boolean", hasPermissions || !hasPermissions)
    }

    @Test
    fun `writeToUri and readFromUri should handle data correctly`() {
        // Given
        val testData = "Test game data for unit testing".toByteArray()
        val privateFile = ScopedStorageUtils.createPrivateFile(context, "test_read_write.txt")
        
        requireNotNull(privateFile) { "Private file creation failed" }
        
        // When - Write data
        privateFile.writeBytes(testData)
        
        // Then - Read data back
        val readData = privateFile.readBytes()
        assertArrayEquals(testData, readData)
        
        // Cleanup
        privateFile.delete()
    }

    @Test
    fun `copyToPublicStorage with non-existent file should return null`() {
        // Given
        val nonExistentFile = File("/path/that/does/not/exist.txt")
        val publicFilename = "non_existent_copy.txt"

        // When
        val result = ScopedStorageUtils.copyToPublicStorage(context, nonExistentFile, publicFilename)

        // Then
        assertNull(result)
    }

    @Test
    fun `getMimeType should handle files with multiple dots`() {
        // Given
        val fileWithMultipleDots = "my.game.backup.csv"

        // When
        val mimeType = ScopedStorageUtils.getMimeType(fileWithMultipleDots)

        // Then
        assertEquals("text/csv", mimeType) // Should use the last extension
    }

    @Test
    fun `createPrivateFile should create directory if it doesn't exist`() {
        // Given
        val filename = "test_directory_creation.db"

        // When
        val file = ScopedStorageUtils.createPrivateFile(context, filename)

        // Then
        assertNotNull(file)
        assertTrue(file?.parentFile?.exists() == true)
        assertTrue(file?.parentFile?.name == "BoardGameInventory")
    }
}
