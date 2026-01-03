package com.example.onyx

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
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
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.String
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.example.onyx.BuildConfig
import com.example.onyx.Database.AppDatabase
import com.example.onyx.Database.SessionManger
import com.example.onyx.FetchData.TMDBapi
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.example.onyx.FetchData.AnimeApi


class Anime_Page : AppCompatActivity() {

     private lateinit var db: AppDatabase
     private lateinit var  sm: SessionManger

    private lateinit var  fetchAnimeAPI: AnimeApi
     private var userId: Int = -1
     private var urlHome = BuildConfig.A_K      //private var urlHome = "http://192.168.100.22:4000"




    //----------------------------------------------------------------------------------------------


     private lateinit var dubbedAdapter: AnimeGridAdapter
     private lateinit var dubbedRecyclerView: RecyclerView
     private var currentDubbedAnimePage = 0
     private var isLoadingMoreDubbed = false
     //----------------------------------------------------------------------------------------------


     private lateinit var popularAdapter: AnimeGridAdapter
     private lateinit var popularRecyclerView: RecyclerView
     private var currentPopularAnimePage = 0
     private var isLoadingMorePopular = false

     //----------------------------------------------------------------------------------------------

     private lateinit var RecentlyAdapter: AnimeGridAdapter
     private lateinit var RecentlyRecyclerView: RecyclerView
     private var currentRecentlyAnimePage = 0
     private var isLoadingMoreRecently = false

     //---------------------------------------------------------------------------------------------
     private lateinit var searchAdapter: AnimeSearchAdapter
     private lateinit var searchRecyclerView: RecyclerView

     //----------------------------------------------------------------------------------------------

     private lateinit var watchRecyclerView: RecyclerView
     private lateinit var watchAdapter: cWatchingAdapter


    private lateinit var faveRecyclerView: RecyclerView
    private lateinit var faveAdapter: FavAdapter

   //-----------------------------------------------------------------------------------------------

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var  notificationRecyclerView: RecyclerView

   //---------------------------------------------------------------------------------------------

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         GlobalUtils.applyTheme(this)
         enableEdgeToEdge()
         setContentView(R.layout.activity_anime_page)


         NavAction.setupSidebar(this@Anime_Page)

         db = AppDatabase(this)         // Initialize database
         sm = SessionManger(this)
         fetchAnimeAPI = AnimeApi(this)

         userId = sm.getUserId()



         ////////////////////////////////////////////////////////////////////////////////////////

        val navBar = findViewById<CardView>(R.id.animeNavBar)
         val homeAnimeBtn = findViewById<LinearLayout>(R.id.HomeAnimeBtn)
         val favAnimeBtn = findViewById<LinearLayout>(R.id.FavAnimeBtn)
         val searchAnimeBtn = findViewById<LinearLayout>(R.id.SearchAnimeBtn)
         val popularAnimeBtn = findViewById<LinearLayout>(R.id.PopularAnimeBtn)
         val dubbedAnimeBtn = findViewById<LinearLayout>(R.id.DubbedAnimeBtn)
         val cWatchAnimeBtn = findViewById<LinearLayout>(R.id.cWatchAnimeBtn)
         val cNotificationAnimeBtn = findViewById<LinearLayout>(R.id.cNotificationAnimeBtn)

         val homePageAnime = findViewById<LinearLayout>(R.id.HomePageAnime)
         val dubbedPageAnime = findViewById<LinearLayout>(R.id.DubbedPageAnime)
         val popularPageAnime = findViewById<LinearLayout>(R.id.PopularPageAnime)
         val searchPageAnime = findViewById<LinearLayout>(R.id.SearchPageAnime)
         val favPageAnime = findViewById<LinearLayout>(R.id.FavPageAnime)
         val cWatchedPageAnime = findViewById<LinearLayout>(R.id.WatchedListPageAnime)
         val notificationPageAnime = findViewById<LinearLayout>(R.id.notificationPageAnime)





         homeAnimeBtn.setOnClickListener {
             homePageAnime.visibility = View.VISIBLE
             dubbedPageAnime.visibility = View.GONE
             popularPageAnime.visibility = View.GONE
             searchPageAnime.visibility = View.GONE
             cWatchedPageAnime.visibility = View.GONE
             favPageAnime.visibility = View.GONE
             notificationPageAnime.visibility = View.GONE


             homeAnimeBtn.isSelected = true
             dubbedAnimeBtn.isSelected = false
             popularAnimeBtn.isSelected = false
             searchAnimeBtn.isSelected = false
             favAnimeBtn.isSelected = false
             cWatchAnimeBtn.isSelected = false
             cNotificationAnimeBtn.isSelected = false
         }

         homeAnimeBtn.performClick()

         favAnimeBtn.setOnClickListener {
             homePageAnime.visibility = View.GONE
             dubbedPageAnime.visibility = View.GONE
             popularPageAnime.visibility = View.GONE
             searchPageAnime.visibility = View.GONE
             cWatchedPageAnime.visibility = View.GONE
             favPageAnime.visibility = View.VISIBLE
             notificationPageAnime.visibility = View.GONE


             homeAnimeBtn.isSelected = false
             dubbedAnimeBtn.isSelected = false
             popularAnimeBtn.isSelected = false
             searchAnimeBtn.isSelected = false
             favAnimeBtn.isSelected = true
             cWatchAnimeBtn.isSelected = false
             cNotificationAnimeBtn.isSelected = false
         }

         searchAnimeBtn.setOnClickListener {
             homePageAnime.visibility = View.GONE
             dubbedPageAnime.visibility = View.GONE
             popularPageAnime.visibility = View.GONE
             searchPageAnime.visibility = View.VISIBLE
             cWatchedPageAnime.visibility = View.GONE
             favPageAnime.visibility = View.GONE
             notificationPageAnime.visibility = View.GONE


             homeAnimeBtn.isSelected = false
             dubbedAnimeBtn.isSelected = false
             popularAnimeBtn.isSelected = false
             searchAnimeBtn.isSelected = true
             favAnimeBtn.isSelected = false
             cWatchAnimeBtn.isSelected = false

         }

         cWatchAnimeBtn.setOnClickListener {
             homePageAnime.visibility = View.GONE
             dubbedPageAnime.visibility = View.GONE
             popularPageAnime.visibility = View.GONE
             searchPageAnime.visibility = View.GONE
             cWatchedPageAnime.visibility = View.VISIBLE
             favPageAnime.visibility = View.GONE
             notificationPageAnime.visibility = View.GONE


             homeAnimeBtn.isSelected = false
             dubbedAnimeBtn.isSelected = false
             popularAnimeBtn.isSelected = false
             searchAnimeBtn.isSelected = false
             favAnimeBtn.isSelected = false
             cWatchAnimeBtn.isSelected = true
             cNotificationAnimeBtn.isSelected = false
         }

         popularAnimeBtn.setOnClickListener {
             homePageAnime.visibility = View.GONE
             dubbedPageAnime.visibility = View.GONE
             popularPageAnime.visibility = View.VISIBLE
             searchPageAnime.visibility = View.GONE
             cWatchedPageAnime.visibility = View.GONE
             favPageAnime.visibility = View.GONE
             notificationPageAnime.visibility = View.GONE


             homeAnimeBtn.isSelected = false
             dubbedAnimeBtn.isSelected = false
             popularAnimeBtn.isSelected = true
             searchAnimeBtn.isSelected = false
             favAnimeBtn.isSelected = false
             cWatchAnimeBtn.isSelected = false
             cNotificationAnimeBtn.isSelected = false
         }

         dubbedAnimeBtn.setOnClickListener {
             homePageAnime.visibility = View.GONE
             dubbedPageAnime.visibility = View.VISIBLE
             popularPageAnime.visibility = View.GONE
             searchPageAnime.visibility = View.GONE
             cWatchedPageAnime.visibility = View.GONE
             favPageAnime.visibility = View.GONE
             notificationPageAnime.visibility = View.GONE


             homeAnimeBtn.isSelected = false
             dubbedAnimeBtn.isSelected = true
             popularAnimeBtn.isSelected = false
             searchAnimeBtn.isSelected = false
             favAnimeBtn.isSelected = false
             cWatchAnimeBtn.isSelected = false
             cNotificationAnimeBtn.isSelected = false

         }

         cNotificationAnimeBtn.setOnClickListener {
             homePageAnime.visibility = View.GONE
             dubbedPageAnime.visibility = View.GONE
             popularPageAnime.visibility = View.GONE
             searchPageAnime.visibility = View.GONE
             cWatchedPageAnime.visibility = View.GONE
             favPageAnime.visibility = View.GONE
             notificationPageAnime.visibility = View.VISIBLE


             homeAnimeBtn.isSelected = false
             dubbedAnimeBtn.isSelected = false
             popularAnimeBtn.isSelected = false
             searchAnimeBtn.isSelected = false
             favAnimeBtn.isSelected = false
             cWatchAnimeBtn.isSelected = false
             cNotificationAnimeBtn.isSelected = true

             findViewById<CardView>(R.id.cNotificationAnimeIcon).visibility = View.GONE
         }

         GlobalUtils.expandParentOnChildFocus(
             parent = navBar,
             expandedWidthDp = 140f,
             collapsedWidthDp = 50f
         )



         ////////////////////////////////////////////////////////////////////////////////////////
         ////////////////////////////////////////////////////////////////////////////////////////


         val tvSpacing = (10 * resources.displayMetrics.density).toInt()
         //------------------------------------------------------------------------------------------
         val anime_airing_item_width = 150
         dubbedRecyclerView = findViewById(R.id.dubbedRecycler)
         dubbedRecyclerView.layoutManager = GridLayoutManager(
             this@Anime_Page,
             GlobalUtils.calculateSpanCountV2(this@Anime_Page, 160, anime_airing_item_width)
         )
         dubbedRecyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))
         dubbedAdapter = AnimeGridAdapter(mutableListOf(), R.layout.anime_airing_item)
         dubbedRecyclerView.adapter = dubbedAdapter
         dubbedAdapter.onAddMoreClicked = { loadDubbedAnime() }


         popularRecyclerView = findViewById(R.id.popularRecycler)
         popularRecyclerView.layoutManager = GridLayoutManager(
             this@Anime_Page,
             GlobalUtils.calculateSpanCountV2(this@Anime_Page, 160, anime_airing_item_width)
         )
         popularRecyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))
         popularAdapter = AnimeGridAdapter(mutableListOf(), R.layout.anime_airing_item)
         popularRecyclerView.adapter = popularAdapter
         popularAdapter.onAddMoreClicked = { loadPopularAnime() }



         //-----------------------------------------------------------------------------------------

         searchRecyclerView = findViewById(R.id.SearchRecycler)
         searchRecyclerView.layoutManager = GridLayoutManager(this@Anime_Page,GlobalUtils.calculateSpanCountV2(this@Anime_Page, 160, anime_airing_item_width))
         searchAdapter  = AnimeSearchAdapter(mutableListOf(), R.layout.anime_airing_item)
         searchRecyclerView.adapter = searchAdapter
         searchRecyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))

         //------------------------------------------------------------------------------------------


         watchRecyclerView = findViewById(R.id.watchingRecycler)
         watchRecyclerView.layoutManager = GridLayoutManager(this@Anime_Page, GlobalUtils.calculateSpanCountV2(this@Anime_Page,160,150))
         watchRecyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))


         //------------------------------------------------------------------------------------------

         faveRecyclerView = findViewById(R.id.faveRecycler)
         faveRecyclerView.layoutManager = LinearLayoutManager(
             this,
             LinearLayoutManager.HORIZONTAL,
             false
         )
         faveRecyclerView.addItemDecoration(EqualSpaceItemDecoration(tvSpacing))

         //------------------------------------------------------------------------------------------

         notificationRecyclerView = findViewById<RecyclerView>(R.id.notificationRecycler)
         notificationRecyclerView.layoutManager = LinearLayoutManager(this)
         val clearBtn = findViewById<TextView>(R.id.clearNotBtn)

         clearBtn.setOnClickListener {
             db.clearAllAnimeNotifications(userId)
             notificationAdapter.clearItems()
         }


         ////////////////////////////////////////////////////////////////////////////////////////////
         ////////////////////////////////////////////////////////////////////////////////////////////



         CoroutineScope(Dispatchers.Main).launch {
            LoadingAnimation.show(this@Anime_Page)
             animeHomeData()
             //loadDubbedAnime()
             //loadPopularAnime()
             //loadRecentlyAnime()
             //setupSearchUi()
             //animeWatchedList()
             //notificationS()
         }

     }


    override fun onResume() {
        super.onResume()
        if (this::watchAdapter.isInitialized) {
            watchAdapter.clearItems()
        }

        if (this::faveAdapter.isInitialized) {
            faveAdapter.clearItems()
        }

        if(this::notificationAdapter.isInitialized){
            notificationAdapter.clearItems()
        }




        CoroutineScope(Dispatchers.Main).launch {
            animeWatchedList()
            animeFavoritesList()
            notificationS()
        }
    }


     private fun animeHomeData() {
         val displayMetrics = resources.displayMetrics
         val screenWidth = displayMetrics.widthPixels     // in pixels
         val screenHeight = displayMetrics.heightPixels    // in pixels

         val inflater = LayoutInflater.from(this)

         val SpotlightContaner = findViewById<FrameLayout>(R.id.spotlightAnimes)

         val params = SpotlightContaner.layoutParams
         params.height = (screenHeight * 0.75).toInt()
         SpotlightContaner.layoutParams = params

         LoadingAnimation.setup(this@Anime_Page, R.raw.b)


         val jsonObject = fetchAnimeAPI.animeHome()

         if (jsonObject == null) return

         Log.e("ANIME_STATUS HOME 2", jsonObject.toString())

         val ShowHomeData = jsonObject.getJSONObject("data")
         Log.e("ANIME_STATUS HOME 3", ShowHomeData.toString())


         val spotlightAnimes = ShowHomeData.getJSONArray("spotlightAnimes")
         val trendingAnimes = ShowHomeData.getJSONArray("trendingAnimes")
         val latestEpisodeAnimes = ShowHomeData.getJSONArray("spotlightAnimes")
         val top10Animes = ShowHomeData.getJSONArray("spotlightAnimes")
         val topAiringAnimes = ShowHomeData.getJSONArray("topAiringAnimes")
         val latestCompletedAnimes = ShowHomeData.getJSONArray("spotlightAnimes")



         for (i in 0 until spotlightAnimes.length()) {

             val card = inflater.inflate(
                 R.layout.anime_card_spotlight,
                 SpotlightContaner,
                 false
             ) as CardView


             val item = spotlightAnimes.getJSONObject(i)
             val title = item.getString("name")
             val overview = item.getString("description")
             val imageUrl = item.getString("poster")
             val id = item.getString("id")
             val type = item.getString("type")
             val runtime = item.optJSONArray("otherInfo").optString(1, "")
             val release_date = item.optJSONArray("otherInfo").optString(2, "")
             val quality = item.optJSONArray("otherInfo").optString(3, "")
             val sub = item.getJSONObject("episodes").optInt("sub", 0)
             val dub = item.getJSONObject("episodes").optInt("dub", 0)

             card.findViewById<TextView>(R.id.cardTitle).text = title
             card.findViewById<TextView>(R.id.cardPg).text = "PG-13"
             card.findViewById<TextView>(R.id.cardType).text = type
             card.findViewById<TextView>(R.id.cardRuntime).text = runtime
             card.findViewById<TextView>(R.id.cardYear).text = release_date
             card.findViewById<TextView>(R.id.cardQuality).text = quality
             card.findViewById<TextView>(R.id.cardSub).text = sub.toString()
             card.findViewById<TextView>(R.id.cardDub).text = dub.toString()
             card.findViewById<TextView>(R.id.cardOverview).text = overview

             val SliderBackdrop = card.findViewById<ImageView>(R.id.SliderBackdrop)

             Glide.with(card.context)
                 .load(imageUrl)
                 .centerInside()
                 .into(SliderBackdrop)


             card.setOnClickListener {
                 val context = card.context
                 val intent = android.content.Intent(context, Watch_Anime_Page::class.java)
                 intent.putExtra("anime_code", id)
                 intent.putExtra("anime_poster", imageUrl)
                 context.startActivity(intent)
             }

             SpotlightContaner.addView(card)

         }


         showTrending(trendingAnimes)
         showAiring(topAiringAnimes)

         GlobalUtils.setupCardStackFromContainer(SpotlightContaner, 7000L)


         LoadingAnimation.hide(this@Anime_Page)


     }



    private fun setupCardStackFromContainer(
        container: FrameLayout,
        autoSwipeDelay: Long = 2500L
    ) {

        // Ensure container has CardView children
        val cards = (0 until container.childCount)
            .mapNotNull { container.getChildAt(it) as? CardView }

        if (cards.isEmpty()) return

        // ---------------- Auto Swipe ----------------
        val autoSwipeHandler = Handler(Looper.getMainLooper())
        var autoSwipeRunnable: Runnable? = null
        var autoSwipeRunning = false

        fun stopAutoSwipe() {
            autoSwipeRunning = false
            autoSwipeRunnable?.let { autoSwipeHandler.removeCallbacks(it) }
            autoSwipeRunnable = null
        }

        fun startAutoSwipe() {
            if (autoSwipeRunning) return
            autoSwipeRunning = true

            autoSwipeRunnable = object : Runnable {
                override fun run() {
                    if (!container.hasFocus()) {
                        swapRight(container, keepFocus = false)
                        autoSwipeHandler.postDelayed(this, autoSwipeDelay)
                    } else stopAutoSwipe()
                }
            }

            autoSwipeHandler.postDelayed(autoSwipeRunnable!!, autoSwipeDelay)
        }

        // ---------------- Setup Card Listeners ----------------
        cards.forEach { card ->

            card.isFocusable = true
            card.isFocusableInTouchMode = true

            card.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    stopAutoSwipe()
                    v.bringToFront()
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .translationX(dp(0f))
                        .setDuration(200)
                        .start()
                    v.elevation = 7f
                } else {
                    layoutStack(container)
                    container.postDelayed({
                        if (!container.hasFocus()) startAutoSwipe()
                    }, 300)
                }
            }

            card.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> { swapLeft(container); true }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> { swapRight(container); true }
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> false
                    else -> false
                }
            }
        }


        // ---------------- Initial Layout & Focus ----------------
        container.getChildAt(container.childCount - 1)?.requestFocus()
        layoutStack(container)
        container.postDelayed({ if (!container.hasFocus()) startAutoSwipe() }, 2000)
    }


    private fun layoutStack(container: FrameLayout) {

        val count = container.childCount

        for (i in 0 until count) {

            val card = container.getChildAt(i)
            val posFromTop = count - 1 - i

            val (tx, scale, elevation) = when (posFromTop) {
                0 -> Triple(0f, 1.0f, 6f)
                1 -> Triple(50f, 0.95f, 5f)
                2 -> Triple(90f, 0.9f, 4f)
                3 -> Triple(120f, 0.85f, 3f)
                4 -> Triple(140f, 0.8f, 2f)
                else -> Triple(150f, 0.7f, 1f)
            }

            card.animate()
                .translationX(dp(tx))
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(300)
                .start()

            card.elevation = elevation
        }
    }

    private fun swapRight(container: FrameLayout, keepFocus: Boolean = true) {
        if (container.childCount == 0) return
        val top = container.getChildAt(container.childCount - 1)

        top.animate()
            .translationXBy(dp(-250f))
            .scaleX(0.85f)
            .scaleY(0.85f)
            .rotation(-5f)
            .setDuration(300)
            .withEndAction {
                top.rotation = 0f
                container.removeView(top)
                container.addView(top, 0)
                layoutStack(container)

                if (keepFocus) {
                    container.getChildAt(container.childCount - 1)?.requestFocus()
                }
            }
            .start()
    }

    private fun swapLeft(container: FrameLayout, keepFocus: Boolean = true) {
        if (container.childCount == 0) return
        val bottom = container.getChildAt(0)

        bottom.animate()
            .translationXBy(dp(-250f))
            .scaleX(0.85f)
            .scaleY(0.85f)
            .rotation(-5f)
            .setDuration(350)
            .withEndAction {
                bottom.rotation = 0f
                container.removeView(bottom)
                container.addView(bottom)
                layoutStack(container)

                if (keepFocus) {
                    container.getChildAt(container.childCount - 1)?.requestFocus()
                }
            }
            .start()
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }








    /////////////////////////////////////////////////////////////////////////////////////////////////

    private fun showTrending(trending: JSONArray) {

         var trendingItems = mutableListOf<TrendingAnimeItem>()
         for (i in 0 until trending.length()) {


             val item = trending.getJSONObject(i)
             val title = item.getString("name")
             val imageUrl = item.getString("poster")
             val id = item.getString("id")
             val ranking = "0" + item.getString("rank")


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

     private fun showAiring(Airing: JSONArray) {

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

     ////////////////////////////////////////////////////////////////////////////////////////////////
     ////////////////////////////////////////////////////////////////////////////////////////////////

     private fun loadDubbedAnime() {
         if (isLoadingMoreDubbed) return // Prevent multiple rapid clicks
         currentDubbedAnimePage++
         fetchDubbedAnime()
     }

     private fun fetchDubbedAnime() {
         isLoadingMoreDubbed = true
         CoroutineScope(Dispatchers.IO).launch {

             repeat(5) { attempt ->
                 try {
                     val url =
                         "$urlHome/api/v2/hianime/category/dubbed-anime?page=$currentDubbedAnimePage"
                     val connection = URL(url).openConnection() as HttpURLConnection
                     connection.requestMethod = "GET"
                     val response = connection.inputStream.bufferedReader().use { it.readText() }
                     val jsonObject = org.json.JSONObject(response)
                     val fData = jsonObject.getJSONObject("data")

                     Log.e("DEBUG_DubbedAnime1", "${fData}")

                     val dubbedAnime = fData.getJSONArray("animes")
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
                 } catch (e: java.net.ConnectException) {
                     withContext(Dispatchers.Main) {
                         LoadingAnimation.setup(this@Anime_Page, R.raw.error)
                     }
                     Log.e("DEBUG_TAG_ANIME dubbed", "Connect Error", e)
                     currentRecentlyAnimePage--
                     return@repeat
                 } catch (e: Exception) {
                     Log.e("DEBUG_TAG_ANIME dubbed", "Attempt ${attempt + 1} failed", e)
                     delay(10_000)
                     currentDubbedAnimePage--
                 }
                 withContext(Dispatchers.Main) {
                     isLoadingMoreDubbed = false
                 }
             }
         }
     }

     ////////////////////////////////////////////////////////////////////////////////////////////////

     private fun loadPopularAnime() {
         if (isLoadingMorePopular) return // Prevent multiple rapid clicks
         currentPopularAnimePage++
         fetchPopularAnime()
     }

     private fun fetchPopularAnime() {
         isLoadingMorePopular = true
         CoroutineScope(Dispatchers.IO).launch {

             repeat(5) { attempt ->
                 try {
                     val url =
                         "$urlHome/api/v2/hianime/category/most-popular?page=$currentPopularAnimePage"
                     val connection = URL(url).openConnection() as HttpURLConnection
                     connection.requestMethod = "GET"
                     val response = connection.inputStream.bufferedReader().use { it.readText() }
                     val jsonObject = org.json.JSONObject(response)
                     val fData = jsonObject.getJSONObject("data")

                     val popularAnime = fData.getJSONArray("animes")
                     //Log.e("DEBUG_TAG_ANIME Popular", "Data${fData}")

                     val airingItems = mutableListOf<AiringAnimeItem>()

                     for (i in 0 until popularAnime.length()) {

                         val item = popularAnime.getJSONObject(i)
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
                             popularAdapter.addItem(movieItem)
                             isLoadingMorePopular = false
                         }
                     }

                     return@launch
                 } catch (e: java.net.ConnectException) {
                     withContext(Dispatchers.Main) {
                         LoadingAnimation.setup(this@Anime_Page, R.raw.error)
                     }
                     Log.e("DEBUG_TAG_ANIME Popular", "Connect Error", e)
                     currentPopularAnimePage--
                     return@repeat
                 } catch (e: Exception) {
                     Log.e("DEBUG_TAG_ANIME Popular", "Attempt ${attempt + 1} failed", e)
                     delay(10_000)
                     currentPopularAnimePage--
                 }
                 withContext(Dispatchers.Main) {
                     isLoadingMorePopular = false
                 }
             }
         }
     }
     ////////////////////////////////////////////////////////////////////////////////////////////////
     ////////////////////////////////////////////////////////////////////////////////////////////////

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
                     val url =
                         "$urlHome/api/v2/hianime/category/recently-updated?page=$currentRecentlyAnimePage"
                     val connection = URL(url).openConnection() as HttpURLConnection
                     connection.requestMethod = "GET"
                     val response = connection.inputStream.bufferedReader().use { it.readText() }
                     val jsonObject = org.json.JSONObject(response)
                     val fData = jsonObject.getJSONObject("data")

                     Log.e("DEBUG_DubbedAnime1", "${fData}")
                     val dubbedAnime = fData.getJSONArray("animes")
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
                 } catch (e: java.net.ConnectException) {
                     withContext(Dispatchers.Main) {
                         LoadingAnimation.setup(this@Anime_Page, R.raw.error)
                     }
                     Log.e("DEBUG_TAG_ANIME recent", "Connect Error", e)
                     currentRecentlyAnimePage--
                     return@repeat

                 } catch (e: java.io.FileNotFoundException) {
                     withContext(Dispatchers.Main) {
                         LoadingAnimation.setup(this@Anime_Page, R.raw.error)
                     }
                     Log.e(
                         "DEBUG_TAG_ANIME recent",
                         "URL Not Found (404/403) at page $currentRecentlyAnimePage",
                         e
                     )
                     currentRecentlyAnimePage--
                     return@repeat
                 } catch (e: Exception) {
                     Log.e("DEBUG_TAG_ANIME recent", "Attempt ${attempt + 1} failed", e)
                     delay(10_000)
                     currentRecentlyAnimePage--
                 }
                 withContext(Dispatchers.Main) {
                 }
             }
         }

     }
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

         private  fun searchAnimeFetch(searchTerm:String){
             val searchTextDisplay = findViewById<TextView>(R.id.searchTextDisplay)

             searchAdapter.clearItems()

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


                         withContext(Dispatchers.Main) {
                             if(searchData.length() == 0) {
                                 searchTextDisplay.text = "No Results Found"
                             }else{
                                 searchTextDisplay.text = "Search results for: $searchTerm"
                             }
                         }



                         for (i in 0 until searchData.length()) {
                             val item = searchData.getJSONObject(i)
                             val title = item.getString("name")
                             val imageUrl = item.getString("poster")
                             val id = item.getString("id")
                             val type = item.getString("type")
                             val sub = item.getJSONObject("episodes").optString("sub", "")
                             val dub = item.getJSONObject("episodes").optString("dub", "")



                             val searchItem = AnimeSearchItem(
                                 id,
                                 title,
                                 imageUrl,
                                 type,
                                 sub,
                                 dub,
                             )

                             withContext(Dispatchers.Main) {
                                 searchAdapter.addItem(searchItem)
                             }

                         }


                         return@launch
                     } catch (e: Exception) {
                         delay(20_000)
                         Log.e("ANIME_STATUS S-Error", "Error fetching data", e)
                         return@launch
                     }

                 }
             }
             isLoadingMoreRecently = false
         }
         private fun setupSearchUi() {
             val searchInput = findViewById<EditText>(R.id.AnimeSearchInput)
             val searchBar = findViewById<CardView>(R.id.searchBarAnime)

             val focusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                 if (hasFocus) {
                     searchBar.post {
                         searchInput.requestFocus()
                         showKeyboard(searchInput)
                     }
                 } else {
                     hideKeyboard()
                 }
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

     ///////////////////////////////////////////////////////////////////////////////////////////////
     ///////////////////////////////////////////////////////////////////////////////////////////////

        private fun animeWatchedList(){
            val userId = sm.getUserId()
            val cWatching = db.getContinueWatchingAll(userId, "anime")

            watchAdapter = cWatchingAdapter(
                cWatching,
                R.layout.item_watched
            )
            watchRecyclerView.adapter = watchAdapter

        }

    private fun animeFavoritesList(){

        val FavBackdrop: ImageView = findViewById(R.id.FavBackdrop)
        val FavTitle: TextView = findViewById(R.id.FavTitle)
        val FavGenre: TextView = findViewById(R.id.FavGenre)
        val FavType: TextView = findViewById(R.id.FavType)
        val FavRating: TextView = findViewById(R.id.FavRating)
        val FavYear: TextView = findViewById(R.id.FavYear)
        val FavOverview: TextView = findViewById(R.id.FavOverview)
        val RemoveFaveItem: LinearLayout = findViewById(R.id.RemoveFaveItem)


        val animeFavData = db.getFavoriteAnime(userId)

        val items = mutableListOf<FavItem>()

        for (anime in animeFavData) {

            Log.d("Fav_anime", "anime_id: ${anime["anime_id"]}")
            Log.d("Fav_anime", "title: ${anime["name"]}")
            Log.d("Fav_anime", "poster: ${anime["poster"]}")
            Log.d("Fav_anime", "type: ${anime["type"]}")
            Log.d("Fav_anime", "seasons: ${anime["seasons"]}")
            Log.d("Fav_anime", "sub: ${anime["sub"]}")
            Log.d("Fav_anime", "dub: ${anime["dub"]}")

            val genres = anime["genre"] ?: ""
            items.add(
                FavItem(
                    title = anime["name"] ?: "",
                    posterUrl = anime["poster"] ?: "",
                    backdropUrl = anime["poster"] ?: "",
                    releaseDate = anime["aired"] ?: "",
                    runtime = anime["duration"] ?: "",
                    overview = anime["description"] ?: "",
                    voteAverage = anime["rating"] ?: "",
                    genres = genres,
                    production = "",
                    parentalGuide = anime["rating"] ?: "",
                    imdbCode = anime["anime_id"] ?: "",
                    showType = "anime"
                )
            )
        }

        faveAdapter = FavAdapter(
            items,
            R.layout.square_card,
            FavBackdrop,
            FavTitle,
            FavGenre,
            FavType,
            FavRating,
            FavYear,
            FavOverview,
            RemoveFaveItem)

        faveRecyclerView.adapter = faveAdapter

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun notificationS() {

        val fetchedNot = NotificationHelper.getAnimeNotifications(this@Anime_Page)
        Log.d("Not_anime", "fetchedNot: $fetchedNot")
       if(fetchedNot){
                findViewById<CardView>(R.id.cNotificationAnimeIcon).visibility = View.VISIBLE
       }



        val dnot = db.getAllAnimeNotifications(userId)
        val notifications = mutableListOf<NotificationItem>()

        findViewById<TextView>(R.id.notificationHeadline).text = "notifications (${dnot.size})"



        for (item in dnot) {

                Log.d("Not_anime", "notificationId: ${item["id"]}")
                Log.d("Not_anime", "anime_id: ${item["anime_id"]}")
                Log.d("Not_anime", "title: ${item["title"]}")
                Log.d("Not_anime", "poster: ${item["poster"]}")
                Log.d("Not_anime", "subStored: ${item["subStored"]}")
                Log.d("Not_anime", "dubStored: ${item["dubStored"]}")
                Log.d("Not_anime", "seasonsStored: ${item["seasonsStored"]}")
                Log.d("Not_anime", "notify_at: ${item["notify_at"]}\n\n\n")


            val itemData = NotificationItem(
                notificationId = item["id"].toString(),      // ✅ PASS ID
                imdbCode = item["anime_id"].toString(),
                title = item["title"].toString(),
                imageUrl = item["poster"],
                info = "sub: ${item["subStored"]} dub: ${item["dubStored"]}",
                type = "anime",
                newSeason = item["subStored"].toString(),
                newEpisode = item["dubStored"].toString(),
                time =  item["notify_at"].toString()
            )
            notifications.add(itemData)
            }


            notificationAdapter = NotificationAdapter(
                items = notifications.toMutableList(),
                layoutResId = R.layout.item_notification
            )
            notificationRecyclerView.adapter = notificationAdapter


    }
 }