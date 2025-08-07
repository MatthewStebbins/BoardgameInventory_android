package com.boardgameinventory.ads

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.boardgameinventory.BuildConfig
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

/**
 * Ad manager for handling all AdMob-related functionality.
 * Ensures compliance with AdMob policies and proper ad handling.
 */
class AdManager(private val context: Context) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AdManager"

        // Test ad IDs - USED ONLY IN DEBUG BUILDS
        private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

        // Real ad IDs from BuildConfig (loaded from local.properties)
        private val REAL_BANNER_AD_UNIT_ID = BuildConfig.ADMOB_BANNER_ID

        // Singleton instance
        @Volatile
        private var INSTANCE: AdManager? = null

        fun getInstance(context: Context): AdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Get the appropriate ad unit ID based on build type and ad format
         */
        fun getAdUnitId(adFormat: AdFormat): String {
            return if (BuildConfig.DEBUG) {
                when (adFormat) {
                    AdFormat.BANNER -> TEST_BANNER_AD_UNIT_ID
                    // Add other ad formats as needed
                }
            } else {
                when (adFormat) {
                    AdFormat.BANNER -> REAL_BANNER_AD_UNIT_ID
                    // Add other ad formats as needed
                }
            }
        }

        /**
         * Helper method to load banner ad
         */
        fun loadBannerAd(adView: AdView, consentManager: ConsentManager) {
            if (adView.adUnitId.isNullOrEmpty()) {
                adView.adUnitId = getAdUnitId(AdFormat.BANNER)
            }

            try {
                // Create ad request
                val adRequest = createAdRequest(consentManager)

                // Set ad listener for better error handling
                adView.adListener = createAdListener(adView.id.toString())

                // Load the ad
                adView.loadAd(adRequest)
                Log.d(TAG, "Loading banner ad with ID: ${adView.adUnitId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading banner ad: ${e.message}", e)
            }
        }

        /**
         * Create ad request with proper consent settings
         */
        private fun createAdRequest(consentManager: ConsentManager): AdRequest {
            return AdRequest.Builder().apply {
                // Apply non-personalized ads if user didn't consent to personalized ads
                if (!consentManager.canShowPersonalizedAds()) {
                    val extras = Bundle().apply {
                        putString("npa", "1")  // Non-personalized ads flag
                    }
                    addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                    Log.d(TAG, "Requesting non-personalized ads")
                } else {
                    Log.d(TAG, "Requesting personalized ads (user consented)")
                }
            }.build()
        }

        /**
         * Create ad listener for better logging and error handling
         */
        private fun createAdListener(adIdentifier: String): AdListener {
            return object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "Ad loaded successfully: $adIdentifier")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: $adIdentifier - Error: ${error.message} (code: ${error.code})")
                }

                override fun onAdOpened() {
                    Log.d(TAG, "Ad opened: $adIdentifier")
                }

                override fun onAdClosed() {
                    Log.d(TAG, "Ad closed: $adIdentifier")
                }

                override fun onAdImpression() {
                    Log.d(TAG, "Ad impression recorded: $adIdentifier")
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Ad clicked: $adIdentifier")
                }
            }
        }

        /**
         * Helper method to handle AdView lifecycle
         */
        fun resumeAd(adView: AdView?) {
            adView?.resume()
        }

        fun pauseAd(adView: AdView?) {
            adView?.pause()
        }

        fun destroyAd(adView: AdView?) {
            adView?.destroy()
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

    /**
     * Load banner ad into the provided container
     */
    fun loadBannerAd(adContainer: ViewGroup, adView: AdView?) {
        if (adView == null) {
            Log.e(TAG, "AdView is null, cannot load ad")
            return
        }

        if (!::consentManager.isInitialized) {
            Log.e(TAG, "ConsentManager not initialized, deferring ad load")
            return
        }

        // Check if we can show ads based on consent
        if (!consentManager.canShowAds()) {
            Log.d(TAG, "Cannot show ads - user consent required but not provided")
            // Hide ad container completely
            adContainer.visibility = android.view.View.GONE
            return
        }

        // Ensure ad container is visible
        adContainer.visibility = android.view.View.VISIBLE

        // Configure ad view
        adView.apply {
            // Set ad unit ID if not already set
            if (adUnitId.isNullOrEmpty()) {
                adUnitId = getAdUnitId(AdFormat.BANNER)
            }

            // Set ad size
            setAdSize(AdSize.BANNER)

            // Set layout parameters if not already set
            if (layoutParams == null) {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Ensure the AdView is in the container
            if (parent == null) {
                adContainer.addView(this)
            }

            // Load ad with consent settings
            loadBannerAd(this, consentManager)
        }
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

/**
 * Enum defining supported ad formats
 */
enum class AdFormat {
    BANNER
    // Add other ad formats as needed (INTERSTITIAL, REWARDED, etc.)
}
