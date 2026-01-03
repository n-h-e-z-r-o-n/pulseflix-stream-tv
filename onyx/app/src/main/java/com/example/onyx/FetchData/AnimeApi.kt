package com.example.onyx.FetchData
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.onyx.BuildConfig
import com.example.onyx.R
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import kotlin.text.ifEmpty

import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.net.HttpURLConnection
import java.net.URL

class AnimeApi(private val context: Context)  {

    //Anime Home
    fun animeHome(): JSONObject? {
        return runBlocking {
            async(Dispatchers.IO) {
                try {
                    val url = "${BuildConfig.A_K}/api/v2/hianime/home"

                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)
                    //val data = jsonObject.getJSONObject("data")

                    jsonObject

                } catch (e: IOException) {
                    Log.e("fetchAnimeData", "Network error: ${e.message}")
                    null
                } catch (e: JSONException) {
                    Log.e("fetchAnimeData", "JSON error: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("fetchAnimeData", "Unexpected error: ${e.message}")
                    null
                }
            }.await()
        }
    }

    //Anime About Info
    fun animeInfo(animeId: String): JSONObject? {
        return runBlocking {
            async(Dispatchers.IO) {
                try {
                    val url = "${BuildConfig.A_K}/api/v2/hianime/anime/$animeId"

                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)
                    //val data = jsonObject.getJSONObject("data")


                    jsonObject

                } catch (e: IOException) {
                    Log.e("fetchAnimeData", "Network error: ${e.message}")
                    null
                } catch (e: JSONException) {
                    Log.e("fetchAnimeData", "JSON error: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("fetchAnimeData", "Unexpected error: ${e.message}")
                    null
                }
            }.await()
        }
    }

    //Anime Episodes
    fun animeEpisodes(season_id: String): JSONObject? {
        return runBlocking {
            async(Dispatchers.IO) {
                try {
                    val url = "${BuildConfig.A_K}/api/v2/hianime/anime/$season_id/episodes"

                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)

                    jsonObject

                } catch (e: IOException) {
                    Log.e("fetchAnimeData", "Network error: ${e.message}")
                    null
                } catch (e: JSONException) {
                    Log.e("fetchAnimeData", "JSON error: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("fetchAnimeData", "Unexpected error: ${e.message}")
                    null
                }
            }.await()
        }
    }

    //GET Anime Episode Servers
    fun animeEpisodeServers(animeEpisodeId: String): JSONObject? {
        return runBlocking {
            async(Dispatchers.IO) {
                try {
                    val url = "${BuildConfig.A_K}/api/v2/hianime/episode/servers?animeEpisodeId=$animeEpisodeId"

                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)

                    jsonObject

                } catch (e: IOException) {
                    Log.e("fetchAnimeData", "Network error: ${e.message}")
                    null
                } catch (e: JSONException) {
                    Log.e("fetchAnimeData", "JSON error: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("fetchAnimeData", "Unexpected error: ${e.message}")
                    null
                }
            }.await()
        }
    }

    //GET Anime Episode Streaming Links
    fun animeEpisodeStreamingLinks(episodeId: String, serverName: String, category: String): JSONObject? {
        return runBlocking {
            async(Dispatchers.IO) {
                try {
                    val url = "${BuildConfig.A_K}/api/v2/hianime/episode/sources?animeEpisodeId=$episodeId&server=$serverName&category=$category"

                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("accept", "application/json")
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response)

                    jsonObject

                } catch (e: IOException) {
                    Log.e("fetchAnimeData", "Network error: ${e.message}")
                    null
                } catch (e: JSONException) {
                    Log.e("fetchAnimeData", "JSON error: ${e.message}")
                    null
                } catch (e: Exception) {
                    Log.e("fetchAnimeData", "Unexpected error: ${e.message}")
                    null
                }
            }.await()
        }
    }



}