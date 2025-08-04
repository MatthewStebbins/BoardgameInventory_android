package com.boardgameinventory.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityDatabaseManagementBinding
import com.boardgameinventory.utils.DeveloperMode
import com.boardgameinventory.viewmodel.DatabaseManagementViewModel
import com.boardgameinventory.viewmodel.OperationType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Database Management Activity
 * 
 * Provides UI for:
 * - Database health monitoring
 * - Backup creation and restoration
 * - Migration management
 * - Maintenance scheduling
 */
class DatabaseManagementActivity : BaseAdActivity() {
    
    private lateinit var binding: ActivityDatabaseManagementBinding
    private val viewModel: DatabaseManagementViewModel by viewModels()
    private lateinit var backupAdapter: BackupListAdapter
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatabaseManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Database Management"
        }
    }
    
    private fun setupRecyclerView() {
        backupAdapter = BackupListAdapter { backupInfo ->
            showRestoreConfirmation(backupInfo.fileName)
        }
        
        binding.recyclerViewBackups.apply {
            layoutManager = LinearLayoutManager(this@DatabaseManagementActivity)
            adapter = backupAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            btnHealthCheck.setOnClickListener {
                viewModel.performHealthCheck()
            }
            
            btnCreateBackup.setOnClickListener {
                viewModel.createBackup()
            }
            
            btnScheduleMaintenance.setOnClickListener {
                viewModel.scheduleAutomaticMaintenance()
            }
            
            btnRefreshBackups.setOnClickListener {
                viewModel.loadAvailableBackups()
            }
            
            cardDatabaseStats.setOnClickListener {
                showDatabaseStatsDialog()
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.apply {
            isLoading.observe(this@DatabaseManagementActivity) { isLoading ->
                binding.progressBar.visibility = if (isLoading) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                // Disable buttons during loading
                binding.apply {
                    btnHealthCheck.isEnabled = !isLoading
                    btnCreateBackup.isEnabled = !isLoading
                    btnScheduleMaintenance.isEnabled = !isLoading
                    btnRefreshBackups.isEnabled = !isLoading
                }
            }
            
            healthReport.observe(this@DatabaseManagementActivity) { report ->
                updateHealthDisplay(report)
            }
            
            availableBackups.observe(this@DatabaseManagementActivity) { backups ->
                backupAdapter.submitList(backups)
                binding.textBackupCount.text = "Available backups: ${backups.size}"
            }
            
            operationResult.observe(this@DatabaseManagementActivity) { result ->
                result?.let {
                    showOperationResult(it)
                    viewModel.clearOperationResult()
                }
            }
            
            databaseVersion.observe(this@DatabaseManagementActivity) { version ->
                binding.textDatabaseVersion.text = "Database Version: $version"
            }
        }
    }
    
    private fun updateHealthDisplay(report: com.boardgameinventory.data.DatabaseHealthReport) {
        binding.apply {
            // Health status
            val healthColor = if (report.isHealthy) {
                getColor(android.R.color.holo_green_dark)
            } else {
                getColor(android.R.color.holo_red_dark)
            }
            
            textHealthStatus.text = if (report.isHealthy) "Healthy" else "Issues Detected"
            textHealthStatus.setTextColor(healthColor)
            
            // Statistics
            textRecordCount.text = "Total Records: ${report.totalRecords}"
            textDatabaseSize.text = "Database Size: ${report.databaseSizeBytes / 1024} KB"
            textCorruptedRecords.text = "Corrupted Records: ${report.corruptedRecords}"
            
            // Last backup
            if (report.lastBackupDate > 0) {
                textLastBackup.text = "Last Backup: ${dateFormat.format(Date(report.lastBackupDate))}"
            } else {
                textLastBackup.text = "Last Backup: Never"
            }
            
            // Show error message if unhealthy
            if (!report.isHealthy && !report.errorMessage.isNullOrBlank()) {
                textHealthDetails.text = "Error: ${report.errorMessage}"
                textHealthDetails.visibility = android.view.View.VISIBLE
            } else {
                textHealthDetails.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun showOperationResult(result: com.boardgameinventory.viewmodel.OperationResult) {
        val message = when (result.type) {
            OperationType.HEALTH_CHECK -> "Health Check: ${result.message}"
            OperationType.BACKUP -> "Backup: ${result.message}"
            OperationType.RESTORE -> "Restore: ${result.message}"
            OperationType.SCHEDULE_MAINTENANCE -> "Maintenance: ${result.message}"
            OperationType.TRIGGER_BACKUP -> "Backup: ${result.message}"
            else -> result.message
        }
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun showRestoreConfirmation(fileName: String) {
        AlertDialog.Builder(this)
            .setTitle("Restore Database")
            .setMessage("Are you sure you want to restore from backup '$fileName'? This will replace all current data.")
            .setPositiveButton("Restore") { _, _ ->
                viewModel.restoreFromBackup()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDatabaseStatsDialog() {
        val stats = viewModel.getDatabaseStats()
        val message = """
            Version: ${stats.version}
            Total Records: ${stats.totalRecords}
            Database Size: ${stats.databaseSizeKB} KB
            Health Status: ${if (stats.isHealthy) "Healthy" else "Issues"}
            Available Backups: ${stats.backupCount}
            Last Backup: ${if (stats.lastBackupDate > 0) dateFormat.format(Date(stats.lastBackupDate)) else "Never"}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Database Statistics")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Show unauthorized access dialog and close activity
     */
    private fun showUnauthorizedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Access Denied")
            .setMessage("Developer mode required to access database management.\n\nTo enable developer mode, find the app version number and tap it 7 times quickly.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Show developer info dialog
     */
    private fun showDeveloperInfo() {
        val info = DeveloperMode.getDeveloperInfo(this)
        
        AlertDialog.Builder(this)
            .setTitle("Developer Information")
            .setMessage(info)
            .setPositiveButton("OK", null)
            .setNegativeButton("Disable Developer Mode") { _, _ ->
                DeveloperMode.disableDeveloperMode(this)
                finish()
            }
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Re-check developer mode on resume
        if (!DeveloperMode.isDeveloperModeActive(this)) {
            Toast.makeText(this, "Developer mode expired", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Update access time
        DeveloperMode.updateLastAccess(this)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
