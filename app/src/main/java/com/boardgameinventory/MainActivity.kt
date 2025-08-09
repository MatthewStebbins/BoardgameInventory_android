package com.boardgameinventory

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.boardgameinventory.databinding.ActivityMainBinding
import com.boardgameinventory.ui.*
import com.boardgameinventory.ui.ExportImportActivity
import com.boardgameinventory.update.AppUpdateManager
import com.boardgameinventory.update.UpdateState
import com.boardgameinventory.utils.AdManager
import com.boardgameinventory.utils.DeveloperMode
import com.boardgameinventory.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.ads.AdView
import com.google.android.play.core.install.model.AppUpdateType // Correct import for AppUpdateType
import kotlinx.coroutines.launch

class MainActivity : BaseAdActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    // Update manager for handling in-app updates
    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get the update manager from the application
        appUpdateManager = (application as BoardGameInventoryApp).updateManager

        setupClickListeners()
        setupVersionInfo()
        setupAccessibility() // Add accessibility support
        observeStats()
        observeUpdateState()

        // Check for pending updates
        appUpdateManager.checkPendingUpdates(this)

        // Check for new updates
        appUpdateManager.checkForUpdates(this, AppUpdateType.FLEXIBLE)

        // Setup ads manually since BaseAdActivity isn't finding the views
        setupAdsManually()
    }
    
    /**
     * Setup accessibility features for all UI elements
     */
    private fun setupAccessibility() {
        // Main action buttons
        binding.btnAddGame.contentDescription = getString(R.string.add_game_description)
        binding.btnBulkUpload.contentDescription = getString(R.string.bulk_upload_description)
        binding.btnListGames.contentDescription = getString(R.string.list_games_description)
        binding.btnLoanGame.contentDescription = getString(R.string.loan_game_description)
        binding.btnReturnGame.contentDescription = getString(R.string.return_game_description)
        
        // Make stats elements individually focusable for screen readers
        binding.tvTotalGames.isFocusable = true
        binding.tvLoanedGames.isFocusable = true
        binding.tvAvailableGames.isFocusable = true
    }

    private fun observeUpdateState() {
        lifecycleScope.launch {
            appUpdateManager.updateState.collect { state ->
                when (state) {
                    is UpdateState.Downloaded -> {
                        showUpdateReadyDialog()
                    }
                    is UpdateState.Downloading -> {
                        showSnackbar(getString(R.string.update_downloading, state.progress))
                    }
                    is UpdateState.Failed -> {
                        showSnackbar(getString(R.string.update_failed, state.reason))
                    }
                    is UpdateState.InProgress -> {
                        // Update in progress, no UI needed
                    }
                    is UpdateState.Idle, is UpdateState.NoUpdateAvailable -> {
                        // No action needed
                    }
                    is UpdateState.UserRejected -> {
                        // User rejected the update, could show reminder later
                    }
                }
            }
        }
    }

    private fun showUpdateReadyDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.update_available)
            .setMessage(R.string.update_ready_to_install)
            .setPositiveButton(R.string.restart) { _, _ ->
                appUpdateManager.completeUpdate()
            }
            .setNegativeButton(R.string.later, null)
            .setCancelable(false)
            .show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    // Process activity results, including update flow results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the result to the update manager
        if (requestCode == AppUpdateManager.APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                showSnackbar(getString(R.string.update_failed, "Update flow canceled"))
            }
        }
    }

    private fun setupAdsManually() {
        try {
            val localAdView = findViewById<AdView>(R.id.adView)
            adView = localAdView
            localAdView.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    android.util.Log.d("MainActivity", "Ad loaded successfully")
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    android.util.Log.e("MainActivity", "Ad failed to load: ${error.message}")
                }
            }
            AdManager.loadAd(localAdView)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in ad setup: ${e.message}", e)
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
            startActivity(Intent(this, DeleteGameActivity::class.java))
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
        try {
            val versionTextView = findViewById<android.widget.TextView>(R.id.tvVersionInfo)
            versionTextView?.text = getString(R.string.version_info, versionName, versionCode)
            versionTextView?.setOnClickListener {
                if (DeveloperMode.handleVersionTap(this)) {
                    showDeveloperModeActivated()
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Version TextView not found: ${e.message}")
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
}
