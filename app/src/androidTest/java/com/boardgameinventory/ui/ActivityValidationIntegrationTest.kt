package com.boardgameinventory.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test
import org.junit.runner.RunWith
import com.boardgameinventory.R

/**
 * Integration tests for input validation in activities
 * Tests the complete validation workflow from UI to business logic
 */
@RunWith(AndroidJUnit4::class)
class ActivityValidationIntegrationTest {

    @Test
    fun addGameActivity_shouldShowValidationErrors_whenSubmittingInvalidData() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), AddGameActivity::class.java)
        
        ActivityScenario.launch<AddGameActivity>(intent).use { scenario ->
            // When - Submit empty form
            onView(withId(R.id.btnSubmit)).perform(click())
            
            // Then - Should show validation errors
            onView(withId(R.id.tilBarcode))
                .check(matches(hasDescendant(withText(containsString("required")))))
            onView(withId(R.id.tilBookcase))
                .check(matches(hasDescendant(withText(containsString("required")))))
            onView(withId(R.id.tilShelf))
                .check(matches(hasDescendant(withText(containsString("required")))))
        }
    }

    @Test
    fun addGameActivity_shouldClearValidationErrors_whenEnteringValidData() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), AddGameActivity::class.java)
        
        ActivityScenario.launch<AddGameActivity>(intent).use { scenario ->
            // When - Enter valid data
            onView(withId(R.id.etBarcode)).perform(typeText("123456789"))
            onView(withId(R.id.etBookcase)).perform(typeText("A"))
            onView(withId(R.id.etShelf)).perform(typeText("1"))
            
            // Close keyboard
            pressBack()
            
            // Then - Validation errors should be cleared
            onView(withId(R.id.tilBarcode))
                .check(matches(not(hasDescendant(withText(containsString("error"))))))
            onView(withId(R.id.tilBookcase))
                .check(matches(not(hasDescendant(withText(containsString("error"))))))
            onView(withId(R.id.tilShelf))
                .check(matches(not(hasDescendant(withText(containsString("error"))))))
        }
    }

    @Test
    fun addGameActivity_shouldShowSpecificValidationErrors_forInvalidBarcode() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), AddGameActivity::class.java)
        
        ActivityScenario.launch<AddGameActivity>(intent).use { scenario ->
            // When - Enter invalid barcode (too short)
            onView(withId(R.id.etBarcode)).perform(typeText("123"))
            onView(withId(R.id.etBookcase)).perform(typeText("A"))
            onView(withId(R.id.etShelf)).perform(typeText("1"))
            
            // Close keyboard and submit
            pressBack()
            onView(withId(R.id.btnSubmit)).perform(click())
            
            // Then - Should show barcode-specific error
            onView(withId(R.id.tilBarcode))
                .check(matches(hasDescendant(withText(containsString("too short")))))
        }
    }

    @Test
    fun addGameActivity_shouldValidateLocationBarcode_andPopulateFields() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), AddGameActivity::class.java)
        
        ActivityScenario.launch<AddGameActivity>(intent).use { scenario ->
            // When - Enter location barcode in correct format
            onView(withId(R.id.etLocationBarcode)).perform(typeText("A-1"))
            
            // Close keyboard to trigger text watchers
            pressBack()
            
            // Then - Should auto-populate bookcase and shelf
            onView(withId(R.id.etBookcase)).check(matches(withText("A")))
            onView(withId(R.id.etShelf)).check(matches(withText("1")))
        }
    }

    @Test
    fun editGameActivity_shouldPreserveValidationRules_whenEditingExistingGame() {
        // Given - Create intent with game data
        val intent = Intent(ApplicationProvider.getApplicationContext(), EditGameActivity::class.java)
        // Note: In real test, you'd pass a valid game ID
        
        ActivityScenario.launch<EditGameActivity>(intent).use { scenario ->
            // When - Clear required field
            onView(withId(R.id.etGameName)).perform(clearText())
            
            // Close keyboard and try to save
            pressBack()
            onView(withId(R.id.btnSave)).perform(click())
            
            // Then - Should show validation error
            onView(withId(R.id.tilGameName))
                .check(matches(hasDescendant(withText(containsString("required")))))
        }
    }

    @Test
    fun addGameActivity_shouldHandleSpecialCharacters_inGameName() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), AddGameActivity::class.java)
        
        ActivityScenario.launch<AddGameActivity>(intent).use { scenario ->
            // When - Enter name with special characters
            onView(withId(R.id.etGameName)).perform(typeText("Game: Special Edition!"))
            onView(withId(R.id.etBarcode)).perform(typeText("123456789"))
            onView(withId(R.id.etBookcase)).perform(typeText("A"))
            onView(withId(R.id.etShelf)).perform(typeText("1"))
            
            // Close keyboard
            pressBack()
            
            // Then - Should accept valid special characters
            onView(withId(R.id.tilGameName))
                .check(matches(not(hasDescendant(withText(containsString("invalid"))))))
        }
    }

    @Test
    fun addGameActivity_shouldRejectInvalidCharacters_inBookcaseField() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), AddGameActivity::class.java)
        
        ActivityScenario.launch<AddGameActivity>(intent).use { scenario ->
            // When - Enter invalid characters in bookcase
            onView(withId(R.id.etBookcase)).perform(typeText("A B"))  // Space not allowed
            onView(withId(R.id.etBarcode)).perform(typeText("123456789"))
            onView(withId(R.id.etShelf)).perform(typeText("1"))
            
            // Close keyboard and submit
            pressBack()
            onView(withId(R.id.btnSubmit)).perform(click())
            
            // Then - Should show validation error for bookcase
            onView(withId(R.id.tilBookcase))
                .check(matches(hasDescendant(withText(containsString("invalid")))))
        }
    }

    @Test
    fun addGameActivity_shouldEnforceCharacterLimits_withCounters() {
        // Given
        val intent = Intent(ApplicationProvider.getApplicationContext(), AddGameActivity::class.java)
        
        ActivityScenario.launch<AddGameActivity>(intent).use { scenario ->
            // When - Enter text that exceeds limit
            val longBarcode = "A".repeat(25)  // Exceeds max barcode length
            onView(withId(R.id.etBarcode)).perform(typeText(longBarcode))
            
            // Close keyboard
            pressBack()
            
            // Then - Should show character counter and validation error
            onView(withId(R.id.tilBarcode))
                .check(matches(hasDescendant(withText(containsString("too long")))))
        }
    }
}
