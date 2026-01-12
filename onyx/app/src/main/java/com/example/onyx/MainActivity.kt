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
import com.bumptech.glide.Glide
import com.example.onyx.Database.AppDatabase
import com.example.onyx.OnyxObjects.GlobalUtils


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

            if (!GlobalUtils.isTv(this)) {
                startActivity(Intent(this, Instraction::class.java))
            }else {
                if (db.isSubscriptionActive()) {
                    startActivity(Intent(this, Anime_Page::class.java))
                } else {
                    startActivity(Intent(this, PayWall::class.java))
                }
            }
            finish()
        }, 10000)

    }
    
}








