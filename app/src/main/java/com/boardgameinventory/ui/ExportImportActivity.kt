package com.boardgameinventory.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityExportImportBinding
import com.boardgameinventory.utils.ExportUtils
import com.boardgameinventory.utils.ImportUtils
import com.boardgameinventory.utils.Utils
import com.boardgameinventory.viewmodel.ExportImportViewModel
import kotlinx.coroutines.launch

class ExportImportActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityExportImportBinding
    private val viewModel: ExportImportViewModel by viewModels()
    private var currentMode = ""
    private var pendingImportUri: Uri? = null
    
    private val exportCSVLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.exportToCSV(uri)
            }
        }
    }
    
    private val exportExcelLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.exportToExcel(uri)
            }
        }
    }
    
    private val importCSVLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                pendingImportUri = uri
                showImportOptionsDialog(isCSV = true)
            }
        }
    }
    
    private val importExcelLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                pendingImportUri = uri
                showImportOptionsDialog(isCSV = false)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportImportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        currentMode = intent.getStringExtra("mode") ?: "export"
        
        setupToolbar()
        setupUI()
        observeViewModel()
        
        // Show the appropriate dialog based on mode after a short delay
        // to ensure the activity is fully created
        binding.root.post {
            if (currentMode == "export") {
                showExportDialog()
            } else {
                showImportDialog()
            }
        }
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (currentMode == "export") getString(R.string.export_title) else getString(R.string.import_title)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            if (currentMode == "export") {
                tvTitle.text = getString(R.string.export_title)
                tvDescription.text = "Choose export format and location for your games database."
            } else {
                tvTitle.text = getString(R.string.import_title)
                tvDescription.text = "Choose the file to import games from."
            }
            
            btnClose.setOnClickListener {
                finish()
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnClose.isEnabled = !isLoading
            }
        }
        
        lifecycleScope.launch {
            viewModel.message.collect { message ->
                if (message.isNotEmpty()) {
                    binding.tvStatus.text = message
                    binding.tvStatus.visibility = View.VISIBLE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.exportSuccess.collect { success ->
                if (success) {
                    showSuccessDialog("Export completed successfully!")
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.importSuccess.collect { result ->
                result?.let {
                    val message = getString(R.string.import_results_message, result.imported, result.skipped)
                    showSuccessDialog(message)
                }
            }
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
            .setCancelable(true)
            .setOnCancelListener { finish() }
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
            .setCancelable(true)
            .setOnCancelListener { finish() }
            .show()
    }
    
    private fun showImportOptionsDialog(isCSV: Boolean) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.overwrite_database_title))
            .setMessage(getString(R.string.overwrite_database_message))
            .setPositiveButton(getString(R.string.overwrite_database_yes)) { _, _ ->
                pendingImportUri?.let { uri ->
                    if (isCSV) {
                        viewModel.importFromCSV(uri, overwriteDatabase = true)
                    } else {
                        viewModel.importFromExcel(uri, overwriteDatabase = true)
                    }
                }
            }
            .setNegativeButton(getString(R.string.overwrite_database_no)) { _, _ ->
                pendingImportUri?.let { uri ->
                    if (isCSV) {
                        viewModel.importFromCSV(uri, overwriteDatabase = false)
                    } else {
                        viewModel.importFromExcel(uri, overwriteDatabase = false)
                    }
                }
            }
            .setNeutralButton(getString(R.string.cancel)) { _, _ ->
                finish()
            }
            .show()
    }
    
    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.success))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                setResult(RESULT_OK)
                finish()
            }
            .show()
    }
    
    private fun exportAsCSV() {
        binding.tvStatus.text = "Preparing CSV export..."
        binding.tvStatus.visibility = View.VISIBLE
        ExportUtils.exportToCSV(this, emptyList(), exportCSVLauncher)
    }
    
    private fun exportAsExcel() {
        binding.tvStatus.text = "Preparing Excel export..."
        binding.tvStatus.visibility = View.VISIBLE
        ExportUtils.exportToExcel(this, emptyList(), exportExcelLauncher)
    }
    
    private fun importFromCSV() {
        binding.tvStatus.text = "Select CSV file to import..."
        binding.tvStatus.visibility = View.VISIBLE
        ImportUtils.importFromCSV(this, importCSVLauncher)
    }
    
    private fun importFromExcel() {
        binding.tvStatus.text = "Select Excel file to import..."
        binding.tvStatus.visibility = View.VISIBLE
        ImportUtils.importFromExcel(this, importExcelLauncher)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
