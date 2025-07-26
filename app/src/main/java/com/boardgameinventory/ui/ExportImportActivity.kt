package com.boardgameinventory.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.R

class ExportImportActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Create layout and implement export/import functionality
        setContentView(R.layout.activity_main) // Temporary
        
        val mode = intent.getStringExtra("mode") ?: "export"
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (mode == "export") getString(R.string.export_title) else getString(R.string.import_title)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
