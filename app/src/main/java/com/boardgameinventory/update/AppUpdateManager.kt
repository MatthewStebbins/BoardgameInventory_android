package com.boardgameinventory.update

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.boardgameinventory.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages in-app updates using the Play Core library.
 * Implements DefaultLifecycleObserver to automatically manage update state with activity lifecycle.
 */
class AppUpdateManager(private val context: Context) : DefaultLifecycleObserver {
    companion object {
        private const val TAG = "AppUpdateManager"
        const val APP_UPDATE_REQUEST_CODE = 500
        private const val UPDATE_STALENESS_DAYS = 3 // Number of days before considering an update stale
    }

    private val updateManager = AppUpdateManagerFactory.create(context)
    private val updateInProgress = AtomicBoolean(false)

    // Flow to emit update state to observers
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    // Track if a flexible update is in progress and waiting for user confirmation
    private var flexibleUpdateDownloaded = false

    // Install state listener for flexible updates
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                val progress = if (totalBytesToDownload > 0) {
                    (bytesDownloaded * 100 / totalBytesToDownload).toInt()
                } else {
                    0
                }
                _updateState.value = UpdateState.Downloading(progress)
                Log.d(TAG, "Update download progress: $progress%")
            }
            InstallStatus.DOWNLOADED -> {
                flexibleUpdateDownloaded = true
                _updateState.value = UpdateState.Downloaded
                Log.d(TAG, "Update has been downloaded")
            }
            InstallStatus.INSTALLED -> {
                updateInProgress.set(false)
                _updateState.value = UpdateState.Idle
                Log.d(TAG, "Update has been installed")
            }
            InstallStatus.FAILED -> {
                updateInProgress.set(false)
                _updateState.value = UpdateState.Failed("Installation failed")
                Log.e(TAG, "Update installation failed")
            }
            InstallStatus.CANCELED -> {
                updateInProgress.set(false)
                _updateState.value = UpdateState.Failed("Installation canceled")
                Log.w(TAG, "Update installation canceled")
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

    init {
        // Register the listener for install state updates
        updateManager.registerListener(installStateUpdatedListener)
    }

    /**
     * Unregisters the install state listener when no longer needed
     * Should be called when the application is shutting down
     */
    fun cleanup() {
        updateManager.unregisterListener(installStateUpdatedListener)
    }

    /**
     * Check for updates and handle based on update type preference
     */
    fun checkForUpdates(activity: Activity, preferredUpdateType: Int = AppUpdateType.FLEXIBLE) {
        if (updateInProgress.get()) {
            Log.d(TAG, "Update already in progress, skipping check")
            return
        }

        // Start the update flow
        val appUpdateInfoTask = updateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            handleAppUpdateInfo(appUpdateInfo, activity, preferredUpdateType)
        }.addOnFailureListener { e ->
            _updateState.value = UpdateState.Failed(e.message ?: "Update check failed")
            Log.e(TAG, "Failed to check for updates", e)
        }
    }

    /**
     * Handle the app update info received from Google Play
     */
    private fun handleAppUpdateInfo(
        appUpdateInfo: AppUpdateInfo,
        activity: Activity,
        preferredUpdateType: Int
    ) {
        // Check if update is available
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            Log.d(TAG, "Update is available")

            // Check if the update is already downloaded
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateState.value = UpdateState.Downloaded
                return
            }

            // First try the preferred update type
            when (preferredUpdateType) {
                AppUpdateType.IMMEDIATE -> {
                    if (appUpdateInfo.isImmediateUpdateAllowed) {
                        startImmediateUpdate(appUpdateInfo, activity)
                    } else if (appUpdateInfo.isFlexibleUpdateAllowed) {
                        startFlexibleUpdate(appUpdateInfo, activity)
                    }
                }
                AppUpdateType.FLEXIBLE -> {
                    if (appUpdateInfo.isFlexibleUpdateAllowed) {
                        startFlexibleUpdate(appUpdateInfo, activity)
                    } else if (appUpdateInfo.isImmediateUpdateAllowed) {
                        startImmediateUpdate(appUpdateInfo, activity)
                    }
                }
            }
        } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            // If an update is already in progress, resume it
            if (appUpdateInfo.isImmediateUpdateAllowed) {
                resumeUpdate(appUpdateInfo, activity)
            }
        } else {
            _updateState.value = UpdateState.NoUpdateAvailable
            Log.d(TAG, "No update is available")
        }
    }

    /**
     * Start an immediate update that interrupts the user and forces them to update
     * Best for critical updates (security fixes, etc.)
     */
    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) {
        try {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            updateManager.startUpdateFlow(
                appUpdateInfo,
                activity,
                options
            )
            updateInProgress.set(true)
            _updateState.value = UpdateState.InProgress(AppUpdateType.IMMEDIATE)
            Log.d(TAG, "Immediate update started")
        } catch (e: IntentSender.SendIntentException) {
            _updateState.value = UpdateState.Failed("Failed to launch update flow")
            Log.e(TAG, "Error launching immediate update flow", e)
        }
    }

    /**
     * Start a flexible update that allows the user to continue using the app
     * While the update downloads in the background
     */
    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) {
        try {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            updateManager.startUpdateFlow(
                appUpdateInfo,
                activity,
                options
            )
            updateInProgress.set(true)
            _updateState.value = UpdateState.InProgress(AppUpdateType.FLEXIBLE)
            Log.d(TAG, "Flexible update started")
        } catch (e: IntentSender.SendIntentException) {
            _updateState.value = UpdateState.Failed("Failed to launch update flow")
            Log.e(TAG, "Error launching flexible update flow", e)
        }
    }

    /**
     * Resume an update that was already in progress
     */
    private fun resumeUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) {
        try {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            updateManager.startUpdateFlow(
                appUpdateInfo,
                activity,
                options
            )
            updateInProgress.set(true)
            _updateState.value = UpdateState.InProgress(AppUpdateType.IMMEDIATE)
            Log.d(TAG, "Resumed in-progress update")
        } catch (e: IntentSender.SendIntentException) {
            _updateState.value = UpdateState.Failed("Failed to resume update flow")
            Log.e(TAG, "Error resuming update flow", e)
        }
    }

    /**
     * Completes a flexible update that has been downloaded
     * Should be called when user confirms they want to install the update
     */
    fun completeUpdate() {
        if (flexibleUpdateDownloaded) {
            updateManager.completeUpdate()
            flexibleUpdateDownloaded = false
            Log.d(TAG, "Completing flexible update installation")
        }
    }

    /**
     * Checks if a downloaded update is still pending installation
     * Should be called during app startup to catch updates that were
     * downloaded but not installed in a previous session
     */
    fun checkPendingUpdates(activity: Activity) {
        updateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateState.value = UpdateState.Downloaded
                showUpdateSnackbar(activity)
            }
        }
    }

    /**
     * Show a snackbar to prompt the user to complete the update
     */
    private fun showUpdateSnackbar(activity: Activity) {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            R.string.update_ready_to_install,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.restart) {
            completeUpdate()
        }.show()
    }

    /**
     * Lifecycle methods to handle updates across activity lifecycle events
     */
    override fun onResume(owner: LifecycleOwner) {
        if (flexibleUpdateDownloaded) {
            (owner as? Activity)?.let { showUpdateSnackbar(it) }
        }
    }
}

/**
 * Sealed class representing the various states of the update process
 */
sealed class UpdateState {
    object Idle : UpdateState()
    object NoUpdateAvailable : UpdateState()
    data class InProgress(val updateType: Int) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    object Downloaded : UpdateState()
    object UserRejected : UpdateState()
    data class Failed(val reason: String) : UpdateState()
}
