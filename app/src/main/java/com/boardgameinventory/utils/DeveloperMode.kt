package com.boardgameinventory.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.boardgameinventory.BuildConfig
import androidx.core.content.edit

/**
 * Developer Mode Utility
 * 
 * Manages developer mode access through secret gesture (7 taps on version number)
 * Only available in debug builds or when explicitly enabled
 */
object DeveloperMode {
    
    private const val PREF_NAME = "developer_mode_prefs"
    private const val KEY_DEVELOPER_MODE_ENABLED = "developer_mode_enabled"
    private const val KEY_LAST_ACCESS_TIME = "last_access_time"

    private const val REQUIRED_TAPS = 7
    private const val TAP_TIMEOUT_MS = 10000L // 10 seconds
    private const val DEVELOPER_MODE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    
    private var currentTapCount = 0
    private var firstTapTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var resetRunnable: Runnable? = null
    
    /**
     * Handle tap on version number for developer mode activation
     */
    fun handleVersionTap(context: Context): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Reset tap count if too much time has passed since first tap
        if (currentTapCount == 0 || (currentTime - firstTapTime) > TAP_TIMEOUT_MS) {
            currentTapCount = 0
            firstTapTime = currentTime
        }
        
        currentTapCount++
        
        // Cancel previous reset runnable
        resetRunnable?.let { handler.removeCallbacks(it) }
        
        when {
            currentTapCount == 1 -> {
                // Start timeout timer for tap sequence
                resetRunnable = Runnable {
                    currentTapCount = 0
                    firstTapTime = 0L
                }
                handler.postDelayed(resetRunnable!!, TAP_TIMEOUT_MS)
            }
            
            currentTapCount == REQUIRED_TAPS -> {
                // Enable developer mode
                enableDeveloperMode(context)
                currentTapCount = 0
                firstTapTime = 0L
                resetRunnable?.let { handler.removeCallbacks(it) }
                return true
            }
            
            currentTapCount > 3 -> {
                // Give feedback for progress
                val remaining = REQUIRED_TAPS - currentTapCount
                Toast.makeText(
                    context, 
                    "Developer mode: $remaining more taps", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        return false
    }
    
    /**
     * Check if developer mode is currently active
     */
    fun isDeveloperModeActive(context: Context): Boolean {
        // Always allow in debug builds
        if (BuildConfig.DEBUG) {
            return true
        }
        
        val prefs = getPrefs(context)
        val isEnabled = prefs.getBoolean(KEY_DEVELOPER_MODE_ENABLED, false)
        val lastAccessTime = prefs.getLong(KEY_LAST_ACCESS_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        
        // Check if developer mode has expired
        if (isEnabled && (currentTime - lastAccessTime) > DEVELOPER_MODE_DURATION_MS) {
            disableDeveloperMode(context)
            return false
        }
        
        return isEnabled
    }
    
    /**
     * Enable developer mode temporarily
     */
    private fun enableDeveloperMode(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit {
            putBoolean(KEY_DEVELOPER_MODE_ENABLED, true)
                .putLong(KEY_LAST_ACCESS_TIME, System.currentTimeMillis())
        }
        
        Toast.makeText(
            context, 
            "Developer mode enabled for 30 minutes", 
            Toast.LENGTH_LONG
        ).show()
    }
    
    /**
     * Disable developer mode
     */
    fun disableDeveloperMode(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit {
            putBoolean(KEY_DEVELOPER_MODE_ENABLED, false)
                .putLong(KEY_LAST_ACCESS_TIME, 0L)
        }
    }
    
    /**
     * Update last access time to extend developer mode session
     */
    fun updateLastAccess(context: Context) {
        if (isDeveloperModeActive(context)) {
            val prefs = getPrefs(context)
            prefs.edit {
                putLong(KEY_LAST_ACCESS_TIME, System.currentTimeMillis())
            }
        }
    }
    
    /**
     * Get developer info for display
     */
    fun getDeveloperInfo(context: Context): String {
        val prefs = getPrefs(context)
        val isEnabled = prefs.getBoolean(KEY_DEVELOPER_MODE_ENABLED, false)
        val lastAccessTime = prefs.getLong(KEY_LAST_ACCESS_TIME, 0L)
        
        return buildString {
            appendLine("Developer Mode: ${if (isEnabled) "ACTIVE" else "INACTIVE"}")
            appendLine("Debug Build: ${BuildConfig.DEBUG}")
            appendLine("Build Type: ${BuildConfig.BUILD_TYPE}")
            appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            
            if (isEnabled && lastAccessTime > 0) {
                val remainingTime = DEVELOPER_MODE_DURATION_MS - (System.currentTimeMillis() - lastAccessTime)
                val remainingMinutes = (remainingTime / 60000).toInt()
                appendLine("Time remaining: $remainingMinutes minutes")
            }
            
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        }
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
