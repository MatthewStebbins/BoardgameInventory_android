package com.boardgameinventory.data

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.IOException

/**
 * Migration testing utilities for the Board Game Inventory database.
 * 
 * These tests ensure that database migrations preserve data integrity
 * and properly transform the schema between versions.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTests {
    
    private val testDbName = "migration_test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName
    )
    
    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(testDbName, 1).apply {
            // Insert test data for version 1
            execSQL("""
                INSERT INTO games (id, name, barcode, bookcase, shelf, dateAdded) 
                VALUES (1, 'Test Game', '123456789', 'A', '1', ${System.currentTimeMillis()})
            """)
            close()
        }
        
        // Apply migration
        db = helper.runMigrationsAndValidate(testDbName, 2, true, DatabaseMigrations.MIGRATION_1_2)
        
        // Verify migration results
        val cursor = db.query("SELECT * FROM games WHERE id = 1")
        assert(cursor.moveToFirst())
        
        // Check that yearPublished column exists and has default value
        val yearPublishedIndex = cursor.getColumnIndex("yearPublished")
        assert(yearPublishedIndex >= 0)
        assert(cursor.getInt(yearPublishedIndex) == 0)
        
        cursor.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        var db = helper.createDatabase(testDbName, 2).apply {
            // Insert test data for version 2
            execSQL("""
                INSERT INTO games (id, name, barcode, bookcase, shelf, dateAdded, yearPublished) 
                VALUES (1, 'Test Game', '123456789', 'A', '1', ${System.currentTimeMillis()}, 2023)
            """)
            close()
        }
        
        // Apply migration
        db = helper.runMigrationsAndValidate(testDbName, 3, true, DatabaseMigrations.MIGRATION_2_3)
        
        // Verify migration results
        val cursor = db.query("SELECT * FROM games WHERE id = 1")
        assert(cursor.moveToFirst())
        
        // Check that category and tags columns exist
        val categoryIndex = cursor.getColumnIndex("category")
        val tagsIndex = cursor.getColumnIndex("tags")
        assert(categoryIndex >= 0)
        assert(tagsIndex >= 0)
        assert(cursor.getString(categoryIndex) == "Uncategorized")
        assert(cursor.getString(tagsIndex) == "")
        
        cursor.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        var db = helper.createDatabase(testDbName, 3).apply {
            execSQL("""
                INSERT INTO games (id, name, barcode, bookcase, shelf, dateAdded, yearPublished, category, tags) 
                VALUES (1, 'Test Game', '123456789', 'A', '1', ${System.currentTimeMillis()}, 2023, 'Strategy', 'euro,worker-placement')
            """)
            close()
        }
        
        db = helper.runMigrationsAndValidate(testDbName, 4, true, DatabaseMigrations.MIGRATION_3_4)
        
        val cursor = db.query("SELECT * FROM games WHERE id = 1")
        assert(cursor.moveToFirst())
        
        // Check new columns
        assert(cursor.getColumnIndex("rating") >= 0)
        assert(cursor.getColumnIndex("notes") >= 0)
        assert(cursor.getColumnIndex("playCount") >= 0)
        assert(cursor.getColumnIndex("lastPlayed") >= 0)
        
        cursor.close()
    }
    
    @Test
    @Throws(IOException::class)
    fun migrateAllVersions() {
        // Test migrating through all versions
        helper.createDatabase(testDbName, 1).apply {
            execSQL("""
                INSERT INTO games (id, name, barcode, bookcase, shelf, dateAdded) 
                VALUES (1, 'Test Game', '123456789', 'A', '1', ${System.currentTimeMillis()})
            """)
            close()
        }
        
        // Apply all migrations
        val db = helper.runMigrationsAndValidate(
            testDbName, 
            6, 
            true, 
            *DatabaseMigrations.getAllMigrations()
        )
        
        // Verify final schema
        val cursor = db.query("SELECT * FROM games WHERE id = 1")
        assert(cursor.moveToFirst())
        assert(cursor.getString(cursor.getColumnIndex("name")) == "Test Game")
        cursor.close()
    }
    
    /**
     * Test database creation with Room
     */
    @Test
    fun testDatabaseCreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addMigrations(*DatabaseMigrations.getAllMigrations())
            .build()
        
        val gameDao = db.gameDao()
        
        // Test basic operations
        val testGame = Game(
            name = "Test Game",
            barcode = "123456789",
            bookcase = "A",
            shelf = "1"
        )
        
        // Insert and verify
        Thread.sleep(100) // Allow for async operations
        
        db.close()
    }
}

/**
 * Manual migration testing utilities for development
 */
object MigrationTestUtils {
    
    /**
     * Create test database with sample data for migration testing
     */
    fun createTestDatabase(context: Context, version: Int): SupportSQLiteDatabase {
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName
        )
        
        return helper.createDatabase("test_migration_db", version).apply {
            when (version) {
                1 -> {
                    execSQL("""
                        INSERT INTO games (name, barcode, bookcase, shelf, dateAdded) VALUES
                        ('Catan', '841333103392', 'A', '1', ${System.currentTimeMillis()}),
                        ('Ticket to Ride', '824968717493', 'A', '2', ${System.currentTimeMillis()}),
                        ('Pandemic', '681706711003', 'B', '1', ${System.currentTimeMillis()})
                    """)
                }
                // Add more version-specific test data as needed
            }
        }
    }
    
    /**
     * Verify data integrity after migration
     */
    fun verifyDataIntegrity(db: SupportSQLiteDatabase, expectedRecordCount: Int): Boolean {
        val cursor = db.query("SELECT COUNT(*) FROM games")
        return try {
            cursor.moveToFirst()
            val actualCount = cursor.getInt(0)
            actualCount == expectedRecordCount
        } finally {
            cursor.close()
        }
    }
}
