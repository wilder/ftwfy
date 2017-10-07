package com.wilderpereira.ftwfy

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages dealing with SharedPreferences
 * Created by Wilder on 16/09/17.
 */
class PreferencesManager (context: Context) {
    val PREFS_FILENAME = "fito.prefs"
    val INTRO_KEY = "intro_key"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0);

    var hasSeenIntro: Boolean
        get() = prefs.getBoolean(INTRO_KEY, false)
        set(value) = prefs.edit().putBoolean(INTRO_KEY, value).apply()

}