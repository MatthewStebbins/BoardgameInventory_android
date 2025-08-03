package com.boardgameinventory.utils

import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.AdListener
import com.boardgameinventory.BuildConfig

/**
 * Utility class for managing AdMob banner ads
 * Provides easy integration for banner ads across the app
 */
object AdManager {
    
    private const val TAG = "AdManager"
    
    // Test ad unit ID for development (replace with real ID for production)
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val PRODUCTION_BANNER_AD_UNIT_ID = "ca-app-pub-YOUR_ACTUAL_ID/YOUR_BANNER_ID"
    
    private var isInitialized = false
    
    /**
     * Initialize AdMob SDK
     * Call this once in Application or MainActivity
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
            isInitialized = true
        }
        
        // Configure test devices for development
        if (BuildConfig.DEBUG) {
            val testDeviceIds = listOf("33BE2250B43518CCDA7DE426D04EE231") // Add your test device ID
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }
    }
    
    /**
     * Create and configure a banner ad view
     */
    fun createBannerAd(context: Context): AdView {
        val adView = AdView(context)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = if (BuildConfig.DEBUG) {
            TEST_BANNER_AD_UNIT_ID
        } else {
            PRODUCTION_BANNER_AD_UNIT_ID
        }
        
        // Set ad listener for debugging and error handling
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Ad failed to load: ${adError.message}")
                // Hide the ad container if ad fails to load
                adView.visibility = View.GONE
            }
            
            override fun onAdLoaded() {
                Log.d(TAG, "Ad loaded successfully")
                adView.visibility = View.VISIBLE
            }
        }
        
        return adView
    }
    
    /**
     * Load an ad into the provided AdView
     */
    fun loadAd(adView: AdView) {
        if (!isInitialized) {
            Log.w(TAG, "AdMob not initialized. Initializing now...")
            initialize(adView.context)
        }
        
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
    
    /**
     * Create a smart banner ad that adapts to screen size
     */
    fun createSmartBannerAd(context: Context): AdView {
        val adView = AdView(context)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = if (BuildConfig.DEBUG) {
            TEST_BANNER_AD_UNIT_ID
        } else {
            PRODUCTION_BANNER_AD_UNIT_ID
        }
        
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Smart banner ad failed to load: ${adError.message}")
                adView.visibility = View.GONE
            }
            
            override fun onAdLoaded() {
                Log.d(TAG, "Smart banner ad loaded successfully")
                adView.visibility = View.VISIBLE
            }
        }
        
        return adView
    }
    
    /**
     * Pause ad when activity is paused
     */
    fun pauseAd(adView: AdView?) {
        adView?.pause()
    }
    
    /**
     * Resume ad when activity is resumed
     */
    fun resumeAd(adView: AdView?) {
        adView?.resume()
    }
    
    /**
     * Destroy ad when activity is destroyed
     */
    fun destroyAd(adView: AdView?) {
        adView?.destroy()
    }
    
    /**
     * Check if ads should be shown (can be used for premium features)
     */
    fun shouldShowAds(): Boolean {
        // In a real app, this might check for premium subscription
        return true
    }
    
    /**
     * Get appropriate ad unit ID based on build type
     */
    fun getBannerAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            TEST_BANNER_AD_UNIT_ID
        } else {
            PRODUCTION_BANNER_AD_UNIT_ID
        }
    }
}
