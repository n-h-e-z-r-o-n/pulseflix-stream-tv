package com.example.onyx

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.onyx.Database.AppDatabase
import com.example.onyx.OnyxObjects.GlobalUtils
import com.example.onyx.OnyxObjects.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalUtils.applyTheme(this)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupBackPressedCallback()

        // Hide navigation bar and status bar (Immersive mode)
        GlobalUtils.hideSystemUI(this)

        db = AppDatabase(this)


        val loadingBar = findViewById<View>(R.id.loading_bar)
        val animation = AnimationUtils.loadAnimation(this, R.drawable.loading_slide)
        loadingBar.startAnimation(animation)

        NotificationHelper.getTvNotifications(this@MainActivity)
        NotificationHelper.getAnimeNotifications(this@MainActivity)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@MainActivity, Login_Page::class.java))
            finish()
        }, 10000)
    }


    // This ensures the UI stays hidden when the activity regains focus
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            GlobalUtils.hideSystemUI(this)
        }
    }

    // This handles when the immersive mode is interrupted
    override fun onResume() {
        super.onResume()
        GlobalUtils.hideSystemUI(this)
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
    }
}