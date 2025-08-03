package com.boardgameinventory.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Modern scoped storage utility for handling file operations across different Android versions
 * Replaces deprecated external storage APIs with MediaStore and Document APIs
 */
object ScopedStorageUtils {
    
    private const val BOARDGAME_DIRECTORY = "BoardGameInventory"
    
    /**
     * Create a file in the app's private external storage (no permissions needed)
     * Best for app-specific data that doesn't need to persist after uninstall
     */
    fun createPrivateFile(context: Context, filename: String): File? {
        return try {
            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BOARDGAME_DIRECTORY)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            File(directory, filename)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Create a file in public Documents using MediaStore (Android 10+)
     * File will persist after app uninstall and be accessible by other apps
     */
    fun createPublicDocument(context: Context, filename: String, mimeType: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/$BOARDGAME_DIRECTORY")
                }
                resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            // Fallback for Android 9 and below
            createLegacyPublicFile(filename)
        }
    }
    
    /**
     * Legacy method for Android 9 and below
     */
    private fun createLegacyPublicFile(filename: String): Uri? {
        return try {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val appDir = File(documentsDir, BOARDGAME_DIRECTORY)
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            val file = File(appDir, filename)
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Write data to a URI using ContentResolver
     */
    fun writeToUri(context: Context, uri: Uri, data: ByteArray): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data)
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Write data to a URI using ContentResolver with OutputStream
     */
    fun writeToUri(context: Context, uri: Uri, writeAction: (OutputStream) -> Unit): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                writeAction(outputStream)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Read data from a URI using ContentResolver
     */
    fun readFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Read data from a URI using ContentResolver with InputStream
     */
    fun readFromUri(context: Context, uri: Uri, readAction: (InputStream) -> Unit): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                readAction(inputStream)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get the MIME type for common file extensions
     */
    fun getMimeType(filename: String): String {
        return when (filename.substringAfterLast('.').lowercase()) {
            "csv" -> "text/csv"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "xls" -> "application/vnd.ms-excel"
            "json" -> "application/json"
            "txt" -> "text/plain"
            "db" -> "application/x-sqlite3"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Check if we have the necessary permissions for file operations
     */
    fun hasFilePermissions(context: Context): Boolean {
        return StoragePermissionUtils.hasStoragePermissions(context)
    }
    
    /**
     * Get the app's private external directory path (for reference)
     */
    fun getPrivateExternalDir(context: Context): File? {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    }
    
    /**
     * Delete a file by URI
     */
    fun deleteByUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Copy a file from private storage to public storage
     */
    fun copyToPublicStorage(context: Context, privateFile: File, publicFilename: String): Uri? {
        if (!privateFile.exists()) return null
        
        val mimeType = getMimeType(publicFilename)
        val publicUri = createPublicDocument(context, publicFilename, mimeType)
        
        return if (publicUri != null) {
            try {
                val inputStream = privateFile.inputStream()
                val success = writeToUri(context, publicUri) { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()
                
                if (success) publicUri else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
}
