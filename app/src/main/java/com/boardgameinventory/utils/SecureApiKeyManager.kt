package com.boardgameinventory.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.boardgameinventory.BuildConfig
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Securely manages API keys using Android Keystore and EncryptedSharedPreferences
 * Provides encryption, storage, and retrieval of sensitive API keys
 */
class SecureApiKeyManager(private val context: Context) {

    companion object {
        private const val TAG = "SecureApiKeyManager"
        private const val KEY_ALIAS = "api_key_encryption_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128

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
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences", e)
            null
        }
    }

    /**
     * Initialize API keys with build config values on first run
     * Should be called once from the Application class
     */
    fun initializeApiKeys() {
        try {
            if (encryptedPreferences?.contains(KEY_RAPIDAPI_KEY) != true) {
                // First run - encrypt and store the keys from BuildConfig
                val rapidApiKey = BuildConfig.RAPIDAPI_KEY
                val rapidApiHost = BuildConfig.RAPIDAPI_HOST

                if (rapidApiKey.isNotBlank() && rapidApiKey != "your_api_key_here") {
                    encryptedPreferences?.edit()
                        ?.putString(KEY_RAPIDAPI_KEY, rapidApiKey)
                        ?.putString(KEY_RAPIDAPI_HOST, rapidApiHost)
                        ?.apply()

                    Log.d(TAG, "API keys initialized successfully")
                } else {
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
    }

    /**
     * Get RapidAPI Key
     */
    fun getRapidApiKey(): String {
        return encryptedPreferences?.getString(KEY_RAPIDAPI_KEY, "") ?: ""
    }

    /**
     * Get RapidAPI Host
     */
    fun getRapidApiHost(): String {
        return encryptedPreferences?.getString(KEY_RAPIDAPI_HOST, "") ?: ""
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
}
