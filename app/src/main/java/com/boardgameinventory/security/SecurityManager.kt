package com.boardgameinventory.security

import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

/**
 * Security manager class that handles encryption keys and secure storage.
 * Uses Android Security library for modern encryption practices.
 */
class SecurityManager(private val context: Context) {

    companion object {
        private const val ENCRYPTED_PREFS_FILE = "secure_app_prefs"
        private const val DB_KEY_ALIAS = "database_encryption_key"
        private const val KEY_SIZE = 256
        private const val ALGORITHM = "AES"
        private const val TAG = "SecurityManager"
    }

    // Master key for encrypted shared preferences
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    // Encrypted SharedPreferences instance
    private val securePreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Retrieves the database encryption key, generating one if it doesn't exist
     */
    fun getDatabaseEncryptionKey(): String {
        var encryptionKey = securePreferences.getString(DB_KEY_ALIAS, null)

        if (encryptionKey == null) {
            encryptionKey = generateAndStoreNewKey()
        }

        return encryptionKey
    }

    /**
     * Generates a new secure encryption key and stores it
     */
    private fun generateAndStoreNewKey(): String {
        val key = generateSecureAesKey()
        val keyString = Base64.encodeToString(key.encoded, Base64.NO_WRAP)
        securePreferences.edit().putString(DB_KEY_ALIAS, keyString).apply()
        return keyString
    }

    /**
     * Generates a cryptographically secure AES key
     */
    private fun generateSecureAesKey(): SecretKey {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Use Android KeyGenerator for API 23+
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
            keyGenerator.init(KEY_SIZE)
            keyGenerator.generateKey()
        } else {
            // Fallback for older devices
            val secureRandom = SecureRandom()
            val keyBytes = ByteArray(KEY_SIZE / 8)
            secureRandom.nextBytes(keyBytes)
            SecretKeySpec(keyBytes, ALGORITHM)
        }
    }

    /**
     * Securely stores a string value
     */
    fun secureStoreString(key: String, value: String) {
        securePreferences.edit().putString(key, value).apply()
    }

    /**
     * Retrieves a securely stored string value
     */
    fun secureRetrieveString(key: String, defaultValue: String? = null): String? {
        return securePreferences.getString(key, defaultValue)
    }

    /**
     * Removes a securely stored value
     */
    fun secureRemoveValue(key: String) {
        securePreferences.edit().remove(key).apply()
    }
}
