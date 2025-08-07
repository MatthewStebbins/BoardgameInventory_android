package com.boardgameinventory.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.BoardGameInventoryApp
import com.boardgameinventory.ads.AdManager
import com.boardgameinventory.ads.ConsentManager
import com.google.android.gms.ads.AdView

/**
 * Base activity class that provides common ad management functionality with consent flows
 * All activities that display ads should extend this class
 */
abstract class BaseAdActivity : AppCompatActivity() {
    
    protected var adView: AdView? = null

    // References to the consent and ad managers
    protected lateinit var consentManager: ConsentManager
    protected lateinit var adManager: AdManager

    /**
     * Helper function for activities using data binding to setup ads securely
     * Call this from onCreate() after setContentView()
     */
    protected fun setupAdsWithBinding(
        adContainer: ViewGroup,
        adView: AdView,
        activityName: String
    ) {
        try {
            Log.d(activityName, "Setting up ads with consent-based configuration")

            // Store reference to the AdView
            this.adView = adView

            // Get references to the consent and ad managers
            consentManager = BoardGameInventoryApp.consentManager
            adManager = BoardGameInventoryApp.adManager

            // Only show ads if consent requirements are met
            if (consentManager.canShowAds()) {
                // Load banner ad with proper consent settings
                adManager.loadBannerAd(adContainer, adView)
            } else {
                // Hide ad container if consent requirements aren't met
                adContainer.visibility = View.GONE
                Log.d(activityName, "Ads not shown - consent requirements not met")
            }
        } catch (e: Exception) {
            Log.e(activityName, "Error setting up ads: ${e.message}", e)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get references to the consent and ad managers
        consentManager = BoardGameInventoryApp.consentManager
        adManager = BoardGameInventoryApp.adManager

        // Setup ads after content view is set
        // Note: Specific ad setup will be done in the setupAds() method
    }
    
    override fun onResume() {
        super.onResume()
        AdManager.resumeAd(adView)
    }
    
    override fun onPause() {
        super.onPause()
        AdManager.pauseAd(adView)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        AdManager.destroyAd(adView)
    }
    
    /**
     * Setup ads for this activity
     * This method should be called after setContentView() in activities
     */
    protected open fun setupAds() {
        // The implementation will depend on the specific activity
        // Each activity should find its own adContainer and adView
        Log.d(javaClass.simpleName, "Base setupAds() called - override in activity")
    }
    
    /**
     * Shows the consent form if required
     * Can be called from activities to provide an option for users to review consent
     */
    protected fun showConsentForm() {
        if (::consentManager.isInitialized) {
            consentManager.showConsentForm()
        }
    }
}
