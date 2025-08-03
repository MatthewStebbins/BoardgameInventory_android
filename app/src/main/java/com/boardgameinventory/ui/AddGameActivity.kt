package com.boardgameinventory.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityAddGameBinding
import com.boardgameinventory.utils.AdManager
import com.boardgameinventory.utils.Utils
import com.boardgameinventory.viewmodel.AddGameViewModel
import com.boardgameinventory.validation.GameInputValidation
import com.boardgameinventory.validation.ValidationUtils
import com.boardgameinventory.validation.validateMultipleInputs
import com.boardgameinventory.validation.areAllInputsValid
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class AddGameActivity : BaseAdActivity() {
    
    private lateinit var binding: ActivityAddGameBinding
    private val viewModel: AddGameViewModel by viewModels()
    
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            binding.etBarcode.setText(result.contents)
        }
    }
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startBarcodeScan()
        } else {
            Utils.showToast(this, getString(R.string.camera_permission_required))
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
        setupAdsManually()
    }
    
    private fun setupAdsManually() {
        try {
            setupAdsWithBinding(binding.adContainer, binding.adView, "AddGameActivity")
        } catch (e: Exception) {
            android.util.Log.e("AddGameActivity", "Error in ad setup: ${e.message}", e)
        }
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.add_game_title)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnScanBarcode.setOnClickListener {
            checkCameraPermissionAndScan()
        }
        
        binding.tilBarcode.setEndIconOnClickListener {
            checkCameraPermissionAndScan()
        }
        
        binding.btnSubmit.setOnClickListener {
            submitGame()
        }
        
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun setupTextWatchers() {
        // Setup validation for all input fields
        GameInputValidation.setupBarcodeValidation(binding.tilBarcode, this)
        GameInputValidation.setupBookcaseValidation(binding.tilBookcase, this)
        GameInputValidation.setupShelfValidation(binding.tilShelf, this)
        
        // Location barcode helper
        binding.etLocationBarcode.addTextChangedListener { text ->
            val barcode = text.toString()
            val (bookcase, shelf) = Utils.validateLocationBarcode(barcode)
            if (bookcase != null && shelf != null) {
                binding.etBookcase.setText(bookcase)
                binding.etShelf.setText(shelf)
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnSubmit.isEnabled = !isLoading
            }
        }
        
        lifecycleScope.launch {
            viewModel.message.collect { message ->
                if (message.isNotEmpty()) {
                    Utils.showToast(this@AddGameActivity, message)
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.gameAdded.collect { success ->
                if (success) {
                    finish()
                }
            }
        }
    }
    
    private fun checkCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == 
                    PackageManager.PERMISSION_GRANTED -> {
                startBarcodeScan()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                Utils.showToast(this, getString(R.string.camera_permission_required))
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun startBarcodeScan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        options.setPrompt("Scan a barcode")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        scanLauncher.launch(options)
    }
    
    private fun submitGame() {
        // Validate all inputs
        val validationResults = validateMultipleInputs(
            this,
            binding.tilBarcode to ValidationUtils::validateBarcode,
            binding.tilBookcase to ValidationUtils::validateBookcase,
            binding.tilShelf to ValidationUtils::validateShelf
        )
        
        // Check if all inputs are valid
        if (!areAllInputsValid(validationResults)) {
            Utils.showToast(this, getString(R.string.validation_failed))
            return
        }
        
        // Get sanitized input values
        val barcode = binding.tilBarcode.editText?.text.toString().trim()
        val bookcase = binding.tilBookcase.editText?.text.toString().trim()
        val shelf = binding.tilShelf.editText?.text.toString().trim()
        
        // Submit to ViewModel
        viewModel.addGame(barcode, bookcase, shelf)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
