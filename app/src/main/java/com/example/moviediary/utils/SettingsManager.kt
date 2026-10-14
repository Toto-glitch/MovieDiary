package com.example.moviediary.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object SettingsManager {

    private const val PREFS_NAME = "moviediary_settings"
    private const val KEY_DARK_THEME = "dark_theme"
    private const val KEY_DEFAULT_SORT = "default_sort"
    private const val KEY_DURATION_HOURS = "duration_in_hours"

    val sortOptions = listOf(
        "YEAR" to "По году",
        "TITLE" to "По названию А–Я",
        "RATING" to "По оценке"
    )

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDarkTheme(context: Context): Boolean = prefs(context).getBoolean(KEY_DARK_THEME, true)

    fun setDarkTheme(context: Context, dark: Boolean) {
        prefs(context).edit { putBoolean(KEY_DARK_THEME, dark) }
    }

    fun applyTheme(context: Context) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme(context)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun getDefaultSort(context: Context): String =
        prefs(context).getString(KEY_DEFAULT_SORT, "YEAR") ?: "YEAR"

    fun setDefaultSort(context: Context, key: String) {
        prefs(context).edit { putString(KEY_DEFAULT_SORT, key) }
    }

    fun isDurationInHours(context: Context): Boolean = prefs(context).getBoolean(KEY_DURATION_HOURS, false)

    fun setDurationInHours(context: Context, hours: Boolean) {
        prefs(context).edit { putBoolean(KEY_DURATION_HOURS, hours) }
    }

    fun formatDuration(context: Context, minutes: Int): String =
        if (isDurationInHours(context)) "${minutes / 60}ч ${minutes % 60}м" else "$minutes мин"
}