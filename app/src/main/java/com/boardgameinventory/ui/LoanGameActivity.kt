package com.boardgameinventory.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.boardgameinventory.R
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.ActivityLoanGameBinding
import com.boardgameinventory.utils.Utils
import com.boardgameinventory.viewmodel.GameListViewModel
import com.boardgameinventory.validation.GameInputValidation
import com.boardgameinventory.validation.ValidationUtils
import com.boardgameinventory.validation.setupValidation
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class LoanGameActivity : BaseAdActivity() {
    
    private lateinit var binding: ActivityLoanGameBinding
    private lateinit var adapter: GameAdapter
    private val gameViewModel: GameListViewModel by viewModels()
    private var selectedGame: Game? = null
    
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            findGameByBarcode(result.contents)
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
        binding = ActivityLoanGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupActionBar()
        setupRecyclerView()
        setupClickListeners()
        setupValidation()
        observeGames()
        setupAdsManually()
    }
    
    private fun setupValidation() {
        // Setup validation for borrower name field
        binding.tilBorrowerName.setupValidation(this, ValidationUtils::validateLoanedTo)
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.loan_game_title)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = GameAdapter { game, action ->
            when (action) {
                GameAdapter.ACTION_CLICK -> onGameSelected(game)
            }
        }
        
        binding.recyclerViewGames.apply {
            layoutManager = LinearLayoutManager(this@LoanGameActivity)
            adapter = this@LoanGameActivity.adapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLoanGame.setOnClickListener {
            loanSelectedGame()
        }
        
        binding.btnScanBarcode.setOnClickListener {
            scanBarcode()
        }
        
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun observeGames() {
        gameViewModel.availableGames.observe(this) { games ->
            adapter.submitList(games)
            
            if (games.isEmpty()) {
                binding.recyclerViewGames.visibility = View.GONE
                binding.tvNoGames.visibility = View.VISIBLE
                binding.tvNoGames.text = getString(R.string.no_games_available_to_loan)
                binding.layoutLoanForm.visibility = View.GONE
            } else {
                binding.recyclerViewGames.visibility = View.VISIBLE
                binding.tvNoGames.visibility = View.GONE
                binding.layoutLoanForm.visibility = View.VISIBLE
            }
        }
    }
    
    private fun onGameSelected(game: Game) {
        selectedGame = game
        binding.tvSelectedGame.text = getString(R.string.selected_game_format, game.name, game.barcode)
        binding.tvSelectedGame.visibility = View.VISIBLE
        binding.btnLoanGame.isEnabled = true
    }
    
    private fun loanSelectedGame() {
        val borrowerName = binding.etBorrowerName.text.toString().trim()
        
        // Validate borrower name
        val validationResult = ValidationUtils.validateLoanedTo(borrowerName)
        if (!validationResult.isValid) {
            val errorMessage = when {
                validationResult.errorMessageRes != null -> getString(validationResult.errorMessageRes)
                validationResult.errorMessage != null -> validationResult.errorMessage
                else -> "Please enter a valid name"
            }
            Utils.showToast(this, errorMessage)
            binding.etBorrowerName.requestFocus()
            return
        }
        
        selectedGame?.let { game ->
            lifecycleScope.launch {
                try {
                    gameViewModel.loanGame(game.id, borrowerName)
                    Utils.showToast(this@LoanGameActivity, 
                        getString(R.string.game_loaned_success, game.name, borrowerName))
                    setResult(RESULT_OK)
                    finish()
                } catch (e: Exception) {
                    Utils.showToast(this@LoanGameActivity, 
                        getString(R.string.error_loan_game, e.message))
                }
            }
        } ?: run {
            Utils.showToast(this, getString(R.string.error_no_game_selected))
        }
    }
    
    private fun scanBarcode() {
        checkCameraPermissionAndScan()
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
        options.setPrompt("Scan a game barcode")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        scanLauncher.launch(options)
    }
    
    
    private fun findGameByBarcode(barcode: String) {
        lifecycleScope.launch {
            try {
                // Search through the available games list for the barcode
                val currentGames = adapter.currentList
                val game = currentGames.find { it.barcode == barcode }
                if (game != null) {
                    onGameSelected(game)
                    // Scroll to the selected game in the list
                    val position = currentGames.indexOf(game)
                    if (position >= 0) {
                        binding.recyclerViewGames.scrollToPosition(position)
                    }
                } else {
                    Utils.showToast(this@LoanGameActivity, 
                        getString(R.string.error_game_not_found_barcode, barcode))
                }
            } catch (e: Exception) {
                Utils.showToast(this@LoanGameActivity, 
                    getString(R.string.error_finding_game, e.message))
            }
        }
    }
    
    private fun setupAdsManually() {
        setupAdsWithBinding(binding.adContainer, binding.adView, "LoanGameActivity")
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
