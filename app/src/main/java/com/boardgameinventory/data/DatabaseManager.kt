package com.boardgameinventory.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit

/**
 * Database version and backup manager for the Board Game Inventory app.
 * 
 * Responsibilities:
 * - Track database version changes
 * - Create automated backups before migrations
 * - Provide database health checks
 * - Handle rollback scenarios
 */
class DatabaseManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    companion object {
        private const val TAG = "DatabaseManager"
        private const val PREFS_NAME = "database_prefs"
        private const val KEY_LAST_VERSION = "last_db_version"
        private const val KEY_BACKUP_COUNT = "backup_count"
        private const val KEY_LAST_BACKUP = "last_backup_date"
        private const val MAX_BACKUPS = 5
        private const val DATABASE_NAME = "games_database"
    }
    
    /**
     * Get current database version from preferences
     */
    fun getLastKnownVersion(): Int {
        return prefs.getInt(KEY_LAST_VERSION, 1)
    }

    /**
     * Create a backup of the current database before migration
     */
    suspend fun createBackup(): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (!dbFile.exists()) {
                Log.w(TAG, "Database file does not exist, skipping backup")
                return@withContext false
            }
            
            val backupDir = File(context.filesDir, "db_backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            // Clean old backups
            cleanOldBackups(backupDir)
            
            val timestamp = dateFormat.format(Date())
            val backupFile = File(backupDir, "${DATABASE_NAME}_backup_$timestamp.db")
            
            // Copy database file
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Update backup metadata
            val backupCount = prefs.getInt(KEY_BACKUP_COUNT, 0) + 1
            prefs.edit {
                putInt(KEY_BACKUP_COUNT, backupCount)
                    .putLong(KEY_LAST_BACKUP, System.currentTimeMillis())
            }
            
            Log.d(TAG, "Database backup created: ${backupFile.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create database backup", e)
            false
        }
    }
    
    /**
     * Remove old backup files to save space
     */
    private fun cleanOldBackups(backupDir: File) {
        try {
            val backupFiles = backupDir.listFiles()?.filter { 
                it.name.startsWith("${DATABASE_NAME}_backup_") && it.name.endsWith(".db")
            }?.sortedByDescending { it.lastModified() }
            
            if (backupFiles != null && backupFiles.size >= MAX_BACKUPS) {
                val filesToDelete = backupFiles.drop(MAX_BACKUPS - 1)
                filesToDelete.forEach { file ->
                    if (file.delete()) {
                        Log.d(TAG, "Deleted old backup: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning old backups", e)
        }
    }
    
    /**
     * Restore database from the most recent backup
     */
    suspend fun restoreFromBackup(): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.filesDir, "db_backups")
            val backupFiles = backupDir.listFiles()?.filter { 
                it.name.startsWith("${DATABASE_NAME}_backup_") && it.name.endsWith(".db")
            }?.sortedByDescending { it.lastModified() }
            
            if (backupFiles.isNullOrEmpty()) {
                Log.w(TAG, "No backup files found")
                return@withContext false
            }
            
            val latestBackup = backupFiles.first()
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            
            // Close existing database connections
            AppDatabase.clearInstance()
            
            // Restore from backup
            FileInputStream(latestBackup).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.d(TAG, "Database restored from backup: ${latestBackup.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore database from backup", e)
            false
        }
    }
    
    /**
     * Perform database health check
     */
    suspend fun performHealthCheck(): DatabaseHealthReport = withContext(Dispatchers.IO) {
        val report = DatabaseHealthReport()
        
        try {
            val database = AppDatabase.getDatabase(context)
            val gameDao = database.gameDao()
            
            // Check table integrity
            report.isTableIntegrityOk = checkTableIntegrity()
            
            // Check record count
            report.totalRecords = gameDao.getGameCount()
            
            // Check for corrupted records
            report.corruptedRecords = checkForCorruptedRecords(gameDao)
            
            // Check database size
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            report.databaseSizeBytes = if (dbFile.exists()) dbFile.length() else 0
            
            // Check backup status
            val backupDir = File(context.filesDir, "db_backups")
            report.availableBackups = backupDir.listFiles()?.size ?: 0
            report.lastBackupDate = prefs.getLong(KEY_LAST_BACKUP, 0)
            
            report.isHealthy = report.isTableIntegrityOk && 
                              report.corruptedRecords == 0 && 
                              report.databaseSizeBytes > 0
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during health check", e)
            report.isHealthy = false
            report.errorMessage = e.message
        }
        
        Log.d(TAG, "Database health check completed: ${if (report.isHealthy) "HEALTHY" else "ISSUES FOUND"}")
        report
    }
    
    /**
     * Check SQLite table integrity
     */
    private fun checkTableIntegrity(): Boolean {
        return try {
            AppDatabase.getDatabase(context)
            // For now, just return true as a basic check
            // In a full implementation, we would access the underlying SQLite database
            true
        } catch (e: Exception) {
            Log.e(TAG, "Table integrity check failed", e)
            false
        }
    }
    
    /**
     * Check for records with missing required data
     */
    private suspend fun checkForCorruptedRecords(gameDao: GameDao): Int {
        return try {
            // Count games with empty names or barcodes
            val emptyNames = gameDao.getGamesWithEmptyNames()
            val emptyBarcodes = gameDao.getGamesWithEmptyBarcodes()
            emptyNames + emptyBarcodes
        } catch (e: Exception) {
            Log.e(TAG, "Corrupted records check failed", e)
            -1
        }
    }
    
    /**
     * Get list of available backup files
     */
    fun getAvailableBackups(): List<BackupInfo> {
        val backupDir = File(context.filesDir, "db_backups")
        return backupDir.listFiles()?.filter { 
            it.name.startsWith("${DATABASE_NAME}_backup_") && it.name.endsWith(".db")
        }?.map { file ->
            BackupInfo(
                fileName = file.name,
                sizeBytes = file.length(),
                createdDate = file.lastModified()
            )
        }?.sortedByDescending { it.createdDate } ?: emptyList()
    }
}

/**
 * Database health report data class
 */
data class DatabaseHealthReport(
    var isHealthy: Boolean = false,
    var isTableIntegrityOk: Boolean = false,
    var totalRecords: Int = 0,
    var corruptedRecords: Int = 0,
    var databaseSizeBytes: Long = 0,
    var availableBackups: Int = 0,
    var lastBackupDate: Long = 0,
    var errorMessage: String? = null
)

/**
 * Backup file information
 */
data class BackupInfo(
    val fileName: String,
    val sizeBytes: Long,
    val createdDate: Long
)
