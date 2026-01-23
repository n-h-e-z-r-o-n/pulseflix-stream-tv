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
    // Store previously focused view at the class level
    private var previouslyFocusedView: View? = null
    private var isSidebarOpen = false

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
                        // Clear focus before transition
                        validButtons.forEach { it.clearFocus() }
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

        // --- FOCUS MANAGEMENT LOGIC START ---
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
                    // When a button gets focus, show sidebar and store the previous focus
                    if (!isSidebarOpen) {
                        showSidebar(activity, sidebar, mainBox, activeButton)
                    }
                } else {
                    /* When focus is lost, check if sidebar should hide.
                       Only hide if no button has focus and sidebar is open.
                    */
                    view.postDelayed({
                        if (!hasAnyButtonFocus() && isSidebarOpen) {
                            hideSidebar(activity, sidebar, mainBox)
                        }
                    }, 50)
                }
            }
        }
        // --- FOCUS MANAGEMENT LOGIC END ---

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
                        if (isSidebarOpen) {
                            validButtons.forEach { it.clearFocus() }
                            hideSidebar(activity, sidebar, mainBox)
                        } else {
                            showSidebar(activity, sidebar, mainBox, activeButton)
                        }
                    }
                }
            )
        }

        // Handle taps outside
        mainBox.setOnClickListener {
            if (isSidebarOpen) {
                validButtons.forEach { it.clearFocus() }
                hideSidebar(activity, sidebar, mainBox)
            }
        }

        if (profileImg != null) loadProfileImage(activity, profileImg)
    }

    private fun showSidebar(activity: Activity, sidebar: FrameLayout, mainBox: CardView, activeButton: View?) {
        if (isSidebarOpen) return

        // Store the currently focused view before showing sidebar
        previouslyFocusedView = activity.currentFocus

        sidebar.visibility = View.VISIBLE
        isSidebarOpen = true

        val density = activity.resources.displayMetrics.density
        val params = mainBox.layoutParams as ViewGroup.MarginLayoutParams

        if (mainBox.isLaidOut) {
            mainBox.radius = 20 * density
            mainBox.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(120)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            val margin = (40 * density).toInt()
            params.setMargins(margin, 0, 0, 0)
            mainBox.layoutParams = params
        }

        // Request focus on active button after a short delay
        activeButton?.postDelayed({
            if (isSidebarOpen) {
                activeButton.requestFocus()
            }
        }, 50)
    }

    private fun hideSidebar(activity: Activity, sidebar: FrameLayout, mainBox: CardView) {
        if (!isSidebarOpen) return

        sidebar.visibility = View.GONE
        isSidebarOpen = false

        val density = activity.resources.displayMetrics.density
        val params = mainBox.layoutParams as ViewGroup.MarginLayoutParams

        if (mainBox.isLaidOut) {
            mainBox.radius = 0f
            mainBox.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(120)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            params.setMargins(0, 0, 0, 0)
            mainBox.layoutParams = params
        }

        // Restore focus to previously focused view
        previouslyFocusedView?.post {
            try {
                previouslyFocusedView?.requestFocus()
            } catch (e: Exception) {
            } finally {
                previouslyFocusedView = null
            }
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