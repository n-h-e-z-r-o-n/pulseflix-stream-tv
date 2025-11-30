package com.example.onyx

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
import com.bumptech.glide.request.target.Target
import java.io.IOException

class Shows_Page : AppCompatActivity() {
    private var currentMoviePage = 1
    private var isLoadingMoreMovies = false



    private var currentTvPage = 1
    private var isLoadingMoreTv = false



    private lateinit var moviesBtn: LinearLayout
    private lateinit var tvBtn: LinearLayout
    private lateinit var SearchBtn: LinearLayout
    private lateinit var filterBtn: LinearLayout


    private lateinit var mvBtnText: TextView
    private lateinit var tvBtnText: TextView


    private lateinit var searchContainerImg: ImageView
    private lateinit var filterContainerImg: ImageView

    private lateinit var searchContainer: LinearLayout
    private lateinit var fliterContainer: LinearLayout


    //Adapters
    private lateinit var movieAdapter: GridAdapter
    private lateinit var tvAdapter: GridAdapter
    private lateinit var searchAdapter: GridAdapter2
    private lateinit var filterAdapter: FilterAdapter


    //RecyclerViews
    private lateinit var tvRecyclerView : RecyclerView
    private lateinit var movieRecyclerView : RecyclerView
    private lateinit var searchRecyclerView : RecyclerView
    private lateinit var fliterRecyclerView : RecyclerView






    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shows_page)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        LoadingAnimation.setup(this, R.raw.b)
        LoadingAnimation.show(this)
        NavAction.setupSidebar(this)

        ////////////////////////////////////////////////////////////////////////////////////////////
        moviesBtn = findViewById(R.id.MoviesButtonLayout)
        tvBtn = findViewById(R.id.TvButtonLayout)
        SearchBtn  =  findViewById(R.id.SearchButtonLayout)
        filterBtn = findViewById(R.id.FilterButtonLayout)




        mvBtnText = findViewById(R.id.MoviesButtonText)
        tvBtnText = findViewById(R.id.TvButtonText)

        searchContainer  =  findViewById(R.id.searchContainerHome)
        searchContainerImg = findViewById(R.id.SearchButtonImg)

        fliterContainer  =  findViewById(R.id.FilterContainer)
        filterContainerImg= findViewById(R.id.FilterButtonImg)



        moviesBtn.setOnClickListener {
            showMovies()
        }
        tvBtn.setOnClickListener {
            showTv()
        }
        SearchBtn.setOnClickListener {
            showSearch()
        }
        filterBtn.setOnClickListener {
            showFilter()
        }
        ////////////////////////////////////////////////////////////////////////////////////////////

        setupRecyclerViews()
        showMovies()
        fetchMovies()
        fetchTvShows()
        filter()
    }

    private fun showMovies() {
        movieRecyclerView.visibility = View.VISIBLE
        tvRecyclerView.visibility = View.GONE
        searchContainer.visibility = View.GONE
        fliterContainer.visibility = View.GONE


        moviesBtn.isSelected = true
        tvBtn.isSelected = false
        SearchBtn.isSelected = false

        val fgColor = getThemeColor(R.attr.FG_color)
        val accentColor = getThemeColor(R.attr.AccentColor)

        tvBtnText.setTextColor(fgColor)
        mvBtnText.setTextColor(accentColor )
        searchContainerImg.setColorFilter(fgColor)
        filterContainerImg.setColorFilter(fgColor)

    }

    private fun showTv() {
        movieRecyclerView.visibility = View.GONE
        tvRecyclerView.visibility = View.VISIBLE
        searchContainer.visibility = View.GONE
        fliterContainer.visibility = View.GONE




        val fgColor = getThemeColor(R.attr.FG_color)
        val accentColor = getThemeColor(R.attr.AccentColor)

        tvBtnText.setTextColor(accentColor )
        mvBtnText.setTextColor(fgColor)
        searchContainerImg.setColorFilter(fgColor)
        filterContainerImg.setColorFilter(fgColor)

    }

    private fun showFilter() {
        movieRecyclerView.visibility = View.GONE
        tvRecyclerView.visibility = View.GONE
        searchContainer.visibility = View.GONE
        fliterContainer.visibility = View.VISIBLE



        val fgColor = getThemeColor(R.attr.FG_color)
        val accentColor = getThemeColor(R.attr.AccentColor)

        tvBtnText.setTextColor(fgColor )
        mvBtnText.setTextColor(fgColor)
        searchContainerImg.setColorFilter(fgColor)
        filterContainerImg.setColorFilter(accentColor)

    }
    private fun showSearch() {
        movieRecyclerView.visibility = View.GONE
        tvRecyclerView.visibility = View.GONE
        searchContainer.visibility = View.VISIBLE
        fliterContainer.visibility = View.GONE



        val fgColor = getThemeColor(R.attr.FG_color)
        val accentColor = getThemeColor(R.attr.AccentColor)

        tvBtnText.setTextColor(fgColor)
        mvBtnText.setTextColor(fgColor)
        searchContainerImg.setColorFilter(accentColor)
        filterContainerImg.setColorFilter(fgColor)

    }
    private fun getThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun setupRecyclerViews() {

        val Spacing = (16 * resources.displayMetrics.density).toInt()
        val item_grid2_width = 289


        //  Movies
        movieRecyclerView = findViewById(R.id.MoviesRecyclerView)
        movieRecyclerView.layoutManager  = GridLayoutManager(this@Shows_Page, GlobalUtils.calculateSpanCount(this, item_grid2_width))
        movieRecyclerView.addItemDecoration(EqualSpaceItemDecoration(Spacing))
        movieAdapter = GridAdapter(mutableListOf(), R.layout.item_grid2)
        movieRecyclerView.adapter = movieAdapter
        movieAdapter.onAddMoreClicked = { loadMoreMovies() }
        movieAdapter.onItemFocused = { view, item ->
            showPopupBeside(view, item.posterUlr, 170)
        }
        movieAdapter.onItemFocusLost = {
            hidePopup()
        }


        //  TV Shows
        tvRecyclerView = findViewById(R.id.TVsRecyclerView)
        tvRecyclerView.layoutManager  = GridLayoutManager(this@Shows_Page, GlobalUtils.calculateSpanCount(this, item_grid2_width) )
        tvRecyclerView.addItemDecoration(EqualSpaceItemDecoration(Spacing))
        tvAdapter = GridAdapter(mutableListOf(), R.layout.item_grid2)
        tvRecyclerView.adapter = tvAdapter
        tvAdapter.onAddMoreClicked = { loadMoreTv() }
        tvAdapter.onItemFocused = { view, item ->
            showPopupBeside(view, item.posterUlr, 170)
        }
        tvAdapter.onItemFocusLost = {
            hidePopup()
        }

        //  Search
        searchAdapter = GridAdapter2(mutableListOf(), R.layout.item_grid)
        searchRecyclerView = findViewById<RecyclerView>(R.id.SearchResults)
        searchRecyclerView.layoutManager = GridLayoutManager(this@Shows_Page, GlobalUtils.calculateSpanCountV2(this, 140,320 ))

        searchRecyclerView.adapter = searchAdapter
        searchRecyclerView.addItemDecoration(EqualSpaceItemDecoration(Spacing))
        setupSearchUi()

        // Filter
        filterAdapter = FilterAdapter(mutableListOf(), R.layout.item_filter)
        filterAdapter.onItemFocused = { view, item ->
            showPopupBeside(view, item.posterUlr, 195)
        }
        filterAdapter.onItemFocusLost = {
            hidePopup()
        }
        fliterRecyclerView = findViewById<RecyclerView>(R.id.filterResults)
        fliterRecyclerView.layoutManager = GridLayoutManager(this@Shows_Page, GlobalUtils.calculateSpanCount(this, 140))
        fliterRecyclerView.adapter = filterAdapter
        fliterRecyclerView.addItemDecoration(EqualSpaceItemDecoration(Spacing))



    }


    private fun showPopupBeside(targetView: View, imageUrl: String, popupHeightDp: Int = 0) {
        val popup = findViewById<CardView>(R.id.floatingPopup)

        val popupHeightPx = (popupHeightDp * targetView.context.resources.displayMetrics.density).toInt()
        popup.layoutParams.height = popupHeightPx
        popup.requestLayout()

        //findViewById<TextView>(R.id.popupTitle).text = item.posterUlr
        val imageC = findViewById<ImageView>(R.id.floatingPopupImg)

        Glide.with(targetView.context)
            .load(imageUrl)
            .override(Target.SIZE_ORIGINAL, popup.height) // scale height to container
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
    private fun hidePopup() {
        val popup = findViewById<CardView>(R.id.floatingPopup)
        if (popup.visibility == View.VISIBLE) {
            popup.animate()
                .alpha(0f)
                .setDuration(120)
                .withEndAction { popup.visibility = View.GONE }
                .start()
        }
    }


    private fun Int.dp(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()


    private fun fetchMovies() {
        if (isLoadingMoreMovies) return
        isLoadingMoreMovies = true
        movieAdapter.isLoadingMore = true

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
                        movieAdapter.isLoadingMore = false
                        LoadingAnimation.hide(this@Shows_Page)
                    }

                    return@launch // success → stop repeating
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Log.e("DEBUG_SHOWS PAGE", "Network error ", e)
                        LoadingAnimation.setup(this@Shows_Page, R.raw.error)
                        LoadingAnimation.show(this@Shows_Page)
                    }
                    delay(30_000)
                } catch (e: Exception) {
                    Log.e("DEBUG_MOVIES_ERROR", "Attempt ${attempt + 1} failed: ${e.message}", e)
                    delay(5000)
                }
            }

            withContext(Dispatchers.Main) {
                isLoadingMoreMovies = false
                movieAdapter.isLoadingMore = false
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
        tvAdapter.isLoadingMore = true
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
                        val numberOfSeasons = try {
                            jsonObject.getJSONObject("last_episode_to_air")
                                .getString("season_number")
                        } catch (e: Exception) {
                            ""
                        }
                        val episodeNumber = try {
                            jsonObject.getJSONObject("last_episode_to_air")
                                .getString("episode_number")
                        } catch (e: Exception) {
                            ""
                        }
                        val showD = "SS$numberOfSeasons EPS$episodeNumber"
                        val firstAirDate = if (jsonObject.getString("first_air_date").length >= 4) {
                            jsonObject.getString("first_air_date").substring(0, 4)
                        } else {
                            jsonObject.getString("first_air_date")
                        }

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
                        //movies.add(MovieItemOne(title=title, backdropUrl = imgUrl, posterUlr = imgUrl2, imdbCode=id, type=type, year="", rating="", runtime=""))

                        val MovieItemOne = MovieItemOne(
                            title = title,
                            backdropUrl = imgUrl,
                            posterUlr = imgUrl2,
                            imdbCode = id,
                            type = type,
                            year = firstAirDate,
                            rating = voteAverage,
                            runtime = showD
                        )

                        withContext(Dispatchers.Main) {
                            tvAdapter.addItem(MovieItemOne)
                            isLoadingMoreTv = false
                            tvAdapter.isLoadingMore = false
                            LoadingAnimation.hide(this@Shows_Page)
                        }


                    }
                    Log.e("DEBUG_TAG_TvShows 4", movies.toString())

                    return@launch
                } catch (e: IOException) {
                        withContext(Dispatchers.Main) {
                            Log.e("DEBUG_SHOWS PAGE", "Network error ", e)
                            LoadingAnimation.setup(this@Shows_Page, R.raw.error)
                            LoadingAnimation.show(this@Shows_Page)
                        }
                        delay(30_000)

                } catch (e: Exception) {
                    Log.e("DEBUG_TAG_TvShows", "Attempt ${attempt+1} failed", e)
                    delay(10_000)
                    currentTvPage--
                }
                withContext(Dispatchers.Main) {
                    isLoadingMoreTv = false
                    tvAdapter.isLoadingMore = false
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
        val keyboardLayout = findViewById<LinearLayout>(R.id.keyboard_layout)
        val keyboardManager = CustomKeyboardManager(
            this, 
            searchInput, 
            keyboardLayout,
            object : OnSearchListener {
                override fun EnterActionTrigger(query: String) {
                    val searchTerm  = query.trim()
                    if (searchTerm.isNotEmpty()) {
                        performSearch(searchTerm)
                    }
                }
            }
        )
        keyboardManager.showKeyboard()
        //keyboardManager.hideKeyboard()
        keyboardManager.isKeyboardVisible()

    }

    private fun performSearch(searchTerm:String) {
        CoroutineScope(Dispatchers.IO).launch {
            repeat(3) { attempt ->
                try {

                    Log.e("SEARCH RESULTS", searchTerm)

                    // --- Background work (network request) ---
                    val url =
                        "https://api.themoviedb.org/3/search/multi?include_adult=false&query=$searchTerm"
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


                    withContext(Dispatchers.Main)  {
                        val find = findViewById<TextView>(R.id.searchResultsDisplay)
                        find.text = "Search Results for: $searchTerm (${moviesArray.length()})"
                        searchAdapter.clearItems()
                    }

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

    private fun filter(){

        //filter options buttons
        val filterTypeBtn = findViewById<TextView>(R.id.FilterTypeBtn)
        val filterCountryBtn = findViewById<TextView>(R.id.FilterCountryBtn)
        val filterSortBtn = findViewById<TextView>(R.id.FilterSortBtn)
        val filterGenreBtn = findViewById<TextView>(R.id.FilterGenreBtn)
        val filterYearBtn = findViewById<TextView>(R.id.FilterYearBtn)


        // Filter data
        val typeOptions = listOf("Movie", "TV", "All")
        val countryOptions = listOf(
            FilterChoice("AR", "Argentina"),
            FilterChoice("AT", "Austria"),
            FilterChoice("BR", "Brazil"),
            FilterChoice("FI", "Finland"),
            FilterChoice("AR", "Argentina"),
            FilterChoice("AR", "Argentina")
        )

        val sortOptions = listOf(
            FilterChoice("original_title.asc", "Original Title ↑ Ascending"),
            FilterChoice("original_title.desc", "Original Title ↓ Descending"),
            FilterChoice("popularity.asc", "Popularity ↑ Ascending"),
            FilterChoice("popularity.desc", "Popularity ↓ Descending"),
            FilterChoice("revenue.asc", "Revenue ↑ Ascending"),
            FilterChoice("revenue.desc", "Revenue ↓ Descending")
        )

        val genreOptions = listOf(
            FilterChoice("28", "Action"),
            FilterChoice("12", "Adventure"),
            FilterChoice("16", "Animation"),
            FilterChoice("35", "Comedy"),
            FilterChoice("80", "Crime"),
            FilterChoice("99", "Documentary"),
            FilterChoice("18", "Drama"),
            FilterChoice("10751", "Family"),
            FilterChoice("9648", "Mystery"),
            FilterChoice("10749", "Romance"),
            FilterChoice("878", "Sci-Fi"),
            FilterChoice("10770", "TV Movie"),
            FilterChoice("53", "Thriller"),
            FilterChoice("37", "Western"),
            FilterChoice("10752", "War")
        )

        val yearOptions = listOf(
            FilterChoice("2025", "2025"),
            FilterChoice("2024", "2024"),
            FilterChoice("2023", "2023"),
            FilterChoice("2022", "2022"),
            FilterChoice("2021", "2021")
        )

        // Filter state
        var selectedType: String? = null
        val selectedGenres = mutableSetOf<String>()
        val selectedCountries = mutableSetOf<String>()
        val selectedsortOptions = mutableSetOf<String>()
        val selectedyearOptions   = mutableSetOf<String>()

        val filterDisplay = findViewById<TextView>(R.id.FilterDisplay)
        var movieUrl: String
        var tvUrl: String
        var filterPage = 1
        var isLoadingFliter = false

        fun updateFilterDisplay() {
            isLoadingFliter = true
            filterAdapter.isLoadingMore = true
            //filterAdapter.clearItems()

            // Reset attributes
            movieUrl = "https://api.themoviedb.org/3/discover/movie?"
            tvUrl = "https://api.themoviedb.org/3/discover/tv?"


            /*
            val parts = mutableListOf<String>()
            //selectedType?.let { parts.add("Type: ${it.label}") }
            if (selectedGenres.isNotEmpty()) parts.add("Genres: ${selectedGenres.joinToString()}")
            if (selectedCountries.isNotEmpty()) parts.add("Country: ${selectedCountries.joinToString()}")
            if (selectedsortOptions.isNotEmpty()) parts.add("Sort: ${selectedsortOptions.joinToString()}")
            if (selectedyearOptions.isNotEmpty()) parts.add("Year: ${selectedyearOptions.joinToString()}")

            filterDisplay.text = if (parts.isEmpty()) "Filter Results ($default)" else parts.joinToString(" | ")

             */

            if (selectedGenres.isNotEmpty()) {
                val genres = selectedGenres.joinToString(",")
                movieUrl += "&with_genres=$genres"
                tvUrl += "&with_genres=$genres"
            }

            if (selectedyearOptions.isNotEmpty()) {
                val year = selectedyearOptions.first()
                movieUrl += "&year=$year"
                tvUrl += "&year=$year"
            }

            if (selectedCountries.isNotEmpty()) {
                val country = selectedCountries.first()
                movieUrl += "&with_origin_country=$country"
                tvUrl += "&with_origin_country=$country"
            }

            if (selectedsortOptions.isNotEmpty()) {
                val sort = selectedsortOptions.first()
                movieUrl += "&sort_by=$sort"
                tvUrl += "&sort_by=$sort"
            }

            if(selectedType == "Movies"){
                tvUrl = ""
            } else if(selectedType == "Tv"){
                movieUrl = ""
            } else {}

            CoroutineScope(Dispatchers.IO).launch {

                val urlM = "$movieUrl&page=$filterPage"
                val urlT = "$tvUrl&page=$filterPage"

                val movieList = mutableListOf<JSONObject>()
                val tvList = mutableListOf<JSONObject>()

                Log.e("Filter Results urlM", urlM.toString())
                Log.e("Filter Results urlT", urlT.toString())

                // ------------------ MOVIES ------------------
                try {
                    val connection2 = URL(urlM).openConnection() as HttpURLConnection
                    connection2.requestMethod = "GET"
                    connection2.setRequestProperty("accept", "application/json")
                    connection2.setRequestProperty(
                        "Authorization",
                        "Bearer ${BuildConfig.TM_K}"
                    )

                    val response2 = connection2.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject2 = org.json.JSONObject(response2)
                    val mvData = jsonObject2.getJSONArray("results")

                    for (i in 0 until mvData.length()) {
                        movieList.add(mvData.getJSONObject(i))
                    }



                    Log.e("Filter Results mvData", mvData.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // ------------------ TV SHOWS ------------------
                try {
                    val connection2 = URL(urlT).openConnection() as HttpURLConnection
                    connection2.requestMethod = "GET"
                    connection2.setRequestProperty("accept", "application/json")
                    connection2.setRequestProperty(
                        "Authorization",
                        "Bearer ${BuildConfig.TM_K}"
                    )

                    val response2 = connection2.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject2 = org.json.JSONObject(response2)
                    val tvData = jsonObject2.getJSONArray("results")

                    for (i in 0 until tvData.length()) {
                        tvList.add(tvData.getJSONObject(i))
                    }

                    Log.e("Filter Results tvData", tvData.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Merge movie + tv
                val combined = (movieList + tvList).shuffled()
                for (i in 0 until combined.size) {
                    val current = combined[i]

                    val backdrop_path = current.optString("backdrop_path", "")
                    val poster_path = current.optString("poster_path", "")



                    val vote_average = current.optString("vote_average", "")
                    val title = current.optString("title", "")
                    //val overview = current.optString("overview", "")
                    val id = current.optString("id", "")
                    val original_title = current.optString("original_title", "")
                    //val original_name = current.optString("original_name", "")

                    val type: String
                    var date: String
                    var showD: String = ""

                    val imgPost =  "https://image.tmdb.org/t/p/w780$poster_path"
                    val imgback  = "https://image.tmdb.org/t/p/w1280$backdrop_path"

                    if (original_title == "") {
                        type = "tv";
                        date = if (current.getString("first_air_date").length >= 4) {
                            current.getString("first_air_date").substring(0, 4)
                        } else{
                            current.getString("first_air_date")}
                    } else {
                        type = "movie"
                        date = if (current.getString("release_date").length >= 4) {
                            current.getString("release_date").substring(0, 4)
                        } else{
                            current.getString("release_date")}
                    }

                    val Item = filterItemOne(title=title, backdropUrl = imgPost, posterUlr =  imgback , imdbCode=id, type=type, year=date, rating=vote_average, runtime=showD)

                    withContext(Dispatchers.Main) {
                        filterAdapter.addItem(Item)
                        isLoadingFliter = false
                        filterAdapter.isLoadingMore = false


                    }

                }

            }


        }
        fun loadMoreFilter() {
            if (isLoadingFliter) return // Prevent multiple rapid clicks
            filterPage++
            updateFilterDisplay()
        }

        filterAdapter.onAddMoreClicked = { loadMoreFilter() }
        updateFilterDisplay() //Default

        filterTypeBtn.setOnClickListener {
            showSingleChoiceDialog(
                title = "Select Type",
                options = typeOptions,
                currentSelection = typeOptions.indexOf(selectedType ?: "")
            ) { selected ->
                selectedType = selected   // <— this is just a STRING
                filterPage = 1
                filterAdapter.clearItems()
                updateFilterDisplay()
            }
        }

        filterGenreBtn.setOnClickListener {
            showMultiChoiceDialog(
                title = "Select Genres",
                options = genreOptions,
                selectedKeys = selectedGenres
            ) { selected ->
                filterPage = 1
                filterAdapter.clearItems()
                updateFilterDisplay()
            }
        }

        filterCountryBtn.setOnClickListener {
            showMultiChoiceDialog(
                title = "Select Genres",
                options = countryOptions,
                selectedKeys = selectedCountries
            ) { selected ->
                filterPage = 1
                filterAdapter.clearItems()
                updateFilterDisplay()
            }
        }

        filterSortBtn.setOnClickListener {
            showMultiChoiceDialog(
                title = "Select Genres",
                options = sortOptions,
                selectedKeys = selectedsortOptions
            ) { selected ->
                filterPage = 1
                filterAdapter.clearItems()
                updateFilterDisplay()
            }
        }

        filterYearBtn.setOnClickListener {
            showMultiChoiceDialog(
                title = "Select Genres",
                options = yearOptions,
                selectedKeys = selectedyearOptions
            ) { selected ->
                filterPage = 1
                filterAdapter.clearItems()
                updateFilterDisplay()
            }
        }

    }
    data  class FilterChoice(
        val value: String,
        val label: String
    )

    private fun showSingleChoiceDialog(
        title: String,
        options: List<String>,
        currentSelection: Int,
        onDone: (String) -> Unit
    ) {
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle(title)

        builder.setSingleChoiceItems(options.toTypedArray(), currentSelection) { dialog, which ->
            onDone(options[which])
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showMultiChoiceDialog(
        title: String,
        options: List<FilterChoice>,
        selectedKeys: MutableSet<String>,
        onDone: (Set<String>) -> Unit
    ) {
        val labels = options.map { it.label }.toTypedArray()
        val checkedItems = options.map { selectedKeys.contains(it.value) }.toBooleanArray()

        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle(title)

        builder.setMultiChoiceItems(labels, checkedItems) { _, index, isChecked ->
            // Ensure you reference the options list inside the lambda
            val selectedKey = options[index].value
            if (isChecked) selectedKeys.add(selectedKey)
            else selectedKeys.remove(selectedKey)
        }

        builder.setPositiveButton("Apply") { _, _ ->
            onDone(selectedKeys)
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }



}
