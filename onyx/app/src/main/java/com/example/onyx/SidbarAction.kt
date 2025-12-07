package com.example.onyx

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Typeface
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.onyx.Database.SessionManger

object NavAction {

    private lateinit var mainBox: CardView
    private lateinit var sidcebar: CardView


    fun setupSidebar(activity: Activity) {
        val sidebar = activity.findViewById<FrameLayout>(R.id.sideBar)
        mainBox = activity.findViewById(R.id.mainBox)

        val btnHome = activity.findViewById<ImageButton>(R.id.btnHome)
        //val btnMovies = activity.findViewById<ImageButton>(R.id.btnMovies)
        val btnTvMv = activity.findViewById<ImageButton>(R.id.btnMvTv)
        val btnAnime = activity.findViewById<ImageButton>(R.id.btnAnime)
        val btnFav = activity.findViewById<ImageButton>(R.id.btnFav)
        val btnNotification = activity.findViewById<ImageButton>(R.id.btnNotification)
        val btnProfile = activity.findViewById<ImageView>(R.id.btnProfile) // This is now ImageView


        val labelHome = activity.findViewById<TextView>(R.id.labelHome)
        //val labelMovies = activity.findViewById<TextView>(R.id.labelMovies)
        val labelMvTv = activity.findViewById<TextView>(R.id.labelMvTv)
        val labelAnime = activity.findViewById<TextView>(R.id.labelAnime)
        val labelFav = activity.findViewById<TextView>(R.id.labelFav)
        val labelNotification = activity.findViewById<TextView>(R.id.labelNotification)
        val labelProfile = activity.findViewById<TextView>(R.id.labelProfile)

        // Keep btnProfile separate since it's ImageView, not ImageButton
        val buttons = listOf(btnHome, btnTvMv, btnAnime, btnFav, btnNotification)
        val labels = listOf(labelHome, labelMvTv, labelAnime, labelFav, labelNotification, labelProfile)

        val navigationMap = mapOf<View, Class<*>>(
            btnHome to Home_Page::class.java,
            //btnMovies to Movie_Page::class.java,
            btnTvMv to Shows_Page::class.java,
            btnAnime to Anime_Page::class.java,
            btnFav to Favorite_Page::class.java,
            btnNotification to Notification_Page::class.java,
            btnProfile to Profile_Page::class.java
        )

        // Highlight based on current activity
        val activeView: View? = when (activity) {
            is Home_Page -> btnHome
            is Shows_Page -> btnTvMv
            is Anime_Page -> btnAnime
            is Favorite_Page -> btnFav
            is Notification_Page -> btnNotification
            is Profile_Page -> btnProfile
            else -> btnHome
        }

        // --- MODIFICATION: Call setupBackPressedCallback here if the activity is a ComponentActivity ---
        if (activity is ComponentActivity) {
            setupBackPressedCallback(activity, activeView)
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////
        val sm = SessionManger(activity)         // Initialize database
        val profileImage = activity.findViewById<ImageView>(R.id.btnProfile)
        val assetPath = "file:///android_asset/${sm.getUserAvatar()}"
        Log.e("assetPath sibar", assetPath)

        Glide.with(activity)
            .load(assetPath)
            .placeholder(R.drawable.ic_person) // optional
            .error(R.drawable.ic_person) // optional
            .into(profileImage)

        ////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////

        highlightActive(activeView, buttons + btnProfile)
        activeView?.post { activeView.requestFocus() }          // Request focus on the active button for TV D-pad usability

        navigationMap.forEach { (view, targetClass) ->
            view?.setOnClickListener {
                if (activity::class.java != targetClass) {
                    val intent = Intent(activity, targetClass)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    activity.startActivity(intent)
                }
            }
        }

        // ✅ Add focus scaling effect to each button and btnProfile
        // ✅ Focus animations for buttons + labels

        // First handle the ImageButtons
        buttons.forEachIndexed { index, btn ->
            val label = labels[index]
            btn?.setOnFocusChangeListener { v, hasFocus ->
                handleFocusChange(v, hasFocus, label, sidebar, buttons + btnProfile, labels)
            }
        }

        // Handle btnProfile separately since it's ImageView
        btnProfile?.setOnFocusChangeListener { v, hasFocus ->
            handleFocusChange(v, hasFocus, labelProfile, sidebar, buttons + btnProfile, labels)
        }

        NotificationHelper.checkNotificationsWithBadge(activity)
    }

    private fun handleFocusChange(
        view: View,
        hasFocus: Boolean,
        label: TextView?,
        sidebar: View?,
        allViews: List<View?>,
        allLabels: List<TextView?>
    ) {
        if (hasFocus) {
            // View scaling
            view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).start()

            // Label visible, bold, and scaled
            label?.let {
                it.setTypeface(null, Typeface.BOLD)
            }
        } else {
            // Reset scaling
            view.animate().scaleX(1f).scaleY(1f).setDuration(150).start()

            label?.let {
                it.setTypeface(null, Typeface.NORMAL)
            }
        }

        // Sidebar expand/collapse
        sidebar?.let { bar ->
            val anyFocused = allViews.any { it?.hasFocus() == true }
            val layoutParams = mainBox.layoutParams as ViewGroup.MarginLayoutParams

            val context = view.context
            val density = context.resources.displayMetrics.density

            if (anyFocused) {
                //expandSidebar(view.context, bar, true)

                mainBox.radius = 20 * context.resources.displayMetrics.density
                animateCardViewScale(mainBox, 0.95f, 0.9f)


                val marginLeft = (70 * context.resources.displayMetrics.density).toInt()
                val marginTop = (0 * context.resources.displayMetrics.density).toInt()
                val marginRight = (0 *context.resources.displayMetrics.density).toInt()
                val marginBottom = (0 * context.resources.displayMetrics.density).toInt()
                layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom)


                bar.visibility = View.VISIBLE

            } else {
                //expandSidebar(view.context, bar, false)
                bar.visibility = View.GONE

                val margin0dp = (0 * context.resources.displayMetrics.density).toInt()
                layoutParams.setMargins(margin0dp, margin0dp, margin0dp, margin0dp)

                animateCardViewScale(mainBox, 1f, 1f)

                mainBox.radius = 0f

            }
            mainBox.layoutParams = layoutParams
            mainBox.requestLayout() // Force layout update

        }
    }

    private fun animateCardViewScale(view: View, scaleX: Float, scaleY: Float) {
        view.animate()
            .scaleX(scaleX)
            .scaleY(scaleY)
            .setDuration(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun highlightActive(
        activeView: View?,
        allViews: List<View?>
    ) {
        allViews.forEach { view ->
            if (view is ImageButton) {
                view.isSelected = false
            } else if (view is ImageView) {
                // For ImageView, you might want to set a different selected state
                // For example, you could change the tint or use a different image
                view.isSelected = false
                // Or you could use: view.setColorFilter(context.resources.getColor(R.color.normal_color))
            }
        }
        activeView?.isSelected = true
    }

    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun expandSidebar(context: Context, sidebar: CardView, expand: Boolean) {
        val collapsed = 41.dpToPx(context)
        val expanded = 200.dpToPx(context)
        val start = sidebar.layoutParams.width
        val end = if (expand) expanded else collapsed
        if (start == end) return

        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = 180
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val params: ViewGroup.LayoutParams = sidebar.layoutParams
            params.width = value
            sidebar.layoutParams = params
        }
        animator.start()
    }



    private fun setupBackPressedCallback(activity: ComponentActivity, activeView: View?) {
        var previouslyFocusedView: View? = null
        val sidebar = activity.findViewById<FrameLayout>(R.id.sideBar)


        activity.onBackPressedDispatcher.addCallback(activity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {


                if (sidebar.visibility != View.VISIBLE) {

                    previouslyFocusedView = activity.currentFocus
                    Log.e("previouslyFocusedView", previouslyFocusedView.toString())

                    // When back is pressed and sidebar is hidden, show it and focus the active button
                    sidebar.visibility = View.VISIBLE
                    activeView?.let {
                        it.postDelayed({
                            it.requestFocus()
                        }, 50)
                    }
                } else {
                    // If the sidebar is already visible, hide it
                    sidebar.visibility = View.GONE

                    previouslyFocusedView?.post {
                        if (previouslyFocusedView != null) {
                            previouslyFocusedView?.requestFocus()
                            previouslyFocusedView = null
                        }
                    }

                }
            }
        })
    }
}