package com.boardgameinventory.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.boardgameinventory.BuildConfig
import java.lang.ref.WeakReference


/**
 * Securely manages API keys using Android Keystore and EncryptedSharedPreferences
 * Provides encryption, storage, and retrieval of sensitive API keys
 */
class SecureApiKeyManager(private val context: Context) {

    companion object {
        private const val TAG = "SecureApiKeyManager"
        private const val ENCRYPTED_PREFS_FILE = "encrypted_api_keys"
        private const val KEY_RAPIDAPI_KEY = "rapidapi_key"
        private const val KEY_RAPIDAPI_HOST = "rapidapi_host"
        private const val KEY_ADMOB_APP_ID = "admob_app_id" // Added AdMob App ID key
        private const val KEY_ADMOB_BANNER_ID = "admob_banner_id" // Added AdMob Banner ID key

        private var INSTANCE: WeakReference<SecureApiKeyManager>? = null
        get() = synchronized(this) { field }

        fun getInstance(context: Context): SecureApiKeyManager {
            return INSTANCE?.get() ?: synchronized(this) {
                val instance = SecureApiKeyManager(context.applicationContext)
                INSTANCE = WeakReference(instance)
                instance
            }
        }
    }

    private val masterKey by lazy {
        try {
            println("[DEBUG] Initializing MasterKey")
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        } catch (e: Exception) {
            println("[ERROR] Failed to initialize MasterKey: ${e.message}")
            e.printStackTrace()
            null // Fallback to null if MasterKey initialization fails
        }
    }

    private val encryptedPreferences by lazy {
        try {
            println("[DEBUG] Initializing EncryptedSharedPreferences")
            masterKey?.let {
                EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILE,
                    it,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } ?: throw IllegalStateException("MasterKey is null. Cannot initialize EncryptedSharedPreferences.")
        } catch (e: Exception) {
            println("[ERROR] Failed to initialize EncryptedSharedPreferences: ${e.message}")
            e.printStackTrace()
            println("[DEBUG] Falling back to regular SharedPreferences")
            context.getSharedPreferences(ENCRYPTED_PREFS_FILE, Context.MODE_PRIVATE)
        }
    }

    init {
        println("[DEBUG] SecureApiKeyManager initialized with context: $context")
    }

    /**
     * Initialize API keys with build config values on first run
     * Should be called once from the Application class
     */
    fun initializeApiKeys() {
        try {
            println("[DEBUG] Checking if encryptedPreferences contains KEY_RAPIDAPI_KEY: ${encryptedPreferences?.contains(KEY_RAPIDAPI_KEY)}")
            println("[DEBUG] Retrieved BuildConfig.RAPIDAPI_KEY: ${BuildConfig.RAPIDAPI_KEY}")
            println("[DEBUG] Retrieved BuildConfig.RAPIDAPI_HOST: ${BuildConfig.RAPIDAPI_HOST}")

            if (encryptedPreferences?.contains(KEY_RAPIDAPI_KEY) != true) {
                // First run - encrypt and store the keys from BuildConfig
                val rapidApiKey = BuildConfig.RAPIDAPI_KEY
                val rapidApiHost = BuildConfig.RAPIDAPI_HOST

                if (rapidApiKey.isNotBlank()) {
                    println("[DEBUG] Storing API keys in encryptedPreferences")
                    encryptedPreferences?.edit {
                        putString(KEY_RAPIDAPI_KEY, rapidApiKey)
                        putString(KEY_RAPIDAPI_HOST, rapidApiHost)
                    }

                    Log.d(TAG, "API keys initialized successfully")
                } else {
                    println("[DEBUG] API key is blank or default, skipping initialization")
                    Log.w(TAG, "API key is blank or default - not initializing")
                }
            }

            // Initialize AdMob IDs if not already stored
            if (encryptedPreferences?.contains(KEY_ADMOB_APP_ID) != true) {
                // Store AdMob App ID from BuildConfig
                val adMobAppId = BuildConfig.ADMOB_APP_ID
                val adMobBannerId = BuildConfig.ADMOB_BANNER_ID

                if (adMobAppId.isNotBlank()) {
                    encryptedPreferences?.edit {
                        putString(KEY_ADMOB_APP_ID, adMobAppId)
                        putString(KEY_ADMOB_BANNER_ID, adMobBannerId)
                    }

                    Log.d(TAG, "AdMob IDs initialized successfully")
                } else {
                    Log.w(TAG, "AdMob ID is blank or default - not initializing")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing secure keys", e)
        }

        // Debug: Log the state of encryptedPreferences after initialization
        println("[DEBUG] EncryptedPreferences state after initialization: ${encryptedPreferences?.all}")
    }

    /**
     * Get RapidAPI Key
     */
    fun getRapidApiKey(): String {
        val apiKey = encryptedPreferences?.getString(KEY_RAPIDAPI_KEY, "") ?: ""
        println("[DEBUG] Retrieved RapidAPI Key from encryptedPreferences: $apiKey")
        return apiKey
    }

    /**
     * Get RapidAPI Host
     */
    fun getRapidApiHost(): String {
        val apiHost = encryptedPreferences?.getString(KEY_RAPIDAPI_HOST, "") ?: ""
        println("[DEBUG] Retrieved RapidAPI Host from encryptedPreferences: $apiHost")
        return apiHost
    }

}
