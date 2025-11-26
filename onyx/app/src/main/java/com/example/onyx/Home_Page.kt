package com.example.onyx

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.ScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import com.example.onyx.BuildConfig
import java.util.Locale

class Home_Page : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        LoadingAnimation.setup(this, R.raw.b)
        LoadingAnimation.show(this)



        NavAction.setupSidebar(this)

        //setupBackPressedCallback()

        sliderData()
        categoryShow()

    }





    private fun sliderData() {

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels     // in pixels
        val screenHeight = displayMetrics.heightPixels    // in pixels

        val recyclerView = findViewById<RecyclerView>(R.id.Slider_widget)
        val params = recyclerView.layoutParams
        params.height = (screenHeight * 0.80).toInt()
        recyclerView.layoutParams = params

        CoroutineScope(Dispatchers.IO).launch {

            repeat(10) { attempt ->
                try {
                    val url = "https://api.themoviedb.org/3/discover/movie?include_adult=true"
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")
                    connection.setRequestProperty(
                        "Authorization",
                        "Bearer ${BuildConfig.TM_K}"
                    )

                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)
                    val moviesArray = jsonObject.getJSONArray("results")


                    val url2 = "https://api.themoviedb.org/3/discover/tv?include_adult=true"
                    val connection2 = URL(url2).openConnection() as HttpURLConnection
                    connection2.requestMethod = "GET"
                    connection2.setRequestProperty("accept", "application/json")
                    connection2.setRequestProperty(
                        "Authorization",
                        "Bearer ${BuildConfig.TM_K}"
                    )

                    val response2 = connection2.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject2 = org.json.JSONObject(response2)
                    val moviesArray2 = jsonObject2.getJSONArray("results")


                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val url3 =
                        "https://api.themoviedb.org/3/trending/all/day?primary_release_year=$currentYear"
                    val connection3 = URL(url3).openConnection() as HttpURLConnection
                    connection3.requestMethod = "GET"
                    connection3.setRequestProperty("accept", "application/json")
                    connection3.setRequestProperty(
                        "Authorization",
                        "Bearer ${BuildConfig.TM_K}"
                    )

                    val response3 = connection3.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject3 = org.json.JSONObject(response3)
                    val moviesArray3 = jsonObject3.getJSONArray("results")

                    Log.e("DEBUG_MAIN_Slider raw", moviesArray3.toString())



                    var movies = mutableListOf<SliderItem>()
                    for (i in 0 until moviesArray.length()) {

                        val item = moviesArray.getJSONObject(i)

                        val title = item.getString("title")
                        //val backdrop_path = "https://image.tmdb.org/t/p/w1280" + item.getString("backdrop_path")

                        val backdrop_path =
                            if (item.has("backdrop_path") && !item.isNull("backdrop_path")) {
                                "https://image.tmdb.org/t/p/w1280${item.getString("backdrop_path")}"
                            } else if (item.has("poster_path") && !item.isNull("poster_path")) {
                                "https://image.tmdb.org/t/p/w780${item.getString("poster_path")}"
                            } else {
                                ""
                            }

                        val PG = if (item.optString("adult") == "true") "PG-18 +" else "PG-13"
                        if (PG == "PG-18 +") {
                            continue
                        }

                        val id = item.getString("id")
                        val type = "movie"
                        val overview = item.getString("overview")
                        val release_date = item.getString("release_date").substring(0, 4)
                        val vote_average = item.getString("vote_average").substring(0, 3)
                        val poster_path = item.getString("poster_path")
                        val genreIdsJson = item.getJSONArray("genre_ids")
                        val genreIds: List<Int> = List(genreIdsJson.length()) { idx ->
                            genreIdsJson.getInt(idx)
                        }

                        movies.add(
                            SliderItem(
                                title,
                                backdrop_path,
                                id,
                                type,
                                overview,
                                release_date,
                                vote_average,
                                poster_path,
                                genreIds,
                                PG
                            )

                        )
                    }


                    for (i in 0 until moviesArray2.length()) {

                        val item = moviesArray2.getJSONObject(i)
                        val title = item.getString("original_name")

                        val backdrop_path =
                            if (item.has("backdrop_path") && !item.isNull("backdrop_path")) {
                                "https://image.tmdb.org/t/p/w1280${item.getString("backdrop_path")}"
                            } else if (item.has("poster_path") && !item.isNull("poster_path")) {
                                "https://image.tmdb.org/t/p/w780${item.getString("poster_path")}"
                            } else {
                                ""
                            }

                        val PG = if (item.optString("adult") == "true") "PG-18 +" else "PG-13"

                        val id = item.getString("id")
                        val type = "tv"
                        val overview = item.getString("overview")
                        val release_date = item.getString("first_air_date").substring(0, 4)
                        val vote_average = item.getString("vote_average").substring(0, 3)
                        val poster_path = item.getString("poster_path")
                        val genreIdsJson = item.getJSONArray("genre_ids")
                        val genreIds: List<Int> = List(genreIdsJson.length()) { idx ->
                            genreIdsJson.getInt(idx)
                        }

                        movies.add(
                            SliderItem(
                                title,
                                backdrop_path,
                                id,
                                type,
                                overview,
                                release_date,
                                vote_average,
                                poster_path,
                                genreIds,
                                PG
                            )
                        )
                    }

                    for (i in 0 until moviesArray3.length()) {
                        val item = moviesArray3.getJSONObject(i)
                        val title = when {
                            item.has("original_name") && !item.isNull("original_name") -> item.getString(
                                "original_name"
                            )

                            item.has("original_title") && !item.isNull("original_title") -> item.getString(
                                "original_title"
                            )

                            item.has("title") && !item.isNull("title") -> item.getString("title")
                            else -> "Untitled"
                        }

                        val type = item.getString("media_type")
                        if (type != "movie" && type != "tv") {
                            continue   // skip this loop iteration
                        }

                        val backdrop_path =
                            if (item.has("backdrop_path") && !item.isNull("backdrop_path")) {
                                "https://image.tmdb.org/t/p/w1280${item.getString("backdrop_path")}"
                            } else if (item.has("poster_path") && !item.isNull("poster_path")) {
                                "https://image.tmdb.org/t/p/w780${item.getString("poster_path")}"
                            } else {
                                ""
                            }

                        val PG = if (item.optString("adult") == "true") "PG-18 +" else "PG-13"
                        val id = item.getString("id")
                        val overview = item.getString("overview")
                        val release_date = try {
                            item.getString("release_date").substring(0, 4)
                        } catch (e: Exception) {
                            item.getString("first_air_date").substring(0, 4)
                        }
                        val vote_average = item.getString("vote_average").substring(0, 3)
                        val poster_path = item.getString("poster_path")
                        val genreIdsJson = item.getJSONArray("genre_ids")
                        val genreIds: List<Int> = List(genreIdsJson.length()) { idx ->
                            genreIdsJson.getInt(idx)
                        }

                        movies.add(
                            SliderItem(
                                title,
                                backdrop_path,
                                id,
                                type,
                                overview,
                                release_date,
                                vote_average,
                                poster_path,
                                genreIds,
                                PG
                            )
                        )

                    }


                    //movies.shuffle()
                    movies = movies.distinctBy { it.imdbCode }.toMutableList()

                    withContext(Dispatchers.Main) {
                        val recyclerView = findViewById<RecyclerView>(R.id.Slider_widget)
                        val adapter = CardSwiper(movies, R.layout.card_layout)

                        val layoutManager = LinearLayoutManager(
                            this@Home_Page,
                            LinearLayoutManager.HORIZONTAL, // 👈 makes it horizontal
                            false
                        )
                        recyclerView.layoutManager = layoutManager
                        recyclerView.adapter = adapter
                        LoadingAnimation.hide(this@Home_Page)
                    }

                    return@launch
                } catch (e: Exception) {
                    delay(20_000)
                    Log.e("DEBUG_MAINSliderPage", "Error fetching data", e)
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////



    private fun categoryShow() {
        val company_show = mapOf(
            "Marvel Studios" to Pair(420, "https://image.tmdb.org/t/p/w1280/hUzeosd33nzE5MCNsZxCGEKTXaQ.png"),
            "Marvel Animation" to Pair(13252, "https://image.tmdb.org/t/p/w1280/1gKwYyTDNhumwBKUlKqoxXRUdpC.png"),
            "DC Films" to Pair(128064, "https://image.tmdb.org/t/p/w1280/13F3Jf7EFAcREU0xzZqJnVnyGXu.png"),
            "Walt Disney Pictures" to Pair(2, "https://image.tmdb.org/t/p/w1280/wdrCwmRnLFJhEoH8GSfymY85KHT.png"),
            "Walt Disney Television" to Pair(670, "https://image.tmdb.org/t/p/w1280/rRGi5UkwvdOPSfr5Xf42RZUsYgd.png"),
            "Warner Bros. Pictures" to Pair(174, "https://image.tmdb.org/t/p/w1280/zhD3hhtKB5qyv7ZeL4uLpNxgMVU.png"),
            "Universal Pictures" to Pair(33, "https://image.tmdb.org/t/p/w1280/3wwjVpkZtnog6lSKzWDjvw2Yi00.png"),
            "Paramount Pictures" to Pair(4, "https://image.tmdb.org/t/p/w1280/gz66EfNoYPqHTYI4q9UEN4CbHRc.png"),
            "Sony Pictures Entertainment" to Pair(34, "https://image.tmdb.org/t/p/w1280/mtp1fvZbe4H991Ka1HOORl572VH.png"),
            "Lionsgate " to Pair(1632, "https://image.tmdb.org/t/p/w1280/cisLn1YAUuptXVBa0xjq7ST9cH0.png"),
            "DreamWorks Animation " to Pair(521, "https://image.tmdb.org/t/p/w1280/3BPX5VGBov8SDqTV7wC1L1xShAS.png"),
            "Netflix Animation " to Pair(171251, "https://image.tmdb.org/t/p/w1280/AqUAfMC270bGGK09Nh3mycwT1hY.png"),
            "Netflix" to Pair(178464, "https://image.tmdb.org/t/p/w1280/tyHnxjQJLH6h4iDQKhN5iqebWmX.png"),
            "Pixar" to Pair(3, "https://image.tmdb.org/t/p/w1280/1TjvGVDMYsj6JBxOAkUHpPEwLf7.png"),
            "Illumination" to Pair(6704, "https://image.tmdb.org/t/p/w1280/fOG2oY4m1YuYTQh4bMqqZkmgOAI.png"),
            "Blue Sky Studios" to Pair(9383, "https://image.tmdb.org/t/p/w1280/ppeMh4iZJQUMm1nAjRALeNhWDfU.png"),
            "Laika" to Pair(11537, "https://image.tmdb.org/t/p/w1280/AgCkAk8EpUG9fTmK6mWcaJA2Zwh.png"),
            "Amazon Studios" to Pair(20580, "https://image.tmdb.org/t/p/w1280/oRR9EXVoKP9szDkVKlze5HVJS7g.png"),
            "HBO" to Pair(3268, "https://image.tmdb.org/t/p/w1280/tuomPhY2UtuPTqqFnKMVHvSb724.png"),
            "Apple" to Pair(14801, "https://image.tmdb.org/t/p/w1280/bnlD5KJ5oSzBYbEpDkwi6w8SoBO.png")
        )

        // Convert map to list of categoryItem objects
        val categoryItems = mutableListOf<categoryItem>()
        company_show.forEach { (name, pair) ->
            categoryItems.add(
                categoryItem(
                    cCode = pair.first.toString(),
                    cImg = pair.second,
                    cName = name
                )
            )
        }

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.CategoryRecyclerView)
        val adapter = CategoryAdapter(categoryItems, R.layout.item_category)

        val tvSpacing = (10 * resources.displayMetrics.density).toInt()
        recyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))

        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

    }


}

