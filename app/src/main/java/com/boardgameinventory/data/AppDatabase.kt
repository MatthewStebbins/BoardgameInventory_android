package com.boardgameinventory.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import android.util.Log

@Database(
    entities = [Game::class],
    version = 1,
    exportSchema = false // Simplified for now
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
                val instance = Room.databaseBuilder(
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
                .fallbackToDestructiveMigration() // Use only during development
                .build()
                
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
