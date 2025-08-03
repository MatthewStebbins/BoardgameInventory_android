package com.boardgameinventory

/**
 * Test configuration and constants for the BoardGame Inventory app tests
 */
object TestConfig {
    
    // Sample test data
    const val SAMPLE_GAME_NAME = "Test Board Game"
    const val SAMPLE_BARCODE = "123456789012"
    const val SAMPLE_BOOKCASE = "A"
    const val SAMPLE_SHELF = "1"
    const val SAMPLE_LOANED_TO = "John Doe"
    const val SAMPLE_DESCRIPTION = "A test board game for unit testing"
    const val SAMPLE_IMAGE_URL = "https://example.com/test-image.jpg"
    
    // Test location barcodes
    const val VALID_LOCATION_BARCODE = "A-1"
    const val INVALID_LOCATION_BARCODE_NO_DASH = "A1"
    const val INVALID_LOCATION_BARCODE_MULTIPLE_DASH = "A-B-1"
    const val INVALID_LOCATION_BARCODE_EMPTY = ""
    
    // Test file names
    const val TEST_BACKUP_FILE = "test_backup.db"
    const val TEST_CSV_FILE = "test_export.csv"
    const val TEST_EXCEL_FILE = "test_export.xlsx"
    
    // Test MIME types
    const val CSV_MIME_TYPE = "text/csv"
    const val EXCEL_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    const val JSON_MIME_TYPE = "application/json"
    const val DEFAULT_MIME_TYPE = "application/octet-stream"
    
    // Test game statistics
    const val TEST_TOTAL_GAMES = 10
    const val TEST_LOANED_GAMES = 3
    const val TEST_AVAILABLE_GAMES = 7
}
