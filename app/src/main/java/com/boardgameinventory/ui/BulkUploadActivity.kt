package com.boardgameinventory.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityBulkUploadBinding
import com.boardgameinventory.viewmodel.BulkUploadViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class BulkUploadActivity : BaseAdActivity() {
    private lateinit var binding: ActivityBulkUploadBinding
    private lateinit var viewModel: BulkUploadViewModel
    private lateinit var adapter: ScannedBarcodesAdapter

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
        setupAdsManually(binding.adContainer, binding.adView, "BulkUploadActivity")
        binding = ActivityBulkUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[BulkUploadViewModel::class.java]
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
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        options.setPrompt("Scan Location Barcode (e.g., A-1)")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(false)
        options.setOrientationLocked(false)
        scanLocationBarcodeLauncher.launch(options)
    }

    private fun scanGameBarcode() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        options.setPrompt("Scan Game Barcode")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(false)
        options.setOrientationLocked(false)
        scanGameBarcodeLauncher.launch(options)
    }

    private fun showManualBarcodeDialog() {
        val editText = EditText(this)
        editText.hint = getString(R.string.enter_barcode_manually_hint)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_barcode_title))
            .setView(editText)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val barcode = editText.text.toString().trim()
                if (barcode.isNotEmpty()) {
                    addGameBarcode(barcode)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun addGameBarcode(barcode: String) {
        if (barcode.isNotEmpty()) {
            viewModel.addGameBarcode(barcode)
        }
    }

    private fun finishBulkUpload() {
        val bookcase = binding.etBookcase.text.toString().trim()
        val shelf = binding.etShelf.text.toString().trim()

        if (bookcase.isEmpty() || shelf.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_bookcase_shelf_required), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            viewModel.processBulkUpload(bookcase, shelf)
        }
    }

    private fun showUploadResultDialog(result: BulkUploadViewModel.UploadResult) {
        val message = buildString {
            append(getString(R.string.bulk_upload_results_message, result.successful))
            if (result.failed.isNotEmpty()) {
                append("\n\n")
                append(getString(R.string.bulk_upload_results_with_failures, result.successful, result.failed.size))
                result.failed.forEach { barcode ->
                    append("\n")
                    append(getString(R.string.bulk_upload_failed_item, barcode))
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.bulk_upload_complete))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                if (result.failed.isEmpty()) {
                    finish()
                }
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
