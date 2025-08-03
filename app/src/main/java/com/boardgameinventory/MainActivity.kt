package com.boardgameinventory

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.boardgameinventory.databinding.ActivityMainBinding
import com.boardgameinventory.ui.*
import com.boardgameinventory.utils.AdManager
import com.boardgameinventory.utils.DeveloperMode
import com.boardgameinventory.viewmodel.MainViewModel
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var adView: AdView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
        setupVersionInfo()
        setupAds()
        observeStats()
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
            val intent = Intent(this, LoanGameActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnReturnGame.setOnClickListener {
            val intent = Intent(this, ReturnGameActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnDeleteGame.setOnClickListener {
            val intent = Intent(this, GameListActivity::class.java)
            intent.putExtra("deleteMode", true)
            startActivity(intent)
        }
        
        binding.btnExportImportGames.setOnClickListener {
            showExportImportScreen()
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
    
    private fun setupAds() {
        // Initialize AdMob
        AdManager.initialize(this)
        
        // Only show ads if the feature is enabled
        if (AdManager.shouldShowAds()) {
            // Find the AdView in the included layout using getIdentifier
            try {
                val adViewId = resources.getIdentifier("adView", "id", packageName)
                adView = findViewById(adViewId)
                
                if (adView != null) {
                    AdManager.loadAd(adView!!)
                }
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "AdView not found: ${e.message}")
            }
        } else {
            // Hide ad container if ads are disabled
            try {
                val adContainerId = resources.getIdentifier("adContainer", "id", packageName)
                findViewById<android.view.View>(adContainerId)?.visibility = android.view.View.GONE
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Ad container not found: ${e.message}")
            }
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
