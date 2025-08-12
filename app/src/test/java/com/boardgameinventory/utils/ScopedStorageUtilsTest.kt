package com.boardgameinventory.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Unit tests for the ScopedStorageUtils class
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ScopedStorageUtilsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `createPrivateFile should create file in app private storage`() {
        val filename = "test_file.db"
        val result = ScopedStorageUtils.createPrivateFile(context, filename)
        assertTrue(result is ScopedStorageUtils.StorageResult.Success)
        val file = (result as ScopedStorageUtils.StorageResult.Success).data
        assertNotNull(file)
        assertEquals(filename, file.name)
        assertTrue(file.exists())
    }

    @Test
    fun `createPrivateFile should handle empty filename`() {
        val result = ScopedStorageUtils.createPrivateFile(context, "")
        assertTrue(result is ScopedStorageUtils.StorageResult.Error)
        val error = result as ScopedStorageUtils.StorageResult.Error
        assertEquals("Filename is empty", error.errorMessage)
    }

    @Test
    fun `createPublicDocument should create file in public storage`() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val filename = "public_file.txt"
            val mimeType = "text/plain"
            val result = ScopedStorageUtils.createPublicDocument(context, filename, mimeType)
            assertTrue(result is ScopedStorageUtils.StorageResult.Success)
        }
    }

    @Test
    fun `deleteByUri should delete existing file`() {
        val tempFile = File.createTempFile("testFile", ".txt")
        val fileUri = Uri.fromFile(tempFile)
        val mockContext = mock(Context::class.java)
        val mockContentResolver = mock(ContentResolver::class.java)
        `when`(mockContext.contentResolver).thenReturn(mockContentResolver)
        `when`(mockContentResolver.delete(fileUri, null, null)).thenReturn(1)
        val result = ScopedStorageUtils.deleteByUri(mockContext, fileUri)
        assertTrue(result is ScopedStorageUtils.StorageResult.Success)
    }

    @Test
    fun `deleteByUri should handle non-existent file`() {
        val mockContext = mock(Context::class.java)
        val mockUri = mock(Uri::class.java)
        val mockContentResolver = mock(ContentResolver::class.java)
        `when`(mockContext.contentResolver).thenReturn(mockContentResolver)
        `when`(mockContentResolver.delete(mockUri, null, null)).thenReturn(0)
        val result = ScopedStorageUtils.deleteByUri(mockContext, mockUri)
        assertTrue(result is ScopedStorageUtils.StorageResult.Error)
        val error = result as ScopedStorageUtils.StorageResult.Error
        assertEquals("Failed to delete file - file may not exist", error.errorMessage)
    }

    @Test
    fun `writeToUri should write data to file`() {
        val tempFile = File.createTempFile("testFile", ".txt")
        val fileUri = Uri.fromFile(tempFile)
        val mockContext = mock(Context::class.java)
        val mockContentResolver = mock(ContentResolver::class.java)
        val mockOutputStream = mock(OutputStream::class.java)
        `when`(mockContext.contentResolver).thenReturn(mockContentResolver)
        `when`(mockContentResolver.openOutputStream(fileUri)).thenReturn(mockOutputStream)
        val result = ScopedStorageUtils.writeToUri(mockContext, fileUri) {
            it.write("Test data".toByteArray())
        }
        assertTrue(result is ScopedStorageUtils.StorageResult.Success)
    }

    @Test
    fun `readFromUri should read data from file`() {
        val tempFile = File.createTempFile("testFile", ".txt")
        tempFile.writeText("Test data")
        val fileUri = Uri.fromFile(tempFile)
        val mockContext = mock(Context::class.java)
        val mockContentResolver = mock(ContentResolver::class.java)
        val mockInputStream = mock(InputStream::class.java)
        `when`(mockContext.contentResolver).thenReturn(mockContentResolver)
        `when`(mockContentResolver.openInputStream(fileUri)).thenReturn(mockInputStream)
        val result = ScopedStorageUtils.readFromUri(mockContext, fileUri) {
            assertNotNull(it)
        }
        assertTrue(result is ScopedStorageUtils.StorageResult.Success)
    }

    @Test
    fun `getMimeType should return correct MIME type`() {
        assertEquals("text/csv", ScopedStorageUtils.getMimeType("file.csv"))
        assertEquals("application/json", ScopedStorageUtils.getMimeType("file.json"))
        assertEquals("application/octet-stream", ScopedStorageUtils.getMimeType("file.unknown"))
    }

    @Test
    fun `copyToPublicStorage should handle non-existent file`() {
        val nonExistentFile = File("/path/that/does/not/exist.txt")
        val result = ScopedStorageUtils.copyToPublicStorage(context, nonExistentFile, "copy.txt")
        assertTrue(result is ScopedStorageUtils.StorageResult.Error)
    }

}
