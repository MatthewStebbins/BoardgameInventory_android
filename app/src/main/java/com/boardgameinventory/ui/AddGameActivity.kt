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
import com.boardgameinventory.utils.BarcodeUtils
import com.boardgameinventory.utils.PermissionUtils
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
    
    // Register for permission results using Activity Result API
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all required permissions are granted
        if (permissions.all { it.value }) {
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
            // Find the AdView directly from the layout rather than using binding
            val localAdView = binding.adView

            // Set the class-level adView property
            adView = localAdView

            if (localAdView != null) {
                // Set up the ad container
                val adContainer = binding.adContainer

                // Configure the listener
                localAdView.adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdLoaded() {
                        android.util.Log.d("AddGameActivity", "Ad loaded successfully")
                    }

                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        android.util.Log.e("AddGameActivity", "Ad failed to load: ${error.message}")
                    }
                }

                // Load the ad
                com.boardgameinventory.utils.AdManager.loadAd(localAdView)
            }
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
        // Use the new PermissionUtils class for permission handling
        PermissionUtils.requestPermissions(
            this,
            PermissionUtils.PermissionType.CAMERA,
            cameraPermissionLauncher
        ) { granted ->
            if (granted) {
                startBarcodeScan()
            } else {
                // Permission was denied - show the permission denied dialog
                PermissionUtils.showPermissionDeniedDialog(
                    this,
                    PermissionUtils.PermissionType.CAMERA
                )
            }
        }
    }
    
    private fun startBarcodeScan() {
        // Set up barcode scanning options with orientation handling
        val options = BarcodeUtils.createGameBarcodeScanOptions()
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
