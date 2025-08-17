package com.boardgameinventory.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.pow

/**
 * Utility class for accessibility improvements and verification.
 * Provides methods to enhance accessibility features across the app.
 */
object AccessibilityUtils {

    /**
     * Audit a single activity for missing content descriptions
     * @return Pair of (total elements checked, number of missing descriptions)
     */
    fun auditActivityForContentDescriptions(activity: Activity): Pair<Int, List<View>> {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        val viewsWithoutDescriptions = mutableListOf<View>()
        val totalCheckedElements = 0

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
        } catch (_: Exception) {
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
     * Checks if the contrast ratio between textColor and backgroundColor meets WCAG AA standards (>= 4.5:1 for normal text).
     */
    fun hasAdequateContrast(textColor: Int, backgroundColor: Int): Boolean {
        fun luminance(color: Int): Double {
            val r = ((color shr 16) and 0xff) / 255.0
            val g = ((color shr 8) and 0xff) / 255.0
            val b = (color and 0xff) / 255.0
            val list = listOf(r, g, b).map {
                if (it <= 0.03928) it / 12.92 else ((it + 0.055) / 1.055).pow(2.4)
            }
            return 0.2126 * list[0] + 0.7152 * list[1] + 0.0722 * list[2]
        }
        val lum1 = luminance(textColor) + 0.05
        val lum2 = luminance(backgroundColor) + 0.05
        val ratio = if (lum1 > lum2) lum1 / lum2 else lum2 / lum1
        return ratio >= 4.5 // WCAG AA standard for normal text
    }
}
