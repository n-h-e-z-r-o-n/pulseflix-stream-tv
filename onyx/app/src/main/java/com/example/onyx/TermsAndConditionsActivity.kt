package com.example.onyx

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.onyx.OnyxObjects.GlobalUtils
import com.example.onyx.OnyxObjects.NavAction

class TermsAndConditionsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_terms_and_conditions)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Load Terms and Conditions content asynchronously
        val tvTermsContent = findViewById<android.widget.TextView>(R.id.tv_terms_content)
        
        Thread {
            try {
                val inputStream = resources.openRawResource(R.raw.terms_and_conditions)
                val buffer = ByteArray(inputStream.available())
                inputStream.read(buffer)
                val str = String(buffer)
                
                runOnUiThread {
                    tvTermsContent.text = str
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    tvTermsContent.text = "Error loading Terms and Conditions."
                }
            }
        }.start()

}}


