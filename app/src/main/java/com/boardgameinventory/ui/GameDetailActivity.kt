package com.boardgameinventory.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.boardgameinventory.R
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.ActivityGameDetailBinding
import com.boardgameinventory.repository.GameRepository
import com.boardgameinventory.utils.Utils
import com.boardgameinventory.utils.TextDarknessManager
import com.boardgameinventory.viewmodel.GameDetailViewModel
import com.boardgameinventory.viewmodel.GameDetailViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameDetailActivity : BaseAdActivity() {
    
    private lateinit var binding: ActivityGameDetailBinding
    private var game: Game? = null
    
    // Modern ActivityResult launchers
    private val loanGameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Game was loaned, refresh the activity
            setResult(RESULT_OK)
            finish()
        }
    }
    
    private val editGameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Game was edited, refresh the activity
            setResult(RESULT_OK)
            finish()
        }
    }
    
private val viewModel: GameDetailViewModel by viewModels {
    GameDetailViewModelFactory(GameRepository(AppDatabase.getDatabase(this).gameDao()))
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Apply text darkness setting
        TextDarknessManager.applyTextDarknessToActivity(this)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = game?.name ?: getString(R.string.game_detail_title)
        }

        // Get the game from intent
        game = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("game", Game::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("game")
        }
        
        if (game == null) {
            Utils.showToast(this, getString(R.string.error_game_data_not_found))
            finish()
            return
        }
        
        displayGameDetails()
        setupClickListeners()
        setupAccessibility() // Add accessibility setup
        setupAdsManually()
    }
    
    private fun displayGameDetails() {
        game?.let { game ->
            binding.apply {
                // Game name
                tvGameName.text = game.name
                
                // Barcode
                tvBarcode.text = game.barcode
                
                // Location
                tvLocation.text = getString(R.string.location, game.bookcase, game.shelf)
                
                // Date added
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                tvDateAdded.text = dateFormat.format(Date(game.dateAdded))
                
                // Description
                if (!game.description.isNullOrBlank()) {
                    tvDescription.text = game.description
                    tvDescription.visibility = View.VISIBLE
                    tvNoDescription.visibility = View.GONE
                } else {
                    tvDescription.visibility = View.GONE
                    tvNoDescription.visibility = View.VISIBLE
                }
                
                // Loan status
                if (!game.loanedTo.isNullOrBlank()) {
                    layoutLoanStatus.visibility = View.VISIBLE
                    tvLoanedTo.text = game.loanedTo
                    btnLoanReturn.text = getString(R.string.return_game)
                    btnLoanReturn.setIconResource(R.drawable.ic_return)
                } else {
                    layoutLoanStatus.visibility = View.GONE
                    btnLoanReturn.text = getString(R.string.loan_game)
                    btnLoanReturn.setIconResource(R.drawable.ic_loan)
                }
                
                // Load game image if available
                if (!game.imageUrl.isNullOrBlank()) {
                    Glide.with(this@GameDetailActivity)
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
    
    private fun setupClickListeners() {
        binding.apply {
            btnEditGame.setOnClickListener {
                editGame()
            }
            
            btnLoanReturn.setOnClickListener {
                game?.let { game ->
                    if (game.loanedTo.isNullOrBlank()) {
                        // Loan the game
                        val intent = Intent(this@GameDetailActivity, LoanGameActivity::class.java)
                        intent.putExtra("gameId", game.id)
                        loanGameLauncher.launch(intent)
                    } else {
                        // Return the game
                        returnGame()
                    }
                }
            }
        }
    }
    
    private fun editGame() {
        game?.let { game ->
            val intent = Intent(this@GameDetailActivity, EditGameActivity::class.java)
            intent.putExtra("game", game)
            editGameLauncher.launch(intent)
        }
    }
    
    private fun returnGame() {
        game?.let { currentGame ->
            lifecycleScope.launch {
                try {
                    // Create a ViewModel instance for game operations
                    val gameRepository = GameRepository(AppDatabase.getDatabase(this@GameDetailActivity).gameDao())
                    
                    // Return the game by clearing the loanedTo field
                    gameRepository.returnGame(currentGame.id)
                    
                    Utils.showToast(this@GameDetailActivity, getString(R.string.game_returned))
                    setResult(RESULT_OK)
                    finish()
                } catch (e: Exception) {
                    Utils.showToast(this@GameDetailActivity, getString(R.string.error_returning_game, e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    /**
     * Setup accessibility features for the game detail screen
     */
    private fun setupAccessibility() {
        game?.let { game ->
            binding.apply {
                // Set content descriptions for images
                ivGameImage.contentDescription = getString(R.string.game_image_content_description, game.name)

                // Only set content descriptions for views that exist in the binding
                btnEditGame.contentDescription = getString(R.string.edit_game_description, game.name)
                // Loan status button accessibility
                if (game.loanedTo != null) {
                    btnLoanReturn.contentDescription = getString(
                        R.string.return_loaned_game_description,
                        game.name,
                        game.loanedTo
                    )
                } else {
                    btnLoanReturn.contentDescription = getString(R.string.loan_game_description_with_name, game.name)
                }
                // Set heading for game name using accessibilityPaneTitle for best compatibility
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    tvGameName.accessibilityPaneTitle = game.name
                }
                // Group related information for better screen reader experience
                val locationInfo = getString(R.string.location_group_description,
                    game.bookcase,
                    game.shelf
                )
                tvLocation.contentDescription = locationInfo

                // Loan status needs a comprehensive description for screen readers
                val loanStatusDescription = if (game.loanedTo != null) {
                    getString(
                        R.string.loan_status_loaned_description,
                        game.name,
                        game.loanedTo,
                        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                            .format(Date(System.currentTimeMillis()))
                    )
                } else {
                    getString(R.string.loan_status_available_description, game.name)
                }
                layoutLoanStatus.contentDescription = loanStatusDescription
                layoutLoanStatus.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES

                // Description text needs proper focus handling for screen readers
                if (!game.description.isNullOrBlank()) {
                    tvDescription.isFocusable = true
                }
            }
        }
    }

    private fun setupAdsManually() {
        try {
            val localAdView = binding.adView
            adView = localAdView
            localAdView.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    android.util.Log.d("GameDetailActivity", "Ad loaded successfully")
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    android.util.Log.e("GameDetailActivity", "Ad failed to load: ${error.message}")
                }
            }
            com.boardgameinventory.utils.AdManager.loadAd(localAdView)
        } catch (e: Exception) {
            android.util.Log.e("GameDetailActivity", "Error in ad setup: ${e.message}", e)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_sync -> {
                val gameTitle = viewModel.getGameTitle() // Assuming a ViewModel method exists to get the title
                if (gameTitle != "Unknown Game") {
                    viewModel.syncGameData() // Assuming a ViewModel method exists to handle sync
                    Toast.makeText(this, "Syncing game data...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Cannot sync: Unknown Game", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
