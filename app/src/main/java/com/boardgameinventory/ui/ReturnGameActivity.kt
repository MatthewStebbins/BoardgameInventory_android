package com.boardgameinventory.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.R

class ReturnGameActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Create layout and implement return game functionality
        setContentView(R.layout.activity_main) // Temporary
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.return_game_title)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
