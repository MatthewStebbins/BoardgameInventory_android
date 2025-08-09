package com.boardgameinventory.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout

/**
 * Utility class for accessibility improvements and verification.
 * Provides methods to enhance accessibility features across the app.
 */
object AccessibilityUtils {

    /**
     * Content description types for different UI elements
     */
    enum class DescriptionType {
        BUTTON,
        IMAGE,
        INPUT_FIELD,
        STATUS_INDICATOR,
        HEADER,
        CARD
    }

    /**
     * Audit a single activity for missing content descriptions
     * @return Pair of (total elements checked, number of missing descriptions)
     */
    fun auditActivityForContentDescriptions(activity: Activity): Pair<Int, List<View>> {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        val viewsWithoutDescriptions = mutableListOf<View>()
        var totalCheckedElements = 0

        scanViewHierarchyForContentDescriptions(rootView, viewsWithoutDescriptions, totalCheckedElements)
        return Pair(totalCheckedElements, viewsWithoutDescriptions)
    }

    /**
     * Recursively scan view hierarchy to find elements without content descriptions
     */
    private fun scanViewHierarchyForContentDescriptions(
        view: View,
        viewsWithoutDescriptions: MutableList<View>,
        totalChecked: Int
    ): Int {
        var checked = totalChecked

        // Check if this view needs a content description
        if (viewNeedsContentDescription(view) && view.contentDescription.isNullOrBlank()) {
            viewsWithoutDescriptions.add(view)
        }

        if (viewNeedsContentDescription(view)) {
            checked++
        }

        // Recursively check child views if this is a ViewGroup
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                checked = scanViewHierarchyForContentDescriptions(
                    view.getChildAt(i),
                    viewsWithoutDescriptions,
                    checked
                )
            }
        }

        return checked
    }

    /**
     * Determine if a view needs a content description based on its type
     */
    private fun viewNeedsContentDescription(view: View): Boolean {
        return view is Button ||
               view is ImageButton ||
               view is FloatingActionButton ||
               view is ImageView ||
               (view is TextView && view.isClickable)
    }

    /**
     * Add appropriate content descriptions to common views in the layout
     */
    fun applyStandardContentDescriptions(rootView: View) {
        if (rootView is ViewGroup) {
            for (i in 0 until rootView.childCount) {
                val child = rootView.getChildAt(i)

                // Apply content descriptions based on view type
                when (child) {
                    is ImageButton -> {
                        if (child.contentDescription.isNullOrBlank()) {
                            child.contentDescription = deriveContentDescription(child)
                        }
                    }
                    is Button -> {
                        if (child.contentDescription.isNullOrBlank() && !child.text.isNullOrBlank()) {
                            child.contentDescription = child.text
                        }
                    }
                    is ImageView -> {
                        if (child.contentDescription.isNullOrBlank()) {
                            child.contentDescription = "Image"
                        }
                    }
                }

                // Recursively check child views
                if (child is ViewGroup) {
                    applyStandardContentDescriptions(child)
                }
            }
        }
    }

    /**
     * Attempt to derive a content description from view properties or parent
     */
    private fun deriveContentDescription(view: View): String {
        // Try to derive from ID name
        val idResourceName = try {
            view.resources.getResourceEntryName(view.id)
        } catch (e: Exception) {
            null
        }

        return when {
            idResourceName != null -> formatIdAsDescription(idResourceName)
            view.parent is TextView -> (view.parent as TextView).text.toString() + " button"
            else -> "Interactive element"
        }
    }

    /**
     * Format ID resource name into readable content description
     */
    private fun formatIdAsDescription(idName: String): String {
        return idName
            .replace("btn", "")
            .replace("iv", "")
            .replace("_", " ")
            .split("(?=[A-Z])".toRegex())
            .joinToString(" ") { it.lowercase() }
            .trim()
            .replaceFirstChar { it.uppercase() }
    }

    /**
     * Set appropriate content description based on view type and context
     */
    fun setDescription(view: View, description: String, type: DescriptionType = DescriptionType.BUTTON) {
        view.contentDescription = description

        // Add additional accessibility traits based on element type
        when (type) {
            DescriptionType.BUTTON -> {
                if (!view.isClickable) {
                    view.isClickable = true
                }
            }
            DescriptionType.HEADER -> {
                ViewCompat.setAccessibilityHeading(view, true)
            }
            DescriptionType.STATUS_INDICATOR -> {
                view.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            }
            else -> { /* No additional properties needed */ }
        }
    }

    /**
     * Add custom action description to a view for improved accessibility
     */
    fun addCustomActionDescription(view: View, actionDescription: String) {
        ViewCompat.setAccessibilityDelegate(view, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)

                // Add custom action description to the accessibility info
                info.addAction(
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK,
                        actionDescription
                    )
                )
            }
        })

        // Set content description if not already present
        if (view.contentDescription.isNullOrBlank()) {
            view.contentDescription = actionDescription
        }
    }

    /**
     * Setup logical traversal order for form elements
     * This helps screen readers navigate in a logical sequence
     */
    fun setupTraversalOrder(vararg viewPairs: Pair<View, View>) {
        viewPairs.forEach { (before, after) ->
            before.accessibilityTraversalBefore = after.id
            after.accessibilityTraversalAfter = before.id
        }
    }

    /**
     * Make important announcement for screen readers
     */
    fun announceForAccessibility(view: View, announcement: String) {
        view.announceForAccessibility(announcement)
    }

    /**
     * Mark a view as a heading for screen reader navigation
     */
    fun markAsHeading(view: View) {
        ViewCompat.setAccessibilityHeading(view, true)
    }

    /**
     * Set up input fields with proper accessibility support
     */
    fun setupAccessibleInputField(textInputLayout: TextInputLayout, labelText: String, errorText: String? = null) {
        textInputLayout.hint = labelText

        val editText = textInputLayout.editText ?: return

        // Ensure edit text has proper input type information for screen readers
        editText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES

        // Handle error state accessibility
        if (!errorText.isNullOrBlank() && textInputLayout.isErrorEnabled) {
            textInputLayout.error = errorText
            editText.setAccessibilityError(errorText)
        }
    }

    /**
     * Set accessibility error message on TextView or EditText
     */
    private fun TextView.setAccessibilityError(errorText: String) {
        ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.setError(errorText)
            }
        })
    }

    /**
     * Perform an accessibility audit on an entire screen and report issues
     */
    fun performAccessibilityAudit(activity: Activity): List<AccessibilityIssue> {
        val issues = mutableListOf<AccessibilityIssue>()
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        // Check for missing content descriptions
        val (_, viewsWithoutDescriptions) = auditActivityForContentDescriptions(activity)

        viewsWithoutDescriptions.forEach { view ->
            issues.add(
                AccessibilityIssue(
                    view = view,
                    type = AccessibilityIssueType.MISSING_CONTENT_DESCRIPTION,
                    description = "Missing content description"
                )
            )
        }

        // Check for contrast issues using ContrastChecker
        ContrastChecker.findTextContrastIssues(rootView) { view, contrastRatio ->
            issues.add(
                AccessibilityIssue(
                    view = view,
                    type = AccessibilityIssueType.LOW_CONTRAST,
                    description = "Low contrast ratio: $contrastRatio"
                )
            )
        }

        // Check for touch target size issues (minimum 48dp)
        findSmallTouchTargets(rootView) { view, size ->
            issues.add(
                AccessibilityIssue(
                    view = view,
                    type = AccessibilityIssueType.SMALL_TOUCH_TARGET,
                    description = "Touch target too small: $size dp"
                )
            )
        }

        return issues
    }

    /**
     * Find interactive elements with too small touch targets
     */
    private fun findSmallTouchTargets(view: View, onFound: (View, Int) -> Unit) {
        val minTouchSize = 48 // dp

        if (view.isClickable || view.isLongClickable) {
            val widthDp = view.width / view.resources.displayMetrics.density
            val heightDp = view.height / view.resources.displayMetrics.density

            if (widthDp < minTouchSize || heightDp < minTouchSize) {
                onFound(view, kotlin.math.min(widthDp.toInt(), heightDp.toInt()))
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findSmallTouchTargets(view.getChildAt(i), onFound)
            }
        }
    }

    /**
     * Data class to represent accessibility issues
     */
    data class AccessibilityIssue(
        val view: View,
        val type: AccessibilityIssueType,
        val description: String
    )

    /**
     * Types of accessibility issues
     */
    enum class AccessibilityIssueType {
        MISSING_CONTENT_DESCRIPTION,
        LOW_CONTRAST,
        SMALL_TOUCH_TARGET,
        MISSING_LABEL
    }

    /**
     * Checks if the contrast ratio between textColor and backgroundColor meets WCAG AA standards (>= 4.5:1 for normal text).
     */
    fun hasAdequateContrast(textColor: Int, backgroundColor: Int): Boolean {
        fun luminance(color: Int): Double {
            val r = ((color shr 16) and 0xff) / 255.0
            val g = ((color shr 8) and 0xff) / 255.0
            val b = (color and 0xff) / 255.0
            val list = listOf(r, g, b).map {
                if (it <= 0.03928) it / 12.92 else Math.pow((it + 0.055) / 1.055, 2.4)
            }
            return 0.2126 * list[0] + 0.7152 * list[1] + 0.0722 * list[2]
        }
        val lum1 = luminance(textColor) + 0.05
        val lum2 = luminance(backgroundColor) + 0.05
        val ratio = if (lum1 > lum2) lum1 / lum2 else lum2 / lum1
        return ratio >= 4.5 // WCAG AA standard for normal text
    }
}
