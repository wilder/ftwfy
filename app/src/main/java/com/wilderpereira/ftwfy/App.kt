package com.wilderpereira.ftwfy

import android.app.Application

/**
 * Created by Wilder on 16/09/17.
 */
val prefs: PreferencesManager by lazy {
    App.prefs!!
}

class App : Application() {
    companion object {
        var prefs: PreferencesManager? = null
    }

    override fun onCreate() {
        prefs = PreferencesManager(applicationContext)
        super.onCreate()
    }
}