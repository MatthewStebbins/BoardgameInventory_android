package com.boardgameinventory.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
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
     * File metadata data class for storing file information
     */
    data class FileMetadata(
        val name: String?,
        val size: Long,
        val mimeType: String?,
        val lastModified: Long,
        val uri: Uri?
    )

    /**
     * Create a file in the app's private external storage (no permissions needed)
     * Best for app-specific data that doesn't need to persist after uninstall
     */
    fun createPrivateFile(context: Context, filename: String): StorageResult<File> {
        return try {
            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), BOARDGAME_DIRECTORY)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, filename)
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
    fun writeToUri(context: Context, uri: Uri, data: ByteArray): StorageResult<Boolean> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data)
                outputStream.flush()
                StorageResult.Success(true)
            } ?: StorageResult.Error(
                Exception("Null output stream"),
                "Failed to open file for writing"
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied writing to URI", e)
            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "URI no longer valid", e)
            StorageResult.Error(e, "File URI no longer valid: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to URI: ${e.message}", e)
            StorageResult.Error(e, "Failed to write data: ${e.localizedMessage}")
        }
    }
    
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
    fun readFromUri(context: Context, uri: Uri): StorageResult<ByteArray> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val data = inputStream.readBytes()
                StorageResult.Success(data)
            } ?: StorageResult.Error(
                Exception("Null input stream"),
                "Failed to open file for reading"
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied reading from URI", e)
            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "URI no longer valid", e)
            StorageResult.Error(e, "File URI no longer valid: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error reading from URI: ${e.message}", e)
            StorageResult.Error(e, "Failed to read data: ${e.localizedMessage}")
        }
    }
    
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
     * Check if storage permissions are needed based on API level and operation type
     */
    fun needsStoragePermissions(context: Context, isPublicStorage: Boolean): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, we don't need traditional storage permissions for most operations
            // For Android 11+ (API 30), check MANAGE_EXTERNAL_STORAGE permission for all files access
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                isPublicStorage && !Environment.isExternalStorageManager()
            } else {
                // For Android 10 (API 29), we still use the scoped storage model
                // but don't need to check isExternalStorageManager
                isPublicStorage
            }
        } else {
            // For Android 9 and below, check the traditional storage permission
            !StoragePermissionUtils.hasStoragePermissions(context)
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
                "Source file doesn't exist: ${privateFile.path}"
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

    /**
     * Opens the system file picker to let user select a directory
     * Returns an Intent that can be used with registerForActivityResult
     */
    fun createOpenDirectoryIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
    }

    /**
     * Opens the system file picker to let user select a file
     * Returns an Intent that can be used with registerForActivityResult
     */
    fun createOpenFileIntent(mimeTypes: Array<String>? = null): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            if (mimeTypes != null && mimeTypes.isNotEmpty()) {
                if (mimeTypes.size == 1) {
                    type = mimeTypes[0]
                } else {
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            } else {
                type = "*/*"
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Creates the intent for creating a new document
     */
    fun createNewDocumentIntent(filename: String, mimeType: String): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, filename)
        }
    }

    /**
     * Take persistable permissions on a URI
     */
    fun takePersistablePermissions(context: Context, uri: Uri, write: Boolean = true): StorageResult<Boolean> {
        return try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    (if (write) Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)

            context.contentResolver.takePersistableUriPermission(uri, flags)
            StorageResult.Success(true)
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when taking persistable permissions", e)
            StorageResult.Error(e, "Failed to get persistent access: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error taking persistable URI permissions: ${e.message}", e)
            StorageResult.Error(e, "Failed to get persistent access: ${e.localizedMessage}")
        }
    }

    /**
     * Creates a file in a user-selected directory using DocumentFile API
     */
    fun createFileInUserDirectory(context: Context, directoryUri: Uri, filename: String, mimeType: String? = null): StorageResult<Uri> {
        return try {
            val directory = DocumentFile.fromTreeUri(context, directoryUri)
            val actualMimeType = mimeType ?: getMimeType(filename)

            val file = directory?.createFile(actualMimeType, filename)
            if (file != null) {
                StorageResult.Success(file.uri)
            } else {
                StorageResult.Error(
                    Exception("Failed to create file in selected directory"),
                    "Couldn't create file in the selected folder"
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied creating file in user directory", e)
            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid URI for directory", e)
            StorageResult.Error(e, "Invalid directory location: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating file in user directory: ${e.message}", e)
            StorageResult.Error(e, "Failed to create file: ${e.localizedMessage}")
        }
    }

    /**
     * Creates a subdirectory in a user-selected directory
     */
    fun createSubdirectory(context: Context, parentUri: Uri, directoryName: String): StorageResult<Uri> {
        return try {
            val parentDir = DocumentFile.fromTreeUri(context, parentUri)
            val subDir = parentDir?.createDirectory(directoryName)

            if (subDir != null) {
                StorageResult.Success(subDir.uri)
            } else {
                StorageResult.Error(
                    Exception("Failed to create subdirectory"),
                    "Couldn't create folder"
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied creating subdirectory", e)
            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating subdirectory: ${e.message}", e)
            StorageResult.Error(e, "Failed to create folder: ${e.localizedMessage}")
        }
    }

    /**
     * Lists files in a user-selected directory
     */
    fun listFilesInDirectory(context: Context, directoryUri: Uri): StorageResult<List<FileMetadata>> {
        return try {
            val directory = DocumentFile.fromTreeUri(context, directoryUri)
            val files = directory?.listFiles()

            if (files != null) {
                val fileList = files.mapNotNull { file ->
                    val name = file.name
                    val type = file.type
                    val lastModified = file.lastModified()
                    val size = file.length()
                    val uri = file.uri

                    if (name != null) {
                        FileMetadata(name, size, type, lastModified, uri)
                    } else null
                }
                StorageResult.Success(fileList)
            } else {
                StorageResult.Error(
                    Exception("Failed to list directory contents"),
                    "Couldn't access folder contents"
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied listing directory", e)
            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error listing directory: ${e.message}", e)
            StorageResult.Error(e, "Failed to list folder contents: ${e.localizedMessage}")
        }
    }

    /**
     * Query for files in public storage using MediaStore
     */
    fun queryMediaStoreFiles(context: Context, folderName: String? = null): StorageResult<List<FileMetadata>> {
        return try {
            val files = mutableListOf<FileMetadata>()
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.RELATIVE_PATH
            )

            val selection = if (folderName != null)
                "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ?"
            else null

            val selectionArgs = if (folderName != null)
                arrayOf("%$folderName%")
            else null

            context.contentResolver.query(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val mimeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val dateColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val mime = if (mimeColumn >= 0) cursor.getString(mimeColumn) else null
                    val size = if (sizeColumn >= 0) cursor.getLong(sizeColumn) else 0
                    val date = if (dateColumn >= 0) cursor.getLong(dateColumn) * 1000 else 0 // Convert to milliseconds

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), id)

                    files.add(FileMetadata(name, size, mime, date, contentUri))
                }
            }

            StorageResult.Success(files)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied querying MediaStore", e)
            StorageResult.Error(e, "Permission denied: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore: ${e.message}", e)
            StorageResult.Error(e, "Failed to query files: ${e.localizedMessage}")
        }
    }

    /**
     * Get file metadata from URI
     */
    fun getFileMetadata(context: Context, uri: Uri): StorageResult<FileMetadata> {
        return try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED
                ),
                null,
                null,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                    val mimeIndex = it.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                    val dateIndex = it.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)

                    val name = if (nameIndex >= 0) it.getString(nameIndex) else getFilenameFromUri(uri)
                    val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0
                    val mime = if (mimeIndex >= 0) it.getString(mimeIndex) else getMimeTypeFromUri(context, uri)
                    val date = if (dateIndex >= 0) it.getLong(dateIndex) * 1000 else 0 // Convert to milliseconds

                    StorageResult.Success(FileMetadata(name, size, mime, date, uri))
                } else {
                    // If we can't get metadata through the cursor, try alternative methods
                    val name = getFilenameFromUri(uri)
                    val mime = getMimeTypeFromUri(context, uri)

                    StorageResult.Success(FileMetadata(name, 0, mime, 0, uri))
                }
            } ?: run {
                // If query fails, try to extract basic info from the URI itself
                val name = getFilenameFromUri(uri)
                val mime = getMimeTypeFromUri(context, uri)

                StorageResult.Success(FileMetadata(name, 0, mime, 0, uri))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file metadata: ${e.message}", e)
            StorageResult.Error(e, "Failed to get file information: ${e.localizedMessage}")
        }
    }

    /**
     * Extract filename from URI as a fallback method
     */
    private fun getFilenameFromUri(uri: Uri): String? {
        return when {
            uri.scheme == "file" -> File(uri.path ?: "").name
            uri.scheme == "content" -> uri.lastPathSegment
            else -> null
        }
    }

    /**
     * Get MIME type from URI as a fallback method
     */
    private fun getMimeTypeFromUri(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri) ?: uri.lastPathSegment?.let {
            getMimeType(it)
        }
    }

    /**
     * Extension function to register for directory selection result
     */
    fun AppCompatActivity.registerForDirectoryResult(onResult: (Uri?) -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                onResult(uri)
            } else {
                onResult(null)
            }
        }
    }

    /**
     * Extension function to register for file selection result
     */
    fun AppCompatActivity.registerForFileResult(onResult: (Uri?) -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val uri = result.data?.data
                onResult(uri)
            } else {
                onResult(null)
            }
        }
    }

    /**
     * Extension function to register for directory selection result in a Fragment
     */
    fun Fragment.registerForDirectoryResult(onResult: (Uri?) -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                onResult(uri)
            } else {
                onResult(null)
            }
        }
    }
}
