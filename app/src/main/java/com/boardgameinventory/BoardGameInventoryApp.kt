package com.boardgameinventory

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ProcessLifecycleOwner
import com.boardgameinventory.ads.AdManager
import com.boardgameinventory.ads.ConsentManager
import com.boardgameinventory.api.ApiClient
import com.boardgameinventory.utils.SecureApiKeyManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

/**
 * Main application class for BoardgameInventory
 * Handles initialization of app-wide components
 */
class BoardGameInventoryApp : Application() {

    companion object {
        private const val TAG = "BoardGameInventoryApp"

        // Store managers as singletons for access throughout the app
        lateinit var consentManager: ConsentManager
            private set

        lateinit var adManager: AdManager
            private set

        // Notification channel constants
        const val CHANNEL_ID_LOANS = "channel_loans"
        const val CHANNEL_ID_UPDATES = "channel_updates"
        const val CHANNEL_ID_IMPORTS = "channel_imports"
        const val CHANNEL_ID_SYNC = "channel_sync"

        const val NOTIFICATION_GROUP_GAME_MANAGEMENT = "group_game_management"
        const val NOTIFICATION_GROUP_DATA = "group_data_operations"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the secure API key manager
        initializeApiKeys()

        // Initialize API client with application context
        initializeApiClient()

        // Initialize consent and ad management
        initializeAdConsent()

        // Create notification channels for Android 8.0+
        createNotificationChannels()
    }

    private fun initializeApiKeys() {
        try {
            // Initialize API keys securely
            SecureApiKeyManager.getInstance(this).initializeApiKeys()
            Log.d(TAG, "API keys initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing API keys", e)
        }
    }

    private fun initializeApiClient() {
        try {
            // Initialize API client with context
            ApiClient.initialize(applicationContext)
            Log.d(TAG, "API client initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing API client", e)
        }
    }

    /**
     * Initialize consent management and AdMob with proper consent flows
     */
    private fun initializeAdConsent() {
        try {
            // Create consent manager instance
            consentManager = ConsentManager.getInstance(this)

            // Create ad manager instance
            adManager = AdManager.getInstance(this)

            // Add lifecycle observers to process lifecycle owner to manage lifecycle events
            ProcessLifecycleOwner.get().lifecycle.addObserver(consentManager)
            ProcessLifecycleOwner.get().lifecycle.addObserver(adManager)

            // Initialize ad manager with consent manager
            adManager.initialize(consentManager)

            Log.d(TAG, "Ad consent management initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ad consent management", e)
        }
    }

    /**
     * Creates notification channels for Android Oreo (API 26) and above
     * This is required for apps targeting Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannelGroups()
            createLoanNotificationChannel()
            createUpdateNotificationChannel()
            createImportExportNotificationChannel()
            createSyncNotificationChannel()

            Log.d(TAG, "Notification channels created successfully")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelGroups() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel groups for better organization
        val gameManagementGroup = NotificationChannelGroup(
            NOTIFICATION_GROUP_GAME_MANAGEMENT,
            getString(R.string.notification_group_game_management)
        )

        val dataOperationsGroup = NotificationChannelGroup(
            NOTIFICATION_GROUP_DATA,
            getString(R.string.notification_group_data_operations)
        )

        notificationManager.createNotificationChannelGroups(listOf(gameManagementGroup, dataOperationsGroup))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createLoanNotificationChannel() {
        val channelName = getString(R.string.channel_loans_name)
        val channelDescription = getString(R.string.channel_loans_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(CHANNEL_ID_LOANS, channelName, importance).apply {
            description = channelDescription
            group = NOTIFICATION_GROUP_GAME_MANAGEMENT
            setShowBadge(true)
            enableLights(true)
            lightColor = Color.YELLOW
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createUpdateNotificationChannel() {
        val channelName = getString(R.string.channel_updates_name)
        val channelDescription = getString(R.string.channel_updates_description)
        val importance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(CHANNEL_ID_UPDATES, channelName, importance).apply {
            description = channelDescription
            group = NOTIFICATION_GROUP_GAME_MANAGEMENT
            setShowBadge(true)
            enableLights(true)
            lightColor = Color.BLUE
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createImportExportNotificationChannel() {
        val channelName = getString(R.string.channel_imports_name)
        val channelDescription = getString(R.string.channel_imports_description)
        val importance = NotificationManager.IMPORTANCE_LOW

        val channel = NotificationChannel(CHANNEL_ID_IMPORTS, channelName, importance).apply {
            description = channelDescription
            group = NOTIFICATION_GROUP_DATA
            setShowBadge(false)
            enableLights(false)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSyncNotificationChannel() {
        val channelName = getString(R.string.channel_sync_name)
        val channelDescription = getString(R.string.channel_sync_description)
        val importance = NotificationManager.IMPORTANCE_LOW

        val channel = NotificationChannel(CHANNEL_ID_SYNC, channelName, importance).apply {
            description = channelDescription
            group = NOTIFICATION_GROUP_DATA
            setShowBadge(false)
            enableLights(false)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Legacy method maintained for compatibility - now handled by ConsentManager
     * @deprecated Use ConsentManager for ad initialization
     */
    @Deprecated("Use ConsentManager for ad initialization with proper consent flows")
    private fun initializeSecureAdMob() {
        // This functionality is now handled by the ConsentManager
        Log.d(TAG, "initializeSecureAdMob called (deprecated)")
    }
}
