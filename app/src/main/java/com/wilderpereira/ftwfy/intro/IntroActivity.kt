package com.wilderpereira.ftwfy.intro

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.wilderpereira.ftwfy.CameraActivity
import com.wilderpereira.ftwfy.R
import com.wilderpereira.ftwfy.prefs


/**
 * Created by Wilder on 12/09/17.
 */
class IntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }

        addSlide(AppIntroFragment.newInstance(title, getString(R.string.tutorial_1), R.drawable.research, resources.getColor(R.color.blue)))
        addSlide(AppIntroFragment.newInstance(title, getString(R.string.tutorial_2), R.drawable.straight, resources.getColor(R.color.orange)))
        addSlide(AppIntroFragment.newInstance(title, getString(R.string.tutorial_3), R.drawable.regex, resources.getColor(R.color.green)))

        // Turn vibration on and set intensity.
        setVibrate(true)
        setVibrateIntensity(30)
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)
        saveAndGoToMain()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        saveAndGoToMain()
    }

    private fun saveAndGoToMain() {
        prefs.hasSeenIntro = true
        startActivity(Intent(this@IntroActivity, CameraActivity::class.java))
    }

}