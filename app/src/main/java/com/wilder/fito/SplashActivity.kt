package com.wilder.fito

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.wilder.fito.intro.IntroActivity

/**
 * Created by Wilder on 16/09/17.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var intent: Intent? = null
        intent = if (prefs.hasSeenIntro) {
            Intent(this, CameraActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}