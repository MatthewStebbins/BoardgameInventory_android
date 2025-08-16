package com.boardgameinventory.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.View
import androidx.preference.PreferenceManager

object TextDarknessUtil {

    private const val TEXT_DARKNESS_KEY = "text_darkness"

    fun saveTextDarkness(context: Context, darkness: Int) {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putInt(TEXT_DARKNESS_KEY, darkness).apply()
    }

    fun getTextDarkness(context: Context): Int {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getInt(TEXT_DARKNESS_KEY, 50) // Default to medium darkness
    }

    fun applyTextDarknessToView(view: View, darkness: Int) {
        val adjustedColor = (255 - darkness * 2.55).toInt()
        val textColor = Color.parseColor(String.format("#%02X%02X%02X", adjustedColor, adjustedColor, adjustedColor))
        view.setBackgroundColor(textColor)
    }
}
