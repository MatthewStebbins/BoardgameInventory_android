package com.boardgameinventory.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.boardgameinventory.R
import com.boardgameinventory.data.Game
import com.boardgameinventory.databinding.ActivityGameDetailBinding
import com.boardgameinventory.utils.Utils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGameDetailBinding
    private var game: Game? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get the game from intent
        game = intent.getParcelableExtra("game")
        
        if (game == null) {
            Utils.showToast(this, getString(R.string.error_game_data_not_found))
            finish()
            return
        }
        
        setupActionBar()
        displayGameDetails()
        setupClickListeners()
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
                // TODO: Implement edit functionality
                Utils.showToast(this@GameDetailActivity, getString(R.string.edit_functionality_coming_soon))
            }
            
            btnLoanReturn.setOnClickListener {
                game?.let { game ->
                    if (game.loanedTo.isNullOrBlank()) {
                        // Loan the game
                        val intent = Intent(this@GameDetailActivity, LoanGameActivity::class.java)
                        intent.putExtra("gameId", game.id)
                        startActivityForResult(intent, REQUEST_LOAN_GAME)
                    } else {
                        // Return the game
                        returnGame()
                    }
                }
            }
        }
    }
    
    private fun returnGame() {
        game?.let { game ->
            lifecycleScope.launch {
                try {
                    // TODO: Add return game functionality to repository/viewmodel
                    // For now, just show a success message and finish
                    Utils.showToast(this@GameDetailActivity, getString(R.string.game_returned))
                    setResult(RESULT_OK)
                    finish()
                } catch (e: Exception) {
                    Utils.showToast(this@GameDetailActivity, getString(R.string.error_returning_game, e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOAN_GAME && resultCode == RESULT_OK) {
            // Game was loaned, refresh the activity
            setResult(RESULT_OK)
            finish()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    companion object {
        private const val REQUEST_LOAN_GAME = 1001
    }
}
