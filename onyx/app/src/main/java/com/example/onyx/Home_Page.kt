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

class Home_Page : AppCompatActivity() {


    private var currentMoviePage = 1
    private var isLoadingMoreMovies = false
    private lateinit var movieAdapter: GridAdapter
    private lateinit var movieRecyclerView : RecyclerView

    private var currentTvPage = 1
    private var isLoadingMoreTv = false
    private lateinit var tvAdapter: GridAdapter
    private lateinit var searchAdapter: GridAdapter2

    private lateinit var tvRecyclerView : RecyclerView

    private var isSearchContainerHomeVisible = false

    private var lastRefreshTime: Long = 0L



    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        LoadingAnimation.setup(this, R.raw.b)
        LoadingAnimation.show(this)



        NavAction.setupSidebar(this)

        setupBackPressedCallback()

        setupRecyclerViews()
        sliderData()
        fetchMovies()
        fetchTvShows()
    }

    private fun setupRecyclerViews() {
        // 🎬 Movies
        movieRecyclerView = findViewById(R.id.MoviesRecyclerView)
        movieRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false).apply {
            isSmoothScrollbarEnabled = false
            isItemPrefetchEnabled = false
        }

        val movieSpacing = (15 * resources.displayMetrics.density).toInt()
        movieRecyclerView.addItemDecoration(EqualSpaceItemDecoration(movieSpacing))

        movieAdapter = GridAdapter(mutableListOf(), R.layout.item_grid2)
        movieRecyclerView.adapter = movieAdapter
        movieAdapter.onAddMoreClicked = { loadMoreMovies() }

        // 📺 TV Shows
        tvRecyclerView = findViewById(R.id.TVsRecyclerView)
        tvRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false).apply {
            isSmoothScrollbarEnabled = false
            isItemPrefetchEnabled = false
        }

        val tvSpacing = (16 * resources.displayMetrics.density).toInt()
        tvRecyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))

        tvAdapter = GridAdapter(mutableListOf(), R.layout.item_grid2)
        tvRecyclerView.adapter = tvAdapter
        tvAdapter.onAddMoreClicked = { loadMoreTv() }



        searchAdapter = GridAdapter2(mutableListOf(), R.layout.item_grid)
        val searchRecyclerView = findViewById<RecyclerView>(R.id.SearchResults)
        searchRecyclerView.layoutManager = GridLayoutManager(this@Home_Page, 6)
        searchRecyclerView.adapter = searchAdapter
        val spacing = (19 * resources.displayMetrics.density).toInt()
        searchRecyclerView.addItemDecoration(EqualSpaceItemDecoration(spacing))
        setupSearchUi()
    }




    private fun sliderData() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels     // in pixels
        val screenHeight = displayMetrics.heightPixels    // in pixels

        val recyclerView = findViewById<RecyclerView>(R.id.Slider_widget)
        val params = recyclerView.layoutParams
        params.height = (screenHeight * 0.75).toInt()
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
                        Log.e("DEBUG_MAIN_Slider 1", movies.toString())
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

    private fun fetchMovies() {
        if (isLoadingMoreMovies) return
        isLoadingMoreMovies = true

        CoroutineScope(Dispatchers.IO).launch {
            repeat(3) { attempt ->
                try {
                    val url = "https://vidsrc.xyz/movies/latest/page-$currentMoviePage.json"
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    val jsonObject = JSONObject(response)
                    val moviesArray = jsonObject.getJSONArray("result")

                    val movieIds = mutableSetOf<String>()
                    for (i in 0 until moviesArray.length()) {
                        val item = moviesArray.getJSONObject(i)
                        val tmdbId = item.optString("tmdb_id")
                        if (tmdbId.isNotEmpty() && tmdbId != "null") {
                            movieIds.add(tmdbId)
                        }
                    }

                    Log.e("DEBUG_MOVIES_IDS", movieIds.joinToString())

                    val movies = mutableListOf<MovieItem>()

                    for (id in movieIds) {
                        val detailsUrl = "https://api.themoviedb.org/3/movie/$id"
                        val detailsConn = URL(detailsUrl).openConnection() as HttpURLConnection
                        detailsConn.requestMethod = "GET"
                        detailsConn.setRequestProperty("accept", "application/json")
                        detailsConn.setRequestProperty(
                            "Authorization",
                            "Bearer ${BuildConfig.TM_K}"
                        )

                        val detailsResponse = detailsConn.inputStream.bufferedReader().use { it.readText() }
                        val detailsJson = JSONObject(detailsResponse)

                        val title = detailsJson.optString("title", "Unknown")
                        val year = detailsJson.optString("release_date", "")
                        val runtime = detailsJson.optString("runtime", "0")
                        val rating = detailsJson.optDouble("vote_average", 0.0)
                        //val imgUrl = "https://image.tmdb.org/t/p/w780" + detailsJson.optString("poster_path", "")
                        val imgUrl =
                            if (detailsJson.has("backdrop_path") && !detailsJson.isNull("backdrop_path")) {
                                "https://image.tmdb.org/t/p/w1280${detailsJson.getString("backdrop_path")}"
                            } else if (detailsJson.has("poster_path") && !detailsJson.isNull("poster_path")) {
                                "https://image.tmdb.org/t/p/w780${detailsJson.getString("poster_path")}"
                            } else {
                                ""
                            }

                        movies.add(
                            MovieItem(
                                title = title,
                                imageUrl = imgUrl,
                                imdbCode = id,
                                type = "movie",
                                year = year,
                                rating = "☆${String.format("%.1f", rating)}",
                                runtime = "⏱$runtime min"
                            )
                        )
                    }

                    // ✅ Update UI once per batch
                    withContext(Dispatchers.Main) {
                        movieAdapter.addItems(movies)
                        isLoadingMoreMovies = false
                    }

                    return@launch // success → stop repeating
                } catch (e: Exception) {
                    Log.e("DEBUG_MOVIES_ERROR", "Attempt ${attempt + 1} failed: ${e.message}", e)
                    delay(5000)
                }
            }

            withContext(Dispatchers.Main) {
                isLoadingMoreMovies = false
            }
        }
    }

    private fun loadMoreMovies() {
        if (isLoadingMoreMovies) return // Prevent multiple rapid clicks
        currentMoviePage++
        fetchMovies()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun fetchTvShows() {
        isLoadingMoreTv = true
        CoroutineScope(Dispatchers.IO).launch {

            repeat(5) { attempt ->
                try {
                    val url = "https://vidsrc.xyz/episodes/latest/page-$currentTvPage.json"
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val moviesArray = jsonObject.getJSONArray("result")

                    val finalArray = JSONArray()

                    for (i in 0 until moviesArray.length()) {
                        finalArray.put(moviesArray.getJSONObject(i))
                    }

                    Log.e("DEBUG_TAG_TvShows 1", finalArray.toString())


                    val movies = mutableListOf<MovieItem>()

                    val movies_temp = mutableListOf<String>()

                    for (i in 0 until finalArray.length()) {
                        val item = finalArray.getJSONObject(i)
                        val imdb_code = item.getString("tmdb_id")
                        if (imdb_code == "null" || imdb_code.isEmpty()) continue
                        movies_temp.add(imdb_code)
                    }
                    val uniqueMovies = movies_temp.toSet().toList()

                    Log.e("DEBUG_TAG_TvShows 2", movies_temp.toString())


                    for (i in 0 until uniqueMovies.size) {
                        val imdb_code = uniqueMovies[i]
                        val url = "https://api.themoviedb.org/3/tv/$imdb_code?"
                        val connection = URL(url).openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.setRequestProperty("accept", "application/json")
                        connection.setRequestProperty(
                            "Authorization",
                            "Bearer ${BuildConfig.TM_K}"
                        )
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonObject = org.json.JSONObject(response)

                        Log.e("DEBUG_TAG_TvShows 3", jsonObject.toString())

                        val title = jsonObject.getString("name")
                        val numberOfSeasons = try{jsonObject.getJSONObject("last_episode_to_air").getString("season_number")} catch (e: Exception) {""}
                        val episodeNumber = try{jsonObject.getJSONObject("last_episode_to_air").getString("episode_number")} catch (e: Exception) {""}
                        val showD = "SS$numberOfSeasons EPS$episodeNumber"
                        val firstAirDate = if (jsonObject.getString("first_air_date").length >= 4) {
                                  jsonObject.getString("first_air_date").substring(0, 4)
                                } else{
                                  jsonObject.getString("first_air_date")}

                        val voteAverage = "☆" + jsonObject.getString("vote_average").substring(0, 3)

                        //val imgUrl = "https://image.tmdb.org/t/p/w500" + jsonObject.getString("poster_path")
                        val imgUrl =
                            if (jsonObject.has("backdrop_path") && !jsonObject.isNull("backdrop_path")) {
                                "https://image.tmdb.org/t/p/w1280${jsonObject.getString("backdrop_path")}"
                            } else if (jsonObject.has("poster_path") && !jsonObject.isNull("poster_path")) {
                                "https://image.tmdb.org/t/p/w780${jsonObject.getString("poster_path")}"
                            } else {
                                ""
                            }
                        val id = jsonObject.getString("id")
                        val type = "tv"
                        movies.add(MovieItem(title=title, imageUrl=imgUrl, imdbCode=id, type=type, year="", rating="", runtime=""))

                        val movieItem = MovieItem(title=title, imageUrl=imgUrl, imdbCode=id, type=type, year=firstAirDate, rating=voteAverage, runtime=showD)

                        withContext(Dispatchers.Main) {
                            tvAdapter.addItem(movieItem)
                            isLoadingMoreTv = false
                        }


                    }
                    Log.e("DEBUG_TAG_TvShows 4", movies.toString())

                    return@launch
                } catch (e: Exception) {
                    Log.e("DEBUG_TAG_TvShows", "Attempt ${attempt+1} failed", e)
                    delay(10_000)
                    currentTvPage--
                }
                withContext(Dispatchers.Main) {
                    isLoadingMoreTv = false
                }
            }
        }
    }

    private fun loadMoreTv() {

        if (isLoadingMoreTv) return // Prevent multiple rapid clicks
        currentTvPage++
        fetchTvShows()

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun setupSearchUi() {

        val searchInput = findViewById<EditText>(R.id.HomeSearchInput)
        try{
            searchInput.setOnEditorActionListener { _, actionId, event ->

                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {

                    val searchTerm = searchInput.text.toString().trim()
                    if (searchTerm.isNotEmpty()) {
                        findViewById<LinearLayout>(R.id.searchContainerHome).visibility = View.VISIBLE
                        findViewById<ScrollView>(R.id.HomeContainer).visibility = View.GONE
                        isSearchContainerHomeVisible = true
                        findViewById<View>(R.id.HomeSearchInput).nextFocusDownId = R.id.searchContainerHome

                        // Hide keyboard
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)

                        performSearch(searchTerm)

                    }

                    true
                } else {
                    false
                }
            }

        }catch (e: Exception){
            Log.e("ANIME_STATUS S-Error", "setupSearchUi() ", e)

        }
    }

    private fun performSearch(searchTerm:String) {
        CoroutineScope(Dispatchers.IO).launch {
            repeat(3) { attempt ->
                try {

                        Log.e("SEARCH RESULTS", searchTerm)

                        // --- Background work (network request) ---
                        val url =
                            "https://api.themoviedb.org/3/search/multi?include_adult=true&query=$searchTerm"
                        val connection = URL(url).openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.setRequestProperty("accept", "application/json")
                        connection.setRequestProperty(
                            "Authorization",
                            "Bearer ${BuildConfig.TM_K}"
                        )

                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonObject = JSONObject(response)

                        Log.e("SEARCH RESULTS", jsonObject.toString())
                        val moviesArray = jsonObject.getJSONArray("results")

                        //Log.e("SEARCH RESULTS", moviesArray.toString())

                        for (i in 0 until moviesArray.length()) {
                            val current = moviesArray.getJSONObject(i)
                            current.remove("overview")
                            current.remove("genre_ids")
                            current.remove("popularity")
                            current.remove("video")

                            val mediaType = current.optString("media_type", "movie")

                            var title = "Unknown"
                            var info = ""
                            var date = ""
                            var voteAverage = ""
                            var imgUrl = ""
                            var poster = ""

                            Log.e("SEARCH RESULTS $i", current.toString())
                            when (mediaType) {
                                "person" -> {
                                    title = current.optString(
                                        "name",
                                        current.optString("original_name", "Unknown")
                                    )
                                    imgUrl = "https://image.tmdb.org/t/p/w500" + current.optString(
                                        "profile_path",
                                        ""
                                    )
                                    poster = current.optString("profile_path", "null")
                                    info = current.optString("known_for_department", "")
                                    voteAverage = ""
                                }

                                "tv" -> {
                                    title = current.optString(
                                        "name",
                                        current.optString("original_name", "Unknown")
                                    )
                                    date =
                                        current.optString("first_air_date")
                                            .takeIf { it.isNotEmpty() }
                                            ?.substring(0, 4) ?: ""
                                    imgUrl = "https://image.tmdb.org/t/p/w500" + current.optString(
                                        "poster_path",
                                        ""
                                    )
                                    poster = current.optString("poster_path", "null")
                                    info = ""
                                    voteAverage =
                                        current.optDouble("vote_average", 0.0).toInt()
                                            .toString() + " ★"
                                }

                                "movie" -> {
                                    title = current.optString("original_title", "Unknown")
                                    date =
                                        current.optString("release_date").takeIf { it.isNotEmpty() }
                                            ?.substring(0, 4) ?: ""
                                    imgUrl = "https://image.tmdb.org/t/p/w500" + current.optString(
                                        "poster_path",
                                        ""
                                    )
                                    poster = current.optString("poster_path", "null")
                                    info = "" // TODO: runtime if available
                                    voteAverage = current.optDouble("vote_average", 0.0).toInt()
                                        .toString() + " ★"
                                }
                            }

                            if (poster.isBlank() || poster.endsWith("null")) continue

                            val id = current.getString("id")


                            //movies.add(MovieItem(title, imgUrl, id, type))

                            val movieItem = MovieItem(
                                title = title,
                                imageUrl = imgUrl,
                                imdbCode = id,
                                type = mediaType,
                                year = date,
                                rating = voteAverage,
                                runtime = info
                            )

                            withContext(Dispatchers.Main) {
                                searchAdapter.addItem(movieItem)
                            }

                        }


                    return@launch
                } catch (e: Exception) {
                    Log.e("SEARCH RESULTS ERROR", "S ERROR", e)
                    delay(10_000)
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (isSearchContainerHomeVisible) {
                    findViewById<LinearLayout>(R.id.searchContainerAnime).visibility = View.GONE
                    findViewById<LinearLayout>(R.id.HomeContainer).visibility = View.VISIBLE
                    isSearchContainerHomeVisible = false
                    findViewById<View>(R.id.HomeSearchInput).nextFocusDownId = R.id.spotlightMovie


                }else {
                    findViewById<ImageButton>(R.id.btnHome).requestFocus()
                }
            }
        })
    }


}

