package com.boardgameinventory.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityBulkUploadBinding
import com.boardgameinventory.utils.BarcodeUtils
import com.boardgameinventory.utils.PermissionUtils
import com.boardgameinventory.utils.TextDarknessManager
import com.boardgameinventory.viewmodel.BulkUploadViewModel
import com.journeyapps.barcodescanner.ScanContract

class BulkUploadActivity : BaseAdActivity() {
    private lateinit var binding: ActivityBulkUploadBinding
    private lateinit var viewModel: BulkUploadViewModel
    private lateinit var adapter: ScannedBarcodesAdapter
    
    // Toggles for orientation lock and torch. Replace with real UI toggles as needed.
    private var isOrientationLocked: Boolean = false
    private var isTorchOn: Boolean = false

    private val scanLocationBarcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            binding.etLocationBarcode.setText(result.contents)
            parseLocationBarcode(result.contents)
        }
    }

    private val scanGameBarcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            addGameBarcode(result.contents)
        }
    }

    // Permission launcher using the Activity Result API
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // Determine which scanner to launch based on the current request type
            when (currentScanType) {
                ScanType.LOCATION -> launchLocationBarcodeScanner()
                ScanType.GAME -> launchGameBarcodeScanner()
            }
        } else {
            Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show()
        }
    }

    // Enum to track which scanner should be launched after permission check
    private enum class ScanType {
        LOCATION, GAME
    }

    // Track the current scan type
    private var currentScanType = ScanType.GAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBulkUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Apply text darkness setting
        TextDarknessManager.applyTextDarknessToActivity(this)

        // Set up the toolbar with a back arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel = ViewModelProvider(this)[BulkUploadViewModel::class.java]
        setupAdsManually()
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Remove the setSupportActionBar call since we're using the default action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Bulk Upload Games"

        // Setup recycler view for scanned barcodes
        adapter = ScannedBarcodesAdapter { barcode ->
            // Remove barcode when clicked
            viewModel.removeGameBarcode(barcode)
        }
        binding.rvScannedBarcodes.layoutManager = LinearLayoutManager(this)
        binding.rvScannedBarcodes.adapter = adapter

        // Location barcode text watcher
        binding.etLocationBarcode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                parseLocationBarcode(s?.toString() ?: "")
            }
        })

        // Button listeners
        binding.btnScanLocationBarcode.setOnClickListener {
            scanLocationBarcode()
        }
        
        binding.btnScanGameBarcode.setOnClickListener {
            scanGameBarcode()
        }

        binding.btnAddManually.setOnClickListener {
            showManualBarcodeDialog()
        }

        binding.btnFinishUpload.setOnClickListener {
            finishBulkUpload()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun scanLocationBarcode() {
        currentScanType = ScanType.LOCATION
        checkCameraPermission()
    }

    private fun scanGameBarcode() {
        currentScanType = ScanType.GAME
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        // Use the PermissionUtils class for systematic permission handling
        PermissionUtils.requestPermissions(
            this,
            PermissionUtils.PermissionType.CAMERA,
            cameraPermissionLauncher
        ) { granted ->
            if (granted) {
                when (currentScanType) {
                    ScanType.LOCATION -> launchLocationBarcodeScanner()
                    ScanType.GAME -> launchGameBarcodeScanner()
                }
            } else {
                // Show the permission denied dialog to guide the user to app settings
                PermissionUtils.showPermissionDeniedDialog(
                    this,
                    PermissionUtils.PermissionType.CAMERA
                )
            }
        }
    }

    private fun launchLocationBarcodeScanner() {
        val options = BarcodeUtils.createLocationBarcodeScanOptions()
        scanLocationBarcodeLauncher.launch(options)
    }

    private fun launchGameBarcodeScanner() {
        val options = BarcodeUtils.createGameBarcodeScanOptions()
        scanGameBarcodeLauncher.launch(options)
    }

    private fun observeViewModel() {
        viewModel.scannedBarcodes.observe(this) { barcodes ->
            adapter.updateBarcodes(barcodes)
            binding.tvBarcodeCount.text = "Scanned: ${barcodes.size} barcodes"
        }

        viewModel.uploadResult.observe(this) { result ->
            result?.let {
                showUploadResultDialog(it)
                viewModel.clearUploadResult()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnFinishUpload.isEnabled = !isLoading
            binding.btnFinishUpload.text = if (isLoading) "Processing..." else "Finish Upload"
        }
    }

    private fun parseLocationBarcode(locationBarcode: String) {
        if (locationBarcode.contains("-")) {
            val parts = locationBarcode.split("-")
            if (parts.size == 2) {
                binding.etBookcase.setText(parts[0].trim())
                binding.etShelf.setText(parts[1].trim())
            }
        }
    }

    private fun showManualBarcodeDialog() {
        val editText = EditText(this)
        editText.hint = getString(R.string.enter_barcode_manually_hint)

        AlertDialog.Builder(this)
            .setTitle("Add Barcode Manually")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val barcode = editText.text.toString().trim()
                if (barcode.isNotEmpty()) {
                    addGameBarcode(barcode)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addGameBarcode(barcode: String) {
        viewModel.addGameBarcode(barcode)
    }

    private fun finishBulkUpload() {
        val bookcase = binding.etBookcase.text.toString().trim()
        val shelf = binding.etShelf.text.toString().trim()

        if (bookcase.isEmpty() || shelf.isEmpty()) {
            Toast.makeText(this, "Please fill in bookcase and shelf information", Toast.LENGTH_SHORT).show()
            return
        }

        if (viewModel.scannedBarcodes.value?.isEmpty() == true) {
            Toast.makeText(this, "Please scan at least one game barcode", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.uploadGames(bookcase, shelf)
    }

    private fun showUploadResultDialog(result: BulkUploadViewModel.UploadResult) {
        val message = buildString {
            append("Upload completed!\n\n")
            append("Successfully added: ${result.successfulCount} games\n")
            if (result.failedBarcodes.isNotEmpty()) {
                append("Failed to add: ${result.failedBarcodes.size} games\n")
                append("Failed barcodes:\n")
                result.failedBarcodes.forEach { barcode ->
                    append("• $barcode\n")
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Upload Result")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                if (result.failedBarcodes.isEmpty()) {
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun setupAdsManually() {
        try {
            // Find the AdView directly using findViewById since this activity doesn't use view binding properly
            val localAdView = findViewById<com.google.android.gms.ads.AdView>(R.id.adView)

            // Set the class-level adView property
            adView = localAdView

            if (localAdView != null) {
                // Configure the listener
                localAdView.adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdLoaded() {
                        android.util.Log.d("BulkUploadActivity", "Ad loaded successfully")
                    }

                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        android.util.Log.e("BulkUploadActivity", "Ad failed to load: ${error.message}")
                    }
                }

                // Load the ad
                com.boardgameinventory.utils.AdManager.loadAd(localAdView)
            }
        } catch (e: Exception) {
            android.util.Log.e("BulkUploadActivity", "Error in ad setup: ${e.message}", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
