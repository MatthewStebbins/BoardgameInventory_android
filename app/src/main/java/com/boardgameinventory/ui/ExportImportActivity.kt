package com.boardgameinventory.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.R

class ExportImportActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_ad_banner) // Use the existing ad banner layout temporarily
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Export/Import (Simplified)"
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
