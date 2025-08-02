package com.boardgameinventory.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.R
import com.boardgameinventory.utils.Utils

class ExportImportActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val mode = intent.getStringExtra("mode") ?: "export"
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (mode == "export") getString(R.string.export_title) else getString(R.string.import_title)
        }
        
        // Show the appropriate dialog based on mode
        if (mode == "export") {
            showExportDialog()
        } else {
            showImportDialog()
        }
    }
    
    private fun showExportDialog() {
        val options = arrayOf(
            getString(R.string.export_csv_option),
            getString(R.string.export_excel_option)
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.export_dialog_title))
            .setMessage(getString(R.string.select_export_format))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportAsCSV()
                    1 -> exportAsExcel()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                finish()
            }
            .setOnDismissListener {
                finish()
            }
            .show()
    }
    
    private fun showImportDialog() {
        val options = arrayOf(
            getString(R.string.import_csv_option),
            getString(R.string.import_excel_option)
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.import_dialog_title))
            .setMessage(getString(R.string.select_import_format))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> importFromCSV()
                    1 -> importFromExcel()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                finish()
            }
            .setOnDismissListener {
                finish()
            }
            .show()
    }
    
    private fun exportAsCSV() {
        // TODO: Implement CSV export functionality
        showFeatureComingSoonDialog()
    }
    
    private fun exportAsExcel() {
        // TODO: Implement Excel export functionality
        showFeatureComingSoonDialog()
    }
    
    private fun importFromCSV() {
        // TODO: Implement CSV import functionality
        showFeatureComingSoonDialog()
    }
    
    private fun importFromExcel() {
        // TODO: Implement Excel import functionality
        showFeatureComingSoonDialog()
    }
    
    private fun showFeatureComingSoonDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.feature_coming_soon))
            .setMessage(getString(R.string.export_import_placeholder))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                finish()
            }
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
