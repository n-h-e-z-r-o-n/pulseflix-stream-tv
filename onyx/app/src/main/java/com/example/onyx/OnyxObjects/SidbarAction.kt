package com.example.onyx.OnyxObjects

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.onyx.*
import com.example.onyx.Database.SessionManger

object NavAction {

    fun setupSidebar(activity: Activity) {
        val sidebar = activity.findViewById<FrameLayout>(R.id.sideBar)
        val mainBox = activity.findViewById<CardView>(R.id.mainBox)

        if (sidebar == null || mainBox == null) return

        val btnShows = activity.findViewById<ImageButton>(R.id.sidebarBtnShows)
        val btnAnime = activity.findViewById<ImageButton>(R.id.sidebarBtnAnime)
        val btnProfile = activity.findViewById<CardView>(R.id.sidebarBtnProfile)
        val profileImg = activity.findViewById<ImageView>(R.id.sidebarBtnProfileImg)

        val labelMvTv = activity.findViewById<TextView>(R.id.sidebarLabelShows)
        val labelAnime = activity.findViewById<TextView>(R.id.sidebarLabelAnime)
        val labelProfile = activity.findViewById<TextView>(R.id.sidebarLabelProfile)

        val buttons = listOf(btnShows, btnAnime, btnProfile)
        val labels = listOf(labelMvTv, labelAnime, labelProfile)

        val validButtons = buttons.filterNotNull()
        val validLabels = labels.filterNotNull()

        val navMap = mapOf(
            btnShows to Shows_Page::class.java,
            btnAnime to Anime_Page::class.java,
            btnProfile to Profile_Page::class.java
        )

        val validNavMap = navMap.filterKeys { it != null }

        val activeButton = when (activity) {
            is Shows_Page -> btnShows
            is Anime_Page -> btnAnime
            is Profile_Page -> btnProfile
            else -> btnShows
        }

        validButtons.forEach { it.isSelected = it == activeButton }

        // Navigation clicks
        validNavMap.forEach { (view, target) ->
            view?.setOnClickListener {
                if (activity::class.java != target) {
                    val intent = Intent(activity, target)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    try {
                        activity.startActivity(intent)
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        fun hasAnyButtonFocus(): Boolean = validButtons.any { it.hasFocus() }

        fun focusActiveButton() {
            if (sidebar.visibility == View.VISIBLE && !hasAnyButtonFocus() && activeButton != null) {
                activeButton.requestFocus()
            }
        }

        // --- CRITICAL FOCUS LOGIC START ---
        validButtons.forEachIndexed { index, view ->
            view.setOnFocusChangeListener { _, hasFocus ->
                // Visual feedback for focus
                view.animate()
                    .scaleX(if (hasFocus) 1.2f else 1f)
                    .scaleY(if (hasFocus) 1.2f else 1f)
                    .setDuration(80)
                    .start()

                if (index < validLabels.size) {
                    validLabels[index].setTypeface(
                        null,
                        if (hasFocus) Typeface.BOLD else Typeface.NORMAL
                    )
                }

                if (hasFocus) {
                    // If a button gets focus, ensure sidebar is shown
                    toggleSidebar(activity, sidebar, mainBox, true)
                } else {
                    /* When focus is lost, wait a frame to see where focus went.
                       If no other button in the sidebar has focus, hide the sidebar.
                    */
                    view.postDelayed({
                        if (!hasAnyButtonFocus()) {
                            toggleSidebar(activity, sidebar, mainBox, false)
                        }
                    }, 50)
                }
            }
        }
        // --- CRITICAL FOCUS LOGIC END ---

        // Initial setup
        if (activeButton != null) {
            activeButton.post {
                if (sidebar.visibility == View.VISIBLE) {
                    activeButton.requestFocus()
                }
            }
        }

        // Back button handling
        if (activity is ComponentActivity) {
            activity.onBackPressedDispatcher.addCallback(
                activity,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (sidebar.visibility == View.VISIBLE) {
                            validButtons.forEach { it.clearFocus() }
                            toggleSidebar(activity, sidebar, mainBox, false)
                        } else {
                            toggleSidebar(activity, sidebar, mainBox, true)
                            focusActiveButton()
                        }
                    }
                }
            )
        }

        // Handle taps outside
        mainBox.setOnClickListener {
            if (sidebar.visibility == View.VISIBLE) {
                validButtons.forEach { it.clearFocus() }
                toggleSidebar(activity, sidebar, mainBox, false)
            }
        }

        if (profileImg != null) loadProfileImage(activity, profileImg)
    }

    private fun toggleSidebar(activity: Activity, sidebar: FrameLayout, mainBox: CardView, show: Boolean) {
        if (sidebar.visibility == (if (show) View.VISIBLE else View.GONE)) return

        val density = activity.resources.displayMetrics.density
        val params = mainBox.layoutParams as ViewGroup.MarginLayoutParams

        sidebar.visibility = if (show) View.VISIBLE else View.GONE

        if (mainBox.isLaidOut) {
            mainBox.radius = if (show) 20 * density else 0f
            mainBox.animate()
                .scaleX(if (show) 0.9f else 1f)
                .scaleY(if (show) 0.9f else 1f)
                .setDuration(120)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            val margin = if (show) (40 * density).toInt() else 0
            params.setMargins(margin, 0, 0, 0)
            mainBox.layoutParams = params
        }
    }

    private fun loadProfileImage(activity: Activity, imageView: ImageView) {
        try {
            val avatar = SessionManger(activity).getUserAvatar()
            val avatarPath = "file:///android_asset/$avatar"
            Glide.with(activity)
                .load(avatarPath)
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(imageView)
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.ic_person)
        }
    }
}