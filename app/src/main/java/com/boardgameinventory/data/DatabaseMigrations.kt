package com.boardgameinventory.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database    /**
     * Migration from version 6 to 7
     * Fix field nullability and constraint issues
     */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create a new table with the correct schema
            db.execSQL("""
                CREATE TABLE games_new (
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
                    rating REAL,
                    notes TEXT,
                    playCount INTEGER,
                    lastPlayed INTEGER,
                    condition TEXT,
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
            """.trimIndent())
            
            // Copy data from old table to new table with proper handling of nullable fields
            db.execSQL("""
                INSERT INTO games_new (
                    id, name, barcode, bookcase, shelf, loanedTo, description, imageUrl,
                    dateAdded, dateLoaned, yearPublished, category, tags, rating, notes,
                    playCount, lastPlayed, condition, purchasePrice, purchaseDate, retailer,
                    minPlayers, maxPlayers, playingTime, minAge, designer, publisher, bggId
                )
                SELECT 
                    id, name, barcode, bookcase, shelf, loanedTo, description, imageUrl,
                    dateAdded, dateLoaned, yearPublished, category, tags, rating, notes,
                    playCount, lastPlayed, condition, purchasePrice, purchaseDate, retailer,
                    minPlayers, maxPlayers, playingTime, minAge, designer, publisher, bggId
                FROM games
            """.trimIndent())
            
            // Drop the old table
            db.execSQL("DROP TABLE games")
            
            // Rename the new table
            db.execSQL("ALTER TABLE games_new RENAME TO games")
            
            // Recreate indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS index_games_name ON games(name)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_games_barcode ON games(barcode)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_games_bookcase ON games(bookcase)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_games_loanedTo ON games(loanedTo)")
        }
    }

    /**
     * Get all available migrations
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7
        )
    }n strategies for the Board Game Inventory app.
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
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add yearPublished column to games table
            db.execSQL("ALTER TABLE games ADD COLUMN yearPublished INTEGER")
            
            // Set default value for existing records
            db.execSQL("UPDATE games SET yearPublished = 0 WHERE yearPublished IS NULL")
        }
    }
    
    /**
     * Migration from version 2 to 3
     * Example: Adding categories and tags support
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add category and tags columns
            db.execSQL("ALTER TABLE games ADD COLUMN category TEXT")
            db.execSQL("ALTER TABLE games ADD COLUMN tags TEXT")
            
            // Set default values
            db.execSQL("UPDATE games SET category = 'Uncategorized' WHERE category IS NULL")
            db.execSQL("UPDATE games SET tags = '' WHERE tags IS NULL")
        }
    }
    
    /**
     * Migration from version 3 to 4
     * Example: Adding rating and notes fields
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add rating and notes columns
            db.execSQL("ALTER TABLE games ADD COLUMN rating REAL DEFAULT 0.0")
            db.execSQL("ALTER TABLE games ADD COLUMN notes TEXT")
            db.execSQL("ALTER TABLE games ADD COLUMN playCount INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE games ADD COLUMN lastPlayed INTEGER")
        }
    }
    
    /**
     * Migration from version 4 to 5
     * Example: Adding game conditions and purchase info
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add condition and purchase information
            db.execSQL("ALTER TABLE games ADD COLUMN condition TEXT DEFAULT 'Good'")
            db.execSQL("ALTER TABLE games ADD COLUMN purchasePrice REAL")
            db.execSQL("ALTER TABLE games ADD COLUMN purchaseDate INTEGER")
            db.execSQL("ALTER TABLE games ADD COLUMN retailer TEXT")
        }
    }
    
    /**
     * Migration from version 5 to 6
     * Example: Adding comprehensive game metadata
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add comprehensive metadata fields
            db.execSQL("ALTER TABLE games ADD COLUMN minPlayers INTEGER")
            db.execSQL("ALTER TABLE games ADD COLUMN maxPlayers INTEGER")
            db.execSQL("ALTER TABLE games ADD COLUMN playingTime INTEGER")
            db.execSQL("ALTER TABLE games ADD COLUMN minAge INTEGER")
            db.execSQL("ALTER TABLE games ADD COLUMN designer TEXT")
            db.execSQL("ALTER TABLE games ADD COLUMN publisher TEXT")
            db.execSQL("ALTER TABLE games ADD COLUMN bggId INTEGER")
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

}
