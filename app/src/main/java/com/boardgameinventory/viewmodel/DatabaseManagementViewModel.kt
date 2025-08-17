package com.boardgameinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.boardgameinventory.data.DatabaseManager
import com.boardgameinventory.data.DatabaseHealthReport
import com.boardgameinventory.data.BackupInfo
import com.boardgameinventory.service.DatabaseMaintenanceService
import kotlinx.coroutines.launch

/**
 * ViewModel for database management operations.
 * 
 * Provides UI access to:
 * - Database health monitoring
 * - Backup management
 * - Migration status
 * - Maintenance operations
 */
class DatabaseManagementViewModel(application: Application) : AndroidViewModel(application) {
    
    private val databaseManager = DatabaseManager(application)
    
    // LiveData for UI observation
    private val _healthReport = MutableLiveData<DatabaseHealthReport>()
    val healthReport: LiveData<DatabaseHealthReport> = _healthReport
    
    private val _availableBackups = MutableLiveData<List<BackupInfo>>()
    val availableBackups: LiveData<List<BackupInfo>> = _availableBackups
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _operationResult = MutableLiveData<OperationResult?>()
    val operationResult: LiveData<OperationResult?> = _operationResult
    
    private val _databaseVersion = MutableLiveData<Int>()
    val databaseVersion: LiveData<Int> = _databaseVersion
    
    init {
        loadDatabaseInfo()
    }
    
    /**
     * Load initial database information
     */
    private fun loadDatabaseInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _databaseVersion.value = databaseManager.getLastKnownVersion()
                performHealthCheck()
                loadAvailableBackups()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Perform database health check
     */
    fun performHealthCheck() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val report = databaseManager.performHealthCheck()
                _healthReport.value = report
                
                _operationResult.value = OperationResult(
                    success = report.isHealthy,
                    message = if (report.isHealthy) "Database is healthy" else "Database issues detected: ${report.errorMessage}",
                    type = OperationType.HEALTH_CHECK
                )
            } catch (e: Exception) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "Health check failed: ${e.message}",
                    type = OperationType.HEALTH_CHECK
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Create database backup
     */
    fun createBackup() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = databaseManager.createBackup()
                
                if (success) {
                    loadAvailableBackups() // Refresh backup list
                }
                
                _operationResult.value = OperationResult(
                    success = success,
                    message = if (success) "Backup created successfully" else "Backup creation failed",
                    type = OperationType.BACKUP
                )
            } catch (e: Exception) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "Backup failed: ${e.message}",
                    type = OperationType.BACKUP
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Restore database from backup
     */
    fun restoreFromBackup() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = databaseManager.restoreFromBackup()
                
                if (success) {
                    // Refresh all data after restore
                    performHealthCheck()
                    _databaseVersion.value = databaseManager.getLastKnownVersion()
                }
                
                _operationResult.value = OperationResult(
                    success = success,
                    message = if (success) "Database restored successfully" else "Restore failed",
                    type = OperationType.RESTORE
                )
            } catch (e: Exception) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "Restore failed: ${e.message}",
                    type = OperationType.RESTORE
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load available backup files
     */
    fun loadAvailableBackups() {
        viewModelScope.launch {
            try {
                val backups = databaseManager.getAvailableBackups()
                _availableBackups.value = backups
            } catch (e: Exception) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "Failed to load backups: ${e.message}",
                    type = OperationType.LOAD_BACKUPS
                )
            }
        }
    }
    
    /**
     * Schedule automatic maintenance
     */
    fun scheduleAutomaticMaintenance() {
        try {
            // For now, just trigger a backup service
            DatabaseMaintenanceService.triggerBackup(getApplication())
            _operationResult.value = OperationResult(
                success = true,
                message = "Backup service triggered",
                type = OperationType.SCHEDULE_MAINTENANCE
            )
        } catch (e: Exception) {
            _operationResult.value = OperationResult(
                success = false,
                message = "Failed to trigger backup: ${e.message}",
                type = OperationType.SCHEDULE_MAINTENANCE
            )
        }
    }

    /**
     * Clear operation result (for UI state management)
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }
    
    /**
     * Get database statistics summary
     */
    fun getDatabaseStats(): DatabaseStats {
        val report = _healthReport.value
        val backups = _availableBackups.value
        
        return DatabaseStats(
            version = _databaseVersion.value ?: 1,
            totalRecords = report?.totalRecords ?: 0,
            databaseSizeKB = (report?.databaseSizeBytes ?: 0) / 1024,
            isHealthy = report?.isHealthy ?: false,
            backupCount = backups?.size ?: 0,
            lastBackupDate = report?.lastBackupDate ?: 0
        )
    }
}

/**
 * Operation result for UI feedback
 */
data class OperationResult(
    val success: Boolean,
    val message: String,
    val type: OperationType
)

/**
 * Types of database operations
 */
enum class OperationType {
    HEALTH_CHECK,
    BACKUP,
    RESTORE,
    LOAD_BACKUPS,
    SCHEDULE_MAINTENANCE,
    TRIGGER_BACKUP
}

/**
 * Database statistics summary
 */
data class DatabaseStats(
    val version: Int,
    val totalRecords: Int,
    val databaseSizeKB: Long,
    val isHealthy: Boolean,
    val backupCount: Int,
    val lastBackupDate: Long
)
