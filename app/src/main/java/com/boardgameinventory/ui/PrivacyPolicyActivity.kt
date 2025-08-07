package com.boardgameinventory.ui

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.boardgameinventory.R

/**
 * Activity for displaying the privacy policy
 */
class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Privacy Policy"

        // Setup WebView to load privacy policy
        val webView = findViewById<WebView>(R.id.privacy_policy_webview)
        webView.webViewClient = WebViewClient() // Ensures links open in the WebView

        // Enable JavaScript and zooming for better readability
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        // Load the privacy policy
        webView.loadUrl("file:///android_asset/privacy_policy.html")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
