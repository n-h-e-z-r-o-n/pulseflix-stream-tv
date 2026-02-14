package com.example.onyx

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.onyx.Database.AppDatabase
import com.example.onyx.Database.SessionManger
import com.example.onyx.OnyxObjects.GlobalUtils
import com.example.onyx.OnyxObjects.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
private lateinit var  sm: SessionManger


class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        //installSplashScreen()
        enableEdgeToEdge()
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

            if (!GlobalUtils.isTv(this)) {
                sm = SessionManger(this)
                sm.saveUserId(1453.toInt())
                sm.saveAvatar("profile_avatars/1.png")
                startActivity(Intent(this, Instraction::class.java))
            }else{
                startActivity(Intent(this@MainActivity, Login_Page::class.java))
                //startActivity(Intent(this@MainActivity, web::class.java))

            }
            finish()
        }, 1)
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