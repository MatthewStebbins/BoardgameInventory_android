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
        
        setupToolbar()
        setupUI()
        observeViewModel()
        setupButtonClickListeners()
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.export_import_title)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            tvTitle.text = getString(R.string.export_import_title)
            
            btnClose.setOnClickListener {
                finish()
            }
        }
    }
    
    private fun setupButtonClickListeners() {
        binding.apply {
            btnExportCsv.setOnClickListener { exportAsCSV() }
            btnExportExcel.setOnClickListener { exportAsExcel() }
            btnImportCsv.setOnClickListener { importFromCSV() }
            btnImportExcel.setOnClickListener { importFromExcel() }
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
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "games_export_${System.currentTimeMillis()}.csv")
        }
        exportCSVLauncher.launch(intent)
    }
    
    private fun exportAsExcel() {
        binding.tvStatus.text = "Preparing Excel export..."
        binding.tvStatus.visibility = View.VISIBLE
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_TITLE, "games_export_${System.currentTimeMillis()}.xlsx")
        }
        exportExcelLauncher.launch(intent)
    }
    
    private fun importFromCSV() {
        binding.tvStatus.text = "Select CSV file to import..."
        binding.tvStatus.visibility = View.VISIBLE
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
        }
        importCSVLauncher.launch(intent)
    }
    
    private fun importFromExcel() {
        binding.tvStatus.text = "Select Excel file to import..."
        binding.tvStatus.visibility = View.VISIBLE
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        importExcelLauncher.launch(intent)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
