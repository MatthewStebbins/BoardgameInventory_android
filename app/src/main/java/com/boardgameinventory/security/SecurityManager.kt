package com.boardgameinventory.security

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

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
        securePreferences.edit { putString(DB_KEY_ALIAS, keyString) }
        return keyString
    }

    /**
     * Generates a cryptographically secure AES key
     */
    private fun generateSecureAesKey(): SecretKey {
        // Simplified: Removed fallback for SDK < 23
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey()
    }

}
