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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.boardgameinventory.R
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.ActivityReturnGameBinding
import com.boardgameinventory.utils.Utils
import com.boardgameinventory.viewmodel.GameListViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class ReturnGameActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReturnGameBinding
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
        binding = ActivityReturnGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupActionBar()
        setupRecyclerView()
        setupClickListeners()
        setupLocationBarcodeHandler()
        observeGames()
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.return_game_title)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = GameAdapter { game, action ->
            when (action) {
                GameAdapter.ACTION_CLICK -> onGameSelected(game)
            }
        }
        
        // Configure adapter to show loaned games
        adapter.setShowLoanedTo(true)
        
        binding.recyclerViewGames.apply {
            layoutManager = LinearLayoutManager(this@ReturnGameActivity)
            adapter = this@ReturnGameActivity.adapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnReturnGame.setOnClickListener {
            returnSelectedGame()
        }
        
        binding.btnScanBarcode.setOnClickListener {
            scanBarcode()
        }
        
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun setupLocationBarcodeHandler() {
        binding.etLocationBarcode.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                parseLocationBarcode()
            }
        }
    }
    
    private fun parseLocationBarcode() {
        val locationBarcode = binding.etLocationBarcode.text.toString().trim()
        if (locationBarcode.isEmpty()) return
        
        if (!locationBarcode.contains("-") || locationBarcode.split("-").size != 2) {
            Utils.showToast(this, getString(R.string.error_invalid_location_format))
            return
        }
        
        val parts = locationBarcode.split("-")
        val bookcase = parts[0].trim()
        val shelf = parts[1].trim()
        
        if (bookcase.isEmpty() || shelf.isEmpty()) {
            Utils.showToast(this, getString(R.string.error_invalid_location_format))
            return
        }
        
        binding.etBookcase.setText(bookcase)
        binding.etShelf.setText(shelf)
    }
    
    private fun observeGames() {
        gameViewModel.loanedGames.observe(this) { games ->
            adapter.submitList(games)
            
            if (games.isEmpty()) {
                binding.recyclerViewGames.visibility = View.GONE
                binding.tvNoGames.visibility = View.VISIBLE
                binding.layoutReturnForm.visibility = View.GONE
            } else {
                binding.recyclerViewGames.visibility = View.VISIBLE
                binding.tvNoGames.visibility = View.GONE
                binding.layoutReturnForm.visibility = View.VISIBLE
            }
        }
    }
    
    private fun onGameSelected(game: Game) {
        selectedGame = game
        
        // Display selected game info
        binding.tvSelectedGame.text = getString(R.string.selected_game_format, game.name, game.barcode)
        binding.tvSelectedGame.visibility = View.VISIBLE
        
        // Pre-fill current location
        binding.etBookcase.setText(game.bookcase)
        binding.etShelf.setText(game.shelf)
        
        // Enable return button
        binding.btnReturnGame.isEnabled = true
    }
    
    private fun returnSelectedGame() {
        selectedGame?.let { game ->
            val newBookcase = binding.etBookcase.text.toString().trim()
            val newShelf = binding.etShelf.text.toString().trim()
            
            lifecycleScope.launch {
                try {
                    // Update location if changed
                    val currentBookcase = newBookcase.ifEmpty { game.bookcase }
                    val currentShelf = newShelf.ifEmpty { game.shelf }
                    
                    if (currentBookcase != game.bookcase || currentShelf != game.shelf) {
                        gameViewModel.updateGameLocation(game.id, currentBookcase, currentShelf)
                    }
                    
                    // Return the game
                    gameViewModel.returnGame(game.id)
                    
                    Utils.showToast(this@ReturnGameActivity, 
                        getString(R.string.game_returned_success, game.name, currentBookcase, currentShelf))
                    
                    setResult(RESULT_OK)
                    finish()
                    
                } catch (e: Exception) {
                    Utils.showToast(this@ReturnGameActivity, 
                        getString(R.string.error_return_game, e.message))
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
        options.setPrompt("Scan a game barcode to return")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        scanLauncher.launch(options)
    }
    
    private fun findGameByBarcode(barcode: String) {
        lifecycleScope.launch {
            try {
                // Search through the loaned games list for the barcode
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
                    Utils.showToast(this@ReturnGameActivity, 
                        getString(R.string.error_game_not_found_barcode, barcode))
                }
            } catch (e: Exception) {
                Utils.showToast(this@ReturnGameActivity, 
                    getString(R.string.error_finding_game, e.message))
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
