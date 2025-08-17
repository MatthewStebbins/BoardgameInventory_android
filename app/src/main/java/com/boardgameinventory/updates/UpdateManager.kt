package com.boardgameinventory.updates

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.ref.WeakReference

/**
 * Manager class for handling in-app updates using the Play Core library.
 * Implements lifecycle observer to handle update flow during activity lifecycle events.
 */
class UpdateManager(private val activity: Activity) : DefaultLifecycleObserver {
    companion object {
        private const val TAG = "UpdateManager"
        private const val UPDATE_REQUEST_CODE = 500

        @Volatile
        private var instance: WeakReference<UpdateManager>? = null

        /**
         * Gets or creates the singleton instance of UpdateManager
         * @param activity The activity context
         * @return UpdateManager instance
         */
        fun getInstance(activity: Activity): UpdateManager {
            return instance?.get() ?: synchronized(this) {
                instance?.get() ?: UpdateManager(activity).also { instance = WeakReference(it) }
            }
        }
    }

    // The app update manager from Play Core library
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    // State flows to observe update status
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)

    // Install state listener
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        @Suppress("DEPRECATION")
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                // Calculate download progress
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                val progress = if (totalBytesToDownload > 0) {
                    (bytesDownloaded * 100 / totalBytesToDownload).toInt()
                } else {
                    0
                }
                _updateState.value = UpdateState.Downloading(progress)
            }
            InstallStatus.DOWNLOADED -> {
                _updateState.value = UpdateState.Downloaded
                // Prompt user to complete the update
                appUpdateManager.completeUpdate()
            }
            InstallStatus.INSTALLED -> {
                _updateState.value = UpdateState.Completed
                // Clear any saved state after successful installation
                clearUpdateState()
            }
            InstallStatus.FAILED -> {
                _updateState.value = UpdateState.Failed("Update installation failed")
                // Clear update state on failure
                clearUpdateState()
            }

            InstallStatus.CANCELED -> {
                TODO()
            }

            InstallStatus.INSTALLING -> {
                TODO()
            }

            InstallStatus.PENDING -> {
                TODO()
            }

            InstallStatus.REQUIRES_UI_INTENT -> {
                TODO()
            }

            InstallStatus.UNKNOWN -> {
                TODO()
            }
        }
    }

    init {
        // Register install state listener
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    /**
     * Check for app updates when the activity starts
     * Implements the checkForUpdates method from DefaultLifecycleObserver
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        // Check for in-progress updates
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // If update is already downloaded, notify user to complete it
                _updateState.value = UpdateState.Downloaded
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // If an update is in progress, resume it
                try {
                    startImmediateUpdate(appUpdateInfo)
                } catch (e: IntentSender.SendIntentException) {
                    _updateState.value = UpdateState.Failed("Failed to resume update: ${e.message}")
                }
            } else {
                // Otherwise, check if we need to check for updates
                checkForUpdates()
            }
        }
    }

    /**
     * Clean up resources when activity is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // Unregister the listener to prevent memory leaks
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    /**
     * Check if an update is available and start the update flow accordingly
     */
    fun checkForUpdates() {
        _updateState.value = UpdateState.Checking

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    // Update is available - decide which type of update to use
                    val updateType = selectUpdateType(appUpdateInfo)

                    when (updateType) {
                        AppUpdateType.IMMEDIATE -> {
                            startImmediateUpdate(appUpdateInfo)
                        }
                        AppUpdateType.FLEXIBLE -> {
                            startFlexibleUpdate(appUpdateInfo)
                        }
                        else -> {
                            _updateState.value = UpdateState.NotAvailable
                        }
                    }
                }
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                    _updateState.value = UpdateState.NotAvailable
                }
                UpdateAvailability.UNKNOWN -> {
                    _updateState.value = UpdateState.Failed("Unknown update availability status")
                }
                else -> {
                    _updateState.value = UpdateState.NotAvailable
                }
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Update check failed", exception)
            _updateState.value = UpdateState.Failed("Update check failed: ${exception.message}")
        }
    }

    /**
     * Determine whether to use immediate or flexible updates
     * based on update priority and allowed types
     */
    private fun selectUpdateType(appUpdateInfo: AppUpdateInfo): Int {
        // Check update priority - high priority updates should use immediate
        return when {
            // Immediate update is allowed and priority is high (>= 4)
            appUpdateInfo.isImmediateUpdateAllowed &&
                    appUpdateInfo.updatePriority() >= 4 -> AppUpdateType.IMMEDIATE

            // Flexible update is allowed (default for most cases)
            appUpdateInfo.isFlexibleUpdateAllowed -> AppUpdateType.FLEXIBLE

            // Fall back to immediate if that's the only one allowed
            appUpdateInfo.isImmediateUpdateAllowed -> AppUpdateType.IMMEDIATE

            // No update type is allowed
            else -> -1
        }
    }

    /**
     * Start an immediate update flow
     * @param appUpdateInfo The update info to use
     */
    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                UPDATE_REQUEST_CODE
            )
            _updateState.value = UpdateState.InProgress(AppUpdateType.IMMEDIATE)
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Error starting immediate update", e)
            _updateState.value = UpdateState.Failed("Failed to start immediate update: ${e.message}")
        }
    }

    /**
     * Start a flexible update flow
     * @param appUpdateInfo The update info to use
     */
    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                UPDATE_REQUEST_CODE
            )
            _updateState.value = UpdateState.InProgress(AppUpdateType.FLEXIBLE)
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Error starting flexible update", e)
            _updateState.value = UpdateState.Failed("Failed to start flexible update: ${e.message}")
        }
    }

    /**
     * Clear any update state
     */
    private fun clearUpdateState() {
        // Reset to idle state
        _updateState.value = UpdateState.Idle
    }

}

/**
 * Sealed class representing the different states of the update process
 */
sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object NotAvailable : UpdateState()
    data class InProgress(val updateType: Int) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    object Downloaded : UpdateState()
    object Completed : UpdateState()
    data class Failed(val reason: String) : UpdateState()
}
