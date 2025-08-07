package com.boardgameinventory

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.boardgameinventory.databinding.ActivityMainBinding
import com.boardgameinventory.ui.*
import com.boardgameinventory.ui.ExportImportActivity
import com.boardgameinventory.updates.UpdateManager
import com.boardgameinventory.updates.UpdateState
import com.boardgameinventory.utils.AdManager
import com.boardgameinventory.utils.DeveloperMode
import com.boardgameinventory.viewmodel.MainViewModel
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : BaseAdActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    // Update manager for handling in-app updates
    private lateinit var updateManager: UpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize the update manager
        updateManager = UpdateManager.getInstance(this)
        lifecycle.addObserver(updateManager)

        setupClickListeners()
        setupVersionInfo()
        observeStats()
        observeUpdateState()

        // Setup ads manually since BaseAdActivity isn't finding the views
        setupAdsManually()
    }
    
    private fun observeUpdateState() {
        lifecycleScope.launch {
            updateManager.updateState.collect { state ->
                when (state) {
                    is UpdateState.Downloaded -> {
                        // Show a snackbar that an update has been downloaded
                        Snackbar.make(
                            binding.root,
                            R.string.update_ready_to_install,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(R.string.restart) {
                            // Complete the update
                            updateManager.completeUpdate()
                        }.show()
                    }
                    is UpdateState.Downloading -> {
                        // Optionally show download progress
                        if (state.progress % 20 == 0) { // Show every 20%
                            Snackbar.make(
                                binding.root,
                                getString(R.string.update_downloading, state.progress),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is UpdateState.Failed -> {
                        // Show error message
                        Snackbar.make(
                            binding.root,
                            getString(R.string.update_failed, state.reason),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    is UpdateState.InProgress -> {
                        // Update in progress, no UI needed as the Play Core handles it
                    }
                    is UpdateState.Completed -> {
                        // Update completed, app will restart automatically
                    }
                    is UpdateState.Canceled -> {
                        // User canceled the update - no action needed
                    }
                    else -> {
                        // Idle, Checking, NotAvailable - no UI action needed
                    }
                }
            }
        }
    }

    // Process activity results, including update flow results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the result to the update manager
        updateManager.onActivityResult(requestCode, resultCode)
    }

    private fun setupAdsManually() {
        try {
            setupAdsWithBinding(binding.adContainer, binding.adView, "MainActivity")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in ad setup: ${e.message}", e)
        }
    }
    
    /**
     * Override to use data binding for ad view access
     */
    override fun initializeAdView() {
        try {
            android.util.Log.d("MainActivity", "Initializing AdView using data binding...")
            
            // Access adContainer through binding
            val adContainer = binding.adContainer
            android.util.Log.d("MainActivity", "adContainer from binding found")
            android.util.Log.d("MainActivity", "adContainer class: ${adContainer.javaClass.simpleName}")
            android.util.Log.d("MainActivity", "adContainer visibility: ${adContainer.visibility}")
            
            // Set test background
            adContainer.setBackgroundColor(android.graphics.Color.parseColor("#FF5722")) // Red background
            adContainer.visibility = android.view.View.VISIBLE
            android.util.Log.d("MainActivity", "Set red background on adContainer")
            
            // Access adView through binding
            adView = binding.adView
            android.util.Log.d("MainActivity", "adView from binding: ${adView != null}")
            
            if (adView != null) {
                android.util.Log.d("MainActivity", "Found AdView through binding - configuring...")
                
                // Set yellow background for testing
                adView!!.setBackgroundColor(android.graphics.Color.parseColor("#FFEB3B"))
                adView!!.visibility = android.view.View.VISIBLE
                
                // Configure the AdView
                configureAdView(adView!!)
                
                // Add timeout mechanism
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (!hasAdLoaded) {
                        android.util.Log.w("MainActivity", "Ad loading timeout - no response after 15 seconds")
                        adView!!.setBackgroundColor(android.graphics.Color.parseColor("#9C27B0")) // Purple for timeout
                    }
                }, 15000)
                
                // Load the ad
                AdManager.loadAd(adView!!)
                android.util.Log.d("MainActivity", "AdView configured and ad loading started")
            } else {
                android.util.Log.e("MainActivity", "adView not found in binding")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in initializeAdView: ${e.message}", e)
            // Fall back to parent implementation
            super.initializeAdView()
        }
    }
    
    private var hasAdLoaded = false
    
    /**
     * Configure the AdView with proper settings and listeners
     */
    private fun configureAdView(adView: com.google.android.gms.ads.AdView) {
        android.util.Log.d("MainActivity", "Configuring AdView...")
        
        // Set ad listener for debugging
        adView.adListener = object : com.google.android.gms.ads.AdListener() {
            override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                android.util.Log.e("MainActivity", "Ad failed to load: ${adError.message}")
                hasAdLoaded = true
                adView.setBackgroundColor(android.graphics.Color.parseColor("#FF9800")) // Orange for error
            }
            
            override fun onAdLoaded() {
                android.util.Log.d("MainActivity", "*** AD LOADED SUCCESSFULLY! ***")
                hasAdLoaded = true
                adView.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // Green for success
            }
            
            override fun onAdClicked() {
                android.util.Log.d("MainActivity", "Ad clicked")
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshStats()
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
    
    private fun setupClickListeners() {
        binding.btnAddGame.setOnClickListener {
            startActivity(Intent(this, AddGameActivity::class.java))
        }
        
        binding.btnBulkUpload.setOnClickListener {
            startActivity(Intent(this, BulkUploadActivity::class.java))
        }
        
        binding.btnListGames.setOnClickListener {
            startActivity(Intent(this, GameListActivity::class.java))
        }
        
        binding.btnLoanGame.setOnClickListener {
            startActivity(Intent(this, LoanGameActivity::class.java))
        }
        
        binding.btnReturnGame.setOnClickListener {
            startActivity(Intent(this, ReturnGameActivity::class.java))
        }
        
        binding.btnDeleteGame.setOnClickListener {
            startActivity(Intent(this, GameListActivity::class.java).apply {
                putExtra("mode", "delete")
            })
        }
        
        binding.btnExportImportGames.setOnClickListener {
            startActivity(Intent(this, ExportImportActivity::class.java))
        }

        // Settings button click handler for accessing privacy policy
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun setupVersionInfo() {
        // Display version information
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        
        // Find the TextView by ID - using a try-catch in case R.id is not updated yet
        try {
            val versionTextView = findViewById<android.widget.TextView>(
                resources.getIdentifier("tvVersionInfo", "id", packageName)
            )
            versionTextView?.text = "Board Game Inventory v$versionName ($versionCode)"
            
            // Set up developer mode activation (7 taps on version)
            versionTextView?.setOnClickListener {
                if (DeveloperMode.handleVersionTap(this)) {
                    // Developer mode activated, show database management option
                    showDeveloperModeActivated()
                }
            }
        } catch (e: Exception) {
            // If the view is not found, just continue without version info
            android.util.Log.w("MainActivity", "Version TextView not found: ${e.message}")
        }
    }
    
    private fun observeStats() {
        lifecycleScope.launch {
            viewModel.gameStats.collect { stats ->
                binding.tvTotalGames.text = getString(R.string.total_games, stats.totalGames)
                binding.tvLoanedGames.text = getString(R.string.loaned_count, stats.loanedGames)
                binding.tvAvailableGames.text = getString(R.string.available_count, stats.availableGames)
            }
        }
    }
    
    private fun showExportImportScreen() {
        val intent = Intent(this, ExportImportActivity::class.java)
        startActivity(intent)
    }
    
    private fun showDeveloperModeActivated() {
        AlertDialog.Builder(this)
            .setTitle("Developer Mode Activated")
            .setMessage("Developer mode is now active for 30 minutes.\n\nDatabase Management and other developer tools are now accessible.")
            .setPositiveButton("Developer Settings") { _, _ ->
                startActivity(Intent(this, DeveloperSettingsActivity::class.java))
            }
            .setNeutralButton("Database Management") { _, _ ->
                startActivity(Intent(this, DatabaseManagementActivity::class.java))
            }
            .setNegativeButton("OK", null)
            .show()
    }
}
