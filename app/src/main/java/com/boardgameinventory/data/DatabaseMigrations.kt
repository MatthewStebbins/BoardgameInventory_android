package com.boardgameinventory.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration strategies for the Board Game Inventory app.
 * 
 * Follow these guidelines when adding new migrations:
 * 1. Always test migrations with real data
 * 2. Never delete existing columns without proper migration
 * 3. Use ALTER TABLE for schema changes when possible
 * 4. Create new tables carefully with proper constraints
 * 5. Always preserve user data during migrations
 */
object DatabaseMigrations {
    
    /**
     * Migration from version 1 to 2
     * Example: Adding yearPublished field to games table
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add yearPublished column to games table
            database.execSQL("ALTER TABLE games ADD COLUMN yearPublished INTEGER")
            
            // Set default value for existing records
            database.execSQL("UPDATE games SET yearPublished = 0 WHERE yearPublished IS NULL")
        }
    }
    
    /**
     * Migration from version 2 to 3
     * Example: Adding categories and tags support
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add category and tags columns
            database.execSQL("ALTER TABLE games ADD COLUMN category TEXT")
            database.execSQL("ALTER TABLE games ADD COLUMN tags TEXT")
            
            // Set default values
            database.execSQL("UPDATE games SET category = 'Uncategorized' WHERE category IS NULL")
            database.execSQL("UPDATE games SET tags = '' WHERE tags IS NULL")
        }
    }
    
    /**
     * Migration from version 3 to 4
     * Example: Adding rating and notes fields
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add rating and notes columns
            database.execSQL("ALTER TABLE games ADD COLUMN rating REAL DEFAULT 0.0")
            database.execSQL("ALTER TABLE games ADD COLUMN notes TEXT")
            database.execSQL("ALTER TABLE games ADD COLUMN playCount INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE games ADD COLUMN lastPlayed INTEGER")
        }
    }
    
    /**
     * Migration from version 4 to 5
     * Example: Adding game conditions and purchase info
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add condition and purchase information
            database.execSQL("ALTER TABLE games ADD COLUMN condition TEXT DEFAULT 'Good'")
            database.execSQL("ALTER TABLE games ADD COLUMN purchasePrice REAL")
            database.execSQL("ALTER TABLE games ADD COLUMN purchaseDate INTEGER")
            database.execSQL("ALTER TABLE games ADD COLUMN retailer TEXT")
        }
    }
    
    /**
     * Migration from version 5 to 6
     * Example: Adding comprehensive game metadata
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add comprehensive metadata fields
            database.execSQL("ALTER TABLE games ADD COLUMN minPlayers INTEGER")
            database.execSQL("ALTER TABLE games ADD COLUMN maxPlayers INTEGER")
            database.execSQL("ALTER TABLE games ADD COLUMN playingTime INTEGER")
            database.execSQL("ALTER TABLE games ADD COLUMN minAge INTEGER")
            database.execSQL("ALTER TABLE games ADD COLUMN designer TEXT")
            database.execSQL("ALTER TABLE games ADD COLUMN publisher TEXT")
            database.execSQL("ALTER TABLE games ADD COLUMN bggId INTEGER")
        }
    }
    
    /**
     * Destructive migration fallback
     * WARNING: This will delete all data and recreate the database
     * Only use as a last resort during development
     */
    val DESTRUCTIVE_FALLBACK = object : Migration(1, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Drop existing table
            database.execSQL("DROP TABLE IF EXISTS games")
            
            // Recreate table with latest schema
            database.execSQL("""
                CREATE TABLE games (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    barcode TEXT NOT NULL,
                    bookcase TEXT NOT NULL,
                    shelf TEXT NOT NULL,
                    loanedTo TEXT,
                    description TEXT,
                    imageUrl TEXT,
                    dateAdded INTEGER NOT NULL,
                    dateLoaned INTEGER,
                    yearPublished INTEGER,
                    category TEXT,
                    tags TEXT,
                    rating REAL DEFAULT 0.0,
                    notes TEXT,
                    playCount INTEGER DEFAULT 0,
                    lastPlayed INTEGER,
                    condition TEXT DEFAULT 'Good',
                    purchasePrice REAL,
                    purchaseDate INTEGER,
                    retailer TEXT,
                    minPlayers INTEGER,
                    maxPlayers INTEGER,
                    playingTime INTEGER,
                    minAge INTEGER,
                    designer TEXT,
                    publisher TEXT,
                    bggId INTEGER
                )
            """)
            
            // Create indexes for better performance
            database.execSQL("CREATE INDEX index_games_name ON games(name)")
            database.execSQL("CREATE INDEX index_games_barcode ON games(barcode)")
            database.execSQL("CREATE INDEX index_games_bookcase ON games(bookcase)")
            database.execSQL("CREATE INDEX index_games_loanedTo ON games(loanedTo)")
            database.execSQL("CREATE INDEX index_games_category ON games(category)")
        }
    }
    
    /**
     * Get all migrations in order
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6
        )
    }
    
    /**
     * Migration testing utility
     * Validates that a migration preserves data integrity
     */
    fun validateMigration(migration: Migration, database: SupportSQLiteDatabase): Boolean {
        return try {
            // Count records before migration
            val beforeCursor = database.query("SELECT COUNT(*) FROM games")
            beforeCursor.moveToFirst()
            val recordCountBefore = beforeCursor.getInt(0)
            beforeCursor.close()
            
            // Apply migration
            migration.migrate(database)
            
            // Count records after migration
            val afterCursor = database.query("SELECT COUNT(*) FROM games")
            afterCursor.moveToFirst()
            val recordCountAfter = afterCursor.getInt(0)
            afterCursor.close()
            
            // Validate that no data was lost
            recordCountBefore == recordCountAfter
        } catch (e: Exception) {
            false
        }
    }
}
