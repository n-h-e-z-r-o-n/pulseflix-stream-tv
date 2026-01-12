package com.example.onyx.OnyxObjects

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.graphics.Typeface
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.onyx.Anime_Page
import com.example.onyx.Database.SessionManger
import com.example.onyx.Profile_Page
import com.example.onyx.R
import com.example.onyx.Shows_Page

object NavAction {

    // Cache for views to avoid repeated findViewById calls
    private val viewCache = mutableMapOf<Int, View?>()

    // Prevent duplicate click listeners
    private val clickListenersAdded = mutableSetOf<Int>()

    @SuppressLint("DiscouragedApi")
    fun setupSidebar(activity: Activity) {
        val sidebar = activity.findViewById<FrameLayout>(R.id.sideBar)

        // Get all navigation elements
        val btnTvMv = getOrFindView<ImageButton>(activity, R.id.btnMvTv)
        val btnAnime = getOrFindView<ImageButton>(activity, R.id.btnAnime)
        val btnProfileCard = getOrFindView<CardView>(activity, R.id.btnProfile)
        val btnProfileImage = getOrFindView<ImageView>(activity, R.id.btnProfileImg)

        val labelMvTv = getOrFindView<TextView>(activity, R.id.labelMvTv)
        val labelAnime = getOrFindView<TextView>(activity, R.id.labelAnime)
        val labelProfile = getOrFindView<TextView>(activity, R.id.labelProfile)

        val navigationButtons = listOf(btnTvMv, btnAnime, btnProfileCard)
        val labels = listOf(labelMvTv, labelAnime, labelProfile)

        val navigationMap = mapOf<View, Class<*>>(
            btnTvMv to Shows_Page::class.java,
            btnAnime to Anime_Page::class.java,
            btnProfileCard to Profile_Page::class.java
        )

        // Determine active view
        val activeView = when (activity) {
            is Shows_Page -> btnTvMv
            is Anime_Page -> btnAnime
            is Profile_Page -> btnProfileCard
            else -> btnTvMv
        }

        // Setup navigation and UI
        setupNavigation(activity, navigationMap, activeView)
        setupFocusHandling(activity, navigationButtons, labels, sidebar)

        if (activity is ComponentActivity) {
            setupBackPressedCallback(activity, activeView)
        }

        // Load profile image
        loadProfileImage(activity, btnProfileImage)
    }

    private fun <T : View> getOrFindView(activity: Activity, id: Int): T {
        return viewCache.getOrPut(id) { activity.findViewById(id) } as T
    }

    private fun setupNavigation(
        activity: Activity,
        navigationMap: Map<View, Class<*>>,
        activeView: View?
    ) {
        // Highlight active button
        highlightActive(activeView, navigationMap.keys.toList())
        activeView?.post { activeView.requestFocus() }

        // Add click listeners (only once per activity)
        navigationMap.forEach { (view, targetClass) ->
            val key = activity.hashCode() + view.id
            if (!clickListenersAdded.contains(key)) {
                view.setOnClickListener {
                    if (activity::class.java != targetClass) {
                        navigateToActivity(activity, targetClass)
                    }
                }
                clickListenersAdded.add(key)
            }
        }
    }

    private fun navigateToActivity(activity: Activity, targetClass: Class<*>) {
        val intent = Intent(activity, targetClass)
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivity(intent)
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun setupFocusHandling(
        activity: Activity,
        buttons: List<View>,
        labels: List<TextView?>,
        sidebar: FrameLayout
    ) {
        val mainBox = activity.findViewById<CardView>(R.id.mainBox)

        buttons.forEachIndexed { index, button ->
            val label = labels.getOrNull(index)

            button.setOnFocusChangeListener { v, hasFocus ->
                handleFocusChange(
                    context = activity,
                    view = v,
                    hasFocus = hasFocus,
                    label = label,
                    sidebar = sidebar,
                    allViews = buttons,
                    mainBox = mainBox
                )
            }
        }
    }

    private fun handleFocusChange(
        context: Activity,
        view: View,
        hasFocus: Boolean,
        label: TextView?,
        sidebar: FrameLayout,
        allViews: List<View>,
        mainBox: CardView
    ) {
        // Animate button scale
        view.animate()
            .scaleX(if (hasFocus) 1.2f else 1f)
            .scaleY(if (hasFocus) 1.2f else 1f)
            .setDuration(50)
            .start()

        // Update label font weight
        label?.setTypeface(null, if (hasFocus) Typeface.BOLD else Typeface.NORMAL)

        // Handle sidebar visibility and main box adjustments
        val anyFocused = allViews.any { it.hasFocus() }
        val layoutParams = mainBox.layoutParams as ViewGroup.MarginLayoutParams
        val density = context.resources.displayMetrics.density

        if (anyFocused) {
            showSidebar(context, sidebar, mainBox, layoutParams, density)
        } else {
            hideSidebar(sidebar, mainBox, layoutParams, density)
        }

        mainBox.layoutParams = layoutParams
        mainBox.requestLayout()
    }

    private fun showSidebar(
        context: Activity,
        sidebar: FrameLayout,
        mainBox: CardView,
        layoutParams: ViewGroup.MarginLayoutParams,
        density: Float
    ) {
        sidebar.visibility = View.VISIBLE
        mainBox.radius = 20 * density

        animateCardViewScale(mainBox, 0.9f, 0.9f)

        val marginLeft = (40 * density).toInt()
        layoutParams.setMargins(marginLeft, 0, 0, 0)
    }

    private fun hideSidebar(
        sidebar: FrameLayout,
        mainBox: CardView,
        layoutParams: ViewGroup.MarginLayoutParams,
        density: Float
    ) {
        sidebar.visibility = View.GONE
        mainBox.radius = 0f

        animateCardViewScale(mainBox, 1f, 1f)

        val margin0dp = (0 * density).toInt()
        layoutParams.setMargins(margin0dp, margin0dp, margin0dp, margin0dp)
    }

    private fun animateCardViewScale(view: View, scaleX: Float, scaleY: Float) {
        view.animate()
            .scaleX(scaleX)
            .scaleY(scaleY)
            .setDuration(140)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun highlightActive(activeView: View?, allViews: List<View?>) {
        allViews.forEach { view ->
            view?.isSelected = false
        }
        activeView?.isSelected = true
    }

    private fun setupBackPressedCallback(activity: ComponentActivity, activeView: View?) {
        var previouslyFocusedView: View? = null
        val sidebar = activity.findViewById<FrameLayout>(R.id.sideBar)
        val mainBox = activity.findViewById<CardView>(R.id.mainBox)

        activity.onBackPressedDispatcher.addCallback(activity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (sidebar.visibility != View.VISIBLE) {
                    previouslyFocusedView = activity.currentFocus

                    showSidebar(
                        context = activity,
                        sidebar = sidebar,
                        mainBox = mainBox,
                        layoutParams = mainBox.layoutParams as ViewGroup.MarginLayoutParams,
                        density = activity.resources.displayMetrics.density
                    )

                    activeView?.postDelayed({ activeView.requestFocus() }, 50)
                } else {
                    hideSidebar(
                        sidebar = sidebar,
                        mainBox = mainBox,
                        layoutParams = mainBox.layoutParams as ViewGroup.MarginLayoutParams,
                        density = activity.resources.displayMetrics.density
                    )

                    previouslyFocusedView?.post {
                        previouslyFocusedView?.requestFocus()
                        previouslyFocusedView = null
                    }
                }
            }
        })
    }

    private fun loadProfileImage(activity: Activity, profileImage: ImageView?) {
        profileImage?.let { imageView ->
            try {
                val sm = SessionManger(activity)
                val assetPath = "file:///android_asset/${sm.getUserAvatar()}"

                Glide.with(activity)
                    .load(assetPath)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(imageView)
            } catch (e: Exception) {
                Log.e("NavAction", "Error loading profile image", e)
                // Set default image on error
                imageView.setImageResource(R.drawable.ic_person)
            }
        }
    }

    // Clean up cache when activity is destroyed
    fun clearCache() {
        viewCache.clear()
        clickListenersAdded.clear()
    }
}