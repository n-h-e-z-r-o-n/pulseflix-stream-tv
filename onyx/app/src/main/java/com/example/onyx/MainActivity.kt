package com.example.onyx

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.onyx.Database.AppDatabase
import com.example.onyx.OnyxObjects.GlobalUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Onyx_Dark)
        GlobalUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        db = AppDatabase(this)

        val tv = findViewById<TextView>(R.id.onyxTitle)
        val loadingImageView = findViewById<ImageView>(R.id.LoadingAnimation)
        Glide.with(this)
            .asGif()
            .load(R.raw.c)
            .into(loadingImageView)


        Handler(Looper.getMainLooper()).postDelayed({

            lifecycleScope.launch(Dispatchers.Main) {

                if (!GlobalUtils.isTv(this@MainActivity)) {
                    startActivity(Intent(this@MainActivity, Instraction::class.java))
                } else {
                    if (db.isSubscriptionActive()) {
                        startActivity(Intent(this@MainActivity, Anime_Page::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, PayWall::class.java))
                    }
                }
                finish()
            }
        }, 10000)
    }

}








