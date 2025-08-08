package com.boardgameinventory.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
     * Check if a specific permission is granted
     */
    fun hasPermission(context: Context, permissionType: PermissionType): Boolean {
        return when (permissionType) {
            PermissionType.CAMERA -> hasCameraPermission(context)
            PermissionType.STORAGE -> StoragePermissionUtils.hasStoragePermissions(context)
            PermissionType.INTERNET -> hasInternetPermission(context)
        }
    }

    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if internet permission is granted
     * (Note: INTERNET is a normal permission, not dangerous, so it's always granted if declared in manifest)
     */
    fun hasInternetPermission(context: Context): Boolean {
        // INTERNET is not a dangerous permission, so it's automatically granted if declared in manifest
        return true
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
     *
     * Then call:
     * PermissionUtils.requestPermissions(this, PermissionType.CAMERA, requestPermissionLauncher) { allGranted ->
     *     if (allGranted) {
     *         // Do something requiring permission
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
     * Alternative method for requesting permissions in fragments
     */
    fun requestPermissions(
        fragment: Fragment,
        permissionType: PermissionType,
        launcher: ActivityResultLauncher<Array<String>>,
        onResult: (Boolean) -> Unit
    ) {
        val permissions = getPermissionsFor(permissionType)
        val context = fragment.requireContext()

        // If no permissions needed or all already granted
        if (permissions.isEmpty() || permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }) {
            onResult(true)
            return
        }

        // Check if we should show rationale for any permission
        val shouldShowRationale = permissions.any {
            fragment.shouldShowRequestPermissionRationale(it)
        }

        if (shouldShowRationale) {
            // Show explanation before requesting permission
            showPermissionRationaleDialog(
                context,
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
