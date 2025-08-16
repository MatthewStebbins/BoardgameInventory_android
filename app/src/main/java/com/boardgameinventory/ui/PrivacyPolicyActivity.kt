package com.boardgameinventory.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityPrivacyPolicyBinding
import com.boardgameinventory.utils.TextDarknessManager
import com.google.android.material.elevation.SurfaceColors

/**
 * Activity for displaying the privacy policy
 * Uses WebView to render HTML content from assets folder
 */
class PrivacyPolicyActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Use view binding
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Apply text darkness setting
        TextDarknessManager.applyTextDarknessToActivity(this)

        // Setup toolbar with themed colors
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = getString(R.string.privacy_policy)
            
            // Use Material 3 themed surface colors
            val color = SurfaceColors.SURFACE_2.getColor(this@PrivacyPolicyActivity)
            binding.toolbar.setBackgroundColor(color)
        }

        setupWebView()
    }
    
    private fun setupWebView() {
        // Show loading indicator if available
        binding.progressIndicator?.visibility = View.VISIBLE

        binding.privacyPolicyWebview.apply {
            // Create custom WebViewClient for error handling and loading behavior
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    // Hide progress indicator when loading is complete
                    binding.progressIndicator?.visibility = View.GONE
                }
                
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    // Show error message if privacy policy fails to load
                    Toast.makeText(
                        this@PrivacyPolicyActivity,
                        "Error loading privacy policy. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressIndicator?.visibility = View.GONE
                }
            }
            
            // Configure WebView settings for optimal reading experience
            settings.apply {
                // JavaScript is disabled for security (not needed for static HTML content)
                javaScriptEnabled = false
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
            }
            
            // Load the privacy policy HTML from assets
            loadUrl("file:///android_asset/privacy_policy.html")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle back button in action bar
        if (item.itemId == android.R.id.home) {
            // Use the modern approach instead of deprecated onBackPressed()
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
