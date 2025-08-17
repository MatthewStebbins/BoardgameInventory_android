package com.boardgameinventory.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Modern scoped storage utility for handling file operations across different Android versions
 * Implements Storage Access Framework (SAF) for user-directed file operations
 * Handles permissions and file operations based on Android API levels
 */
object ScopedStorageUtils {
    
    private const val TAG = "ScopedStorageUtils"
    private const val BOARDGAME_DIRECTORY = "BoardGameInventory"
    
    /**
     * Represents the result of a storage operation with proper error handling
     */
    sealed class StorageResult<out T> {
        data class Success<T>(val data: T) : StorageResult<T>()
        data class Error(val exception: Exception, val errorMessage: String) : StorageResult<Nothing>()
    }

    /**
     * Create a file in the app's private external storage (no permissions needed)
     * Best for app-specific data that doesn't need to persist after uninstall
     */
    fun createPrivateFile(context: Context, filename: String): StorageResult<File> {
        Log.d(TAG, "createPrivateFile called with filename: $filename")
        return try {
            if (filename.isBlank()) {
                Log.e(TAG, "Filename is blank")
                return StorageResult.Error(IllegalArgumentException("Filename cannot be empty"), "Filename is empty")
            }

            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BOARDGAME_DIRECTORY)
            Log.d(TAG, "Directory path: ${directory.absolutePath}")
            if (!directory.exists()) {
                val created = directory.mkdirs()
                Log.d(TAG, "Directory created: $created")
            }
            val file = File(directory, filename)
            Log.d(TAG, "File path: ${file.absolutePath}")

            if (!file.exists()) {
                val fileCreated = file.createNewFile()
                Log.d(TAG, "File created: $fileCreated")
            }

            StorageResult.Success(file)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating private file: ${e.message}", e)
            StorageResult.Error(e, "Failed to create private file: ${e.localizedMessage}")
        }
    }
    
    /**
     * Create a file in public Documents using MediaStore (Android 10+)
     * File will persist after app uninstall and be accessible by other apps
     */
    fun createPublicDocument(context: Context, filename: String, mimeType: String): StorageResult<Uri> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/$BOARDGAME_DIRECTORY")
                }
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                if (uri != null) {
                    StorageResult.Success(uri)
                } else {
                    StorageResult.Error(
                        Exception("Failed to create content URI"),
                        "System rejected document creation request"
                    )
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Permission denied when creating public document", e)
                StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating public document: ${e.message}", e)
                StorageResult.Error(e, "Failed to create public document: ${e.localizedMessage}")
            }
        } else {
            // Fallback for Android 9 and below
            createLegacyPublicFile(filename)
        }
    }
    
    /**
     * Legacy method for Android 9 and below
     */
    private fun createLegacyPublicFile(filename: String): StorageResult<Uri> {
        return try {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val appDir = File(documentsDir, BOARDGAME_DIRECTORY)
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            val file = File(appDir, filename)
            StorageResult.Success(Uri.fromFile(file))
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied creating legacy public file", e)
            StorageResult.Error(e, "Storage permission denied: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating legacy public file: ${e.message}", e)
            StorageResult.Error(e, "Failed to create file: ${e.localizedMessage}")
        }
    }
    
    /**
     * Write data to a URI using ContentResolver with proper error handling
     */
//    fun writeToUri(context: Context, uri: Uri, data: ByteArray): StorageResult<Boolean> {
//        return try {
//            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
//                outputStream.write(data)
//                outputStream.flush()
//                StorageResult.Success(true)
//            } ?: StorageResult.Error(
//                Exception("Null output stream"),
//                "Failed to open file for writing"
//            )
//        } catch (e: SecurityException) {
//            Log.e(TAG, "Permission denied writing to URI", e)
//            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
//        } catch (e: IllegalStateException) {
//            Log.e(TAG, "URI no longer valid", e)
//            StorageResult.Error(e, "File URI no longer valid: ${e.localizedMessage}")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error writing to URI: ${e.message}", e)
//            StorageResult.Error(e, "Failed to write data: ${e.localizedMessage}")
//        }
//    }
    
    /**
     * Write data to a URI using ContentResolver with OutputStream and proper error handling
     */
    fun writeToUri(context: Context, uri: Uri, writeAction: (OutputStream) -> Unit): StorageResult<Boolean> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                writeAction(outputStream)
                StorageResult.Success(true)
            } ?: StorageResult.Error(
                Exception("Null output stream"),
                "Failed to open file for writing"
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied writing to URI", e)
            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to URI: ${e.message}", e)
            StorageResult.Error(e, "Failed to write data: ${e.localizedMessage}")
        }
    }
    
    /**
     * Read data from a URI using ContentResolver with proper error handling
     */
//    fun readFromUri(context: Context, uri: Uri): StorageResult<ByteArray> {
//        return try {
//            context.contentResolver.openInputStream(uri)?.use { inputStream ->
//                val data = inputStream.readBytes()
//                StorageResult.Success(data)
//            } ?: StorageResult.Error(
//                Exception("Null input stream"),
//                "Failed to open file for reading"
//            )
//        } catch (e: SecurityException) {
//            Log.e(TAG, "Permission denied reading from URI", e)
//            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
//        } catch (e: IllegalStateException) {
//            Log.e(TAG, "URI no longer valid", e)
//            StorageResult.Error(e, "File URI no longer valid: ${e.localizedMessage}")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error reading from URI: ${e.message}", e)
//            StorageResult.Error(e, "Failed to read data: ${e.localizedMessage}")
//        }
//    }

    /**
     * Read data from a URI using ContentResolver with InputStream and proper error handling
     */
    fun readFromUri(context: Context, uri: Uri, readAction: (InputStream) -> Unit): StorageResult<Boolean> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                readAction(inputStream)
                StorageResult.Success(true)
            } ?: StorageResult.Error(
                Exception("Null input stream"),
                "Failed to open file for reading"
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied reading from URI", e)
            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error reading from URI: ${e.message}", e)
            StorageResult.Error(e, "Failed to read data: ${e.localizedMessage}")
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
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "application/octet-stream"
        }
    }

    /**
     * Delete a file by URI with proper error handling
     */
    fun deleteByUri(context: Context, uri: Uri): StorageResult<Boolean> {
        return try {
            val rowsDeleted = context.contentResolver.delete(uri, null, null)
            if (rowsDeleted > 0) {
                StorageResult.Success(true)
            } else {
                StorageResult.Error(
                    Exception("Delete operation returned zero rows affected"),
                    "Failed to delete file - file may not exist"
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied deleting URI", e)
            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid URI for delete operation", e)
            StorageResult.Error(e, "Invalid file URI: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file by URI: ${e.message}", e)
            StorageResult.Error(e, "Failed to delete file: ${e.localizedMessage}")
        }
    }
    
    /**
     * Copy a file from private storage to public storage with proper error handling
     */
    fun copyToPublicStorage(context: Context, privateFile: File, publicFilename: String): StorageResult<Uri> {
        if (!privateFile.exists()) {
            return StorageResult.Error(
                Exception("Private file doesn't exist"),
                "Source file doesn't exist: ${privateFile.path.replace('\\', '/')}"
            )
        }

        val mimeType = getMimeType(publicFilename)

        return when (val result = createPublicDocument(context, publicFilename, mimeType)) {
            is StorageResult.Success -> {
                try {
                    val inputStream = privateFile.inputStream()
                    val writeResult = writeToUri(context, result.data) { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    inputStream.close()

                    when (writeResult) {
                        is StorageResult.Success -> StorageResult.Success(result.data)
                        is StorageResult.Error -> StorageResult.Error(
                            writeResult.exception,
                            "Failed to copy file contents: ${writeResult.errorMessage}"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error copying file to public storage: ${e.message}", e)
                    StorageResult.Error(e, "Failed to copy file: ${e.localizedMessage}")
                }
            }
            is StorageResult.Error -> result
        }
    }

}
