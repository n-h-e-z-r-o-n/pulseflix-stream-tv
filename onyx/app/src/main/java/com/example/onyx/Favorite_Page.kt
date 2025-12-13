package com.example.onyx

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.json.JSONArray
import kotlin.text.ifEmpty
import com.example.onyx.Database.AppDatabase
import com.example.onyx.Database.SessionManger

class Favorite_Page : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var  sm: SessionManger
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavAdapter
    private lateinit var watchRecyclerView: RecyclerView
    private lateinit var watchAdapter: cWatchingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorite_page)

        db = AppDatabase(this)         // Initialize database
        sm = SessionManger(this)

        val cWatchBtn = findViewById<LinearLayout>(R.id.cWatchBtn)
        val favBtn = findViewById<LinearLayout>(R.id.FavBtn)

        val favePage = findViewById<LinearLayout>(R.id.FavBox)
        val cWatchPage = findViewById<LinearLayout>(R.id.watchingBox)

        cWatchBtn.setOnClickListener {
            favePage.visibility = View.GONE
            cWatchPage.visibility = View.VISIBLE

            cWatchBtn.alpha = 1f
            favBtn.alpha = 0.5f

        }
        favBtn.setOnClickListener {
            favePage.visibility = View.VISIBLE
            cWatchPage.visibility = View.GONE

            cWatchBtn.alpha = 0.5f
            favBtn.alpha = 1f

        }



        ////////////////////////////////////////////////////////////////////////////////////////////
        recyclerView = findViewById(R.id.favoritesRecycler)
        recyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val spacing = (5 * resources.displayMetrics.density).toInt()
        recyclerView.addItemDecoration(EqualSpaceItemDecoration(spacing))


        watchRecyclerView = findViewById(R.id.watchingRecycler)
        watchRecyclerView.layoutManager = GridLayoutManager(this@Favorite_Page, GlobalUtils.calculateSpanCountV2(this@Favorite_Page,170,160))
        val Spacing = (16 * resources.displayMetrics.density).toInt()
        watchRecyclerView.addItemDecoration(EqualSpaceItemDecoration(Spacing))
        ////////////////////////////////////////////////////////////////////////////////////////////






        NavAction.setupSidebar(this@Favorite_Page)
        setupBackPressedCallback()
        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        if (this::adapter.isInitialized) {
            adapter.clearItems()
        }
        if (this::watchAdapter.isInitialized) {
            watchAdapter.clearItems()
        }
        loadFavorites()
    }



    private fun loadFavorites() {

        val userId = sm.getUserId()

        val animeFavData = db.getFavoriteAnime(userId)
        val movies = db.getFavoriteShowsByType(userId, "movie")
        val tvs = db.getFavoriteShowsByType(userId, "tv")
        val cWatching = db.getContinueWatchingAll(userId)

        Log.d("FAVORITE", "Movies: ${movies.size}")
        Log.d("FAVORITE", "TV Shows: ${tvs.size}")
        Log.d("FAVORITE", "Anime: ${animeFavData.size}")
        Log.d("C_WATCHING", "watching: ${cWatching.size}")













        // SHOW EMPTY STATE ONLY IF ALL FAVORITES ARE EMPTY
        val emptyState = findViewById<TextView>(R.id.emptyState)
        if (movies.isEmpty() && tvs.isEmpty() && animeFavData.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            return
        } else {
            emptyState.visibility = View.GONE
        }

        // -----------------------------------------------
        // 🔥 COMBINE ALL FAVORITE ITEMS INTO ONE LIST
        // -----------------------------------------------

        val items = mutableListOf<FavItem>()

        // ---------- MOVIES ----------
        for (movie in movies) {
            items.add(
                FavItem(
                    title = movie["title"] ?: "",
                    posterUrl = movie["poster"] ?: "",
                    backdropUrl = movie["backdrop"] ?: "",
                    releaseDate = movie["year"] ?: "",
                    runtime = movie["runtime"] ?: "",
                    overview = movie["overview"] ?: "",
                    voteAverage = "${movie["rating"] ?: ""}",
                    genres = movie["genres"] ?: "",
                    production = "",
                    parentalGuide = movie["pg"] ?: "",
                    imdbCode = movie["show_id"] ?: "",
                    showType = "movie"
                )
            )
        }

        // ---------- TV SHOWS ----------
        for (show in tvs) {
            items.add(
                FavItem(
                    title = show["title"] ?: "",
                    posterUrl = show["poster"] ?: "",
                    backdropUrl = show["backdrop"] ?: "",
                    releaseDate = show["year"] ?: "",
                    runtime = show["runtime"] ?: "",
                    overview = show["overview"] ?: "",
                    voteAverage = "${show["rating"] ?: ""}",
                    genres = show["genres"] ?: "",
                    production = "",
                    parentalGuide = show["pg"] ?: "",
                    imdbCode = show["show_id"] ?: "",
                    showType = "tv"
                )
            )
        }

        // ---------- ANIME ----------
        for (anime in animeFavData) {
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


        // -----------------------------------------------
        // ADAPTER SETUP
        // E  Duration: 1461144 ms, Last Position: 91733 ms, Logged at: 2025-12-13 00:04:28
        // -----------------------------------------------
        val FavBackdrop: ImageView = findViewById(R.id.FavBackdrop)
        val FavTitle: TextView = findViewById(R.id.FavTitle)
        val FavGenre: TextView = findViewById(R.id.FavGenre)
        val FavType: TextView = findViewById(R.id.FavType)
        val FavRating: TextView = findViewById(R.id.FavRating)
        val FavYear: TextView = findViewById(R.id.FavYear)
        val FavOverview: TextView = findViewById(R.id.FavOverview)
        val RemoveFaveItem: LinearLayout = findViewById(R.id.RemoveFaveItem)

        adapter = FavAdapter(
            items,
            R.layout.square_card,
            FavBackdrop,
            FavTitle,
            FavGenre,
            FavType,
            FavRating,
            FavYear,
            FavOverview,
            RemoveFaveItem
        )

        recyclerView.adapter = adapter



        for (item in cWatching) {
            Log.d("C_WATCHING", "item: $item")
            Log.d("C_WATCHING", "type: ${item["type"]}")
            Log.d("C_WATCHING", "title: ${item["title"]}")
            Log.d("C_WATCHING", "poster: ${item["poster"]}")
            Log.d("C_WATCHING", "backdrop: ${item["backdrop"]}")
            Log.d("C_WATCHING", "season_number: ${item["season_number"]}")
            Log.d("C_WATCHING", "episode_number: ${item["episode_number"]}")
            Log.d("C_WATCHING", "last_position: ${item["last_position"]}")
            Log.d("C_WATCHING", "duration: ${item["duration"]}")
            Log.d("C_WATCHING", "updated_at: ${item["updated_at"]}")


        }


        watchAdapter = cWatchingAdapter(
            cWatching,
            R.layout.item_watched
        )
        watchRecyclerView.adapter = watchAdapter



    }


    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    findViewById<ImageButton>(R.id.btnFav).requestFocus()
            }
        })
    }
}