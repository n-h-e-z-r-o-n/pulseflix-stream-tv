package com.example.onyx

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.TextView
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


    override fun onCreate(savedInstanceState: Bundle?) {
        //installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()


        setupBackPressedCallback()

        // Hide navigation bar and status bar (Immersive mode)
        GlobalUtils.hideSystemUI(this)


        ////////////////////////////////////////////////////////////////////////////////////////////
        val logo = findViewById<TextView>(R.id.onyxTitle)
        logo.apply {
            alpha = 0f
            scaleX = 0.7f
            scaleY = 0.7f
            translationY = 80f
            letterSpacing = 0f
        }

        logo.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(3000)
            .setInterpolator(OvershootInterpolator(2f))
            .withEndAction {
                // settle back to normal size
                logo.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }
            .start()

        val paint = logo.paint
        val width = paint.measureText(logo.text.toString())

        val textShader = LinearGradient(
            0f, 0f, width, 0f,
            intArrayOf(
                Color.parseColor("#152755"), // Original Color
                Color.parseColor("#4A90E2"), // Highlight Color
                Color.parseColor("#152755")  // Original Color
            ), null, Shader.TileMode.CLAMP
        )

        // Animate the gradient position
        ValueAnimator.ofFloat(0f, 2f * width).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                val dx = it.animatedValue as Float
                val matrix = Matrix()
                matrix.setTranslate(dx - width, 0f)
                textShader.setLocalMatrix(matrix)
                logo.paint.shader = textShader
                logo.invalidate()
            }
            start()
        }
        ////////////////////////////////////////////////////////////////////////////////////////////

        db = AppDatabase(this)


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
        }, 7000)
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