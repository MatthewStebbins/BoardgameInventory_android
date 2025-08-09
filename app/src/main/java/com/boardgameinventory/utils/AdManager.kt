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
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741" // Alternative test ID
    private const val FALLBACK_TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Original test ID
    private const val PRODUCTION_BANNER_AD_UNIT_ID = "ca-app-pub-YOUR_ACTUAL_ID/YOUR_BANNER_ID"
    
    private var isInitialized = false
    
    /**
     * Initialize AdMob SDK
     * Call this once in Application or MainActivity
     */
    fun initialize(context: Context) {
        Log.d(TAG, "=== AdManager.initialize() called ===")
        if (isInitialized) {
            Log.d(TAG, "AdMob already initialized, skipping...")
            return
        }
        
        Log.d(TAG, "Starting AdMob SDK initialization...")
        Log.d(TAG, "Context: ${context.javaClass.simpleName}")
        Log.d(TAG, "Package name: ${context.packageName}")
        Log.d(TAG, "Is debug build: ${BuildConfig.DEBUG}")
        
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "=== AdMob SDK Initialization Complete ===")
                Log.d(TAG, "Initialization status: $initializationStatus")
                
                val statusMap = initializationStatus.adapterStatusMap
                for ((className, status) in statusMap) {
                    Log.d(TAG, "Adapter $className: ${status.initializationState} - ${status.description}")
                }
                
                isInitialized = true
                Log.d(TAG, "AdMob initialization flag set to true")
            }
            
            // Configure test devices for development
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Debug build detected - configuring test devices...")
                
                // Get the actual device ID for this emulator
                val androidId = android.provider.Settings.Secure.getString(
                    context.contentResolver, 
                    android.provider.Settings.Secure.ANDROID_ID
                )
                Log.d(TAG, "Current device Android ID: $androidId")
                
                // Use both the hardcoded ID and the current device ID
                val testDeviceIds = listOf(
                    "33BE2250B43518CCDA7DE426D04EE231", // Original test ID
                    androidId, // Current emulator ID
                    "TEST_DEVICE_HASHED_ID" // AdMob test device placeholder
                )
                val configuration = RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
                MobileAds.setRequestConfiguration(configuration)
                Log.d(TAG, "Test device configuration applied with IDs: $testDeviceIds")
                
                // Also set debug geography for testing
                MobileAds.setRequestConfiguration(
                    RequestConfiguration.Builder()
                        .setTestDeviceIds(testDeviceIds)
                        .build()
                )
                Log.d(TAG, "Request configuration set")
            }
            
            Log.d(TAG, "AdMob initialization request completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during AdMob initialization: ${e.message}", e)
        }
    }
    
    /**
     * Create and configure a banner ad view
     */
    fun createBannerAd(context: Context): AdView {
        val adView = AdView(context)
        adView.setAdSize(AdSize.BANNER)
        val adUnitId = if (BuildConfig.DEBUG) {
            TEST_BANNER_AD_UNIT_ID
        } else {
            PRODUCTION_BANNER_AD_UNIT_ID
        }
        adView.adUnitId = adUnitId

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
        Log.d(TAG, "AdManager.loadAd() called")
        Log.d(TAG, "AdView details: adUnitId=${adView.adUnitId}, adSize=${adView.adSize}")
        Log.d(TAG, "Network state: ${checkNetworkConnection(adView.context)}")
        
        if (!isInitialized) {
            Log.w(TAG, "AdMob not initialized. Initializing now...")
            initialize(adView.context)
            // Wait a bit for initialization to complete
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                loadAdInternal(adView)
            }, 1000)
        } else {
            loadAdInternal(adView)
        }
    }
    
    private fun loadAdInternal(adView: AdView) {
        Log.d(TAG, "=== loadAdInternal() called ===")
        Log.d(TAG, "AdView details:")
        Log.d(TAG, "  - adUnitId: ${adView.adUnitId}")
        Log.d(TAG, "  - adSize: ${adView.adSize}")
        Log.d(TAG, "  - visibility: ${adView.visibility}")
        Log.d(TAG, "  - width: ${adView.width}, height: ${adView.height}")
        
        // Check Google Play Services availability
        try {
            val googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(adView.context)
            Log.d(TAG, "Google Play Services availability: $resultCode")
            
            when (resultCode) {
                com.google.android.gms.common.ConnectionResult.SUCCESS -> {
                    Log.d(TAG, "Google Play Services are available ✓")
                }
                com.google.android.gms.common.ConnectionResult.SERVICE_MISSING -> {
                    Log.e(TAG, "Google Play Services are MISSING from this device/emulator!")
                    Log.e(TAG, "AdMob will not work without Google Play Services")
                    return
                }
                com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                    Log.e(TAG, "Google Play Services UPDATE REQUIRED")
                    Log.e(TAG, "Current version is too old for AdMob")
                    return
                }
                com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED -> {
                    Log.e(TAG, "Google Play Services are DISABLED on this device")
                    return
                }
                else -> {
                    Log.e(TAG, "Google Play Services NOT available! Error code: $resultCode")
                    Log.e(TAG, "Error description: ${googleApiAvailability.getErrorString(resultCode)}")
                    return
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Google Play Services: ${e.message}", e)
            return
        }
        
        Log.d(TAG, "Building ad request...")
        val adRequest = AdRequest.Builder().build()
        Log.d(TAG, "Ad request created: $adRequest")
        
        try {
            Log.d(TAG, "Calling adView.loadAd()...")
            adView.loadAd(adRequest)
            Log.d(TAG, "adView.loadAd() call completed - waiting for callback...")
        } catch (e: Exception) {
            Log.e(TAG, "Exception during adView.loadAd(): ${e.message}", e)
        }
    }
    
    private fun checkNetworkConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        Log.d(TAG, "Network connected: $isConnected")
        return isConnected
    }
    
    /**
     * Create a smart banner ad that adapts to screen size
     */
    fun createSmartBannerAd(context: Context): AdView {
        val adView = AdView(context)
        adView.setAdSize(AdSize.BANNER)
        val adUnitId = if (BuildConfig.DEBUG) {
            TEST_BANNER_AD_UNIT_ID
        } else {
            PRODUCTION_BANNER_AD_UNIT_ID
        }
        adView.adUnitId = adUnitId

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
