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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class TMDBapi(private val context: Context) {

    fun fetchLogos (type:String, tmdbId:String, widget:ImageView, widget2: View){
        CoroutineScope(Dispatchers.IO).launch {
            val logosUrl = " https://api.themoviedb.org/3/$type/$tmdbId/images"
            val logosConnection = URL(logosUrl).openConnection() as HttpURLConnection
            logosConnection.requestMethod = "GET"
            logosConnection.setRequestProperty("accept", "application/json")
            logosConnection.setRequestProperty(
                "Authorization",
                "Bearer ${BuildConfig.TM_K}"
            )

            val logosResponse = logosConnection.inputStream.bufferedReader().use { it.readText() }
            val jsonObjectImg = JSONObject(logosResponse)

            Log.e("DEBUG_Watch_Images", jsonObjectImg.toString())
            val logos = jsonObjectImg.getJSONArray("logos")
            Log.e("DEBUG_Watch_Images", logos.toString())

            for (i in 0 until logos.length()) {
                val logo = logos.getJSONObject(i)
                val logoUrl = "https://image.tmdb.org/t/p/original/"  + logo.getString("file_path")
                val languageCode = logo.getString("iso_639_1") //en


                val width = logo.getString("width")

                if (languageCode == "en") {

                    withContext(Dispatchers.Main) {

                        Glide.with(context)
                            .load(logoUrl)
                            .centerCrop()
                            .fitCenter()
                            .into(widget)

                        widget2.visibility = View.GONE

                    }
                    break

                }else {
                    try {
                       withContext(Dispatchers.Main) {

                                Glide.with(context)
                                    .load(logoUrl)
                                    .centerCrop()
                                    .fitCenter()
                                    .into(widget)

                                widget2.visibility = View.GONE
                       }
                    }catch (e: IOException) {
                      widget2.visibility = View.VISIBLE
                    }
                }

            }
        }
    }




}


