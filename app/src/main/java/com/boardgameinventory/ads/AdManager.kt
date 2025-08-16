package com.boardgameinventory.ads

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Ad manager for handling all AdMob-related functionality.
 * Ensures compliance with AdMob policies and proper ad handling.
 */
class AdManager() : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AdManager"

        // Safe getInstance method that doesn't store Context in static field
        fun getInstance(): AdManager {
            return AdManager()
        }

    }

    // Consent manager reference
    private lateinit var consentManager: ConsentManager

    /**
     * Initialize the ad manager with a consent manager
     */
    fun initialize(consentManager: ConsentManager) {
        this.consentManager = consentManager
        Log.d(TAG, "AdManager initialized")
    }

    // Lifecycle methods

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Log.d(TAG, "onResume called")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Log.d(TAG, "onPause called")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d(TAG, "onDestroy called")
    }
}

