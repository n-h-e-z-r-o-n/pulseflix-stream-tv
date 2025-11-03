package com.example.onyx

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class Instraction : AppCompatActivity() {
    private var slideshowJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_instraction)
        loadTrendingMovies()
    }

    private fun   loadTrendingMovies() {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.themoviedb.org/3/trending/all/day"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("accept", "application/json")
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhZjliMmUyN2MxYTZiYzMyMzNhZjE4MzJmNGFjYzg1MCIsIm5iZiI6MTcxOTY3NDUxNy4xOTYsInN1YiI6IjY2ODAyNjk1ZWZhYTI1ZjBhOGE4NGE3MyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.RTms-g8dzOl3WwCeJ7WNLq3i2kXxl3T7gOTa8POcxcw"
                )

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = org.json.JSONObject(response)
                val moviesArray = jsonObject.getJSONArray("results")

                val outputList = mutableListOf<String>()



                for (i in 0 until moviesArray.length()) {
                    val current = moviesArray.getJSONObject(i)
                    val poster = current.optString("poster_path", "")
                    val backdrop_path = current.optString("backdrop_path", "")

                    if (poster.isNotBlank() && !poster.endsWith("null")) {
                        val imgUrl = "https://image.tmdb.org/t/p/w780$poster"
                        val imgUrls = "https://image.tmdb.org/t/p/w1280$backdrop_path"
                        outputList.add(imgUrls)
                    }
                }

                val displaySection = findViewById<ImageView>(R.id.tvScreen)
                val displaySection2 = findViewById<ImageView>(R.id.pageBackground)

                // Loop posters like a slideshow
                slideshowJob = CoroutineScope(Dispatchers.Main).launch {
                    while (true) {
                        try {
                            for (imgUrl in outputList) {
                                Glide.with(this@Instraction)
                                    .load(imgUrl)
                                    .centerCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade(500))
                                    .into(displaySection)

                                Glide.with(this@Instraction)
                                    .load(imgUrl)
                                    .centerCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade(500))
                                    .into(displaySection2)

                                delay(20500)
                            }
                        } catch (e: Exception ){
                            break
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("PayWall", "Error loading trending movies", e)
            }
        }
    }
}