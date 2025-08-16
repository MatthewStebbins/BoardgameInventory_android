package com.boardgameinventory.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.boardgameinventory.R
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.ActivityDeleteGameBinding
import com.boardgameinventory.utils.TextDarknessManager
import com.boardgameinventory.utils.Utils
import com.boardgameinventory.viewmodel.GameListViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class DeleteGameActivity : BaseAdActivity() {

    private lateinit var binding: ActivityDeleteGameBinding
    private lateinit var adapter: GameAdapter
    private val gameViewModel: GameListViewModel by viewModels()
    private var selectedGame: Game? = null
    private var games = mutableListOf<Game>()

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
        binding = ActivityDeleteGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Apply text darkness setting
        TextDarknessManager.applyTextDarknessToActivity(this)

        // Set up the toolbar with a back arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupActionBar()
        setupRecyclerView()
        setupClickListeners()
        observeGames()
        setupAdsManually()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.delete_game_title)
        }
    }

    private fun setupRecyclerView() {
        adapter = GameAdapter { game, action ->
            when (action) {
                GameAdapter.ACTION_CLICK -> onGameSelected(game)
            }
        }

        binding.recyclerViewGames.apply {
            layoutManager = LinearLayoutManager(this@DeleteGameActivity)
            adapter = this@DeleteGameActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.btnDeleteGame.setOnClickListener {
            confirmDeleteSelectedGame()
        }

        binding.btnScanBarcode.setOnClickListener {
            scanBarcode()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun observeGames() {
        // Observe both available and loaned games
        gameViewModel.availableGames.observe(this) { availableGames ->
            val currentGames = games.toMutableList()
            currentGames.addAll(availableGames)
            updateGamesList(currentGames)
        }

        gameViewModel.loanedGames.observe(this) { loanedGames ->
            val currentGames = games.toMutableList()
            currentGames.addAll(loanedGames)
            updateGamesList(currentGames)
        }
    }

    private fun updateGamesList(gamesList: List<Game>) {
        games = gamesList.distinctBy { it.id }.toMutableList()
        adapter.submitList(games)

        if (games.isEmpty()) {
            binding.recyclerViewGames.visibility = View.GONE
            binding.tvNoGames.visibility = View.VISIBLE
            binding.layoutDeleteForm.visibility = View.GONE
        } else {
            binding.recyclerViewGames.visibility = View.VISIBLE
            binding.tvNoGames.visibility = View.GONE
            binding.layoutDeleteForm.visibility = View.VISIBLE
        }
    }

    private fun onGameSelected(game: Game) {
        selectedGame = game
        binding.tvSelectedGame.text = getString(R.string.selected_game_format, game.name, game.barcode)
        binding.tvSelectedGame.visibility = View.VISIBLE
        binding.btnDeleteGame.isEnabled = true
    }

    private fun confirmDeleteSelectedGame() {
        selectedGame?.let { game ->
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, game.name))
                .setPositiveButton(R.string.delete) { _, _ ->
                    deleteSelectedGame(game)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        } ?: run {
            Utils.showToast(this, getString(R.string.error_no_game_selected))
        }
    }

    private fun deleteSelectedGame(game: Game) {
        lifecycleScope.launch {
            try {
                gameViewModel.deleteGame(game.id)
                Utils.showToast(
                    this@DeleteGameActivity,
                    getString(R.string.game_deleted_success, game.name)
                )
                selectedGame = null
                binding.tvSelectedGame.visibility = View.GONE
                binding.btnDeleteGame.isEnabled = false

                // Remove the deleted game from our local list
                val updatedGames = games.filterNot { it.id == game.id }
                updateGamesList(updatedGames)
            } catch (e: Exception) {
                Utils.showToast(
                    this@DeleteGameActivity,
                    getString(R.string.error_deleting_game, e.message)
                )
            }
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
        val options = ScanOptions().apply {
            setPrompt(getString(R.string.scan_barcode_prompt))
            setBeepEnabled(true)
            setOrientationLocked(false)
            setCaptureActivity(CustomCaptureActivity::class.java)
        }
        scanLauncher.launch(options)
    }

    private fun findGameByBarcode(barcode: String) {
        lifecycleScope.launch {
            try {
                // Search through all games for the barcode
                val game = games.find { it.barcode == barcode }
                if (game != null) {
                    onGameSelected(game)
                    // Scroll to the selected game in the list
                    val position = games.indexOf(game)
                    if (position >= 0) {
                        binding.recyclerViewGames.scrollToPosition(position)
                    }
                } else {
                    Utils.showToast(
                        this@DeleteGameActivity,
                        getString(R.string.error_game_not_found_barcode, barcode)
                    )
                }
            } catch (e: Exception) {
                Utils.showToast(
                    this@DeleteGameActivity,
                    getString(R.string.error_finding_game, e.message)
                )
            }
        }
    }

    private fun setupAdsManually() {
        try {
            // Find the AdView directly from the layout rather than using binding
            val localAdView = binding.adView

            // Set the class-level adView property
            adView = localAdView

            // Configure the listener
            localAdView.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    android.util.Log.d("DeleteGameActivity", "Ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    android.util.Log.e("DeleteGameActivity", "Ad failed to load: ${error.message}")
                }
            }

            // Load the ad
            com.boardgameinventory.utils.AdManager.loadAd(localAdView)
        } catch (e: Exception) {
            android.util.Log.e("DeleteGameActivity", "Error in ad setup: ${e.message}", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
