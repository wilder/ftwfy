package com.wilderpereira.ftwfy

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.wilderpereira.ftwfy.intro.IntroActivity

/**
 * Created by Wilder on 16/09/17.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var intent: Intent?
        intent = if (prefs.hasSeenIntro) {
            Intent(this, CameraActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}