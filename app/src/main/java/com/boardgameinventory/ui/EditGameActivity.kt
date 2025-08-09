package com.boardgameinventory.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.boardgameinventory.R
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.ActivityEditGameBinding
import com.boardgameinventory.utils.Utils
import com.boardgameinventory.utils.BarcodeUtils
import com.boardgameinventory.viewmodel.EditGameViewModel
import com.boardgameinventory.validation.GameInputValidation
import com.boardgameinventory.validation.ValidationUtils
import com.boardgameinventory.validation.validateMultipleInputs
import com.boardgameinventory.validation.areAllInputsValid
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class EditGameActivity : BaseAdActivity() {
    
    private lateinit var binding: ActivityEditGameBinding
    private val viewModel: EditGameViewModel by viewModels()
    private var originalGame: Game? = null
    
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
        binding = ActivityEditGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get the game from intent
        originalGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("game", Game::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("game")
        }
        
        if (originalGame == null) {
            Utils.showToast(this, getString(R.string.error_game_data_not_found))
            finish()
            return
        }
        
        setupToolbar()
        setupClickListeners()
        setupTextWatchers()
        populateFields()
        observeViewModel()
        setupAdsManually()
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.edit_game_title)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnScanBarcode.setOnClickListener {
            checkCameraPermissionAndScan()
        }
        
        binding.tilBarcode.setEndIconOnClickListener {
            checkCameraPermissionAndScan()
        }
        
        binding.btnSave.setOnClickListener {
            saveGame()
        }
        
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun setupTextWatchers() {
        // Setup validation for all input fields
        GameInputValidation.setupGameNameValidation(binding.tilGameName, this)
        GameInputValidation.setupBarcodeValidation(binding.tilBarcode, this)
        GameInputValidation.setupBookcaseValidation(binding.tilBookcase, this)
        GameInputValidation.setupShelfValidation(binding.tilShelf, this)
        GameInputValidation.setupDescriptionValidation(binding.tilDescription, this)
        
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
    
    private fun populateFields() {
        originalGame?.let { game ->
            binding.apply {
                etGameName.setText(game.name)
                etBarcode.setText(game.barcode)
                etBookcase.setText(game.bookcase)
                etShelf.setText(game.shelf)
                etDescription.setText(game.description ?: "")
                
                // Load game image if available
                if (!game.imageUrl.isNullOrBlank()) {
                    Glide.with(this@EditGameActivity)
                        .load(game.imageUrl)
                        .placeholder(R.drawable.ic_game_placeholder)
                        .error(R.drawable.ic_game_placeholder)
                        .into(ivGameImage)
                } else {
                    ivGameImage.setImageResource(R.drawable.ic_game_placeholder)
                }
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnSave.isEnabled = !isLoading
            }
        }
        
        lifecycleScope.launch {
            viewModel.message.collect { message ->
                if (message.isNotEmpty()) {
                    Utils.showToast(this@EditGameActivity, message)
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.gameUpdated.collect { success ->
                if (success) {
                    setResult(RESULT_OK)
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
        val options = BarcodeUtils.createGameBarcodeScanOptions()
        scanLauncher.launch(options)
    }
    
    private fun saveGame() {
        // Validate all inputs
        val validationResults = validateMultipleInputs(
            this,
            binding.tilGameName to ValidationUtils::validateGameName,
            binding.tilBarcode to ValidationUtils::validateBarcode,
            binding.tilBookcase to ValidationUtils::validateBookcase,
            binding.tilShelf to ValidationUtils::validateShelf,
            binding.tilDescription to ValidationUtils::validateDescription
        )
        
        // Check if all inputs are valid
        if (!areAllInputsValid(validationResults)) {
            Utils.showToast(this, "Please fix the errors and try again")
            return
        }
        
        // Get sanitized input values
        val name = binding.etGameName.text.toString().trim()
        val barcode = binding.etBarcode.text.toString().trim()
        val bookcase = binding.etBookcase.text.toString().trim()
        val shelf = binding.etShelf.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        
        originalGame?.let { game ->
            val updatedGame = game.copy(
                name = name,
                barcode = barcode.uppercase(),
                bookcase = bookcase.uppercase(),
                shelf = shelf,
                description = description.ifEmpty { null }
            )
            viewModel.updateGame(updatedGame)
        }
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
                    android.util.Log.d("EditGameActivity", "Ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    android.util.Log.e("EditGameActivity", "Ad failed to load: ${error.message}")
                }
            }

            // Load the ad
            com.boardgameinventory.utils.AdManager.loadAd(localAdView)
        } catch (e: Exception) {
            android.util.Log.e("EditGameActivity", "Error in ad setup: ${e.message}", e)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
