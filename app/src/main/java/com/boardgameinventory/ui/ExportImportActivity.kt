package com.boardgameinventory.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityExportImportBinding
import com.boardgameinventory.utils.ExportUtils
import com.boardgameinventory.utils.ImportUtils
import com.boardgameinventory.viewmodel.ExportImportViewModel
import kotlinx.coroutines.launch

class ExportImportActivity : BaseAdActivity() {
    
    private lateinit var binding: ActivityExportImportBinding
    private val viewModel: ExportImportViewModel by viewModels()
    
    private var selectedFileUri: Uri? = null
    private var isExportingCSV = true // Default to CSV
    private var isImportingCSV = true // Default to CSV
    
    // Activity result launchers for file operations
    private val exportFileCreator = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let { 
            handleExportFile(it)
        }
    }
    
    private val importFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            handleImportFileSelected(it)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportImportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupActionBar()
        setupAdsManually()
        setupUI()
        observeViewModel()
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.export_import_title)
        }
    }
    
    private fun setupUI() {
        // Set default selections
        binding.toggleGroupExportFormat.check(R.id.btnExportCSV)
        binding.toggleGroupImportFormat.check(R.id.btnImportCSV)
        
        // Export format toggle listeners
        binding.toggleGroupExportFormat.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isExportingCSV = checkedId == R.id.btnExportCSV
            }
        }
        
        // Import format toggle listeners
        binding.toggleGroupImportFormat.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isImportingCSV = checkedId == R.id.btnImportCSV
                // Clear selected file when format changes
                clearSelectedFile()
            }
        }
        
        // Export button
        binding.btnStartExport.setOnClickListener {
            startExport()
        }
        
        // Import buttons
        binding.btnSelectFile.setOnClickListener {
            selectImportFile()
        }
        
        binding.btnStartImport.setOnClickListener {
            startImport()
        }
        
        binding.btnClearFile.setOnClickListener {
            clearSelectedFile()
        }
        
        // Import options
        binding.cbOverwriteDatabase.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showOverwriteWarning()
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                updateLoadingState(isLoading)
            }
        }
        
        lifecycleScope.launch {
            viewModel.message.collect { message ->
                if (message.isNotEmpty()) {
                    showResults(message)
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.exportSuccess.collect { success ->
                if (success) {
                    binding.cardResults.visibility = View.VISIBLE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.importSuccess.collect { result ->
                result?.let {
                    binding.cardResults.visibility = View.VISIBLE
                    clearSelectedFile()
                }
            }
        }
    }
    
    private fun startExport() {
        val fileName = if (isExportingCSV) {
            "games_export_${System.currentTimeMillis()}.csv"
        } else {
            "games_export_${System.currentTimeMillis()}.xlsx"
        }
        
        exportFileCreator.launch(fileName)
    }
    
    private fun handleExportFile(uri: Uri) {
        if (isExportingCSV) {
            viewModel.exportToCSV(uri)
        } else {
            viewModel.exportToExcel(uri)
        }
    }
    
    private fun selectImportFile() {
        val mimeType = if (isImportingCSV) {
            "text/csv"
        } else {
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        
        importFilePicker.launch(arrayOf(mimeType))
    }
    
    private fun handleImportFileSelected(uri: Uri) {
        selectedFileUri = uri
        
        // Get filename from URI
        val fileName = uri.lastPathSegment ?: "Selected file"
        binding.tvSelectedFileName.text = fileName
        binding.cardSelectedFile.visibility = View.VISIBLE
        binding.btnStartImport.isEnabled = true
    }
    
    private fun startImport() {
        selectedFileUri?.let { uri ->
            val overwriteDatabase = binding.cbOverwriteDatabase.isChecked
            
            if (isImportingCSV) {
                viewModel.importFromCSV(uri, overwriteDatabase)
            } else {
                viewModel.importFromExcel(uri, overwriteDatabase)
            }
        } ?: run {
            Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun clearSelectedFile() {
        selectedFileUri = null
        binding.cardSelectedFile.visibility = View.GONE
        binding.btnStartImport.isEnabled = false
        binding.tvSelectedFileName.text = ""
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        // Export loading state
        binding.progressExport.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvExportStatus.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnStartExport.isEnabled = !isLoading
        
        // Import loading state
        binding.progressImport.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvImportStatus.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSelectFile.isEnabled = !isLoading
        binding.btnStartImport.isEnabled = !isLoading && selectedFileUri != null
        
        // Update status text
        if (isLoading) {
            binding.tvExportStatus.text = "Processing..."
            binding.tvImportStatus.text = "Processing..."
        }
    }
    
    private fun showResults(message: String) {
        binding.tvResults.text = message
        binding.cardResults.visibility = View.VISIBLE
        
        // Clear any previous results from ViewModel
        viewModel.clearResults()
    }
    
    private fun showOverwriteWarning() {
        AlertDialog.Builder(this)
            .setTitle("Warning")
            .setMessage("This will permanently delete all existing games in your collection and replace them with the imported data. This action cannot be undone.\n\nAre you sure you want to continue?")
            .setPositiveButton("Continue") { _, _ ->
                // User confirmed, keep checkbox checked
            }
            .setNegativeButton("Cancel") { _, _ ->
                // User cancelled, uncheck the checkbox
                binding.cbOverwriteDatabase.isChecked = false
            }
            .show()
    }
    
    private fun setupAdsManually() {
        try {
            // Find the AdView directly from the layout rather than using binding
            val localAdView = binding.adView

            // Set the class-level adView property
            adView = localAdView

            // Set up the ad container
            val adContainer = binding.adContainer

            // Configure the listener
            localAdView.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    android.util.Log.d("ExportImportActivity", "Ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    android.util.Log.e("ExportImportActivity", "Ad failed to load: ${error.message}")
                }
            }

            // Load the ad
            com.boardgameinventory.utils.AdManager.loadAd(localAdView)
        } catch (e: Exception) {
            android.util.Log.e("ExportImportActivity", "Error in ad setup: ${e.message}", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
