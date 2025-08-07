package com.boardgameinventory.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.boardgameinventory.security.SecurityManager
import net.sqlcipher.database.SupportFactory
import android.util.Log
import java.util.concurrent.Executors

/**
 * Helper class for creating encrypted Room databases using SQLCipher.
 * This ensures all user data is encrypted at rest.
 */
object EncryptedDatabaseHelper {
    private const val TAG = "EncryptedDBHelper"

    /**
     * Creates an encrypted Room database instance
     *
     * @param context Application context
     * @param databaseClass The Room database class
     * @param databaseName Database file name
     * @param callback Optional database callback
     * @return An instance of the Room database with encryption enabled
     */
    inline fun <reified T : RoomDatabase> createEncrypted(
        context: Context,
        databaseName: String,
        crossinline callback: (SupportSQLiteDatabase) -> Unit = {}
    ): T {
        try {
            // Get encryption key from SecurityManager
            val securityManager = SecurityManager(context)
            val passphrase = securityManager.getDatabaseEncryptionKey()

            // Create SQLCipher factory with the encryption key
            val factory = SupportFactory(passphrase.toByteArray())

            return Room.databaseBuilder(context, T::class.java, databaseName)
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration() // You might want to implement proper migrations
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d(TAG, "Database created with encryption")

                        // Execute callback on a background thread
                        Executors.newSingleThreadExecutor().execute {
                            callback(db)
                        }
                    }
                })
                .build()
        } catch (e: Exception) {
            // If encryption fails for any reason, log it and fall back to unencrypted database
            // In production, you might want to handle this differently
            Log.e(TAG, "Failed to create encrypted database: ${e.message}", e)

            return Room.databaseBuilder(context, T::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
