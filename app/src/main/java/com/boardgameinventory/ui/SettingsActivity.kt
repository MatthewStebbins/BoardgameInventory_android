package com.boardgameinventory.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
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

        // Text darkness slider
        val textDarknessSlider = findViewById<SeekBar>(R.id.text_darkness_slider)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Load saved value
        val savedDarkness = sharedPreferences.getInt("text_darkness", 50)
        textDarknessSlider.progress = savedDarkness

        // Save value on change
        textDarknessSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val editor = sharedPreferences.edit()
                editor.putInt("text_darkness", progress)
                editor.apply()
                applyTextDarkness(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    private fun setupAdsManually() {
        try {
            // Find the AdView directly using findViewById since this activity doesn't use view binding
            val localAdView = findViewById<com.google.android.gms.ads.AdView>(R.id.adView)

            // Set the class-level adView property
            adView = localAdView

            if (localAdView != null) {
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

    private fun applyTextDarkness(darkness: Int) {
        val adjustedColor = (255 - darkness * 2.55).toInt()
        // Replaced `toColorInt` with `Color.parseColor`
        val textColor = Color.parseColor(String.format("#%02X%02X%02X", adjustedColor, adjustedColor, adjustedColor))

        // Fixed `View` and `rootView` references
        findViewById<View>(android.R.id.content).rootView.setBackgroundColor(textColor)
    }

    override fun onResume() {
        super.onResume()
        //applyTextDarkness()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
