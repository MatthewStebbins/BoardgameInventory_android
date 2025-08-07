package com.boardgameinventory.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.utils.AdHelper
import com.boardgameinventory.utils.AdManager
import com.google.android.gms.ads.AdView

/**
 * Base activity class that provides common ad management functionality
 * All activities that display ads should extend this class
 */
abstract class BaseAdActivity : AppCompatActivity() {
    
    protected var adView: AdView? = null
    private var hasAdLoaded = false
    
    /**
     * Helper function for activities using data binding to setup ads securely
     * Call this from onCreate() after setContentView()
     */
    protected fun setupAdsWithBinding(
        adContainer: android.view.View,
        adView: com.google.android.gms.ads.AdView,
        activityName: String
    ) {
        try {
            android.util.Log.d(activityName, "Setting up ads securely with test mode detection")
            AdManager.initialize(this)

            adContainer.visibility = android.view.View.VISIBLE

            // Use the new secure AdHelper to load ads with test mode detection
            AdHelper.loadAd(this, adView, activityName)
        } catch (e: Exception) {
            android.util.Log.e(activityName, "Error setting up ads: ${e.message}", e)
        }
    }
    
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
            android.util.Log.d(javaClass.simpleName, "Starting AdView initialization...")
            android.util.Log.d(javaClass.simpleName, "Package name: $packageName")
            
            // Try direct access using known resource ID
            try {
                val adContainerId = com.boardgameinventory.R.id.adContainer
                android.util.Log.d(javaClass.simpleName, "Using direct R.id.adContainer: $adContainerId")
                
                val adContainer = findViewById<android.view.View>(adContainerId)
                android.util.Log.d(javaClass.simpleName, "adContainer found: ${adContainer != null}")
                
                if (adContainer != null) {
                    android.util.Log.d(javaClass.simpleName, "adContainer class: ${adContainer.javaClass.simpleName}")
                    android.util.Log.d(javaClass.simpleName, "adContainer visibility: ${adContainer.visibility}")
                    
                    // Force show the container with a test background
                    adContainer.setBackgroundColor(android.graphics.Color.parseColor("#FF5722")) // Red background
                    adContainer.visibility = android.view.View.VISIBLE
                    android.util.Log.d(javaClass.simpleName, "Set red background on adContainer for testing")
                    
                    // Try to find adView within the container
                    if (adContainer is android.view.ViewGroup) {
                        android.util.Log.d(javaClass.simpleName, "adContainer is ViewGroup with ${adContainer.childCount} children")
                        for (i in 0 until adContainer.childCount) {
                            val child = adContainer.getChildAt(i)
                            android.util.Log.d(javaClass.simpleName, "Child $i: ${child.javaClass.simpleName}")
                            if (child is com.google.android.gms.ads.AdView) {
                                adView = child
                                android.util.Log.d(javaClass.simpleName, "Found AdView as child!")
                                break
                            }
                        }
                    }
                    
                    // Also try to find AdView directly
                    if (adView == null) {
                        val adViewId = com.boardgameinventory.R.id.adView
                        adView = findViewById(adViewId)
                        android.util.Log.d(javaClass.simpleName, "Direct adView lookup result: ${adView != null}")
                    }
                } else {
                    android.util.Log.e(javaClass.simpleName, "adContainer not found - layout missing or not inflated")
                    
                    // Create a debug view to show the issue
                    val testView = android.widget.TextView(this)
                    testView.text = "AD CONTAINER MISSING - Check layout"
                    testView.setBackgroundColor(android.graphics.Color.parseColor("#FF0000"))
                    testView.setTextColor(android.graphics.Color.WHITE)
                    testView.gravity = android.view.Gravity.CENTER
                    testView.layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        100
                    )
                    
                    // Try to add it to the root layout
                    val rootView = findViewById<android.view.ViewGroup>(android.R.id.content)
                    rootView.addView(testView)
                    android.util.Log.d(javaClass.simpleName, "Added debug view for missing container")
                }
            } catch (e: Exception) {
                android.util.Log.e(javaClass.simpleName, "Error finding views: ${e.message}", e)
            }
            
            if (adView != null) {
                android.util.Log.d(javaClass.simpleName, "Found AdView - configuring...")
                
                // Set yellow background for testing
                adView!!.setBackgroundColor(android.graphics.Color.parseColor("#FFEB3B"))
                adView!!.visibility = android.view.View.VISIBLE
                
                // Configure the existing AdView from XML
                configureAdView(adView!!)
                
                // Add a timeout mechanism - if ad doesn't load in 15 seconds, show error
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (!hasAdLoaded) {
                        android.util.Log.w(javaClass.simpleName, "Ad loading timeout - no response after 15 seconds")
                        adView!!.setBackgroundColor(android.graphics.Color.parseColor("#9C27B0")) // Purple for timeout
                        android.util.Log.e(javaClass.simpleName, "This usually indicates Google Play Services or network issues")
                    }
                }, 15000)
                
                // Load the ad
                AdManager.loadAd(adView!!)
                
                android.util.Log.d(javaClass.simpleName, "AdView configured and ad loading started")
            } else {
                android.util.Log.e(javaClass.simpleName, "AdView still not found after all attempts")
            }
        } catch (e: Exception) {
            android.util.Log.e(javaClass.simpleName, "Error setting up AdView: ${e.message}", e)
        }
    }
    
    /**
     * Configure the AdView with proper settings and listeners
     */
    private fun configureAdView(adView: AdView) {
        android.util.Log.d(javaClass.simpleName, "Configuring AdView...")
        android.util.Log.d(javaClass.simpleName, "AdView current state - adUnitId: ${adView.adUnitId}, adSize: ${adView.adSize}")
        
        // Set ad listener for debugging and error handling
        adView.adListener = object : com.google.android.gms.ads.AdListener() {
            override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                android.util.Log.e(javaClass.simpleName, "Ad failed to load!")
                android.util.Log.e(javaClass.simpleName, "Error code: ${adError.code}")
                android.util.Log.e(javaClass.simpleName, "Error message: ${adError.message}")
                android.util.Log.e(javaClass.simpleName, "Error domain: ${adError.domain}")
                android.util.Log.e(javaClass.simpleName, "Error cause: ${adError.cause}")
                
                hasAdLoaded = true // Prevent timeout handler from triggering
                
                // Keep the yellow background to show the ad space but add error text
                adView.setBackgroundColor(android.graphics.Color.parseColor("#FF9800")) // Orange for error
                
                // Try to add error text to the AdView area
                val errorText = "AD LOAD FAILED: ${adError.message}"
                android.util.Log.e(javaClass.simpleName, errorText)
            }
            
            override fun onAdLoaded() {
                android.util.Log.d(javaClass.simpleName, "*** AD LOADED SUCCESSFULLY! ***")
                hasAdLoaded = true
                adView.visibility = android.view.View.VISIBLE
                // Change background to green to indicate success
                adView.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // Green for success
                showAdContainer()
            }
            
            override fun onAdOpened() {
                android.util.Log.d(javaClass.simpleName, "Ad opened")
            }
            
            override fun onAdClicked() {
                android.util.Log.d(javaClass.simpleName, "Ad clicked")
            }
            
            override fun onAdClosed() {
                android.util.Log.d(javaClass.simpleName, "Ad closed")
            }
            
            override fun onAdImpression() {
                android.util.Log.d(javaClass.simpleName, "Ad impression recorded")
            }
        }
        
        android.util.Log.d(javaClass.simpleName, "AdView listener configured")
    }
    
    /**
     * Show the ad container
     */
    protected open fun showAdContainer() {
        try {
            val adContainerId = resources.getIdentifier("adContainer", "id", packageName)
            findViewById<android.view.View>(adContainerId)?.visibility = android.view.View.VISIBLE
        } catch (e: Exception) {
            android.util.Log.w(javaClass.simpleName, "Ad container not found: ${e.message}")
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
        adView?.let { AdHelper.loadAd(this, it, javaClass.simpleName) }
    }
    
    /**
     * Check if ads are currently visible
     */
    protected fun areAdsVisible(): Boolean {
        return adView?.visibility == android.view.View.VISIBLE
    }
}
