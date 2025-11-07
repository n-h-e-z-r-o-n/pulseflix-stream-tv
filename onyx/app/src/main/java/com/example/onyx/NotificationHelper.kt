// NotificationHelper.kt
package com.example.onyx

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.String
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.onyx.BuildConfig

object NotificationHelper {

    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_NOTIFICATIONS = "notifications_json"

    val results = mutableListOf<NotificationItem>()
    
    // Callback to notify when notifications are updated
    private var onNotificationsUpdated: (() -> Unit)? = null
    
    fun setOnNotificationsUpdatedCallback(callback: () -> Unit) {
        onNotificationsUpdated = callback
    }

    data class NotificationState(
        val season: Int,
        val episode: Int
    )

    fun checkNotifications(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            getNotifications(context)
        }
    }

    fun checkNotificationsWithBadge(activity: Activity) {
        val badge = activity.findViewById<View>(R.id.notificationBadge) // CardView badge

        CoroutineScope(Dispatchers.IO).launch {
            while (true) { // 🔁 infinite loop to periodically check
                val notificationsJson = getNotifications(activity)
                val uniqueNotifications = notificationsJson.distinctBy { it.imdbCode }

                Log.e("NotificationHelper", "AutoCheck ${uniqueNotifications}")

                val file = File(activity.cacheDir, "notifications.json")
                val gson = Gson()
                val jsonString = gson.toJson(uniqueNotifications)
                Log.e("NotificationHelper", "file AutoCheck ${jsonString}")
                file.writeText(jsonString)

                withContext(Dispatchers.Main) {
                    badge?.visibility = if (uniqueNotifications.isNotEmpty()) View.VISIBLE else View.GONE
                }

                delay(18000_000) // ⏳ wait 18 seconds before checking again
            }
        }
    }

    fun loadNotifications(context: Activity): List<NotificationItem> {
        val file = File(context.cacheDir, "notifications.json")
        if (!file.exists()) return emptyList()

        val jsonString = file.readText()
        val gson = Gson()
        val type = object : TypeToken<List<NotificationItem>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun getNotifications(context: Context) : List<NotificationItem>{
        // Clear previous results to avoid duplicates
        results.clear()
        
        val list = FavoritesManager.getFavorites(context).toMutableList()

        // keep only TV shows
        val tvList = list.filter { obj ->
            obj.has("first_air_date") && !obj.optString("first_air_date").isNullOrEmpty()
        }

        val animeList = list.filter { obj ->
            obj.has("anime")
        }

        Log.e("NotificationHelper", "animeList:  ${animeList.toString()}")

        for (item in animeList) {
            val animeId = item.getJSONObject("anime").getJSONObject("info").getString("id")
            val name = item.getJSONObject("anime").getJSONObject("info").getString("name")
            val poster = item.getJSONObject("anime").getJSONObject("info").getString("poster")

            //val subStored = item.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getJSONObject("episodes").optString("sub", "").toIntOrNull() ?: 0
            //val dubStored = item.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getJSONObject("episodes").optString("dub", "").toIntOrNull() ?: 0
            //val seasonsStored  = item.getJSONArray("seasons").length()


            val savedData = getStoredNotification(context, animeId)
            val subStored = savedData?.season ?: 0
            val dubStored = savedData?.episode ?: 0


            val url = "${BuildConfig.A_K}/api/v2/hianime/anime/$animeId"

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("accept", "application/json")
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonObject = org.json.JSONObject(response)
            val data = jsonObject.getJSONObject("data")

            val subFetched = data.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getJSONObject("episodes").optString("sub", "").toIntOrNull() ?: 0
            val dubFetched = data.getJSONObject("anime").getJSONObject("info").getJSONObject("stats").getJSONObject("episodes").optString("dub", "").toIntOrNull() ?: 0
            //val seasonsFetched = data.getJSONArray("seasons").length()

            var info  = ""

            if (subFetched > subStored) {
                val cal = subFetched - subStored
                info = info + "$cal SUB added\n"
            }
            if (dubFetched > dubStored) {
                val cal = dubFetched - dubStored
                info = info + "$cal DUB added\n"
            }



            if (info.isEmpty()) {
                continue
            }
            val itemData = NotificationItem(
                imdbCode = animeId,
                title = name,
                imageUrl = poster,
                info = info,
                type = "anime",
                newSeason = subFetched,
                newEpisode = dubFetched
            )
            results.add(itemData)

        }

        for (item in tvList) {

            try {
                val tvId = item.optString("id", "")
                val name = item.optString("name", "")
                val poster = item.optString("poster_path", "")

                //Log.e("NotificationHelper", "ID:  $tvId")
                if (tvId.isEmpty()) continue



                val tvUrl = "https://api.themoviedb.org/3/tv/$tvId?language=en-US"
                val connection = URL(tvUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("accept", "application/json")
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer ${BuildConfig.TM_K}"
                )

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                //Log.e("NotificationHelper", "jsonObject:  ${response.toString()}")

                val lastEpisode = jsonObject.getJSONObject("last_episode_to_air")
                val newSeason = lastEpisode.optInt("season_number")
                val newEpisode = lastEpisode.optInt("episode_number")

                //Log.e("NotificationHelper", "newSeason:  $newSeason")
                //Log.e("NotificationHelper", "newEpisode:  $newEpisode")

                // Load stored state for this TV
                val savedData = getStoredNotification(context, tvId)
                //Log.e("NotificationHelper", "savedData:  ${savedData.toString()}")

                val lastSeason = savedData?.season ?: 0
                val lastEp = savedData?.episode ?: 0

                val info: String
                val episodeDiff: Int

                if (newSeason > lastSeason) {
                    episodeDiff = newEpisode
                    info = "New Season ($newSeason) with $episodeDiff episodes"
                } else if (newSeason == lastSeason && newEpisode > lastEp) {
                    episodeDiff = newEpisode - lastEp
                    info = "$episodeDiff New Episode${if (episodeDiff > 1) "s" else ""}"


                } else {
                    // No update → skip this show
                    continue
                }
                //Log.e("NotificationHelper", "$name $info")

                val itemData = NotificationItem(
                    imdbCode = tvId,
                    title = name,
                    imageUrl = "https://image.tmdb.org/t/p/w500$poster",
                    info = info,
                    type = "tv",
                    newSeason = newSeason,
                    newEpisode = newEpisode
                )
                results.add(itemData)

                // Update local stored state
                //updateNotification(context, tvId, newSeason, newEpisode)


            } catch (e: Exception) {
                Log.e("NotificationHelper", "Error for item: ${e.message}")
            }
        }
        return results
    }

    fun updateNotification(context: Context, id: String, newSeason: Int, newEpisode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTIFICATIONS, "{}") ?: "{}"
        val root = JSONObject(json)

        val obj = JSONObject()
        obj.put("season", newSeason)
        obj.put("episode", newEpisode)

        root.put(id, obj)

        prefs.edit().putString(KEY_NOTIFICATIONS, root.toString()).apply()
        
        // Refresh the cached notifications file and trigger UI update
        CoroutineScope(Dispatchers.IO).launch {
            val updatedNotifications = getNotifications(context)
            val uniqueNotifications = updatedNotifications.distinctBy { it.imdbCode }
            
            val file = File(context.cacheDir, "notifications.json")
            val gson = Gson()
            val jsonString = gson.toJson(uniqueNotifications)
            file.writeText(jsonString)
            
            // Trigger UI refresh callback
            onNotificationsUpdated?.invoke()
        }
    }

    private fun getStoredNotification(context: Context, id: String): NotificationState? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTIFICATIONS, "{}") ?: "{}"
        val root = JSONObject(json)

        if (!root.has(id)) return null

        val obj = root.getJSONObject(id)
        return NotificationState(
            season = obj.optInt("season", 0),
            episode = obj.optInt("episode", 0)
        )
    }
}

/////




