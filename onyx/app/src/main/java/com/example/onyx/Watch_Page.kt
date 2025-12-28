package com.example.onyx

import android.R.attr.textAllCaps
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import org.json.JSONArray
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.TooltipCompat
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide


import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import kotlin.text.ifEmpty

import com.example.onyx.FetchData.TMDBapi
import com.example.onyx.Database.AppDatabase
import com.example.onyx.Database.SessionManger
import java.time.format.DateTimeFormatter


class Watch_Page : AppCompatActivity() {

    private lateinit var  fetch: TMDBapi
    private lateinit var db: AppDatabase
    private lateinit var  sm: SessionManger


    private var currentServerIndex = 0

    private lateinit var episodes_recycler : RecyclerView

    private lateinit var watchButton : LinearLayout
    private lateinit var trailerButton :LinearLayout
    private lateinit var serverButton: LinearLayout


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_watch_page)
        LoadingAnimation.setup(this@Watch_Page, R.raw.b)
        LoadingAnimation.show(this@Watch_Page)

        fetch = TMDBapi(this)
        db = AppDatabase(this)
        sm = SessionManger(this)


        episodes_recycler = findViewById<RecyclerView>(R.id.episodes_recycler)
        //episodes_recycler.layoutManager = GridLayoutManager(this@Watch_Page, 4)
        episodes_recycler.layoutManager = LinearLayoutManager(
            this@Watch_Page,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        watchButton = findViewById<LinearLayout>(R.id.watchNowButton)
        trailerButton = findViewById<LinearLayout>(R.id.TrailerButton)
        serverButton = findViewById<LinearLayout>(R.id.serverButton)


        // Get extras from Intent
        val imdbCode = intent.getStringExtra("imdb_code")
        val type = intent.getStringExtra("type")

        Log.e("DEBUG_WATCH", "imdbCode: $imdbCode, type: $type")

        if(!imdbCode.isNullOrEmpty()){
            fetchData(imdbCode.toString(), type.toString())
        }else{
            fetchData("76479 ", "tv")
        }

    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchData(tmdbId:String, type: String) {
        LoadingAnimation.show(this@Watch_Page)

        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels


        val mainWidget1 = findViewById<FrameLayout>(R.id.widget_1)
        val params = mainWidget1.layoutParams
        params.height = (screenHeight * 0.8).toInt()
        mainWidget1.layoutParams = params

        // ---------- LOGOS ------------------------------------------------------------------------
        val cShowLogo = findViewById<ImageView>(R.id.cShowLogo)
        val textLogo = findViewById<TextView>(R.id.title_widget)
        fetch.fetchLogos(type, tmdbId, cShowLogo, textLogo)

        // ---------- DATA -------------------------------------------------------------------------
        val jsonObject = fetch.fetchShowData(tmdbId, type=type)
        Log.e("DEBUG_Watch", jsonObject.toString())
        if (jsonObject != null) {

            val overview = jsonObject.optString("overview", "")
            val voteAverage = jsonObject.optString("vote_average", "0")
            val voteCount = jsonObject.optString("vote_count", "0")

            val originalTitle = jsonObject.optString("name").ifEmpty {
                jsonObject.optString("title")
            }

            val releaseDate = jsonObject.optString("release_date").ifEmpty {
                jsonObject.optString("first_air_date")
            }.substring(0, 4)

            val PG = if (jsonObject.optBoolean("adult", false)) "18 +" else "13"

            val backdropUrl =
                if (jsonObject.has("backdrop_path") && !jsonObject.isNull("backdrop_path")) {
                    "https://image.tmdb.org/t/p/original/${jsonObject.getString("backdrop_path")}"
                } else if (jsonObject.has("poster_path") && !jsonObject.isNull("poster_path")) {
                    "https://image.tmdb.org/t/p/original/${jsonObject.getString("poster_path")}"
                } else {
                    ""
                }

            val posterUrl =
                "https://image.tmdb.org/t/p/original/${jsonObject.getString("poster_path")}"

            val runtime = if (jsonObject.has("runtime") && !jsonObject.isNull("runtime")) {
                val runtimeInt = jsonObject.optInt("runtime", 0)
                if (runtimeInt > 0) GlobalUtils.formatRuntime(runtimeInt) else ""
            } else {
                val arr = jsonObject.optJSONArray("episode_run_time")
                val runtimeInt =
                    if (arr != null && arr.length() > 0) arr.optInt(0) else 0
                if (runtimeInt > 0) GlobalUtils.formatRuntime(runtimeInt) else ""
            }

            val genresArray =
                jsonObject.getJSONArray("genres") //[{"id":80,"name":"Crime"},{"id":99,"name":"Documentary"}]
            val genresList = mutableListOf<String>()
            for (i in 0 until genresArray.length()) {
                val genreObject = genresArray.getJSONObject(i)
                val genreName = genreObject.getString("name")
                genresList.add(genreName)
            }

            val genres = jsonObject.optJSONArray("genres")
                ?.let { array ->
                    (0 until array.length()).mapNotNull {
                        array.optJSONObject(it)?.optString("name")
                    }.joinToString("  -  ")
                } ?: ""


            val productionCompanies = jsonObject.optJSONArray("production_companies")
                ?.let { array ->
                    (0 until array.length()).mapNotNull {
                        array.optJSONObject(it)?.optString("name")
                    }.joinToString("  -  ")
                } ?: ""







            // ---------- UI BINDS -----------------------------------------------------------------

            findViewById<TextView>(R.id.title_widget).text = originalTitle
            findViewById<TextView>(R.id.year_widget).text = releaseDate
            findViewById<TextView>(R.id.overview_widget).text = overview
            findViewById<TextView>(R.id.Genres_widget).text = genres
            findViewById<TextView>(R.id.Production_widget).text = productionCompanies
            findViewById<TextView>(R.id.Rating_widget).text = voteAverage
            findViewById<TextView>(R.id.Runtime_widget).text = "$runtime min"
            findViewById<TextView>(R.id.PG_widget).text = PG

            val poster_widget = findViewById<ImageView>(R.id.posterImageView)
            val backdrop_Widget = findViewById<ImageView>(R.id.backdropImageView)


            Glide.with(this@Watch_Page)
                .load(backdropUrl)
                .centerInside()
                .into(backdrop_Widget)


            Glide.with(this@Watch_Page)
                .load(posterUrl)
                .centerInside()
                .into(poster_widget)


        //------------------------------------------------------------------------------------------

            val validSeasons = mutableListOf<JSONObject>()
            var no_of_season: Int = 0
            var lastSeason: Int = 0
            var lastEpisode: Int = 0

            if (type == "tv") {
                try {
                    lastEpisode = jsonObject.getJSONObject("last_episode_to_air")
                        .optInt("episode_number", 0)
                    lastSeason = jsonObject.getJSONObject("last_episode_to_air")
                        .optInt("season_number", 0)
                } catch (e: Exception) {}

                val seasonsArray = jsonObject.getJSONArray("seasons")
                for (i in 0 until seasonsArray.length()) {
                    val season = seasonsArray.getJSONObject(i)
                    val airDate = season.optString("air_date", "")

                    if (airDate.isNotEmpty()) {
                        validSeasons.add(season)
                    }
                }

                no_of_season = validSeasons.size


                params.height = (screenHeight * 1).toInt()
                mainWidget1.layoutParams = params


                watchButton.visibility = View.GONE
                val Season_widget = findViewById<LinearLayout>(R.id.Season_widget)
                Season_widget.visibility = View.VISIBLE

                //val season_count_widget = findViewById<TextView>(R.id.season_count_text)
                //season_count_widget.text = "$no_of_season Seasons"
                createSeasonButtons(no_of_season, validSeasons, tmdbId, jsonObject)
            }

            // ---------- BUTTONS ------------------------------------------------------------------



            watchButton.setOnClickListener {
                val intent = Intent(this@Watch_Page, Play::class.java)
                intent.putExtra("imdb_code", tmdbId)
                intent.putExtra("type", type)
                startActivity(intent)
            }

            serverButton.setOnClickListener {
                showServerDialog()
            }

            trailerButton.setOnClickListener {
            }


            setupFavoriteButton(
                showId = tmdbId,
                type = type,
                title = originalTitle,
                voteAverage = voteAverage,
                genres = genres,
                overview = overview,
                runtime = runtime,
                year = releaseDate,
                voteCount = voteCount,
                pg = PG,
                poster = posterUrl,
                backdrop = backdropUrl,
                noOfSeason = no_of_season,
                lastSeason = lastSeason,
                lastEpisode = lastEpisode
            )

            //--------------------------------------------------------------------------------------


            LoadingAnimation.hide(this@Watch_Page)
            Cast_Data(tmdbId.toString(), type.toString())
            Watch_Recomendation_Data(tmdbId.toString(), type.toString())
        }else{
            LoadingAnimation.setup(this@Watch_Page, R.raw.error)
        }

    }



    private var selectedSeasonButton: Button? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSeasonButtons(
        noOfSeasons: Int,
        seasonData: MutableList<JSONObject>,
        seasonID: String,
        seasonAllData: JSONObject
    ) {
        val container = findViewById<LinearLayout>(R.id.season_selector_container)
        container.removeAllViews()

        var track = 0
        var firstButton: Button? = null  // 👈 Keep a reference to the first

        while (track < noOfSeasons) {
            val selectedSeason = seasonData[track]
            val season_no = selectedSeason.optInt("season_number")
            val s_name = if (season_no == 0) {
                selectedSeason.getString("name")
            } else {
                "Season $season_no"
            }

            val seasonButton = Button(this).apply {
                text = s_name
                textSize = 14f
                isFocusable = true
                isFocusableInTouchMode = true
                isClickable = true
                setTypeface(typeface, Typeface.BOLD)
                stateListAnimator = null
                isAllCaps = false
                typeface = ResourcesCompat.getFont(context, R.font.p)
                background = ContextCompat.getDrawable(context, R.drawable.season_selector)
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(120),
                    dpToPx(38)
                ).apply { marginEnd = dpToPx(0) }
                setTextColor(resolveAttrColor(context, R.attr.FG_color))

            }

            seasonButton.setOnClickListener {
                selectedSeasonButton?.let { previous ->
                    previous.background = ContextCompat.getDrawable(
                        this,
                        R.drawable.season_selector
                    )
                    previous.setTextColor(resolveAttrColor(this, R.attr.FG_color))
                }

                seasonButton.setTextColor(resolveAttrColor(this, R.attr.AccentColor))
                selectedSeasonButton = seasonButton

                seasonButton.isEnabled = false
                ShowSeasonEpisodes(season_no, seasonData, seasonID)
                seasonButton.postDelayed({ seasonButton.isEnabled = true }, 3000)
            }

            // DPAD navigation
            seasonButton.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val index = container.indexOfChild(v)
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (index > 0) container.getChildAt(index - 1).requestFocus()
                            return@setOnKeyListener true
                        }

                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (index < container.childCount - 1) container.getChildAt(index + 1).requestFocus()
                            return@setOnKeyListener true
                        }

                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            val episodesRecycler = findViewById<RecyclerView>(R.id.episodes_recycler)
                            val castRecycler = findViewById<RecyclerView>(R.id.Cast_widget)
                            val recommendationRecycler = findViewById<RecyclerView>(R.id.Recommendation_widget)

                            when {
                                episodesRecycler.childCount > 0 -> episodesRecycler.getChildAt(0)?.requestFocus()
                                castRecycler.childCount > 0 -> castRecycler.getChildAt(0)?.requestFocus()
                                recommendationRecycler.childCount > 0 -> recommendationRecycler.getChildAt(0)?.requestFocus()
                            }
                            return@setOnKeyListener true
                        }

                        KeyEvent.KEYCODE_DPAD_UP -> {
                            findViewById<LinearLayout>(R.id.serverButton)?.requestFocus()
                            return@setOnKeyListener true
                        }
                    }
                }
                false
            }

            container.addView(seasonButton)

            if (track == 0) {
                firstButton = seasonButton  // 👈 store reference to first button
            }

            track++
        }

        // 👇 Auto-click the first button after layout is done
        firstButton?.post {
            firstButton?.performClick()
            firstButton?.requestFocus()  // optional: also focus it visually
        }
    }



    //setBackgroundColor(Color.parseColor("#3D5AFE"))
    private fun resolveAttrColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ShowSeasonEpisodes(SelectedSeasons: Int, seasonData : MutableList<JSONObject>, seriesId: String) {

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE


         findViewById<ImageView>(R.id.backdropImageView)



        val selectedSeason = seasonData.firstOrNull {
            it.optInt("season_number") == SelectedSeasons
        }
        if (selectedSeason == null) {
            return
        }


        val airDate = selectedSeason.optString("air_date", "")
        var selectedSeasonPoster = selectedSeason.optString("poster_path", "")
        val selectedSeasonOverview = selectedSeason.optString("overview", "")
        val selectedSeasonRating = selectedSeason.optDouble("vote_average", 0.0)

        findViewById<TextView>(R.id.season_C).text = "Season $SelectedSeasons"
        findViewById<TextView>(R.id.Rating_widget).text = "$selectedSeasonRating"

        if (selectedSeasonOverview !== "") {
            findViewById<TextView>(R.id.overview_widget).text = selectedSeasonOverview
        }
        if (selectedSeasonPoster !== "") {
            selectedSeasonPoster = "https://image.tmdb.org/t/p/original/$selectedSeasonPoster"
            val posterWidget = findViewById<ImageView>(R.id.posterImageView)
            Glide.with(posterWidget.context)
                .load(selectedSeasonPoster)
                .centerCrop()
                .into(posterWidget)
        }


        val jsonObject = fetch.fetchSeasonInfo(seriesId.toString(), SelectedSeasons.toString())
        if (jsonObject != null) {

                    val episodesArray = jsonObject.getJSONArray("episodes")

                    Log.e("DEBUG_Each E json", jsonObject.toString())
                    Log.e("DEBUG_Each E data", episodesArray.toString())
                    Log.e("DEBUG_Each E leng", "${episodesArray.length()}")


                    val episodesList = mutableListOf<EpisodeItem>()
                    for (i in 0 until episodesArray.length()) {
                        val episodes = episodesArray.getJSONObject(i)

                        val episodesAirDate =  episodes.optString("air_date", "")
                        try {
                            Log.e("DEBUG_Each E date", "airDate: $episodesAirDate,  today $today")
                            val airLocalDate  = LocalDate.parse(episodesAirDate, formatter)
                            if (airLocalDate.isAfter(today)) {
                                continue
                            }
                        }catch (e: Exception){
                            Log.e("DEBUG_Each E date", "airDate: $airDate,  today $today, Error${e.message}")
                        }


                        val stillPathRaw = episodes.optString("still_path", "")
                        val runtimeRaw = episodes.optString("runtime", "")

                        val stillPath = if (stillPathRaw.isNullOrEmpty() || runtimeRaw == "null") ""  else stillPathRaw
                        val runtime = if (runtimeRaw.isNullOrEmpty() || runtimeRaw == "null") "0" else runtimeRaw


                        episodesList.add(

                            EpisodeItem(
                                episodesName = episodes.optString("name", ""),
                                episodesImage = stillPath,
                                episodesNumber = episodes.optString("episode_number", ""),
                                episodesRating = episodes.optString("vote_average", "0.0"),
                                episodesRuntime = runtime,
                                episodesDescription = episodes.optString("overview", ""),
                                seriesId = seriesId,
                                seasonNumber = episodes.optString("season_number", ""),
                            )
                        )
                    }

                        Log.e("DEBUG_Each E list", "${episodesList.size}")
                        episodes_recycler.removeAllViews()
                        episodes_recycler.adapter = EpisodesAdapter(episodesList)
        }
    }


    private fun Cast_Data(show_id: String, type: String) {


                val jsonObject = fetch.fetchShowCast(show_id, type)

                if (jsonObject != null) {

                    val moviesArray = jsonObject.getJSONArray("cast")
                    Log.e("DEBUG_WATCH_Results", jsonObject.toString())

                    val movies = mutableListOf<CastItem>()
                    for (i in 0 until moviesArray.length()) {
                        val item = moviesArray.getJSONObject(i)
                        val title = item.getString("original_name")
                        val imgUrl = "https://image.tmdb.org/t/p/original/" + item.getString("profile_path")
                        val cast_id = item.getString("id")
                        val type = "Actor"
                        movies.add(CastItem(title, imgUrl, cast_id, type))
                    }

                    val recyclerView = findViewById<RecyclerView>(R.id.Cast_widget)
                    recyclerView.layoutManager = LinearLayoutManager(
                        this@Watch_Page,
                        LinearLayoutManager.HORIZONTAL, // 👈 makes it horizontal
                        false
                    )
                    recyclerView.adapter = CastAdapter(movies,  R.layout.round_grid)
                    val spacing = (9 * resources.displayMetrics.density).toInt()
                    recyclerView.addItemDecoration(EqualSpaceItemDecoration(spacing))

                }
    }

    private fun Watch_Recomendation_Data(show_id: String, type: String) {

                val jsonObject = fetch.fetchShowRecommendation(show_id, type)

                if (jsonObject != null) {
                    Log.e("DEBUG_WATCH_RECO", jsonObject.toString())
                    val moviesArray = jsonObject.getJSONArray("results")

                    val dmoviesArray = if (moviesArray.length() ==  0){
                            val fallback = """[{"adult":false,"backdrop_path":"/7HqLLVjdjhXS0Qoz1SgZofhkIpE.jpg","id":1087192,"title":"How to Train Your Dragon","original_title":"How to Train Your Dragon","overview":"On the rugged isle of Berk, where Vikings and dragons have been bitter enemies for generations, Hiccup stands apart, defying centuries of tradition when he befriends Toothless, a feared Night Fury dragon. Their unlikely bond reveals the true nature of dragons, challenging the very foundations of Viking society.","poster_path":"\/q5pXRYTycaeW6dEgsCrd4mYPmxM.jpg","media_type":"movie","original_language":"en","genre_ids":[14,10751,28,12],"popularity":261.0336,"release_date":"2025-06-06","video":false,"vote_average":8.022,"vote_count":1651},{"adult":false,"backdrop_path":"\/zNriRTr0kWwyaXPzdg1EIxf0BWk.jpg","id":1234821,"title":"Jurassic World Rebirth","original_title":"Jurassic World Rebirth","overview":"Five years after the events of Jurassic World Dominion, covert operations expert Zora Bennett is contracted to lead a skilled team on a top-secret mission to secure genetic material from the world's three most massive dinosaurs. When Zora's operation intersects with a civilian family whose boating expedition was capsized, they all find themselves stranded on an island where they come face-to-face with a sinister, shocking discovery that's been hidden from the world for decades.","poster_path":"\/1RICxzeoNCAO5NpcRMIgg1XT6fm.jpg","media_type":"movie","original_language":"en","genre_ids":[878,12,28],"popularity":554.8251,"release_date":"2025-07-01","video":false,"vote_average":6.375,"vote_count":1645},{"adult":false,"backdrop_path":"\/962KXsr09uK8wrmUg9TjzmE7c4e.jpg","id":1119878,"title":"Ice Road: Vengeance","original_title":"Ice Road: Vengeance","overview":"Big rig ice road driver Mike McCann travels to Nepal to scatter his late brother’s ashes on Mt. Everest. While on a packed tour bus traversing the deadly 12,000 ft. terrain of the infamous Road to the Sky, McCann and his mountain guide encounter a group of mercenaries and must fight to save themselves, the busload of innocent travelers, and the local villagers’ homeland.","poster_path":"\/cQN9rZj06rXMVkk76UF1DfBAico.jpg","media_type":"movie","original_language":"en","genre_ids":[28,53,18],"popularity":106.818,"release_date":"2025-06-27","video":false,"vote_average":6.848,"vote_count":174},{"adult":false,"backdrop_path":"\/7Q2CmqIVJuDAESPPp76rWIiA0AD.jpg","id":1011477,"title":"Karate Kid: Legends","original_title":"Karate Kid: Legends","overview":"After a family tragedy, kung fu prodigy Li Fong is uprooted from his home in Beijing and forced to move to New York City with his mother. When a new friend needs his help, Li enters a karate competition – but his skills alone aren't enough. Li's kung fu teacher Mr. Han enlists original Karate Kid Daniel LaRusso for help, and Li learns a new way to fight, merging their two styles into one for the ultimate martial arts showdown.","poster_path":"\/AEgggzRr1vZCLY86MAp93li43z.jpg","media_type":"movie","original_language":"en","genre_ids":[28,12,18],"popularity":133.6611,"release_date":"2025-05-08","video":false,"vote_average":7.151,"vote_count":687},{"adult":false,"backdrop_path":"\/7Zx3wDG5bBtcfk8lcnCWDOLM4Y4.jpg","id":552524,"title":"Lilo & Stitch","original_title":"Lilo & Stitch","overview":"The wildly funny and touching story of a lonely Hawaiian girl and the fugitive alien who helps to mend her broken family.","poster_path":"\/tUae3mefrDVTgm5mRzqWnZK6fOP.jpg","media_type":"movie","original_language":"en","genre_ids":[10751,878,35,12],"popularity":164.7425,"release_date":"2025-05-17","video":false,"vote_average":7.322,"vote_count":1357}]"""
                            JSONArray(fallback)
                    }else{
                         moviesArray
                    }

                    //Log.e("DEBUG_WATCH_Results", jsonObject.toString())

                    val movies = mutableListOf<MovieItem>()

                    for (i in 0 until dmoviesArray.length()) {
                        val item = dmoviesArray.getJSONObject(i)
                        //val title = item.getString("original_title")

                        val title = if (item.has("name") && !item.isNull("name")) {
                            item.optString("name")
                        } else {
                            item.optString("title")
                        }
                        //val imgUrl = "https://image.tmdb.org/t/p/w780" + item.getString("backdrop_path")

                        val imgUrl = if (item.has("backdrop_path") && !item.isNull("backdrop_path")) {
                            "https://image.tmdb.org/t/p/original${item.getString("backdrop_path")}"
                        } else if (item.has("poster_path") && !item.isNull("poster_path")) {
                            "https://image.tmdb.org/t/p/original${item.getString("poster_path")}"
                        } else { "" }

                        val imdb_code = item.getString("id")
                        val type = item.getString("media_type")
                        movies.add(MovieItem(title, imgUrl, imdb_code, type))
                    }



                        val recyclerView = findViewById<RecyclerView>(R.id.Recommendation_widget)
                        recyclerView.isNestedScrollingEnabled = false

                        // Calculate number of rows
                        val columns = 3
                        val itemCount = movies.size
                        val rows = Math.ceil(itemCount.toDouble() / columns).toInt()
                        val dpPerRow = 172f
                        val spacingBetweenRows = 19f // dp (from your item decoration)
                        val totalHeightDp = (dpPerRow * rows) + (spacingBetweenRows * (rows - 1))
                        // Convert dp to pixels
                        val density = resources.displayMetrics.density
                        val totalHeightPx = (totalHeightDp * density).toInt()
                        // Set the calculated height
                        recyclerView.layoutParams.height = totalHeightPx
                        recyclerView.requestLayout() // Important: request layout update

                        recyclerView.layoutManager = GridLayoutManager(this@Watch_Page, 3)
                        recyclerView.adapter = RecommendAdapter(movies,  R.layout.recomendation_card)
                        val spacing = (19 * resources.displayMetrics.density).toInt() // 16dp to px
                        recyclerView.addItemDecoration(EqualSpaceItemDecoration(spacing))

                }
    }



    private fun setupFavoriteButton(
        showId :String,
        type :String,
        title:String,
        voteAverage :String,
        genres :String,
        overview :String,
        runtime :String,
        year:String,
        voteCount :String,
        pg:String,
        poster:String,
        backdrop:String,
        noOfSeason:Int,
        lastSeason: Int,
        lastEpisode:Int
    ) {

        val faveButton = findViewById<LinearLayout>(R.id.favoriteButton)
        val faveButtonImg = findViewById<ImageView>(R.id.favoriteButtonImg)
        val faveButtonText = findViewById<TextView>(R.id.favoriteButtonText)

        val userId = sm.getUserId()


        @RequiresApi(Build.VERSION_CODES.O)
        fun applyIcon() {
            val isFav = db.isFavoriteShow(userId, showId, type)
            if (isFav) {
                faveButtonImg.setImageResource(R.drawable.ic_tickfave)
                faveButtonText.text = "Remove Favorite"
            } else {
                faveButtonImg.setImageResource(R.drawable.ic_addfave)
                faveButtonText.text = "Add Favorites"
            }
        }

        applyIcon()

        faveButton.setOnClickListener {
            val isFav = db.isFavoriteShow(userId, showId, type)
            if (isFav) {

                db.removeFavoriteShow(userId, showId, type)
                applyIcon()

            } else {
                db.addFavoriteShow(
                    userId = userId,
                    showId = showId,
                    type = type,
                    title = title,
                    rating = voteAverage,
                    genres = genres,
                    overview = overview,
                    runtime = runtime,
                    year = year,
                    voteCount = voteCount,
                    pg = pg,
                    poster = poster,
                    backdrop = backdrop ,
                    noOfSeason = noOfSeason,
                    lastSeason =lastSeason,
                    lastEpisode =lastEpisode
                )
                applyIcon()
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun showServerDialog() {

        val servers = listOf(
            "VidSrc",
            "Embed API",
            "2Embed",
            "Embed.su",
            "PrimeWire",
            "vidking"
        )

        val builder = android.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle("Select a Streaming Server (Powered by Third Parties)")

            .setSingleChoiceItems(servers.toTypedArray(), GlobalUtils.getSavedServerIndex(this)) { dialog, which ->

                // Save selection immediately
                GlobalUtils.saveServerIndex(this, which)
                currentServerIndex = which

                Toast.makeText(this, "Server: ${servers[currentServerIndex]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Auto-close dialog
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



}
