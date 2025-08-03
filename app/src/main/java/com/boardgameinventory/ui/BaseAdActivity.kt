package com.boardgameinventory.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.utils.AdManager
import com.google.android.gms.ads.AdView

/**
 * Base activity class that provides common ad management functionality
 * All activities that display ads should extend this class
 */
abstract class BaseAdActivity : AppCompatActivity() {
    
    protected var adView: AdView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAds()
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
     * Subclasses can override to customize ad behavior
     */
    protected open fun setupAds() {
        // Initialize AdMob if not already done
        AdManager.initialize(this)
        
        // Only show ads if the feature is enabled
        if (AdManager.shouldShowAds()) {
            initializeAdView()
        } else {
            hideAdContainer()
        }
    }
    
    /**
     * Initialize the AdView
     * Subclasses can override to customize ad loading
     */
    protected open fun initializeAdView() {
        try {
            val adViewId = resources.getIdentifier("adView", "id", packageName)
            adView = findViewById(adViewId)
            
            if (adView != null) {
                AdManager.loadAd(adView!!)
            }
        } catch (e: Exception) {
            android.util.Log.w(javaClass.simpleName, "AdView not found: ${e.message}")
        }
    }
    
    /**
     * Hide the ad container
     */
    protected open fun hideAdContainer() {
        try {
            val adContainerId = resources.getIdentifier("adContainer", "id", packageName)
            findViewById<android.view.View>(adContainerId)?.visibility = android.view.View.GONE
        } catch (e: Exception) {
            android.util.Log.w(javaClass.simpleName, "Ad container not found: ${e.message}")
        }
    }
    
    /**
     * Refresh the ad (useful for activities that stay open for long periods)
     */
    protected fun refreshAd() {
        adView?.let { AdManager.loadAd(it) }
    }
    
    /**
     * Check if ads are currently visible
     */
    protected fun areAdsVisible(): Boolean {
        return adView?.visibility == android.view.View.VISIBLE
    }
}
