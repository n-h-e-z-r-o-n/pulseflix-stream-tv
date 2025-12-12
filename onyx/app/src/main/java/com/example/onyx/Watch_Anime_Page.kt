package com.example.onyx

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
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
import com.example.onyx.BuildConfig
import com.example.onyx.Database.AppDatabase
import com.example.onyx.Database.SessionManger
import java.io.IOException


class Watch_Anime_Page : AppCompatActivity() {
    private var urlHome = BuildConfig.A_K
    private lateinit var db: AppDatabase
    private lateinit var  sm: SessionManger

    private lateinit var poster :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalUtils.applyTheme(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_watch_anime_page)

        db = AppDatabase(this)
        sm = SessionManger(this)

        LoadingAnimation.setup(this@Watch_Anime_Page, R.raw.b)

        val animeCode = intent.getStringExtra("anime_code")

        Log.e("ANIME_Watch id", animeCode.toString())

        //http://192.168.100.22:4000/api/v2/hianime/anime/$animeCode/episodes

        //getInfo("my-status-as-an-assassin-obviously-exceeds-the-heros-19922")
        getInfo(animeCode.toString())

    }



    private fun getInfo(id: String){

        LoadingAnimation.show(this@Watch_Anime_Page)

        CoroutineScope(Dispatchers.IO).launch {
            repeat(1) { attempt ->
                try {

                    val url = "$urlHome/api/v2/hianime/anime/$id"

                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)
                    val data = jsonObject.getJSONObject("data")

                    Log.e("ANIME_Watch Data", data.toString())



                    val id = data.getJSONObject("anime").getJSONObject("info").getString("id")
                    val anilistId = data.getJSONObject("anime").getJSONObject("info").getString("anilistId")
                    val malId = data.getJSONObject("anime").getJSONObject("info").getString("malId")
                    val name = data.getJSONObject("anime").getJSONObject("info").getString("name")
                    poster = data.getJSONObject("anime").getJSONObject("info").getString("poster")
                    val description = data.getJSONObject("anime").getJSONObject("info").getString("description")
                    val rating = data.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getString("rating")
                    val quality = data.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getString("quality")
                    val type = data.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getString("type")
                    val duration = data.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getString("duration")
                    val sub = data.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getJSONObject("episodes").optString("sub", "")
                    val dub = data.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getJSONObject("episodes").optString("dub", "")
                    val aired = data.getJSONObject("anime").getJSONObject("moreInfo").getString("aired")
                    //val status = data.getJSONObject("anime").getJSONObject("moreInfo").getString("status")
                    //val studios = data.getJSONObject("anime").getJSONObject("moreInfo").getString("studios")
                    val genresArray = data.getJSONObject("anime").getJSONObject("moreInfo").getJSONArray("genres")
                    var genre = ""
                    for (i in 0 until genresArray.length()) {
                        genre = genre +" ~ " +genresArray.getString(i)
                    }

                    val  seasons = data.getJSONArray("seasons")?: JSONArray()
                    val  relatedAnimes = data.getJSONArray("relatedAnimes")?: JSONArray()
                    val  recommendedAnime = data.getJSONArray("recommendedAnimes")?: JSONArray()

                    val saveData  = jsonObject.getJSONObject("data")
                    saveData.remove("recommendedAnimes")
                    saveData.remove("relatedAnimes")
                    saveData.remove("mostPopularAnimes")




                    withContext(Dispatchers.Main) {

                        findViewById<TextView>(R.id.watchTitle).text = name
                        findViewById<TextView>(R.id.watchRating).text = rating
                        findViewById<TextView>(R.id.watchRuntime).text = duration
                        findViewById<TextView>(R.id.watchType).text = type
                        findViewById<TextView>(R.id.watchQuality).text = quality
                        findViewById<TextView>(R.id.watchSub).text = sub
                        findViewById<TextView>(R.id.watchDub).text = dub
                        findViewById<TextView>(R.id.watchYear).text = aired
                        findViewById<TextView>(R.id.watchOverview).text = description
                        findViewById<TextView>(R.id.watchGenres).text = genre

                        val posterWidget = findViewById<ImageView>(R.id.WatchImage)
                        Glide.with(posterWidget.context)
                            .load(poster)
                            .fitCenter()
                            .into(posterWidget)


                        if (seasons.length() > 0){
                            createSeasonButtons(seasons.length(), seasons )
                            findViewById<TextView>(R.id.SeasonTitle).visibility = View.VISIBLE
                        }else{
                            findViewById<TextView>(R.id.SeasonTitle).visibility = View.GONE
                            getEpisodes(id)
                        }

                        showRecommendation(relatedAnimes, recommendedAnime)
                        LoadingAnimation.hide(this@Watch_Anime_Page)


                        setupFavoriteButton(
                            animeId = id,
                            name = name,
                            type = type,
                            anilistId = anilistId,
                            malId = malId,
                            description = description,
                            rating = rating,
                            quality = quality,
                            duration = duration,
                            poster = poster,
                            sub=sub,
                            dub=dub,
                            aired=aired,
                            genre=genre,
                            seasons = seasons.toString()
                        )

                    }

                    return@launch

                } catch (e: java.net.UnknownHostException) {
                    Log.e("ANIME_Watch", "No Internet Connection", e)
                    withContext(Dispatchers.Main) {
                        LoadingAnimation.setup(this@Watch_Anime_Page, R.raw.error)
                    }

                } catch (e: java.net.SocketTimeoutException) {
                    // Server timeout
                    Log.e("ANIME_Watch", "Request timed out", e)
                    withContext(Dispatchers.Main) {
                        LoadingAnimation.show(this@Watch_Anime_Page)
                    }
                    return@repeat

                } catch (e: javax.net.ssl.SSLException) {
                    Log.e("ANIME_Watch", "SSL Error", e)
                    withContext(Dispatchers.Main) {
                        LoadingAnimation.setup(this@Watch_Anime_Page, R.raw.error)
                    }
                    return@repeat
                } catch (e: IOException) {
                    Log.e("ANIME_Watch", "Network IO Error", e)
                    withContext(Dispatchers.Main) {
                        LoadingAnimation.setup(this@Watch_Anime_Page, R.raw.error)
                    }
                    return@repeat
                } catch (e: Exception) {
                    delay(20_000)
                    Log.e("ANIME_Watch 1", "Error fetching data", e)
                    withContext(Dispatchers.Main) {
                        LoadingAnimation.show(this@Watch_Anime_Page)
                    }
                }
            }
        }
    }



    private fun createSeasonButtons(
        noOfSeasons: Int,
        seasonData: JSONArray
    ) {
        val container = findViewById<LinearLayout>(R.id.anime_season_selector_container)
        container.removeAllViews()

        val inflater = LayoutInflater.from(this)

        for (i in 0 until noOfSeasons) {
            val season = seasonData.getJSONObject(i)

            val cardView = inflater.inflate(R.layout.anime_season_item, container, false) as CardView
            val seasonTitle = cardView.findViewById<TextView>(R.id.SeasonTitle)
            val seasonImage = cardView.findViewById<ImageView>(R.id.SeasonImage)

            val title = season.optString("title", "Season ${i + 1}")
            val imageUrl = season.optString("poster", "")
            val season_id = season.optString("id", "")

            seasonTitle.text = title

            if (imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(seasonImage)
            }

            cardView.setOnClickListener {

                /*
                val backdrop = findViewById<ImageView>(R.id.backdrop)

                Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(backdrop)

                 */


                val selected_seasonShow = findViewById<TextView>(R.id.selected_seasonShow)
                selected_seasonShow.text = "List of episodes ($title)"

                getEpisodes(season_id)
            }

            container.addView(cardView)
        }
    }
    @OptIn(UnstableApi::class)
    private fun getEpisodes(season_id: String){
        CoroutineScope(Dispatchers.IO).launch {
            repeat(1) { attempt ->
                try {

                    val url = "$urlHome/api/v2/hianime/anime/$season_id/episodes"

                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)
                    val data = jsonObject.getJSONObject("data")
                    val  episodes = data.getJSONArray("episodes")

                    Log.e("Watch_Anime_Page 1", episodes.toString())

                    withContext(Dispatchers.Main) {

                        val container = findViewById<LinearLayout>(R.id.anime_episodes_selector_container)
                        container.removeAllViews()
                        val inflater = LayoutInflater.from(this@Watch_Anime_Page)

                        for (i in 0 until episodes.length()) {
                            val episode = episodes.getJSONObject(i)

                            val cardView = inflater.inflate(R.layout.anime_item_episode, container, false) as FrameLayout
                            val epTitle = cardView.findViewById<TextView>(R.id.episode_name)
                            val epNumber = cardView.findViewById<TextView>(R.id.episode_Number)

                            val eTitle = episode.optString("title", "${i + 1}")
                            val eNumber = episode.optString("number", "")
                            val episodeId = episode.optString("episodeId", "")


                            epTitle.text = eTitle
                            epNumber.text = eNumber


                            cardView.setOnClickListener {
                                Log.e("ANIME_episodeId ", "episodeId: $episodeId")

                                 Anime_Video_Player.playVideoExternally(this@Watch_Anime_Page, episodeId, episodes, eNumber, poster, season_id, eTitle)

                            }

                            container.addView(cardView)
                        }

                    }


                    return@launch
                } catch (e: Exception) {
                    delay(20_000)
                    Log.e("ANIME_STATUS HOME 1", "Error fetching data", e)
                }
            }
        }
    }


    private fun showRecommendation(data: JSONArray, data2: JSONArray) {

        // ✅ Merge the two JSONArrays
        val Airing = JSONArray()
        for (i in 0 until data.length()) {
            Airing.put(data.getJSONObject(i))
        }
        for (i in 0 until data2.length()) {
            Airing.put(data2.getJSONObject(i))
        }

        var RecommendationItems = mutableListOf<AiringAnimeItem>()

        for (i in 0 until Airing.length()) {


            val item = Airing.getJSONObject(i)

            val title = item.getString("name")

            val imageUrl = item.getString("poster")

            val id = item.getString("id")

            val type = item.getString("type")

            val sub = item.getJSONObject("episodes").optString("sub", "")
            val dub = item.getJSONObject("episodes").optString("dub", "")


            RecommendationItems.add(
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


        val recyclerView = findViewById<RecyclerView>(R.id.animeWatchRecommendation)
        recyclerView.layoutManager = GridLayoutManager(this@Watch_Anime_Page, GlobalUtils.calculateSpanCount(this@Watch_Anime_Page, 170))
        recyclerView.adapter = AnimeAiringAdapter(RecommendationItems, R.layout.anime_airing_item)

        val spacing = (19 * resources.displayMetrics.density).toInt() // 16dp to px
        recyclerView.addItemDecoration(EqualSpaceItemDecoration(spacing))

    }

    private fun streamingLink(episodeId: String){
        CoroutineScope(Dispatchers.IO).launch {
            repeat(1) { attempt ->
                try {

                    val url_servers = "$urlHome/api/v2/hianime/episode/servers?animeEpisodeId=$episodeId"
                    val connection_servers = URL(url_servers).openConnection() as HttpURLConnection
                    connection_servers.requestMethod = "GET"
                    connection_servers.setRequestProperty("accept", "application/json")
                    val response_server = connection_servers.inputStream.bufferedReader().use { it.readText() }
                    val jsonObjectServerInfo = org.json.JSONObject(response_server)
                    val dataServers = jsonObjectServerInfo.getJSONObject("data")

                    val  subServers = dataServers.getJSONArray("sub")
                    val  dubServers = dataServers.getJSONArray("dub")
                    val  rawServers = dataServers.getJSONArray("raw")




                    val url ="$urlHome/api/v2/hianime/episode/sources?animeEpisodeId=$episodeId?server={server}&category={dub || sub || raw}"


                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)
                    val data = jsonObject.getJSONObject("data")
                    val  episodes = data.getJSONArray("episodes")

                    Log.e("Watch_Anime_Page 1", episodes.toString())

                    withContext(Dispatchers.Main) {


                    }


                    return@launch
                } catch (e: Exception) {
                    delay(20_000)

                }
            }
        }
    }

    private fun setupFavoriteButton(
        animeId :String,
        name:String,
        type :String,
        anilistId :String,
        malId :String,
        description:String,
        rating :String,
        quality :String,
        duration :String,
        poster :String,
        sub:String,
        dub:String,
        aired:String,
        genre:String,
        seasons:String,


    ) {
        val userId = sm.getUserId()
        val favoriteButton =  findViewById<LinearLayout>(R.id.favoriteButton)
        val favoriteButtonImg =  findViewById<ImageButton>(R.id.favoriteButtonImg)
        val favoriteButtonText =  findViewById<TextView>(R.id.favoriteButtonText)





        @RequiresApi(Build.VERSION_CODES.O)
        fun applyIcon() {
            val isFav = db.isFavoriteAnime(userId, animeId)
            if (isFav) {
                favoriteButtonImg.setImageResource(R.drawable.ic_tickfave)
                favoriteButtonText.text = "Remove from Fav"
            } else {
                favoriteButtonImg.setImageResource(R.drawable.ic_addfave)
                favoriteButtonText.text= "Add to Fav"
            }
        }

        applyIcon()

        favoriteButton.setOnClickListener {
            val isFav = db.isFavoriteAnime(userId, animeId)
            if (isFav) {
                db.removeFavoriteAnime(userId, animeId)
            } else {
                db.addFavoriteAnime(
                    userId,
                    animeId,
                    name,
                    type,
                    anilistId,
                    malId,
                    description,
                    rating,
                    quality,
                    duration,
                    poster,
                    sub,
                    dub,
                    aired,
                    genre,
                    seasons
                )
            }
            applyIcon()
        }
    }



}