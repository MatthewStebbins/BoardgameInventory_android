package com.boardgameinventory

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.boardgameinventory.databinding.ActivityMainBinding
import com.boardgameinventory.ui.*
import com.boardgameinventory.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
        observeStats()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshStats()
    }
    
    private fun setupClickListeners() {
        binding.btnAddGame.setOnClickListener {
            startActivity(Intent(this, AddGameActivity::class.java))
        }
        
        binding.btnBulkUpload.setOnClickListener {
            startActivity(Intent(this, BulkUploadActivity::class.java))
        }
        
        binding.btnListGames.setOnClickListener {
            startActivity(Intent(this, GameListActivity::class.java))
        }
        
        binding.btnLoanGame.setOnClickListener {
            val intent = Intent(this, LoanGameActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnReturnGame.setOnClickListener {
            val intent = Intent(this, ReturnGameActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnDeleteGame.setOnClickListener {
            val intent = Intent(this, GameListActivity::class.java)
            intent.putExtra("deleteMode", true)
            startActivity(intent)
        }
        
        binding.btnExportImportGames.setOnClickListener {
            showExportImportScreen()
        }
    }
    
    private fun observeStats() {
        lifecycleScope.launch {
            viewModel.gameStats.collect { stats ->
                binding.tvTotalGames.text = getString(R.string.total_games, stats.totalGames)
                binding.tvLoanedGames.text = getString(R.string.loaned_count, stats.loanedGames)
                binding.tvAvailableGames.text = getString(R.string.available_count, stats.availableGames)
            }
        }
    }
    
    private fun showExportImportScreen() {
        val intent = Intent(this, ExportImportActivity::class.java)
        startActivity(intent)
    }
}
