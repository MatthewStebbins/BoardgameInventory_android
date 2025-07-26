package com.boardgameinventory.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.R

class LoanGameActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Create layout and implement loan game functionality
        setContentView(R.layout.activity_main) // Temporary
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.loan_game_title)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
