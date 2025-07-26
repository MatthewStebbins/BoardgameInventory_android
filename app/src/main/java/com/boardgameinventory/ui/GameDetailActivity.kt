package com.boardgameinventory.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.R

class GameDetailActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Create layout and implement game detail view
        setContentView(R.layout.activity_main) // Temporary
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Game Details"
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
