package com.boardgameinventory.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.boardgameinventory.databinding.ActivityDeveloperSettingsBinding
import com.boardgameinventory.utils.DeveloperMode

/**
 * Developer Settings Activity
 * 
 * Only accessible when developer mode is active.
 * Provides access to developer tools and database management.
 */
class DeveloperSettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDeveloperSettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Security check: Verify developer mode access
        if (!DeveloperMode.isDeveloperModeActive(this)) {
            showUnauthorizedDialog()
            return
        }
        
        // Update last access time to extend session
        DeveloperMode.updateLastAccess(this)
        
        binding = ActivityDeveloperSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        updateDeveloperInfo()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Developer Settings"
        }
    }
    
    private fun setupClickListeners() {
        binding.btnDatabaseManagement.setOnClickListener {
            startActivity(Intent(this, DatabaseManagementActivity::class.java))
        }
        
        binding.btnDeveloperInfo.setOnClickListener {
            showDeveloperInfo()
        }
        
        binding.btnDisableDeveloperMode.setOnClickListener {
            showDisableDeveloperModeDialog()
        }
        
        binding.btnRefreshInfo.setOnClickListener {
            updateDeveloperInfo()
        }
    }
    
    private fun updateDeveloperInfo() {
        val info = DeveloperMode.getDeveloperInfo(this)
        binding.tvDeveloperInfo.text = info
    }
    
    private fun showUnauthorizedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Access Denied")
            .setMessage("Developer mode required to access developer settings.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showDeveloperInfo() {
        val info = DeveloperMode.getDeveloperInfo(this)
        
        AlertDialog.Builder(this)
            .setTitle("Developer Information")
            .setMessage(info)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showDisableDeveloperModeDialog() {
        AlertDialog.Builder(this)
            .setTitle("Disable Developer Mode")
            .setMessage("Are you sure you want to disable developer mode? This will close all developer tools.")
            .setPositiveButton("Disable") { _, _ ->
                DeveloperMode.disableDeveloperMode(this)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Re-check developer mode on resume
        if (!DeveloperMode.isDeveloperModeActive(this)) {
            finish()
            return
        }
        
        // Update access time and refresh info
        DeveloperMode.updateLastAccess(this)
        updateDeveloperInfo()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
