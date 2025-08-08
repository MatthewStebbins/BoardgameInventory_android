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
                    is Button -> {
                        if (child.contentDescription.isNullOrBlank() && !child.text.isNullOrBlank()) {
                            child.contentDescription = child.text
                        }
                    }

                    is ImageButton -> {
                        if (child.contentDescription.isNullOrBlank()) {
                            // Try to infer from tag or drawable resource name
                            val resourceName = child.context.resources.getResourceName(
                                child.id
                            ).substringAfterLast("/")

                            child.contentDescription = resourceName
                                .replace("_", " ")
                                .replace("btn", "")
                                .replace("img", "")
                                .trim()
                                .capitalize()
                        }
                    }

                    is TextInputLayout -> {
                        val hint = child.hint
                        if (child.editText?.contentDescription.isNullOrBlank() && hint != null) {
                            child.editText?.contentDescription = hint
                        }
                    }
                }

                // Recursively apply to children
                applyStandardContentDescriptions(child)
            }
        }
    }

    /**
     * Add custom action descriptions to improve TalkBack experience
     */
    fun addCustomActionDescription(view: View, actionDescription: String) {
        ViewCompat.setAccessibilityDelegate(view, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.addAction(
                    AccessibilityNodeInfoCompat.ActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK,
                        actionDescription
                    )
                )
            }
        })
    }

    /**
     * Helper method to set content description based on type
     */
    fun setDescription(view: View, text: String, type: DescriptionType) {
        val prefix = when (type) {
            DescriptionType.BUTTON -> "Button: "
            DescriptionType.IMAGE -> "Image: "
            DescriptionType.INPUT_FIELD -> "Input field for "
            DescriptionType.STATUS_INDICATOR -> "Status: "
            DescriptionType.HEADER -> "Heading: "
            DescriptionType.CARD -> "Card: "
        }

        view.contentDescription = "$prefix$text"
    }

    /**
     * Check contrast ratio between foreground and background colors
     * @return true if contrast ratio meets WCAG AA standard (4.5:1 for normal text)
     */
    fun hasAdequateContrast(foregroundColor: Int, backgroundColor: Int): Boolean {
        val luminance1 = calculateRelativeLuminance(foregroundColor)
        val luminance2 = calculateRelativeLuminance(backgroundColor)

        val lighter = Math.max(luminance1, luminance2)
        val darker = Math.min(luminance1, luminance2)

        // Calculate contrast ratio: (L1 + 0.05) / (L2 + 0.05)
        val contrastRatio = (lighter + 0.05) / (darker + 0.05)

        // WCAG AA requires 4.5:1 for normal text, 3:1 for large text
        return contrastRatio >= 4.5
    }

    /**
     * Calculate the relative luminance of a color per WCAG formula
     */
    private fun calculateRelativeLuminance(color: Int): Double {
        val red = ((color shr 16) and 0xFF) / 255.0
        val green = ((color shr 8) and 0xFF) / 255.0
        val blue = (color and 0xFF) / 255.0

        val r = if (red <= 0.03928) red / 12.92 else Math.pow((red + 0.055) / 1.055, 2.4)
        val g = if (green <= 0.03928) green / 12.92 else Math.pow((green + 0.055) / 1.055, 2.4)
        val b = if (blue <= 0.03928) blue / 12.92 else Math.pow((blue + 0.055) / 1.055, 2.4)

        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Announce a message to screen readers
     */
    fun announceForAccessibility(view: View, message: String) {
        view.announceForAccessibility(message)
    }
}
