package com.boardgameinventory.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.boardgameinventory.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Comprehensive utility for handling all runtime permissions in the app
 * Follows best practices for Android API 33+ (Android 13)
 */
object PermissionUtils {

    /**
     * Permission types handled by this utility
     */
    enum class PermissionType {
        CAMERA,
        STORAGE,
        INTERNET
    }

    /**
     * Get permissions needed for a specific permission type
     */
    fun getPermissionsFor(permissionType: PermissionType): Array<String> {
        return when (permissionType) {
            PermissionType.CAMERA -> arrayOf(Manifest.permission.CAMERA)
            PermissionType.STORAGE -> StoragePermissionUtils.getRequiredStoragePermissions()
            PermissionType.INTERNET -> emptyArray() // Internet doesn't require runtime permission
        }
    }

    /**
     * Request permissions using Activity Result API (recommended approach since Android 11)
     *
     * Usage in Activity/Fragment:
     *
     * private val requestPermissionLauncher = registerForActivityResult(
     *     ActivityResultContracts.RequestMultiplePermissions()
     * ) { permissions ->
     *     // Handle permission results here
     *     if (permissions.all { it.value }) {
     *         // All permissions granted
     *     } else {
     *         // Some permissions denied
     *     }
     * }
     */
    fun requestPermissions(
        activity: Activity,
        permissionType: PermissionType,
        launcher: ActivityResultLauncher<Array<String>>,
        onResult: (Boolean) -> Unit
    ) {
        val permissions = getPermissionsFor(permissionType)

        // If no permissions needed or all already granted
        if (permissions.isEmpty() || permissions.all {
                ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
            }) {
            onResult(true)
            return
        }

        // Check if we should show rationale for any permission
        val shouldShowRationale = permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }

        if (shouldShowRationale) {
            // Show explanation before requesting permission
            showPermissionRationaleDialog(
                activity,
                permissionType,
                onPositive = { launcher.launch(permissions) },
                onNegative = { onResult(false) }
            )
        } else {
            // Request permissions directly
            launcher.launch(permissions)
        }
    }

    /**
     * Show dialog explaining why the permission is needed
     */
    private fun showPermissionRationaleDialog(
        context: Context,
        permissionType: PermissionType,
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        val (title, message) = getPermissionDialogContent(permissionType)

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.grant_permission) { _, _ -> onPositive() }
            .setNegativeButton(R.string.cancel) { _, _ -> onNegative() }
            .setCancelable(false)
            .show()
    }

    /**
     * Show dialog when permission is permanently denied, directing to app settings
     */
    fun showPermissionDeniedDialog(
        context: Context,
        permissionType: PermissionType,
        onDismiss: () -> Unit = {}
    ) {
        val (title, _) = getPermissionDialogContent(permissionType)

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(R.string.permission_denied_message)
            .setPositiveButton(R.string.go_to_settings) { _, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
            .setNegativeButton(R.string.cancel) { _, _ -> onDismiss() }
            .setCancelable(false)
            .show()
    }

    /**
     * Get title and message for permission dialogs based on permission type
     */
    private fun getPermissionDialogContent(permissionType: PermissionType): Pair<Int, Int> {
        return when (permissionType) {
            PermissionType.CAMERA -> Pair(
                R.string.camera_permission_title,
                R.string.camera_permission_rationale
            )
            PermissionType.STORAGE -> Pair(
                R.string.storage_permission_title,
                R.string.storage_permission_rationale
            )
            PermissionType.INTERNET -> Pair(
                R.string.internet_permission_title,
                R.string.internet_permission_rationale
            )
        }
    }
}
