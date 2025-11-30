package com.example.onyx

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import android.widget.LinearLayout
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.String
import com.example.onyx.BuildConfig


class Anime_Page : AppCompatActivity() {
    //private var urlHome = "http://192.168.100.22:4000"
    private var urlHome = BuildConfig.A_K
    private var isSearchContainerAnimeVisible = false
    private var currentAnimePage = 0
    private var isLoadingMoreDubbed = false

    private var currentRecentlyAnimePage = 0
    private var isLoadingMoreRecently = false



    private lateinit var dubbedAdapter: AnimeGridAdapter
    private lateinit var dubbedRecyclerView : RecyclerView
    private lateinit var RecentlyAdapter: AnimeGridAdapter
    private lateinit var RecentlyRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalUtils.applyTheme(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anime_page)
        NavAction.setupSidebar(this@Anime_Page)
        LoadingAnimation.setup(this@Anime_Page, R.raw.b)
        LoadingAnimation.show(this@Anime_Page)



        animeHomeData()
        setupSearchUi()
        setupBackPressedCallback()

        val tvSpacing = (8 * resources.displayMetrics.density).toInt()
        //------------------------------------------------------------------------------------------

        dubbedRecyclerView = findViewById(R.id.Anime_Dubbed_widget)
        dubbedRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false).apply {
            isSmoothScrollbarEnabled = false
            isItemPrefetchEnabled = false
        }
        dubbedRecyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))
        dubbedAdapter = AnimeGridAdapter(mutableListOf(), R.layout.anime_airing_item)
        dubbedRecyclerView.adapter = dubbedAdapter
        dubbedAdapter.onAddMoreClicked = { loadDubbedAnime() }

        //------------------------------------------------------------------------------------------


        RecentlyRecyclerView = findViewById(R.id.Anime_RecentlyUpdated_widget)
        RecentlyRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false).apply {
            isSmoothScrollbarEnabled = false
            isItemPrefetchEnabled = false
        }
        RecentlyRecyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))
        RecentlyAdapter = AnimeGridAdapter(mutableListOf(), R.layout.anime_airing_item)
        RecentlyRecyclerView.adapter = RecentlyAdapter
        RecentlyAdapter.onAddMoreClicked = { loadDubbedAnime() }
        //------------------------------------------------------------------------------------------

        loadDubbedAnime()
        loadRecentlyAnime()
    }


    private fun animeHomeData() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels     // in pixels
        val screenHeight = displayMetrics.heightPixels    // in pixels

        val recyclerView = findViewById<RecyclerView>(R.id.spotlightAnimes)
        val params = recyclerView.layoutParams
        params.height = (screenHeight * 0.75).toInt()
        recyclerView.layoutParams = params

        CoroutineScope(Dispatchers.IO).launch {
            repeat(1) { attempt ->
                try {

                    val url = "$urlHome/api/v2/hianime/home"
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")

                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.e("ANIME_STATUS HOME 1", response.toString())

                    val jsonObject = org.json.JSONObject(response)

                    Log.e("ANIME_STATUS HOME 2", jsonObject.toString())

                    val ShowHomeData = jsonObject.getJSONObject("data")
                    Log.e("ANIME_STATUS HOME 3", ShowHomeData.toString())


                    val  spotlightAnimes = ShowHomeData.getJSONArray("spotlightAnimes")
                    val  trendingAnimes = ShowHomeData.getJSONArray("trendingAnimes")
                    val  latestEpisodeAnimes = ShowHomeData.getJSONArray("spotlightAnimes")
                    val  top10Animes = ShowHomeData.getJSONArray("spotlightAnimes")
                    val  topAiringAnimes = ShowHomeData.getJSONArray("topAiringAnimes")
                    val  latestCompletedAnimes = ShowHomeData.getJSONArray("spotlightAnimes")



                    var spotlightAnimesitmes = mutableListOf<AnimeSliderItem>()

                    for (i in 0 until spotlightAnimes.length()) {

                        val item = spotlightAnimes.getJSONObject(i)

                        val title = item.getString("name")

                        val overview = item.getString("description")


                        val imageUrl = item.getString("poster")

                        val id = item.getString("id")
                        val type = item.getString("type")

                        val runtime = item.optJSONArray("otherInfo").optString(1, "")
                        val release_date = item.optJSONArray("otherInfo").optString(2, "")
                        val quality = item.optJSONArray("otherInfo").optString(3, "")
                        val sub = item.getJSONObject("episodes").optString("sub", "")
                        val dub = item.getJSONObject("episodes").optString("dub", "")





                        spotlightAnimesitmes.add(
                            AnimeSliderItem(
                                title,
                                imageUrl,
                                id,
                                type,
                                overview,
                                release_date,
                                runtime,
                                quality,
                                sub,
                                dub,
                            )
                        )

                    }

                    Log.e("DEBUG_MAIN_Slider 1", spotlightAnimesitmes.toString())



                    withContext(Dispatchers.Main) {

                        showTrending( trendingAnimes)
                        showAiring(topAiringAnimes)

                        LoadingAnimation.hide(this@Anime_Page)
                        val recyclerView = findViewById<RecyclerView>(R.id.spotlightAnimes)
                        recyclerView.layoutManager = LinearLayoutManager(
                            this@Anime_Page,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        recyclerView.adapter = AnimeSwiper(spotlightAnimesitmes, R.layout.anime_card_spotlight)
                        LoadingAnimation.hide(this@Anime_Page)
                    }

                    return@launch
                } catch (e: Exception) {
                    delay(20_000)
                    Log.e("ANIME_STATUS HOME 1", "Error fetching data", e)
                    return@launch
                }



            }
        }
    }


    private fun showTrending( trending: JSONArray){

        var trendingItems = mutableListOf<TrendingAnimeItem>()

        for (i in 0 until trending.length()) {


            val item = trending.getJSONObject(i)

            val title = item.getString("name")

            val imageUrl = item.getString("poster")

            val id = item.getString("id")

            val ranking = "0"+item.getString("rank")



            trendingItems.add(
                TrendingAnimeItem(
                    id,
                    title,
                    imageUrl,
                    ranking
                )
            )

        }

             Log.e("DEBUG_MAIN_Slider 1", trendingItems.toString())


            LoadingAnimation.hide(this@Anime_Page)
            val recyclerView = findViewById<RecyclerView>(R.id.Anime_Trending_widget)
            recyclerView.layoutManager = LinearLayoutManager(
                this@Anime_Page,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerView.adapter = AnimeTrendingAdapter(trendingItems, R.layout.anime_trending_item)
            val spacing = (9 * resources.displayMetrics.density).toInt() // 16dp to px
            recyclerView.addItemDecoration(EqualSpaceItemDecoration(spacing))

    }

    private fun showAiring( Airing: JSONArray){

        var airingItems = mutableListOf<AiringAnimeItem>()

        for (i in 0 until Airing.length()) {


            val item = Airing.getJSONObject(i)

            val title = item.getString("name")

            val imageUrl = item.getString("poster")

            val id = item.getString("id")

            val type = item.getString("type")

            val sub = item.getJSONObject("episodes").optString("sub", "")
            val dub = item.getJSONObject("episodes").optString("dub", "")





            airingItems.add(
                AiringAnimeItem(
                    id,
                    title,
                    imageUrl,
                    type,
                    sub,
                    dub
                )
            )

        }

        Log.e("DEBUG_MAIN_Slider 1", airingItems.toString())


        LoadingAnimation.hide(this@Anime_Page)
        val recyclerView = findViewById<RecyclerView>(R.id.Anime_Airing_widget)
        recyclerView.layoutManager = LinearLayoutManager(
            this@Anime_Page,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerView.adapter = AnimeAiringAdapter(airingItems, R.layout.anime_airing_item)
        val spacing = (9 * resources.displayMetrics.density).toInt() // 16dp to px
        recyclerView.addItemDecoration(EqualSpaceItemDecoration(spacing))

    }


    private fun loadDubbedAnime() {
        if (isLoadingMoreDubbed) return // Prevent multiple rapid clicks
        currentAnimePage++
        fetchDubbedAnime()
    }

    private fun fetchDubbedAnime() {
        isLoadingMoreDubbed = true
        CoroutineScope(Dispatchers.IO).launch {

            repeat(5) { attempt ->
                try {
                    val url = "$urlHome/api/v2/hianime/category/dubbed-anime?page=$currentAnimePage"
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)
                    val fData = jsonObject.getJSONObject("data")

                    Log.e("DEBUG_DubbedAnime1", "${fData}")

                    val  dubbedAnime = fData.getJSONArray("animes")
                    Log.e("DEBUG_DubbedAnime2", "${fData}")

                    val airingItems = mutableListOf<AiringAnimeItem>()

                    for (i in 0 until dubbedAnime.length()) {

                        val item = dubbedAnime.getJSONObject(i)

                        val title = item.getString("name")

                        val imageUrl = item.getString("poster")

                        val id = item.getString("id")

                        val type = item.getString("type")

                        val sub = item.getJSONObject("episodes").optString("sub", "")
                        val dub = item.getJSONObject("episodes").optString("dub", "")

                        airingItems.add(
                            AiringAnimeItem(
                                id,
                                title,
                                imageUrl,
                                type,
                                sub,
                                dub
                            )
                        )


                        val movieItem = AiringAnimeItem(
                            id,
                            title,
                            imageUrl,
                            type,
                            sub,
                            dub
                        )

                        withContext(Dispatchers.Main) {
                            dubbedAdapter.addItem(movieItem)
                            isLoadingMoreDubbed = false
                        }
                    }

                    return@launch
                } catch (e: Exception) {
                    Log.e("DEBUG_TAG_TvShows", "Attempt ${attempt+1} failed", e)
                    delay(10_000)
                    currentAnimePage--
                }
                withContext(Dispatchers.Main) {
                    isLoadingMoreDubbed = false
                }
            }
        }
    }

    private fun loadRecentlyAnime() {
        if (isLoadingMoreRecently) return // Prevent multiple rapid clicks
        currentRecentlyAnimePage++
        fetchRecentlyAnime()
    }

    private fun fetchRecentlyAnime() {
        isLoadingMoreDubbed = true
        CoroutineScope(Dispatchers.IO).launch {

            repeat(5) { attempt ->
                try {
                    val url = "$urlHome/api/v2/hianime/category/recently-updated?page=$currentRecentlyAnimePage"
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)
                    val fData = jsonObject.getJSONObject("data")

                    Log.e("DEBUG_DubbedAnime1", "${fData}")

                    val  dubbedAnime = fData.getJSONArray("animes")
                    Log.e("DEBUG_DubbedAnime2", "${fData}")

                    val airingItems = mutableListOf<AiringAnimeItem>()

                    for (i in 0 until dubbedAnime.length()) {

                        val item = dubbedAnime.getJSONObject(i)

                        val title = item.getString("name")

                        val imageUrl = item.getString("poster")

                        val id = item.getString("id")

                        val type = item.getString("type")

                        val sub = item.getJSONObject("episodes").optString("sub", "")
                        val dub = item.getJSONObject("episodes").optString("dub", "")

                        airingItems.add(
                            AiringAnimeItem(
                                id,
                                title,
                                imageUrl,
                                type,
                                sub,
                                dub
                            )
                        )


                        val movieItem = AiringAnimeItem(
                            id,
                            title,
                            imageUrl,
                            type,
                            sub,
                            dub
                        )

                        withContext(Dispatchers.Main) {
                            RecentlyAdapter.addItem(movieItem)
                            isLoadingMoreRecently = false
                        }
                    }

                    return@launch
                } catch (e: Exception) {
                    Log.e("DEBUG_TAG_TvShows", "Attempt ${attempt+1} failed", e)
                    delay(10_000)
                    currentRecentlyAnimePage--
                }
                withContext(Dispatchers.Main) {
                    isLoadingMoreRecently = false
                }
            }
        }
    }



    private fun searchAnimeFetch(searchTerm:String){

        CoroutineScope(Dispatchers.IO).launch {
            repeat(1) { attempt ->
                try {

                    val url = "$urlHome/api/v2/hianime/search?q=$searchTerm&page=1"
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")

                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.e("ANIME_STATUS search", response.toString())

                    val jsonObject = org.json.JSONObject(response)

                    Log.e("ANIME_STATUS search", jsonObject.toString())

                    val dataFetch = jsonObject.getJSONObject("data")



                    val  searchData = dataFetch.getJSONArray("animes")
                    Log.e("ANIME_STATUS SEARCH-R", dataFetch.toString())




                    var searchDataItmes = mutableListOf<AnimeSearchItem>()

                    for (i in 0 until searchData.length()) {

                        val item = searchData.getJSONObject(i)

                        val title = item.getString("name")
                        val imageUrl = item.getString("poster")
                        val id = item.getString("id")
                        val type = item.getString("type")
                        val sub = item.getJSONObject("episodes").optString("sub", "")
                        val dub = item.getJSONObject("episodes").optString("dub", "")


                        searchDataItmes.add(
                            AnimeSearchItem(
                                id,
                                title,
                                imageUrl,
                                type,
                                sub,
                                dub,
                            )
                        )

                    }



                    withContext(Dispatchers.Main) {

                        LoadingAnimation.hide(this@Anime_Page)
                        val recyclerView = findViewById<RecyclerView>(R.id.AnimeSearch_widget)

                        // Calculate span count dynamically
                        val widthInPixels = this@Anime_Page.resources.getDimension(R.dimen.grid_item_width)
                        val density = this@Anime_Page.resources.displayMetrics.density
                        val widthInDp = widthInPixels / density
                        val displayMetrics = resources.displayMetrics
                        val screenWidthPx = displayMetrics.widthPixels
                        val itemMinWidthPx = ((widthInDp + 19) * displayMetrics.density).toInt() // 160dp per item
                        val spanCount = maxOf(1, screenWidthPx / itemMinWidthPx)

                        recyclerView.layoutManager = GridLayoutManager(this@Anime_Page, spanCount)
                        recyclerView.adapter = AnimeSearchAdapter(searchDataItmes, R.layout.anime_airing_item)
                    }



                    return@launch
                } catch (e: Exception) {
                    delay(20_000)
                    Log.e("ANIME_STATUS S-Error", "Error fetching data", e)
                    return@launch
                }

            }
        }

    }

    private fun setupSearchUi() {

        val searchInput = findViewById<EditText>(R.id.AnimeSearchInput)
        val searchBar = findViewById<LinearLayout>(R.id.searchBarAnime)

        val focusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                expandSearchBar(searchBar)
                searchBar.post {
                    searchInput.requestFocus()
                    showKeyboard(searchInput)
                }
            } else {
                collapseSearchBar(searchBar)
                hideKeyboard()
            }
        }

        // Key listener to detect microphone button press (VOICE_ASSIST)
        searchInput.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_VOICE_ASSIST -> {
                        // Handle microphone button press here
                        searchInput.requestFocus()
                        showKeyboard(searchInput)
                        true
                    }
                }
            }
            false
        }

        // Set focus listeners on both the search bar and the input field
        searchBar.onFocusChangeListener = focusChangeListener
        searchInput.onFocusChangeListener = focusChangeListener

        try{
            searchInput.setOnEditorActionListener { _, actionId, event ->

                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {

                    val searchTerm = searchInput.text.toString().trim()
                    if (searchTerm.isNotEmpty()) {
                        findViewById<LinearLayout>(R.id.searchContainerAnime).visibility = View.VISIBLE
                        findViewById<LinearLayout>(R.id.AnimeHomeContainer).visibility = View.GONE
                        findViewById<View>(R.id.AnimeSearchInput).nextFocusDownId = R.id.searchContainerAnime
                        isSearchContainerAnimeVisible = true
                        searchAnimeFetch(searchTerm)
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

    private fun showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun expandSearchBar(searchBar: LinearLayout) {
        val params = searchBar.layoutParams as ViewGroup.MarginLayoutParams
        params.width = 400.dpToPx(this)
        params.marginEnd = 0
        searchBar.layoutParams = params

        // Optional: Add some visual feedback
        searchBar.elevation = 8f
    }

    private fun collapseSearchBar(searchBar: LinearLayout) {
        val params = searchBar.layoutParams as ViewGroup.MarginLayoutParams
        params.width =30.dpToPx(this) // Convert dp to pixels
        searchBar.layoutParams = params

        // Reset visual changes
        searchBar.elevation = 0f
    }
    // Extension function to convert dp to pixels
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }







        private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (isSearchContainerAnimeVisible) {
                    findViewById<LinearLayout>(R.id.searchContainerAnime).visibility = View.GONE
                    findViewById<LinearLayout>(R.id.AnimeHomeContainer).visibility = View.VISIBLE
                    isSearchContainerAnimeVisible = false
                    findViewById<View>(R.id.AnimeSearchInput).nextFocusDownId = R.id.spotlightAnimesCard

                }else {
                    findViewById<ImageButton>(R.id.btnAnime).requestFocus()
                }
            }
        })
    }


}