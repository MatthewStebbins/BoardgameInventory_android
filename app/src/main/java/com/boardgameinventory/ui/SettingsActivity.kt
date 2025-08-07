package com.boardgameinventory.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.boardgameinventory.BoardGameInventoryApp
import com.boardgameinventory.R
import com.boardgameinventory.ads.ConsentManager

/**
 * Activity for app settings and information including privacy policy and ad consent
 */
class SettingsActivity : BaseAdActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Get reference to ConsentManager
        consentManager = BoardGameInventoryApp.consentManager

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
            consentManager.showConsentForm()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
