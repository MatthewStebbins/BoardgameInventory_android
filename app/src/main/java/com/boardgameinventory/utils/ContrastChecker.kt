package com.boardgameinventory.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.boardgameinventory.R
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class to check text contrast ratios for accessibility.
 * Helps ensure text is readable against its background.
 */
object ContrastChecker {

    private const val TAG = "ContrastChecker"

    // Minimum recommended contrast ratio for accessibility (WCAG 2.0)
    private const val MIN_CONTRAST_RATIO = 4.5f  // Standard text
    private const val MIN_CONTRAST_RATIO_LARGE = 3.0f  // Large text (18pt+)

    /**
     * Perform a quick contrast check on all text views in the given view hierarchy
     * and show the results in a dialog
     */
    fun quickCheck(context: Context, rootView: View) {
        val issuesList = mutableListOf<ContrastIssue>()

        // Find all text views with contrast issues
        findTextViewsWithContrastIssues(rootView, issuesList)

        // Show results
        showContrastIssuesDialog(context, issuesList)
    }

    /**
     * Find all text views in the hierarchy that have contrast issues
     */
    private fun findTextViewsWithContrastIssues(view: View, issues: MutableList<ContrastIssue>) {
        if (view is TextView) {
            val textColor = view.currentTextColor
            val backgroundColor = getEffectiveBackgroundColor(view)

            val contrastRatio = calculateContrastRatio(textColor, backgroundColor)
            val minRequiredRatio = if (view.textSize >= 24f) MIN_CONTRAST_RATIO_LARGE else MIN_CONTRAST_RATIO

            if (contrastRatio < minRequiredRatio) {
                issues.add(
                    ContrastIssue(
                        view,
                        view.text.toString(),
                        textColor,
                        backgroundColor,
                        contrastRatio,
                        minRequiredRatio
                    )
                )
                Log.d(TAG, "Contrast issue found: ${view.text}, ratio: $contrastRatio (required: $minRequiredRatio)")
            }
        }

        // Recursively check children
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findTextViewsWithContrastIssues(view.getChildAt(i), issues)
            }
        }
    }

    /**
     * Calculate the contrast ratio between two colors
     * Formula: (L1 + 0.05) / (L2 + 0.05) where L1 is the lighter color and L2 is the darker
     */
    fun calculateContrastRatio(foreground: Int, background: Int): Float {
        val foregroundLuminance = calculateLuminance(foreground)
        val backgroundLuminance = calculateLuminance(background)

        // Determine which is lighter and which is darker
        val lighter = max(foregroundLuminance, backgroundLuminance)
        val darker = min(foregroundLuminance, backgroundLuminance)

        // Calculate contrast ratio
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * Calculate the relative luminance of a color
     * Formula based on WCAG 2.0 definition: https://www.w3.org/TR/WCAG20/#relativeluminancedef
     */
    private fun calculateLuminance(color: Int): Float {
        val red = Color.red(color) / 255f
        val green = Color.green(color) / 255f
        val blue = Color.blue(color) / 255f

        val r = if (red <= 0.03928f) red / 12.92f else ((red + 0.055f) / 1.055f).pow(2.4f)
        val g = if (green <= 0.03928f) green / 12.92f else ((green + 0.055f) / 1.055f).pow(2.4f)
        val b = if (blue <= 0.03928f) blue / 12.92f else ((blue + 0.055f) / 1.055f).pow(2.4f)

        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    /**
     * Get the effective background color of a view by traversing up the view hierarchy
     */
    private fun getEffectiveBackgroundColor(view: View): Int {
        var current = view

        // Try to get the background color from the view itself or its parents
        while (true) {
            val drawable = current.background
            if (drawable != null) {
                if (drawable is android.graphics.drawable.ColorDrawable) {
                    return drawable.color
                }
            }

            val parent = current.parent
            if (parent !is View) {
                break
            }
            current = parent
        }

        // Default to white if we couldn't determine the background color
        return Color.WHITE
    }

    /**
     * Show a dialog with contrast issues found
     */
    private fun showContrastIssuesDialog(context: Context, issues: List<ContrastIssue>) {
        if (issues.isEmpty()) {
            // No issues found
            AlertDialog.Builder(context)
                .setTitle(R.string.contrast_check_title)
                .setMessage(R.string.contrast_check_no_issues)
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }

        // Format the issues for display
        val message = context.getString(R.string.contrast_check_issues_found, issues.size)

        // Show dialog with option to fix issues
        AlertDialog.Builder(context)
            .setTitle(R.string.contrast_check_title)
            .setMessage(message)
            .setPositiveButton(R.string.fix_all_issues) { _, _ ->
                fixContrastIssues(context, issues)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Fix contrast issues by adjusting text colors
     */
    private fun fixContrastIssues(context: Context, issues: List<ContrastIssue>) {
        var fixedCount = 0

        issues.forEach { issue ->
            val textView = issue.view

            // Use the primary color for better contrast
            val newTextColor = ContextCompat.getColor(context, R.color.text_primary)
            textView.setTextColor(newTextColor)

            // Check if contrast is now sufficient
            val newRatio = calculateContrastRatio(newTextColor, issue.backgroundColor)
            if (newRatio >= issue.requiredRatio) {
                fixedCount++
            }
        }

        // Show summary dialog
        AlertDialog.Builder(context)
            .setTitle(R.string.contrast_fixed_title)
            .setMessage(context.getString(R.string.contrast_fixed_message, fixedCount))
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    /**
     * Data class to hold information about a contrast issue
     */
    data class ContrastIssue(
        val view: TextView,
        val text: String,
        val textColor: Int,
        val backgroundColor: Int,
        val contrastRatio: Float,
        val requiredRatio: Float
    )

    /**
     * Extension function for Float to calculate power
     */
    private fun Float.pow(exponent: Float): Float {
        return Math.pow(this.toDouble(), exponent.toDouble()).toFloat()
    }
}
