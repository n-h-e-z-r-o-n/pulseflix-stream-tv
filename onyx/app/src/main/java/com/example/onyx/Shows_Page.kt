package com.example.onyx

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class Shows_Page : AppCompatActivity() {
    private var currentMoviePage = 1
    private var isLoadingMoreMovies = false
    private lateinit var movieAdapter: GridAdapter
    private lateinit var movieRecyclerView : RecyclerView

    private var currentTvPage = 1
    private var isLoadingMoreTv = false
    private lateinit var tvAdapter: GridAdapter
    private lateinit var tvRecyclerView : RecyclerView

    private lateinit var moviesBtn: LinearLayout
    private lateinit var tvBtn: LinearLayout
    private lateinit var SearchBtn: LinearLayout
    private lateinit var searchContainer: LinearLayout
    private lateinit var tvBtnText: TextView
    private lateinit var SearchBtnText: TextView
    private lateinit var searchContainerImg: ImageView

    private lateinit var searchAdapter: GridAdapter2



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shows_page)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        LoadingAnimation.setup(this, R.raw.b)
        //LoadingAnimation.show(this)


        NavAction.setupSidebar(this)

        ////////////////////////////////////////////////////////////////////////////////////////////
        moviesBtn = findViewById(R.id.MoviesButtonLayout)
        tvBtn = findViewById(R.id.TvButtonLayout)
        SearchBtn  =  findViewById(R.id.SearchButtonLayout)
        searchContainer  =  findViewById(R.id.searchContainerHome)

        tvBtnText = findViewById(R.id.MoviesButtonText)
        SearchBtnText = findViewById(R.id.TvButtonText)
        searchContainerImg = findViewById(R.id.SearchButtonImg)



        moviesBtn.setOnClickListener {
            showMovies()
        }
        tvBtn.setOnClickListener {
            showTv()
        }
        SearchBtn.setOnClickListener {
            showSearch()
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        setupBackPressedCallback()
        setupRecyclerViews()
        showMovies()
        fetchMovies()
        fetchTvShows()



    }

    private fun showMovies() {
        movieRecyclerView.visibility = View.VISIBLE
        tvRecyclerView.visibility = View.GONE
        searchContainer.visibility = View.GONE
        moviesBtn.alpha = 1f
        tvBtn.alpha = 0.5f
        SearchBtn.alpha = 0.5f

        moviesBtn.isSelected = true
        tvBtn.isSelected = false
        SearchBtn.isSelected = false

        val fgColor = getThemeColor(R.attr.FG_color)
        val accentColor = getThemeColor(R.attr.AccentColor)

        tvBtnText.setTextColor(accentColor)
        SearchBtnText.setTextColor(fgColor)
        searchContainerImg.setColorFilter(fgColor)

    }

    private fun showTv() {
        movieRecyclerView.visibility = View.GONE
        tvRecyclerView.visibility = View.VISIBLE
        searchContainer.visibility = View.GONE

        moviesBtn.alpha = 0.5f
        tvBtn.alpha = 1f
        SearchBtn.alpha = 0.5f


        val fgColor = getThemeColor(R.attr.FG_color)
        val accentColor = getThemeColor(R.attr.AccentColor)

        tvBtnText.setTextColor(fgColor)
        SearchBtnText.setTextColor(accentColor)
        searchContainerImg.setColorFilter(fgColor)

    }
    private fun showSearch() {
        movieRecyclerView.visibility = View.GONE
        tvRecyclerView.visibility = View.GONE
        searchContainer.visibility = View.VISIBLE
        moviesBtn.alpha = 0.5f
        tvBtn.alpha = 0.5f
        SearchBtn.alpha = 1f


        val fgColor = getThemeColor(R.attr.FG_color)
        val accentColor = getThemeColor(R.attr.AccentColor)

        tvBtnText.setTextColor(fgColor)
        SearchBtnText.setTextColor(fgColor)
        searchContainerImg.setColorFilter(accentColor)

    }
    private fun getThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun setupRecyclerViews() {
        // 🎬 Movies
        val item_grid2_width = 289
        movieRecyclerView = findViewById(R.id.MoviesRecyclerView)
        movieRecyclerView.layoutManager  = GridLayoutManager(this@Shows_Page, GlobalUtils.calculateSpanCount(this, item_grid2_width))

        val movieSpacing = (15 * resources.displayMetrics.density).toInt()
        movieRecyclerView.addItemDecoration(EqualSpaceItemDecoration(movieSpacing))

        movieAdapter = GridAdapter(mutableListOf(), R.layout.item_grid2)
        movieRecyclerView.adapter = movieAdapter
        movieAdapter.onAddMoreClicked = { loadMoreMovies() }
        movieAdapter.onItemFocused = { view, item ->
            //showPopupBeside(view, item)
            view.waitForDraw  {
                showPopupBeside(view, item)
            }
        }


        // 📺 TV Shows
        tvRecyclerView = findViewById(R.id.TVsRecyclerView)
        tvRecyclerView.layoutManager  = GridLayoutManager(this@Shows_Page, GlobalUtils.calculateSpanCount(this, item_grid2_width) )




        val tvSpacing = (16 * resources.displayMetrics.density).toInt()
        tvRecyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))

        tvAdapter = GridAdapter(mutableListOf(), R.layout.item_grid2)
        tvRecyclerView.adapter = tvAdapter
        tvAdapter.onAddMoreClicked = { loadMoreTv() }


        searchAdapter = GridAdapter2(mutableListOf(), R.layout.item_grid)
        val searchRecyclerView = findViewById<RecyclerView>(R.id.SearchResults)
        searchRecyclerView.layoutManager = GridLayoutManager(this@Shows_Page, 6)
        searchRecyclerView.adapter = searchAdapter
        val spacing = (19 * resources.displayMetrics.density).toInt()
        searchRecyclerView.addItemDecoration(EqualSpaceItemDecoration(spacing))
        setupSearchUi()

    }


    fun View.waitForDraw(onDraw: () -> Unit) {
        if (isShown) {
            val vto = viewTreeObserver
            vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    onDraw()
                    return true
                }
            })
        } else {
            post { waitForDraw(onDraw) }
        }
    }



    private fun showPopupBeside(targetView: View, item: MovieItemOne) {
        val popup = findViewById<CardView>(R.id.floatingPopup)

        // If view is not laid out yet → wait
        if (targetView.width == 0 || targetView.height == 0) {
            targetView.post { showPopupBeside(targetView, item) }
            return
        }

        // Set popup text
        //findViewById<TextView>(R.id.popupTitle).text = item.posterUlr
        val imageC = findViewById<ImageView>(R.id.floatingPopupImg)

        Glide.with(targetView.context)
            .load(item.posterUlr)
            .centerCrop()
            .into(imageC)

        val margin = 0.dp(targetView.context)

        // Get item location
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val popupWidth = popup.width.takeIf { it > 0 } ?: 300  // fallback estimate

        var popupX = x + targetView.width + margin   // default → right side

        //  If it goes off-screen, move popup to LEFT
        if (popupX + popupWidth > screenWidth) {
            popupX = x - popupWidth - margin
            if (popupX < 0) popupX = margin // safety
        }

        //val popupY = y + (targetView.height / 3)
        val popupY = y


        // Apply position
        popup.x = popupX.toFloat()
        popup.y = popupY.toFloat()

        popup.visibility = View.VISIBLE
        popup.alpha = 0f
        popup.animate().alpha(1f).setDuration(150).start()
    }


    private fun Int.dp(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()


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

                    val movies = mutableListOf<MovieItemOne>()

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
                        val imgUrl2 =
                            if (detailsJson.has("poster_path") && !detailsJson.isNull("poster_path")) {
                                "https://image.tmdb.org/t/p/w780${detailsJson.getString("poster_path")}"
                            } else if (detailsJson.has("backdrop_path") && !detailsJson.isNull("backdrop_path")) {
                                "https://image.tmdb.org/t/p/w1280${detailsJson.getString("backdrop_path")}"
                            } else {
                                ""
                            }

                        movies.add(
                            MovieItemOne(
                                title = title,
                                backdropUrl = imgUrl,
                                posterUlr = imgUrl2,
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


                    val movies = mutableListOf<MovieItemOne>()
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

                        val imgUrl2 =
                            if (jsonObject.has("poster_path") && !jsonObject.isNull("poster_path")) {
                                "https://image.tmdb.org/t/p/w780${jsonObject.getString("poster_path")}"
                            } else if (jsonObject.has("backdrop_path") && !jsonObject.isNull("backdrop_path")) {
                                "https://image.tmdb.org/t/p/w1280${jsonObject.getString("backdrop_path")}"

                            } else {
                                ""
                            }

                        val id = jsonObject.getString("id")
                        val type = "tv"
                        movies.add(MovieItemOne(title=title, backdropUrl = imgUrl, posterUlr = imgUrl2, imdbCode=id, type=type, year="", rating="", runtime=""))

                        val MovieItemOne = MovieItemOne(title=title, backdropUrl = imgUrl, posterUlr = imgUrl2, imdbCode=id, type=type, year=firstAirDate, rating=voteAverage, runtime=showD)

                        withContext(Dispatchers.Main) {
                            tvAdapter.addItem(MovieItemOne)
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

                findViewById<ImageButton>(R.id.btnMvTv).requestFocus()

            }
        })
    }


}
