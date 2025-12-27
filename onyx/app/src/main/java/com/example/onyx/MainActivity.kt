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


class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
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
                    startActivity(Intent(this, Watch_Page::class.java))
                } else {
                    startActivity(Intent(this, Watch_Page::class.java))
                }
            }
            finish()
        }, 500)

    }





}








