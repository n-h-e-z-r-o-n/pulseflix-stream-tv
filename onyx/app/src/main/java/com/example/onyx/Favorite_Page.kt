package com.example.onyx

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import org.json.JSONArray
import kotlin.text.ifEmpty


class Favorite_Page : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorite_page)

        NavAction.setupSidebar(this@Favorite_Page)
        setupBackPressedCallback()
        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        if (this::adapter.isInitialized) {
            adapter.clearItems()
        }
        loadFavorites()
    }

    private fun loadFavorites(){
        recyclerView = findViewById<RecyclerView>(R.id.favoritesRecycler)
        val FaveData = findViewById<LinearLayout>(R.id.FaveData)
        val emptyState = findViewById<TextView>(R.id.emptyState)



        recyclerView.layoutManager = LinearLayoutManager(
            this@Favorite_Page,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val Spacing = (5 * resources.displayMetrics.density).toInt()
        recyclerView.addItemDecoration(EqualSpaceItemDecoration(Spacing))



        val favorites = FavoritesManager.getFavorites(this)



        if (favorites.isEmpty()) {
            recyclerView.visibility = View.GONE
            FaveData.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            FaveData.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE

            val items = favorites.map { obj ->
                var originalTitle: String
                var posterUrl:String
                var backdropUrl:String
                var releaseDate:String
                var runtime:String
                var voteAverage:String
                var genres:String
                var productionC:String
                var pg:String
                var overview:String
                var id:String
                var type:String


                val _media_type = obj.optString("_media_type")
                try {

                    if (_media_type == "anime"){
                        posterUrl = obj.getJSONObject("anime").getJSONObject("info").getString("poster")
                        originalTitle = obj.getJSONObject("anime").getJSONObject("info").getString("name")
                        backdropUrl = obj.getJSONObject("anime").getJSONObject("info").getString("poster")
                        releaseDate = obj.getJSONObject("anime").getJSONObject("moreInfo").getString("aired")
                        runtime = obj.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getString("duration")
                        overview = obj.getJSONObject("anime").getJSONObject("info").getString("description")

                        voteAverage = ""
                        val genresArray = obj.getJSONObject("anime").getJSONObject("moreInfo").getJSONArray("genres")
                        genres = ""
                        for (i in 0 until genresArray.length()) {
                            genres = genres +" ~ " +genresArray.getString(i)
                        }
                        productionC = ""
                        pg =  obj.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getString("rating")
                        id = obj.getJSONObject("anime").getJSONObject("info").getString("id")
                        type ="anime"

                    }else {
                        releaseDate = obj.optString("release_date").ifEmpty {
                            obj.optString("first_air_date")
                        }

                        releaseDate = releaseDate.substring(0, 4)

                        type = if (obj.optString("release_date").isNotEmpty()) {
                            "movie"
                        } else if (obj.optString("first_air_date").isNotEmpty()) {
                            "tv"
                        } else {
                            "unknown"
                        }


                        runtime = obj.optString("runtime").ifEmpty {
                            val arr: JSONArray? = obj.optJSONArray("episode_run_time")
                            if (arr != null && arr.length() > 0) arr.optInt(0).toString() else ""
                        }

                        overview = obj.optString("overview", "")

                        backdropUrl =
                            "https://image.tmdb.org/t/p/w1280${obj.optString("backdrop_path", "")}"
                        posterUrl =
                            "https://image.tmdb.org/t/p/w780${obj.optString("poster_path", "")}"
                        originalTitle = obj.optString("name", obj.optString("title", ""))

                        voteAverage = obj.optString("vote_average", " ").substring(0, 3)

                        // Genres
                        val genresList = mutableListOf<String>()
                        obj.optJSONArray("genres")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                arr.optJSONObject(i)?.optString("name")?.let { genresList.add(it) }
                            }
                        }
                        genres = genresList.joinToString(" ~ ")


                        // Production companies
                        val productionList = mutableListOf<String>()
                        obj.optJSONArray("production_companies")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                arr.optJSONObject(i)?.optString("name")
                                    ?.let { productionList.add(it) }
                            }
                        }
                        productionC = productionList.joinToString("  ~ ")
                        pg = if (obj.optBoolean("adult", false)) "18 +" else "13"

                        id = obj.optString("id", "")
                    }



                    FavItem(
                        title = originalTitle,
                        posterUrl = posterUrl,
                        backdropUrl = backdropUrl,
                        releaseDate = releaseDate,
                        runtime = runtime,
                        overview = overview,
                        voteAverage = "IMDB $voteAverage",
                        genres = genres,
                        production = productionC,
                        parentalGuide = pg,
                        imdbCode = id,
                        showType = type
                    )


                }catch (e : Exception){
                    //titleFavorites.text = "$e"
                    FavItem(
                        title = "",
                        posterUrl = "",
                        backdropUrl = "",
                        releaseDate = "",
                        runtime = "",
                        overview = "",
                        voteAverage = "",
                        genres = "",
                        production = "",
                        parentalGuide = "",
                        imdbCode = "",
                        showType = ""
                    )
                }


            }.toMutableList()

            val FavBackdrop: ImageView = findViewById(R.id.FavBackdrop)
            val FavTitle: TextView = findViewById(R.id.FavTitle)
            val FavGenre: TextView = findViewById(R.id.FavGenre)
            val FavType: TextView = findViewById(R.id.FavType)
            val FavRating: TextView = findViewById(R.id.FavRating)
            val FavYear: TextView = findViewById(R.id.FavYear)
            val FavOverview: TextView = findViewById(R.id.FavOverview)
            val RemoveFaveItem: LinearLayout = findViewById(R.id.RemoveFaveItem)

            adapter =  FavAdapter(items, R.layout.square_card, FavBackdrop, FavTitle, FavGenre, FavType, FavRating, FavYear, FavOverview,RemoveFaveItem )
            recyclerView.adapter = adapter
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    findViewById<ImageButton>(R.id.btnFav).requestFocus()
            }
        })
    }
}