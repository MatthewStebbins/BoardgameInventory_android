package com.boardgameinventory

import android.app.Application
import android.util.Log
import com.boardgameinventory.api.ApiClient
import com.boardgameinventory.utils.SecureApiKeyManager
import com.google.android.gms.ads.MobileAds

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

        // Initialize AdMob
        initializeAdMob()
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

    private fun initializeAdMob() {
        try {
            MobileAds.initialize(this)
            Log.d(TAG, "AdMob initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdMob", e)
        }
    }
}
