package com.boardgameinventory

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the secure API key manager
        initializeApiKeys()

        // Initialize API client with application context
        initializeApiClient()

        // Initialize AdMob securely
        initializeSecureAdMob()
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
     * Initialize AdMob with secure App ID
     * This overrides the placeholder value in the manifest
     */
    private fun initializeSecureAdMob() {
        try {
            // Get the secure AdMob App ID
            val secureAdMobAppId = SecureApiKeyManager.getInstance(this).getAdMobAppId()

            if (secureAdMobAppId.isNotEmpty() &&
                !secureAdMobAppId.startsWith("ca-app-pub-0000000000000000")) {

                // For Android 12+ (API 31+), use the new method to set the App ID
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAdMobAppIdApi31Plus(secureAdMobAppId)
                } else {
                    // For older Android versions, set it through the manifest metadata
                    // (already set as placeholder, will be used from BuildConfig)
                }

                // Initialize AdMob SDK
                MobileAds.initialize(this) { initializationStatus ->
                    val statusMap = initializationStatus.adapterStatusMap
                    for ((adapter, status) in statusMap) {
                        Log.d(TAG, "AdMob Adapter: $adapter - ${status.initializationState}")
                    }
                    Log.d(TAG, "AdMob initialized successfully")
                }

                // Set up ad request configuration (optional)
                val requestConfiguration = RequestConfiguration.Builder()
                    .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                    .build()
                MobileAds.setRequestConfiguration(requestConfiguration)
            } else {
                Log.w(TAG, "AdMob App ID not properly configured, using test ID")
                // Initialize with default/test ID from manifest
                MobileAds.initialize(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdMob", e)
            // Fallback to standard initialization with manifest value
            MobileAds.initialize(this)
        }
    }

    /**
     * Special handling for Android 12+ to set AdMob App ID programmatically
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun setAdMobAppIdApi31Plus(appId: String) {
        try {
            // On Android 12+, need to use reflection to update the App ID
            val applicationClass = Class.forName("android.app.Application")
            val metaDataField = applicationClass.getDeclaredField("mApplicationInfo")
            metaDataField.isAccessible = true

            val applicationInfo = metaDataField.get(this)
            val metaDataField2 = applicationInfo.javaClass.getDeclaredField("metaData")
            metaDataField2.isAccessible = true

            val metaData = metaDataField2.get(applicationInfo)
            if (metaData != null) {
                val metaDataClass = Class.forName("android.os.Bundle")
                val putStringMethod = metaDataClass.getDeclaredMethod("putString", String::class.java, String::class.java)
                putStringMethod.invoke(metaData, "com.google.android.gms.ads.APPLICATION_ID", appId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set AdMob App ID via reflection: ${e.message}", e)
        }
    }
}
