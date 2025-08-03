package com.boardgameinventory.validation

import org.junit.Test
import org.junit.Assert.*
import com.boardgameinventory.R

/**
 * Security-focused validation tests to ensure input validation
 * protects against malicious input and edge cases
 */
class SecurityValidationTest {

    @Test
    fun `validateGameName should reject XSS attempts`() {
        // These contain characters not allowed by the validation regex: < > { } [ ] \ / | @ # $ % ^ * + = ~ `
        val xssAttempts = listOf(
            "<script>alert('xss')</script>",    // Contains < and >
            "<img src=x onerror=alert('xss')>", // Contains < and >  
            "Game<bad>Name",                    // Contains < and >
            "Name{with}braces",                 // Contains { and }
            "Name[with]brackets",               // Contains [ and ]
            "Name\\with\\backslash",            // Contains \
            "Name/with/slash",                  // Contains /
            "Name|with|pipe",                   // Contains |
            "Name@with@at",                     // Contains @
            "Name#with#hash",                   // Contains #
            "Name\$with\$dollar",               // Contains $
            "Name%with%percent",                // Contains %
            "Name^with^caret",                  // Contains ^
            "Name*with*asterisk",               // Contains *
            "Name+with+plus",                   // Contains +
            "Name=with=equals",                 // Contains =
            "Name~with~tilde",                  // Contains ~
            "Name`with`backtick"                // Contains `
        )
        
        xssAttempts.forEach { xssInput ->
            val result = ValidationUtils.validateGameName(xssInput)
            assertFalse("XSS/malicious input '$xssInput' should be rejected", result.isValid)
            // These are rejected because they contain invalid characters
            assertEquals("Should show invalid characters error", 
                R.string.error_name_invalid_characters, result.errorMessageRes)
        }
    }

    @Test
    fun `validateBarcode should reject SQL injection attempts`() {
        val sqlInjectionAttempts = listOf(
            "'; DROP TABLE games; --",
            "1' OR '1'='1",
            "UNION SELECT * FROM users",
            "'; DELETE FROM games WHERE id = 1; --"
        )
        
        sqlInjectionAttempts.forEach { sqlInput ->
            val result = ValidationUtils.validateBarcode(sqlInput)
            assertFalse("SQL injection attempt '$sqlInput' should be rejected", result.isValid)
        }
    }

    @Test
    fun `validateDescription should handle extremely long input gracefully`() {
        // Test with input much longer than max length
        val veryLongInput = "A".repeat(10000)
        
        val result = ValidationUtils.validateDescription(veryLongInput)
        
        assertFalse("Very long input should be rejected", result.isValid)
        assertEquals("Should show too long error", 
            R.string.error_description_too_long, result.errorMessageRes)
    }

    @Test
    fun `validateImageUrl should reject malicious URLs`() {
        val maliciousUrls = listOf(
            "javascript:alert('xss')",
            "data:text/html,<script>alert('xss')</script>",
            "ftp://evil.com/malware.exe",
            "file:///etc/passwd",
            "http://evil.com/malware.php",
            "https://attacker.com/steal-data?token="
        )
        
        maliciousUrls.forEach { maliciousUrl ->
            val result = ValidationUtils.validateImageUrl(maliciousUrl)
            assertFalse("Malicious URL '$maliciousUrl' should be rejected", result.isValid)
        }
    }

    @Test
    fun `validateLoanedTo should reject special characters that could cause issues`() {
        val problematicNames = listOf(
            "John<script>",
            "Mary'; DROP TABLE",
            "User@domain.com",
            "Name#with#hashes",
            "Name\$with\$dollars",
            "Name%with%percent",
            "Name&with&ampersand"
        )
        
        problematicNames.forEach { problematicName ->
            val result = ValidationUtils.validateLoanedTo(problematicName)
            assertFalse("Problematic name '$problematicName' should be rejected", result.isValid)
            assertEquals("Should show invalid name error", 
                R.string.error_loaned_to_invalid, result.errorMessageRes)
        }
    }

    @Test
    fun `validateLocationBarcode should handle malformed input safely`() {
        val malformedInputs = listOf(
            "A-B-C-D-E-F",  // Too many parts
            "---",           // Only dashes
            "A-",            // Missing shelf
            "-B",            // Missing bookcase
            "A--B",          // Double dash
            "A-B-",          // Trailing dash
            "-A-B",          // Leading dash
            ""               // Empty string
        )
        
        malformedInputs.forEach { malformedInput ->
            val (bookcase, shelf) = com.boardgameinventory.utils.Utils.validateLocationBarcode(malformedInput)
            assertNull("Malformed input '$malformedInput' should return null bookcase", bookcase)
            assertNull("Malformed input '$malformedInput' should return null shelf", shelf)
        }
    }

    @Test
    fun `validation should handle unicode and international characters appropriately`() {
        // ASCII names that should be valid
        val validAsciiNames = listOf(
            "Game Name",
            "Settlers of Catan",
            "7 Wonders: Duel"
        )
        
        validAsciiNames.forEach { asciiName ->
            val result = ValidationUtils.validateGameName(asciiName)
            assertTrue("ASCII name '$asciiName' should be valid", result.isValid)
        }
        
        // Non-ASCII names that will be rejected due to current validation rules
        val nonAsciiNames = listOf(
            "Jeu français",  // Contains é
            "Spiel Größe",   // Contains ö and ß  
            "ゲーム名前",      // Japanese characters
            "游戏名称",        // Chinese characters
            "משחק",          // Hebrew characters
            "игра"           // Cyrillic characters
        )
        
        nonAsciiNames.forEach { nonAsciiName ->
            val result = ValidationUtils.validateGameName(nonAsciiName)
            // Current validation rejects non-ASCII characters
            assertFalse("Non-ASCII name '$nonAsciiName' should be rejected by current validation", result.isValid)
            assertEquals("Should show invalid characters error", 
                R.string.error_name_invalid_characters, result.errorMessageRes)
        }
    }

    @Test
    fun `validation should handle null and whitespace edge cases`() {
        val whitespaceInputs = listOf(
            null,
            "",
            " ",
            "   ",
            "\t",
            "\n",
            "\r",
            "\t\n\r   "
        )
        
        whitespaceInputs.forEach { whitespaceInput ->
            // All these should be treated as empty/required field errors
            val nameResult = ValidationUtils.validateGameName(whitespaceInput)
            assertFalse("Whitespace input '$whitespaceInput' should fail name validation", nameResult.isValid)
            assertEquals("Should show required error", 
                R.string.error_name_required, nameResult.errorMessageRes)
            
            val barcodeResult = ValidationUtils.validateBarcode(whitespaceInput)
            assertFalse("Whitespace input '$whitespaceInput' should fail barcode validation", barcodeResult.isValid)
            assertEquals("Should show required error", 
                R.string.error_barcode_required, barcodeResult.errorMessageRes)
        }
    }

    @Test
    fun `validation should enforce exact length limits`() {
        // Test exact boundary conditions
        val exactMaxName = "A".repeat(ValidationUtils.MAX_NAME_LENGTH)
        val tooLongName = "A".repeat(ValidationUtils.MAX_NAME_LENGTH + 1)
        
        val exactMaxResult = ValidationUtils.validateGameName(exactMaxName)
        assertTrue("Exact max length should be valid", exactMaxResult.isValid)
        
        val tooLongResult = ValidationUtils.validateGameName(tooLongName)
        assertFalse("Too long name should be invalid", tooLongResult.isValid)
        assertEquals("Should show too long error", 
            R.string.error_name_too_long, tooLongResult.errorMessageRes)
    }

    @Test
    fun `validation should handle concurrent access safely`() {
        // Test thread safety by running validation concurrently
        val testThreads = mutableListOf<Thread>()
        val results = mutableListOf<ValidationUtils.ValidationResult>()
        
        repeat(100) { i ->
            val thread = Thread {
                val result = ValidationUtils.validateGameName("Test Game $i")
                synchronized(results) {
                    results.add(result)
                }
            }
            testThreads.add(thread)
            thread.start()
        }
        
        // Wait for all threads to complete
        testThreads.forEach { it.join() }
        
        // All results should be valid
        assertEquals("Should have 100 results", 100, results.size)
        assertTrue("All concurrent validations should succeed", 
            results.all { it.isValid })
    }
}
