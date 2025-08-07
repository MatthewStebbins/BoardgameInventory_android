package com.boardgameinventory.utils

import android.content.Context
import android.util.Log
import com.boardgameinventory.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdListener

/**
 * Helper class for loading ads securely using encrypted AdMob IDs
 */
object AdHelper {
    private const val TAG = "AdHelper"

    // Test ad units for debug builds
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    /**
     * Load an ad securely using the encrypted banner ID
     * Automatically uses test ads in debug builds
     *
     * @param context The context used to get the secure API key manager
     * @param adView The AdView to load the ad into
     * @param adIdentifier Optional identifier for logging (e.g. "MainActivity")
     */
    fun loadAd(context: Context, adView: AdView, adIdentifier: String = "") {
        try {
            // Determine which ad unit ID to use
            val adUnitId = if (BuildConfig.DEBUG) {
                // Use test ad unit ID in debug builds
                Log.d(TAG, "Debug build detected - using test ad unit for $adIdentifier")
                TEST_BANNER_AD_UNIT_ID
            } else {
                // Get the secure banner ID for production builds
                val secureBannerId = SecureApiKeyManager.getInstance(context).getAdMobBannerId()

                if (secureBannerId.isNotEmpty() &&
                    !secureBannerId.startsWith("ca-app-pub-0000000000000000")) {
                    secureBannerId
                } else {
                    // Fallback to test ID if secure ID is not properly configured
                    Log.w(TAG, "Secure banner ID not properly configured - using test ad unit for $adIdentifier")
                    TEST_BANNER_AD_UNIT_ID
                }
            }

            // Set the ad unit ID programmatically
            adView.adUnitId = adUnitId

            // Create ad request
            val adRequest = AdRequest.Builder().build()

            // Set up listener for logging
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "Ad loaded successfully in $adIdentifier")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Ad failed to load in $adIdentifier: ${error.message} (code: ${error.code})")
                }
            }

            // Load the ad
            adView.loadAd(adRequest)
            Log.d(TAG, "Requested ad load for $adIdentifier with ad unit ID: $adUnitId")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ad for $adIdentifier: ${e.message}", e)
            // Try to load with default ID from layout as fallback
            try {
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback ad load also failed: ${e2.message}", e2)
            }
        }
    }
}
