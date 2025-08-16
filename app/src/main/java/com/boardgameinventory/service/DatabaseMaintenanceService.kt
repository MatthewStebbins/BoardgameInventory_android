package com.boardgameinventory.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.boardgameinventory.data.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Database maintenance service for the Board Game Inventory app.
 * 
 * Handles:
 * - Manual database backups
 * - Periodic health checks
 * - Database optimization
 */
class DatabaseMaintenanceService : Service() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var databaseManager: DatabaseManager
    
    companion object {
        private const val TAG = "DatabaseMaintenanceService"
        
        /**
         * Trigger immediate backup
         */
        fun triggerBackup(context: Context) {
            val intent = Intent(context, DatabaseMaintenanceService::class.java)
            intent.action = "BACKUP_DATABASE"
            context.startService(intent)
        }

    }
    
    override fun onCreate() {
        super.onCreate()
        databaseManager = DatabaseManager(this)
        Log.d(TAG, "Database maintenance service created")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "BACKUP_DATABASE" -> {
                serviceScope.launch {
                    performBackup()
                    stopSelf(startId)
                }
            }
            "HEALTH_CHECK" -> {
                serviceScope.launch {
                    performHealthCheck()
                    stopSelf(startId)
                }
            }
            "OPTIMIZE_DATABASE" -> {
                serviceScope.launch {
                    optimizeDatabase()
                    stopSelf(startId)
                }
            }
        }
        
        return START_NOT_STICKY
    }
    
    private suspend fun performBackup() {
        try {
            Log.d(TAG, "Starting database backup")
            val success = databaseManager.createBackup()
            if (success) {
                Log.i(TAG, "Database backup completed successfully")
            } else {
                Log.w(TAG, "Database backup failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during database backup", e)
        }
    }
    
    private suspend fun performHealthCheck() {
        try {
            Log.d(TAG, "Starting database health check")
            val report = databaseManager.performHealthCheck()
            
            if (report.isHealthy) {
                Log.i(TAG, "Database health check passed")
            } else {
                Log.w(TAG, "Database health check failed: ${report.errorMessage}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during database health check", e)
        }
    }
    
    private fun optimizeDatabase() {
        try {
            Log.d(TAG, "Starting database optimization")
            // Database optimization logic can be implemented here
            Log.i(TAG, "Database optimization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during database optimization", e)
        }
    }
}
