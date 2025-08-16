package com.boardgameinventory.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.preference.PreferenceManager

object TextDarknessManager {

    private const val TEXT_DARKNESS_KEY = "text_darkness"

    fun getTextDarkness(context: Context): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getInt(TEXT_DARKNESS_KEY, 50)
    }

    fun setTextDarkness(context: Context, darkness: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit {
            putInt(TEXT_DARKNESS_KEY, darkness)
        }
    }

    fun applyTextDarknessToView(view: View, darkness: Int) {
        val adjustedColor = (255 - darkness * 2.55).toInt()
        val textColor = String.format("#%02X%02X%02X", adjustedColor, adjustedColor, adjustedColor).toColorInt()

        // Update text color for all TextViews in the view hierarchy
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyTextDarknessToView(view.getChildAt(i), darkness)
            }
        } else if (view is android.widget.TextView) {
            view.setTextColor(textColor)
        }
    }

    fun applyTextDarknessToActivity(activity: android.app.Activity) {
        val darkness = getTextDarkness(activity)
        applyTextDarknessToView(activity.findViewById(android.R.id.content), darkness)
    }
}
