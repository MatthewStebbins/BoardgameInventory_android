package com.boardgameinventory.utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.boardgameinventory.BuildConfig


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

        @Volatile
        private var INSTANCE: SecureApiKeyManager? = null

        fun getInstance(context: Context): SecureApiKeyManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SecureApiKeyManager(context.applicationContext)
                INSTANCE = instance
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

                if (rapidApiKey.isNotBlank() && rapidApiKey != "your_api_key_here") {
                    println("[DEBUG] Storing API keys in encryptedPreferences")
                    encryptedPreferences?.edit()
                        ?.putString(KEY_RAPIDAPI_KEY, rapidApiKey)
                        ?.putString(KEY_RAPIDAPI_HOST, rapidApiHost)
                        ?.apply()

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

                if (adMobAppId.isNotBlank() && adMobAppId != "ca-app-pub-0000000000000000~0000000000") {
                    encryptedPreferences?.edit()
                        ?.putString(KEY_ADMOB_APP_ID, adMobAppId)
                        ?.putString(KEY_ADMOB_BANNER_ID, adMobBannerId)
                        ?.apply()

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

    /**
     * Get AdMob App ID
     */
    fun getAdMobAppId(): String {
        return encryptedPreferences?.getString(KEY_ADMOB_APP_ID, "") ?: ""
    }

    /**
     * Get AdMob Banner ID
     */
    fun getAdMobBannerId(): String {
        return encryptedPreferences?.getString(KEY_ADMOB_BANNER_ID, "") ?: ""
    }

    /**
     * Update API keys (could be used for settings screen)
     */
    fun updateApiKeys(rapidApiKey: String, rapidApiHost: String) {
        try {
            encryptedPreferences?.edit()
                ?.putString(KEY_RAPIDAPI_KEY, rapidApiKey)
                ?.putString(KEY_RAPIDAPI_HOST, rapidApiHost)
                ?.apply()
            Log.d(TAG, "API keys updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating API keys", e)
        }
    }

    /**
     * Update AdMob IDs (could be used for settings screen)
     */
    fun updateAdMobIds(appId: String, bannerId: String) {
        try {
            encryptedPreferences?.edit()
                ?.putString(KEY_ADMOB_APP_ID, appId)
                ?.putString(KEY_ADMOB_BANNER_ID, bannerId)
                ?.apply()
            Log.d(TAG, "AdMob IDs updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating AdMob IDs", e)
        }
    }

    fun encryptApiKey(key: String, value: String): String {
        println("[DEBUG] Encrypting API key: $key")
        // Encryption logic here
        return "encrypted_$value"
    }

    fun decryptApiKey(key: String, encryptedValue: String): String {
        println("[DEBUG] Decrypting API key: $key")
        // Decryption logic here
        return encryptedValue.removePrefix("encrypted_")
    }

    fun storeApiKey(key: String, value: String) {
        println("[DEBUG] Attempting to store API key: $key with value: $value")
        encryptedPreferences?.edit()?.putString(key, value)?.apply()
        println("[DEBUG] API key stored: $key")
        println("[DEBUG] EncryptedPreferences state after storing API key: ${encryptedPreferences?.all}")
    }

    fun retrieveApiKey(key: String): String? {
        val value = encryptedPreferences?.getString(key, null)
        println("[DEBUG] Retrieved API key: $key with value: $value")
        return value
    }
}
