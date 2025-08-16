package com.boardgameinventory.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.boardgameinventory.BuildConfig
import com.boardgameinventory.R
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.*
import com.google.android.ump.ConsentDebugSettings.DebugGeography
import com.google.android.ump.ConsentInformation.ConsentStatus
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Consent manager for handling GDPR, CCPA, and other privacy regulations
 * using Google's User Messaging Platform (UMP) SDK.
 *
 * This class manages user consent for personalized ads and ensures
 * compliance with privacy regulations like GDPR and CCPA.
 */
class ConsentManager(private val context: Context) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "ConsentManager"

        // Debug settings - REMOVE IN PRODUCTION
        private const val DEBUG_GEOGRAPHY = DebugGeography.DEBUG_GEOGRAPHY_EEA
        private const val RESET_ON_LAUNCH = false

        // Test device hash - REPLACE WITH YOUR TEST DEVICE HASH IN DEBUG BUILDS
        private val TEST_DEVICE_HASH_IDS = listOf(
            "TEST-DEVICE-HASH" // Replace with your test device hash
        )

        // Singleton instance
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: ConsentManager? = null

        fun getInstance(context: Context): ConsentManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConsentManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Consent information client
    private lateinit var consentInformation: ConsentInformation

    // Consent form
    private var consentForm: ConsentForm? = null

    // State flow to observe consent status changes
    private val _consentStatusFlow = MutableStateFlow(ConsentStatus.UNKNOWN)

    // State flow to observe if consent is required
    private val _consentRequired = MutableStateFlow(true)

    /**
     * Initialize the consent manager
     */
    fun initialize() {
        // Initialize consent information client
        consentInformation = UserMessagingPlatform.getConsentInformation(context)

        // Apply debug settings in debug builds
        if (BuildConfig.DEBUG) {
            setupDebugSettings()
        }

        // Check consent status
        checkConsentStatus()
    }

    /**
     * Check if user consent is required and load the consent form if needed
     */
    fun checkConsentStatus() {
        // Set consent request parameters
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)

        // Add debug settings in debug builds
        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(DEBUG_GEOGRAPHY)

            // Add test device hash IDs
            TEST_DEVICE_HASH_IDS.forEach { hash ->
                debugSettings.addTestDeviceHashedId(hash)
            }

            params.setConsentDebugSettings(debugSettings.build())
        }

        // Request consent information update
        consentInformation.requestConsentInfoUpdate(
            context as Activity,
            params.build(),
            { // Consent information updated successfully
                _consentStatusFlow.value = consentInformation.consentStatus
                _consentRequired.value = consentInformation.isConsentFormAvailable

                // Load consent form if required
                if (consentInformation.isConsentFormAvailable) {
                    loadConsentForm()
                } else {
                    // Consent not required, initialize MobileAds
                    initializeMobileAds()
                }

                Log.d(TAG, "Consent status: ${consentInformation.consentStatus}")
                Log.d(TAG, "Consent form available: ${consentInformation.isConsentFormAvailable}")
            },
            { error -> // Consent information update failed
                Log.e(TAG, "Error requesting consent info: ${error.message}")
                // Initialize MobileAds even if consent check fails to prevent app from being unusable
                initializeMobileAds()
            }
        )
    }

    /**
     * Load the consent form
     */
    private fun loadConsentForm() {
        UserMessagingPlatform.loadConsentForm(
            context,
            { form -> // Form loaded successfully
                consentForm = form
                if (consentInformation.consentStatus == ConsentStatus.REQUIRED) {
                    showConsentForm()
                }
            },
            { error -> // Error loading form
                Log.e(TAG, "Error loading consent form: ${error.message}")
                // Initialize MobileAds even if consent form fails to load
                initializeMobileAds()
            }
        )
    }

    /**
     * Show the consent form to the user
     */
    fun showConsentForm() {
        if (consentForm != null) {
            consentForm?.show(context as Activity) { formError ->
                if (formError != null) {
                    Log.e(TAG, "Error showing consent form: ${formError.message}")
                }

                // Update consent status
                _consentStatusFlow.value = consentInformation.consentStatus

                // Initialize MobileAds after showing form
                initializeMobileAds()

                Log.d(TAG, "Consent form closed. New consent status: ${consentInformation.consentStatus}")
            }
        } else {
            // Show a user-friendly message if the consent form is not available
            android.widget.Toast.makeText(context, context.getString(R.string.ad_consent_form_unavailable), android.widget.Toast.LENGTH_LONG).show()
            Log.w(TAG, "Consent form is not available to show.")
        }
    }

    /**
     * Initialize MobileAds after consent flow
     */
    private fun initializeMobileAds() {
        try {
            // Set real Ad ID programmatically for release builds
            if (!BuildConfig.DEBUG) {
                MobileAds.initialize(context) {
                    Log.d(TAG, "MobileAds initialized successfully")
                }
            } else {
                // Use test Ad ID for debug builds
                MobileAds.initialize(context) {
                    Log.d(TAG, "MobileAds initialized with test ID")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds: ${e.message}", e)
        }
    }

    /**
     * Reset consent for testing purposes (DEBUG ONLY)
     */
    fun resetConsent() {
        if (BuildConfig.DEBUG) {
            consentInformation.reset()
            Log.d(TAG, "Consent reset (DEBUG ONLY)")
        }
    }

    /**
     * Setup debug settings
     */
    private fun setupDebugSettings() {
        if (RESET_ON_LAUNCH) {
            resetConsent()
        }
    }

    // Lifecycle methods

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        initialize()
    }
}
