package com.boardgameinventory.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.boardgameinventory.R
import com.boardgameinventory.data.AppDatabase
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.ActivityGameDetailBinding
import com.boardgameinventory.repository.GameRepository
import com.boardgameinventory.utils.Utils
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
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
        
        setupActionBar()
        displayGameDetails()
        setupClickListeners()
        setupAdsManually()
    }
    
    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = game?.name ?: getString(R.string.game_detail_title)
        }
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
                    val gameRepository = GameRepository(AppDatabase.getDatabase(this@GameDetailActivity).gameDao(), this@GameDetailActivity)
                    
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
    
    private fun setupAdsManually() {
        setupAdsWithBinding(binding.adContainer, binding.adView, "GameDetailActivity")
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
