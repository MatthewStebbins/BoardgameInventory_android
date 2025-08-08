package com.boardgameinventory.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.boardgameinventory.BoardGameInventoryApp
import com.boardgameinventory.R

/**
 * Activity for app settings and information including privacy policy and ad consent
 */
class SettingsActivity : BaseAdActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // ConsentManager is already initialized in the BaseAdActivity.onCreate()
        // No need to re-initialize it here

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        // Setup privacy policy button
        val privacyPolicyButton = findViewById<Button>(R.id.privacy_policy_button)
        privacyPolicyButton.setOnClickListener {
            // Show privacy policy in a dialog
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        // Setup ad consent button
        val adConsentButton = findViewById<Button>(R.id.ad_consent_button)
        adConsentButton.setOnClickListener {
            // Show the consent form to let user review/update choices
            showConsentForm()
        }

        // Manually setup ads
        setupAdsManually()
    }

    private fun setupAdsManually() {
        try {
            // Find the AdView directly using findViewById since this activity doesn't use view binding
            val localAdView = findViewById<com.google.android.gms.ads.AdView>(R.id.adView)

            // Set the class-level adView property
            adView = localAdView

            if (localAdView != null) {
                // Set up the ad container
                val adContainer = findViewById<android.view.ViewGroup>(R.id.adContainer)

                // Configure the listener
                localAdView.adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdLoaded() {
                        android.util.Log.d("SettingsActivity", "Ad loaded successfully")
                    }

                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        android.util.Log.e("SettingsActivity", "Ad failed to load: ${error.message}")
                    }
                }

                // Load the ad
                com.boardgameinventory.utils.AdManager.loadAd(localAdView)
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error in ad setup: ${e.message}", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
