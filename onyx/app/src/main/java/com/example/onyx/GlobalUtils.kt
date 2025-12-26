package com.example.onyx

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.TextView
import kotlin.random.Random
import android.text.Spanned
import android.text.style.ForegroundColorSpan

object GlobalUtils {
    
    // SharedPreferences key constants
    private const val PREF_NAME = "OnyxProfile"
    private const val KEY_MOVIES_WATCHED = "movies_watched"
    private const val KEY_SERIES_WATCHED = "series_watched"
    private const val KEY_AUTO_PLAY = "auto_play"
    private const val KEY_NOTIFICATIONS = "notifications"
    private const val KEY_VIDEO_QUALITY = "video_quality"
    private const val KEY_APP_THEME  = "app_theme"

    // Default values
    private const val DEFAULT_VIDEO_QUALITY = "1080p"
    private const val DEFAULT_THEME = "dark"
    

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    // ==================== STATISTICS MANAGEMENT ====================
    
    /**
     * Increment movies watched counter
     */
    fun incrementMoviesWatched(context: Context) {
        val prefs = getSharedPreferences(context)
        val currentCount = prefs.getInt(KEY_MOVIES_WATCHED, 0)
        prefs.edit().putInt(KEY_MOVIES_WATCHED, currentCount + 1).apply()
        Log.d("GlobalUtils", "Movies watched incremented to: ${currentCount + 1}")
    }
    
    /**
     * Increment series watched counter
     */
    fun incrementSeriesWatched(context: Context) {
        val prefs = getSharedPreferences(context)
        val currentCount = prefs.getInt(KEY_SERIES_WATCHED, 0)
        prefs.edit().putInt(KEY_SERIES_WATCHED, currentCount + 1).apply()
        Log.d("GlobalUtils", "Series watched incremented to: ${currentCount + 1}")
    }
    
    /**
     * Get movies watched count
     */
    fun getMoviesWatched(context: Context): Int {
        return getSharedPreferences(context).getInt(KEY_MOVIES_WATCHED, 0)
    }
    
    /**
     * Get series watched count
     */
    fun getSeriesWatched(context: Context): Int {
        return getSharedPreferences(context).getInt(KEY_SERIES_WATCHED, 0)
    }


    
    /**
     * Reset all statistics
     */
    fun resetStatistics(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .putInt(KEY_MOVIES_WATCHED, 0)
            .putInt(KEY_SERIES_WATCHED, 0)
            .apply()
        Log.d("GlobalUtils", "Statistics reset")
    }
    
    // ==================== SETTINGS MANAGEMENT ====================
    
    /**
     * Set auto-play setting
     */
    fun setAutoPlay(context: Context, enabled: Boolean) {
        getSharedPreferences(context).edit().putBoolean(KEY_AUTO_PLAY, enabled).apply()
        Log.d("GlobalUtils", "Auto-play set to: $enabled")
    }
    
    /**
     * Get auto-play setting
     */
    fun isAutoPlayEnabled(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_AUTO_PLAY, true)
    }
    
    /**
     * Set notifications setting
     */
    fun setNotifications(context: Context, enabled: Boolean) {
        getSharedPreferences(context).edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
        Log.d("GlobalUtils", "Notifications set to: $enabled")
    }
    
    /**
     * Get notifications setting
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_NOTIFICATIONS, true)
    }
    
    /**
     * Set video quality setting
     */
    fun setVideoQuality(context: Context, quality: String) {
        getSharedPreferences(context).edit().putString(KEY_VIDEO_QUALITY, quality).apply()
        Log.d("GlobalUtils", "Video quality set to: $quality")
    }
    
    /**
     * Get video quality setting
     */
    fun getVideoQuality(context: Context): String {
        return getSharedPreferences(context).getString(KEY_VIDEO_QUALITY, DEFAULT_VIDEO_QUALITY) ?: DEFAULT_VIDEO_QUALITY
    }

    // ==================== THEME MANAGEMENT ====================

    // List of your theme keys
    private val availableThemes = listOf(
        "dark",
        "light",
        "amoled",
        "highContrast",
        "green",
        "red",
        "purple"
    )
    fun getAvailableThemes(): List<String> = availableThemes

    fun getAppTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_APP_THEME, "dark") ?: "dark"
    }

    fun setAppTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_APP_THEME, theme).apply()
    }


    fun applyTheme(activity: Activity) {
        when (getAppTheme(activity)) {
            "dark" -> activity.setTheme(R.style.Theme_Onyx_Dark)
            "light"  -> activity.setTheme(R.style.Theme_Onyx_Light)
            "amoled" -> activity.setTheme(R.style.Theme_Onyx_Amoled)
            "highContrast" -> activity.setTheme(R.style.Theme_Onyx_HighContrast)
            "green" -> activity.setTheme(R.style.Theme_Onyx_Green)
            "red" -> activity.setTheme(R.style.Theme_Onyx_Red)
            "purple" -> activity.setTheme(R.style.Theme_Onyx_Purple)
            else     -> activity.setTheme(R.style.Theme_Onyx_Dark)
        }
    }
    
    // ==================== FAVORITES MANAGEMENT ====================
    

    
    // ==================== CACHE MANAGEMENT ====================
    
    /**
     * Clear app cache
     */
    fun clearAppCache(context: Context): Boolean {
        return try {
            // Clear internal cache
            val cacheDir = context.cacheDir
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }
            
            // Clear external cache if available
            val externalCacheDir = context.externalCacheDir
            if (externalCacheDir?.exists() == true) {
                externalCacheDir.deleteRecursively()
            }
            
            Log.d("GlobalUtils", "Cache cleared successfully")
            true
        } catch (e: Exception) {
            Log.e("GlobalUtils", "Failed to clear cache", e)
            false
        }
    }
    
    // ==================== UTILITY FUNCTIONS ====================
    
    ///  Get app version name

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"   // fallback if null
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    // ==================== APP MANAGEMENT ====================
    


    ///Restart the application
    fun restartApp(context: Context) {
        try {
            Log.d("GlobalUtils", "Restarting application...")
            
            // Get the main activity class
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            
            if (intent != null) {
                // Clear the task stack and start fresh
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                
                // Kill the current process
                Process.killProcess(Process.myPid())
            } else {
                Log.e("GlobalUtils", "Could not get launch intent for package: ${context.packageName}")
            }
        } catch (e: Exception) {
            Log.e("GlobalUtils", "Error restarting app", e)
        }
    }


    fun formatRuntime(totalMinutes: Int): String {
        if (totalMinutes <= 0) return ""

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return buildString {
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes} min")
        }.trim()
    }


    ///Checks if the app is running on a TV device.
    fun isTv(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    fun calculateSpanCount(context: Context, itemWidthDp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthPx = displayMetrics.widthPixels
        val itemWidthPx = (itemWidthDp * displayMetrics.density).toInt()
        return (screenWidthPx / itemWidthPx).coerceAtLeast(1)
    }

    fun calculateSpanCountV2(
        context: Context,
        itemWidthDp: Int,
        reservedWidthDp: Int = 0
    ): Int {
        val displayMetrics = context.resources.displayMetrics

        val screenWidthPx = displayMetrics.widthPixels

        // Convert dp → px
        val itemWidthPx = (itemWidthDp * displayMetrics.density).toInt()
        val reservedWidthPx = (reservedWidthDp * displayMetrics.density).toInt()

        // Available screen width after subtracting sidebar / margin / extra UI
        val availableWidthPx = (screenWidthPx - reservedWidthPx).coerceAtLeast(0)

        // Calculate span count
        return (availableWidthPx / itemWidthPx).coerceAtLeast(1)
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    fun scrambleToText(
        textView: TextView,
        finalText: String,
        speed: Int = 400
    ) {
        val chars = "▫▪■□"
        val handler = Handler(Looper.getMainLooper())

        data class QueueItem(
            val to: Char,
            var char: Char = ' ',
            var done: Boolean = false,
            var frame: Int = 0,
            val maxFrames: Int
        )

        val queue = finalText.map {
            QueueItem(
                to = it,
                maxFrames = Random.nextInt(speed, speed * 2)
            )
        }

        fun randomChar(): Char =
            chars[Random.nextInt(chars.length)]

        fun update() {
            val sb = SpannableStringBuilder()
            var complete = 0

            queue.forEach { q ->
                if (q.done) {
                    sb.append(q.to)
                    complete++
                } else {
                    if (q.frame >= q.maxFrames) {
                        q.done = true
                        sb.append(q.to)
                    } else {
                        if (q.frame == 0 || Random.nextFloat() < 0.5f) {
                            q.char = randomChar()
                        }

                        val start = sb.length
                        sb.append(q.char)

                        q.frame++
                    }
                }
            }

            textView.text = sb

            if (complete < queue.size) {
                handler.postDelayed({ update() }, 16L) // ~60fps
            }
        }

        update()
    }






}
