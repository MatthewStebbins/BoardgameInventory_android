package com.boardgameinventory

import android.app.Application
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
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the secure API key manager
        initializeApiKeys()

        // Initialize API client with application context
        initializeApiClient()

        // Initialize consent and ad management
        initializeAdConsent()
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
     * Legacy method maintained for compatibility - now handled by ConsentManager
     * @deprecated Use ConsentManager for ad initialization
     */
    @Deprecated("Use ConsentManager for ad initialization with proper consent flows")
    private fun initializeSecureAdMob() {
        // This functionality is now handled by the ConsentManager
        Log.d(TAG, "initializeSecureAdMob called (deprecated)")
    }
}
