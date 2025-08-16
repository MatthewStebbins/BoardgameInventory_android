package com.boardgameinventory.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.core.content.edit

object TextDarknessUtil {

    private const val TEXT_DARKNESS_KEY = "text_darkness"

    fun saveTextDarkness(context: Context, darkness: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putInt(TEXT_DARKNESS_KEY, darkness)
        }
    }

    fun getTextDarkness(context: Context): Int {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getInt(TEXT_DARKNESS_KEY, 50) // Default to medium darkness
    }

    fun applyTextDarknessToView(view: View, darkness: Int) {
        val adjustedColor = (255 - darkness * 2.55).toInt()
        val textColor = Color.rgb(adjustedColor, adjustedColor, adjustedColor)

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyTextDarknessToView(view.getChildAt(i), darkness)
            }
        } else if (view is TextView) {
            view.setTextColor(textColor)
        }
    }
}
