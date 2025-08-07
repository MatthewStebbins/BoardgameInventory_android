package com.boardgameinventory.utils

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class for accessibility features and checks
 */
object AccessibilityUtils {

    /**
     * Minimum contrast ratios as per WCAG 2.0 guidelines
     */
    const val MIN_CONTRAST_NORMAL_TEXT = 4.5
    const val MIN_CONTRAST_LARGE_TEXT = 3.0  // 18pt or 14pt bold

    /**
     * Announce a message for accessibility services (screen readers)
     * @param view The view to use as anchor for the announcement
     * @param text The text to announce
     */
    fun announceForAccessibility(view: View, text: String) {
        view.announceForAccessibility(text)
    }

    /**
     * Announce a success message with appropriate prefix for screen readers
     * @param view The view to use as anchor
     * @param message The success message
     */
    fun announceSuccess(view: View, message: String) {
        val accessibilityMessage = view.context.getString(com.boardgameinventory.R.string.success_screen_reader, message)
        view.announceForAccessibility(accessibilityMessage)
    }

    /**
     * Announce an error message with appropriate prefix for screen readers
     * @param view The view to use as anchor
     * @param message The error message
     */
    fun announceError(view: View, message: String) {
        val accessibilityMessage = view.context.getString(com.boardgameinventory.R.string.error_screen_reader, message)
        view.announceForAccessibility(accessibilityMessage)
    }

    /**
     * Show an accessibility-friendly snackbar
     * @param view The view to anchor the snackbar to
     * @param message The message to show
     * @param duration The duration to show the snackbar
     * @param announce Whether to announce the message to screen readers
     */
    fun showAccessibleSnackbar(view: View, message: String, duration: Int = Snackbar.LENGTH_LONG, announce: Boolean = true) {
        Snackbar.make(view, message, duration).show()
        if (announce) {
            view.announceForAccessibility(message)
        }
    }

    /**
     * Calculate the contrast ratio between two colors
     * @param foregroundColor The text/foreground color
     * @param backgroundColor The background color
     * @return The contrast ratio (higher is better)
     */
    fun calculateContrastRatio(foregroundColor: Int, backgroundColor: Int): Double {
        val foregroundLuminance = calculateLuminance(foregroundColor)
        val backgroundLuminance = calculateLuminance(backgroundColor)

        // Calculate contrast ratio as per WCAG formula
        val lighter = max(foregroundLuminance, backgroundLuminance)
        val darker = min(foregroundLuminance, backgroundLuminance)

        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * Calculate the luminance of a color as per WCAG formula
     * @param color The color to calculate luminance for
     * @return The luminance value (0 to 1)
     */
    private fun calculateLuminance(color: Int): Double {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0

        val r1 = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val g1 = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val b1 = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

        return 0.2126 * r1 + 0.7152 * g1 + 0.0722 * b1
    }

    /**
     * Check if the contrast ratio between two colors meets accessibility standards
     * @param foregroundColor The text/foreground color
     * @param backgroundColor The background color
     * @param isLargeText Whether the text is large (18pt or 14pt bold)
     * @return true if the contrast ratio meets standards, false otherwise
     */
    fun hasAdequateContrast(foregroundColor: Int, backgroundColor: Int, isLargeText: Boolean = false): Boolean {
        val ratio = calculateContrastRatio(foregroundColor, backgroundColor)
        return if (isLargeText) {
            ratio >= MIN_CONTRAST_LARGE_TEXT
        } else {
            ratio >= MIN_CONTRAST_NORMAL_TEXT
        }
    }

    /**
     * Check if a TextView has adequate contrast against its background
     * @param textView The TextView to check
     * @return true if contrast is adequate, false otherwise
     */
    fun hasAdequateTextContrast(textView: TextView): Boolean {
        val textColor = textView.currentTextColor
        val background = textView.background

        // If the TextView has no background, check the parent's background
        val backgroundColor = if (background != null) {
            // This is simplified; in real-world you'd need to get the actual background color
            // from the drawable which might be more complex
            Color.TRANSPARENT
        } else {
            // Try to get parent background
            Color.WHITE // Default fallback
        }

        val isLargeText = textView.textSize >= 18.0f ||
                (textView.textSize >= 14.0f && textView.typeface?.isBold == true)

        return hasAdequateContrast(textColor, backgroundColor, isLargeText)
    }

    /**
     * Suggest a more accessible color that meets contrast requirements
     * @param backgroundColor The background color
     * @param preferredTextColor The preferred text color that might not meet contrast requirements
     * @param isLargeText Whether the text is large (18pt or 14pt bold)
     * @return A new text color with adequate contrast
     */
    fun suggestAccessibleTextColor(backgroundColor: Int, preferredTextColor: Int, isLargeText: Boolean = false): Int {
        // If the contrast is already adequate, return the preferred color
        if (hasAdequateContrast(preferredTextColor, backgroundColor, isLargeText)) {
            return preferredTextColor
        }

        // Otherwise, adjust the color to meet contrast requirements
        val backgroundLuminance = calculateLuminance(backgroundColor)

        // Decide whether to go lighter or darker based on background luminance
        return if (backgroundLuminance > 0.5) {
            // Dark text on light background
            Color.BLACK
        } else {
            // Light text on dark background
            Color.WHITE
        }
    }
}
