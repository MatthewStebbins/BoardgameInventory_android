package com.boardgameinventory.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityBulkUploadBinding
import com.boardgameinventory.utils.BarcodeUtils
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBulkUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BulkUploadViewModel::class.java]
        setupAdsWithBinding(binding.adContainer, binding.adView, "BulkUploadActivity")
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
        // Example: Toggle orientation lock and torch for demonstration
        binding.btnScanLocationBarcode.setOnClickListener {
            isOrientationLocked = !isOrientationLocked
            isTorchOn = !isTorchOn
            scanLocationBarcode()
        }
        
        binding.btnScanGameBarcode.setOnClickListener {
            isOrientationLocked = !isOrientationLocked
            isTorchOn = !isTorchOn
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

    private fun scanLocationBarcode() {
        val options = BarcodeUtils.createLocationBarcodeScanOptions(
            orientationLocked = isOrientationLocked,
            torchOn = isTorchOn
        )
        scanLocationBarcodeLauncher.launch(options)
    }

    private fun scanGameBarcode() {
        val options = BarcodeUtils.createBulkScanOptions(
            orientationLocked = isOrientationLocked,
            torchOn = isTorchOn
        )
        scanGameBarcodeLauncher.launch(options)
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
