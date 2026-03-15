package com.example.onyx

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.onyx.OnyxObjects.StreamingLinks
import kotlinx.coroutines.launch

class testpage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_testpage)

        lifecycleScope.launch {
            /*
            val webView = findViewById<WebView>(R.id.webViewTest)

            //https://vidsrc.to/embed/movie/533444
            val stream = StreamingLinks.extractStreamFromServer(
                this@testpage,
                "https://www.vidking.net/embed/movie/533444",
                webView
            )
            Log.e("Stream-Result", "Server : $stream")

            */


            //val result = StreamingLinks.extractAllStreams(this@testpage, "533444", "movie", "", "")
            val result = StreamingLinks.extractAllStreamsParallel(this@testpage, "533444", "movie", "", "")

            Log.d("Stream-Result", " extractAll : $result")


        }
    }
}