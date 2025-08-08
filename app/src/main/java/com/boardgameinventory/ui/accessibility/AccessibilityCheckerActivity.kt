package com.boardgameinventory.ui.accessibility

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boardgameinventory.R
import com.boardgameinventory.databinding.ActivityAccessibilityCheckerBinding
import com.boardgameinventory.databinding.ItemAccessibilityIssueBinding
import com.boardgameinventory.utils.AccessibilityUtils
import com.google.android.material.snackbar.Snackbar

/**
 * Activity to check and improve accessibility features across the app.
 * This is a development tool to ensure compliance with accessibility guidelines.
 */
class AccessibilityCheckerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccessibilityCheckerBinding
    private val accessibilityIssueAdapter = AccessibilityIssueAdapter()
    private val checkedActivities = mutableMapOf<String, Pair<Int, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccessibilityCheckerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupButtons()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Accessibility Checker"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        binding.rvAccessibilityIssues.layoutManager = LinearLayoutManager(this)
        binding.rvAccessibilityIssues.adapter = accessibilityIssueAdapter
    }

    private fun setupButtons() {
        binding.btnCheckContentDescriptions.setOnClickListener {
            performContentDescriptionCheck()
        }

        binding.btnCheckContrastRatios.setOnClickListener {
            performContrastCheck()
        }

        binding.btnApplyContentDescriptions.setOnClickListener {
            applyMissingContentDescriptions()
        }

        binding.btnRunAllChecks.setOnClickListener {
            performContentDescriptionCheck()
            performContrastCheck()
        }
    }

    private fun performContentDescriptionCheck() {
        // Clear previous results
        accessibilityIssueAdapter.clearIssues()

        // Check current activity
        val activityName = this.javaClass.simpleName
        val (totalElements, missingDescriptionViews) = AccessibilityUtils.auditActivityForContentDescriptions(this)

        // Add results to adapter
        accessibilityIssueAdapter.addIssues(
            missingDescriptionViews.map { view ->
                AccessibilityIssue(
                    view = view,
                    viewType = getViewTypeName(view),
                    activityName = activityName,
                    issueType = "Missing Content Description",
                    viewId = getViewResourceId(view)
                )
            }
        )

        // Update summary
        checkedActivities[activityName] = Pair(totalElements, missingDescriptionViews.size)
        updateSummary()

        // Show result
        if (missingDescriptionViews.isEmpty()) {
            Snackbar.make(
                binding.root,
                "Great! No content description issues found in this activity.",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            Snackbar.make(
                binding.root,
                "Found ${missingDescriptionViews.size} elements without content descriptions.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun performContrastCheck() {
        // Implementation for contrast ratio checking
        // This would scan all text views and check their contrast ratio against backgrounds

        // For demonstration purposes, we'll just check this activity's main text elements
        val contrastIssues = mutableListOf<AccessibilityIssue>()
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        scanForContrastIssues(rootView, contrastIssues)

        // Add contrast issues to adapter
        accessibilityIssueAdapter.addIssues(contrastIssues)

        // Show result
        if (contrastIssues.isEmpty()) {
            Snackbar.make(
                binding.root,
                "Great! No contrast ratio issues found in this activity.",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            Snackbar.make(
                binding.root,
                "Found ${contrastIssues.size} elements with poor contrast ratios.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun scanForContrastIssues(view: View, issues: MutableList<AccessibilityIssue>) {
        if (view is TextView) {
            val textColor = view.currentTextColor
            val backgroundColor = getBackgroundColor(view)

            if (!AccessibilityUtils.hasAdequateContrast(textColor, backgroundColor)) {
                issues.add(
                    AccessibilityIssue(
                        view = view,
                        viewType = "TextView",
                        activityName = this.javaClass.simpleName,
                        issueType = "Poor Contrast Ratio",
                        viewId = getViewResourceId(view)
                    )
                )
            }
        }

        // Recursively scan children
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                scanForContrastIssues(view.getChildAt(i), issues)
            }
        }
    }

    private fun getBackgroundColor(view: View): Int {
        // Try to get the background color of the view
        val background = view.background

        // If there's a direct background color, use it
        if (view.backgroundTintList != null) {
            return view.backgroundTintList!!.defaultColor
        }

        // If no background, try to get the parent's background
        var current = view
        while (current.background == null && current.parent is View) {
            current = current.parent as View
        }

        // If we found a background, try to get its color
        if (current.background != null) {
            return (current.backgroundTintList?.defaultColor) ?: Color.WHITE
        }

        // Default to white if we can't determine the color
        return Color.WHITE
    }

    private fun applyMissingContentDescriptions() {
        // Apply standard content descriptions to the current activity
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        AccessibilityUtils.applyStandardContentDescriptions(rootView)

        // Re-run the check to update the results
        performContentDescriptionCheck()

        Snackbar.make(
            binding.root,
            "Applied default content descriptions based on view properties.",
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun updateSummary() {
        var totalChecked = 0
        var totalIssues = 0

        checkedActivities.forEach { (_, stats) ->
            totalChecked += stats.first
            totalIssues += stats.second
        }

        binding.tvSummary.text = "Checked $totalChecked elements across ${checkedActivities.size} activities. " +
                "Found $totalIssues issues (${if (totalChecked > 0) String.format("%.1f", totalIssues * 100.0 / totalChecked) else "0"}%)."
    }

    private fun getViewTypeName(view: View): String {
        return when (view) {
            is Button -> "Button"
            is ImageView -> "ImageView"
            is TextView -> "TextView"
            else -> view.javaClass.simpleName
        }
    }

    private fun getViewResourceId(view: View): String {
        try {
            if (view.id != View.NO_ID) {
                return resources.getResourceName(view.id).substringAfterLast("/")
            }
        } catch (e: Exception) {
            // Resource ID not found
        }
        return "unknown_id"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Data class to represent an accessibility issue
     */
    data class AccessibilityIssue(
        val view: View,
        val viewType: String,
        val activityName: String,
        val issueType: String,
        val viewId: String
    )

    /**
     * Adapter for displaying accessibility issues
     */
    inner class AccessibilityIssueAdapter : RecyclerView.Adapter<AccessibilityIssueAdapter.ViewHolder>() {
        private val issues = mutableListOf<AccessibilityIssue>()

        fun addIssues(newIssues: List<AccessibilityIssue>) {
            val startPosition = issues.size
            issues.addAll(newIssues)
            notifyItemRangeInserted(startPosition, newIssues.size)
        }

        fun clearIssues() {
            val size = issues.size
            issues.clear()
            notifyItemRangeRemoved(0, size)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAccessibilityIssueBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(issues[position])
        }

        override fun getItemCount(): Int = issues.size

        inner class ViewHolder(private val binding: ItemAccessibilityIssueBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(issue: AccessibilityIssue) {
                binding.tvIssueType.text = issue.issueType
                binding.tvViewType.text = "View: ${issue.viewType}"
                binding.tvViewId.text = "ID: ${issue.viewId}"
                binding.tvActivity.text = "In: ${issue.activityName}"

                // Set button click action
                binding.btnFix.setOnClickListener {
                    when (issue.issueType) {
                        "Missing Content Description" -> {
                            // Generate a default content description
                            val defaultDescription = when {
                                issue.viewId.contains("btn", ignoreCase = true) ->
                                    issue.viewId.replace("btn", "", ignoreCase = true).replace("_", " ").trim().capitalize()
                                issue.viewId.contains("img", ignoreCase = true) ->
                                    issue.viewId.replace("img", "", ignoreCase = true).replace("_", " ").trim().capitalize()
                                else -> issue.viewId.replace("_", " ").trim().capitalize()
                            }

                            issue.view.contentDescription = defaultDescription
                            issues.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)

                            // Update the count
                            val activityName = issue.activityName
                            checkedActivities[activityName] = checkedActivities[activityName]?.let {
                                Pair(it.first, it.second - 1)
                            } ?: Pair(0, 0)
                            updateSummary()
                        }

                        "Poor Contrast Ratio" -> {
                            if (issue.view is TextView) {
                                // Increase contrast by setting a more accessible color
                                issue.view.setTextColor(Color.BLACK)
                                issue.view.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                                issues.removeAt(adapterPosition)
                                notifyItemRemoved(adapterPosition)
                            }
                        }
                    }
                }
            }
        }
    }
}
