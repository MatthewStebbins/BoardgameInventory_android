package com.boardgameinventory.utils

import android.Manifest
import android.os.Build

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

}
