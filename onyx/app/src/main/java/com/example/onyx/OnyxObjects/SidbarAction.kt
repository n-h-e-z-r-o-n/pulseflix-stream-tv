package com.example.onyx.OnyxObjects

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
import com.example.onyx.Anime_Page
import com.example.onyx.Database.SessionManger
import com.example.onyx.Profile_Page
import com.example.onyx.R
import com.example.onyx.Shows_Page

object NavAction {




    fun setupSidebar(activity: Activity) {
        val sidebar = activity.findViewById<FrameLayout>(R.id.sideBar)



        val btnTvMv = activity.findViewById<ImageButton>(R.id.btnMvTv)
        val btnAnime = activity.findViewById<ImageButton>(R.id.btnAnime)
        val btnProfile = activity.findViewById<View>(R.id.btnProfile)


        val labelMvTv = activity.findViewById<TextView>(R.id.labelMvTv)
        val labelAnime = activity.findViewById<TextView>(R.id.labelAnime)
        val labelProfile = activity.findViewById<TextView>(R.id.labelProfile)

        val buttons = listOf( btnTvMv, btnAnime, btnProfile)

        val labels = listOf(labelMvTv, labelAnime, labelProfile)

        val navigationMap = mapOf<View, Class<*>>(
            btnTvMv to Shows_Page::class.java,
            btnAnime to Anime_Page::class.java,
            btnProfile to Profile_Page::class.java
        )


        val activeView: View? = when (activity) {
            is Shows_Page -> btnTvMv
            is Anime_Page -> btnAnime
            is Profile_Page -> btnProfile
            else -> btnTvMv
        }

        highlightActive(activeView, buttons + btnProfile)
        activeView?.post { activeView.requestFocus() }

        navigationMap.forEach { (view, targetClass) ->
            view?.setOnClickListener {
                if (activity::class.java != targetClass) {
                    val intent = Intent(activity, targetClass)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    activity.startActivity(intent)
                }
            }
        }

        navigationMap.forEach { (view, targetClass) ->
            view?.setOnClickListener {
                if (activity::class.java != targetClass) {
                    val intent = Intent(activity, targetClass)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    activity.startActivity(intent)
                }
            }
        }

        buttons.forEachIndexed { index, btn ->
            val label = labels[index]
            btn?.setOnFocusChangeListener { v, hasFocus ->
                val mainBox = activity.findViewById<CardView>(R.id.mainBox)
                handleFocusChange(mainBox, v, hasFocus, label, sidebar, buttons, labels)
            }
        }

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





    }

    private fun handleFocusChange(
        mainBox: CardView,
        view: View,
        hasFocus: Boolean,
        label: TextView?,
        sidebar: View?,
        allViews: List<View?>,
        allLabels: List<TextView?>
    ) {

        if (hasFocus) {
            // View scaling
            view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(50).start()

            label?.let {
                it.setTypeface(null, Typeface.BOLD)
            }
        } else {
            view.animate().scaleX(1f).scaleY(1f).setDuration(50).start()

            label?.let {
                it.setTypeface(null, Typeface.NORMAL)
            }
        }

        sidebar?.let { bar ->
            val anyFocused = allViews.any { it?.hasFocus() == true }
            val layoutParams = mainBox.layoutParams as ViewGroup.MarginLayoutParams

            val context = view.context
            val density = context.resources.displayMetrics.density

            if (anyFocused) {
                //expandSidebar(view.context, bar, true)

                mainBox.radius = 20 * context.resources.displayMetrics.density
                animateCardViewScale(mainBox, 0.9f, 0.9f)


                val marginLeft = (40 * context.resources.displayMetrics.density).toInt()
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
        Log.e("scaleX", scaleX.toString())
        Log.e("scaleY", scaleY.toString())
        view.animate()
            .scaleX(scaleX)
            .scaleY(scaleY)
            .setDuration(140)
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
                view.isSelected = false
            }
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
                    Log.e("previouslyFocusedView", previouslyFocusedView.toString())

                    sidebar.visibility = View.VISIBLE
                    animateCardViewScale(mainBox, 0.9f, 0.9f)
                    activeView?.let {
                        it.postDelayed({
                            it.requestFocus()
                        }, 50)
                    }
                } else {
                    sidebar.visibility = View.GONE
                    animateCardViewScale(mainBox, 1f, 1f)

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