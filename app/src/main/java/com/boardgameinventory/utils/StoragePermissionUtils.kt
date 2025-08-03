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
     */
    fun getRequiredStoragePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ (API 33+) - Use granular media permissions
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11-12 (API 30-32) - Use legacy READ_EXTERNAL_STORAGE
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                // Android 10 and below (API 29-) - Use legacy permissions
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
