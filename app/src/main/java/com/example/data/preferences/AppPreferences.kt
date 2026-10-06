package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("accountability_settings", Context.MODE_PRIVATE)

    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY, "Rs. ") ?: "Rs. "
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    var currencyCode: String
        get() = prefs.getString(KEY_CURRENCY_CODE, "PKR") ?: "PKR"
        set(value) = prefs.edit().putString(KEY_CURRENCY_CODE, value).apply()

    var isPinLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_PIN_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_PIN_ENABLED, value).apply()

    var pinCode: String
        get() = prefs.getString(KEY_PIN_CODE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PIN_CODE, value).apply()

    var isDarkModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var followSystemTheme: Boolean
        get() = prefs.getBoolean(KEY_FOLLOW_SYSTEM_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM_THEME, value).apply()

    companion object {
        private const val KEY_CURRENCY = "currency_symbol"
        private const val KEY_CURRENCY_CODE = "currency_code"
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_PIN_CODE = "pin_code"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_FOLLOW_SYSTEM_THEME = "follow_system_theme"
    }
}
