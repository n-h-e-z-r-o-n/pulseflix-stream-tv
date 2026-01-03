package com.example.onyx

import android.animation.ValueAnimator
import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
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
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView

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

     fun dpToPx(dp: Int, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
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


    fun saveServerIndex(context: Context, index: Int) {
        val prefs = context.getSharedPreferences("server_prefs", MODE_PRIVATE)
        prefs.edit().putInt("selected_server_index", index).apply()
    }

    fun getSavedServerIndex(context: Context): Int {
        val prefs = context.getSharedPreferences("server_prefs", MODE_PRIVATE)
        Log.e("DEBUG_SERVER", prefs.getInt("selected_server_index", 0).toString())
        return prefs.getInt("selected_server_index", 0)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    fun setupCardStackFromContainer(
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
                        .translationX(dp(container.context,0f))
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
        //container.getChildAt(container.childCount - 1)?.requestFocus()
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
                .translationX(dp(container.context,tx))
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
            .translationXBy(dp(container.context,-250f))
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
            .translationXBy(dp(container.context,-250f))
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

    private fun dp(context: Context, value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics
        )
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun expandParentOnChildFocus(
        parent: View,
        expandedWidthDp: Float,
        collapsedWidthDp: Float,
        animationDuration: Long = 200L
    ) {
        val context = parent.context
        val expandedWidthPx = dp(context, expandedWidthDp).toInt()
        val collapsedWidthPx = dp(context, collapsedWidthDp).toInt()

        fun animateWidth(targetWidth: Int) {
            val startWidth = parent.width
            if (startWidth == targetWidth) return

            ValueAnimator.ofInt(startWidth, targetWidth).apply {
                duration = animationDuration
                addUpdateListener {
                    parent.layoutParams = parent.layoutParams.apply {
                        width = it.animatedValue as Int
                    }
                    parent.requestLayout()
                }
                start()
            }
        }

        fun checkFocus() {
            if (parent.hasFocus()) {
                animateWidth(expandedWidthPx)
            } else {
                animateWidth(collapsedWidthPx)
            }
        }

        // Watch all children
        fun attachFocusListeners(view: View) {
            view.setOnFocusChangeListener { _, _ ->
                parent.post { checkFocus() }
            }

            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    attachFocusListeners(view.getChildAt(i))
                }
            }
        }

        attachFocusListeners(parent)
    }





}
