package com.boardgameinventory.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

/**
 * Utility class for handling storage permissions across different Android versions
 */
object StoragePermissionUtils {
    
    /**
     * Get the required storage permissions based on Android version
     * Updated for scoped storage compatibility
     */
    fun getRequiredStoragePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ (API 33+) - Use granular media permissions only if accessing media
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11-12 (API 30-32) - Use READ_EXTERNAL_STORAGE only
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10 (API 29) - Scoped storage, minimal permissions needed
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                // Android 9 and below (API 28-) - Legacy permissions
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }
    
    /**
     * Check if all required storage permissions are granted
     */
    fun hasStoragePermissions(context: Context): Boolean {
        val requiredPermissions = getRequiredStoragePermissions()
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if the app can manage all files (Android 11+)
     * This should only be used when absolutely necessary
     */
    fun canManageAllFiles(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // Not applicable for older versions
        }
    }
    
    /**
     * Check if we need to request storage permissions for our use case
     * With scoped storage, many operations don't require permissions
     */
    fun needsStoragePermissions(context: Context, operationType: StorageOperationType): Boolean {
        return when (operationType) {
            StorageOperationType.PRIVATE_APP_FILES -> false // No permissions needed
            StorageOperationType.PUBLIC_DOCUMENTS -> {
                // Only need permissions for reading existing files on Android 10+
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !hasStoragePermissions(context)
            }
            StorageOperationType.MEDIA_FILES -> hasStoragePermissions(context).not()
        }
    }
    
    /**
     * Types of storage operations
     */
    enum class StorageOperationType {
        PRIVATE_APP_FILES,  // App's private external storage (no permissions needed)
        PUBLIC_DOCUMENTS,   // Public documents folder (limited permissions needed)
        MEDIA_FILES        // Media files (requires media permissions)
    }
    
    /**
     * Get permissions that need to be requested
     */
    fun getPermissionsToRequest(context: Context): Array<String> {
        val requiredPermissions = getRequiredStoragePermissions()
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
    
    /**
     * Check if we should show rationale for storage permissions
     */
    fun shouldShowStoragePermissionRationale(context: Context): Boolean {
        if (context !is androidx.activity.ComponentActivity) return false
        
        val requiredPermissions = getRequiredStoragePermissions()
        return requiredPermissions.any { permission ->
            context.shouldShowRequestPermissionRationale(permission)
        }
    }
}
