package com.boardgameinventory.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import android.util.Log
import com.boardgameinventory.data.db.EncryptedDatabaseHelper
import com.boardgameinventory.BuildConfig

@Database(
    entities = [Game::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun gameDao(): GameDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val TAG = "AppDatabase"
        private const val DATABASE_NAME = "games_database"
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = if (BuildConfig.DEBUG) {
                    // Use unencrypted database in debug builds for easier debugging
                    Log.d(TAG, "Creating unencrypted database (DEBUG mode)")
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    )
                    .addMigrations(*DatabaseMigrations.getAllMigrations())
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "Database created with version ${db.version}")
                            // Note: Index creation moved to avoid callback restrictions
                        }

                        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d(TAG, "Database opened with version ${db.version}")
                            // Note: PRAGMA statements moved to avoid callback restrictions
                        }
                    })
                    .build()
                } else {
                    // Use encrypted database in release builds for security
                    Log.d(TAG, "Creating encrypted database (RELEASE mode)")
                    EncryptedDatabaseHelper.createEncrypted<AppDatabase>(
                        context.applicationContext,
                        DATABASE_NAME
                    ) { db ->
                        Log.d(TAG, "Encrypted database created with version ${db.version}")
                    }
                }

                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Create performance indexes for the database
         */
        private fun createIndexes(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            try {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_games_name ON games(name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_games_barcode ON games(barcode)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_games_bookcase ON games(bookcase)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_games_loanedTo ON games(loanedTo)")
                Log.d(TAG, "Database indexes created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating database indexes", e)
            }
        }
        
        /**
         * Get database for testing with custom configuration
         */
        fun getTestDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
            .addMigrations(*DatabaseMigrations.getAllMigrations())
            .allowMainThreadQueries()
            .build()
        }
        
        /**
         * Clear database instance (for testing)
         */
        fun clearInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
