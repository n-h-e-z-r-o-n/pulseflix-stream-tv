package com.example.onyx

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onyx.Database.AppDatabase
import com.example.onyx.Database.SessionManger

enum class PlayerState { NORMAL, FULLSCREEN }

class LiveStreamFragment : Fragment() {

    private lateinit var viewModel: LiveStreamViewModel
    private lateinit var liveRecyclerView: RecyclerView
    private lateinit var liveCategoriesRecyclerView: RecyclerView
    private lateinit var liveProgressBar: ProgressBar
    private lateinit var liveErrorText: TextView
    private lateinit var currentChannelInfo: View
    private lateinit var divider: View

    // UI elements for the embedded player & info
    private lateinit var livePlayerView: CustomPlayerView
    private lateinit var currentChannelLogo: ImageView
    private lateinit var currentChannelName: TextView
    private lateinit var currentChannelCategory: TextView

    private var liveFaveCount: TextView? = null
    private var liveCategoryCount: TextView? = null
    private var categoryChannelCount: TextView? = null

    private lateinit var channelAdapter: LiveChannelAdapter
    private lateinit var categoryAdapter: LiveCategoryAdapter

    private var liveFavoritesRecyclerView: RecyclerView? = null
    private var favoritesAdapter: LiveChannelAdapter? = null
    private var liveFavoritesToggleContainer: View? = null
    private var liveFavoritesToggleIcon: ImageView? = null

    private lateinit var sessionManager: SessionManger
    private var userId: Int = -1

    private var exoPlayer: ExoPlayer? = null
    
    private var currentPlayerState = PlayerState.NORMAL
    private var currentPlayingChannel: IptvChannel? = null
    
    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (currentPlayerState == PlayerState.FULLSCREEN) {
                setPlayerState(PlayerState.NORMAL)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_live, container, false)
        
        liveRecyclerView = view.findViewById(R.id.liveRecyclerView)
        liveCategoriesRecyclerView = view.findViewById(R.id.liveCategoriesRecyclerView)
        liveProgressBar = view.findViewById(R.id.liveProgressBar)
        liveErrorText = view.findViewById(R.id.liveErrorText)
        currentChannelInfo = view.findViewById(R.id.currentChannelInfo)
        divider = view.findViewById(R.id.divider)

        livePlayerView = view.findViewById(R.id.livePlayerView)
        currentChannelLogo = view.findViewById(R.id.currentChannelLogo)
        currentChannelName = view.findViewById(R.id.currentChannelName)
        currentChannelCategory = view.findViewById(R.id.currentChannelCategory)

        liveFaveCount = view.findViewById(R.id.live_fave_count)
        liveCategoryCount = view.findViewById(R.id.live_category_count)
        categoryChannelCount = view.findViewById(R.id.cateogry_channel_count)

        liveFavoritesRecyclerView = view.findViewById(R.id.liveFavoritesRecyclerView)
        liveFavoritesToggleContainer = view.findViewById(R.id.liveFavoritesToggleContainer)
        liveFavoritesToggleIcon = view.findViewById(R.id.liveFavoritesToggleIcon)

        val db = AppDatabase(requireActivity())
        sessionManager = SessionManger(requireActivity())
        userId = sessionManager.getUserId()
        val factory = LiveStreamViewModelFactory(db, userId)
        viewModel = ViewModelProvider(this, factory)[LiveStreamViewModel::class.java]

        setupRecyclerViews()
        setupSearchViews(view)
        observeViewModel()
        
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        return view
    }

    private fun setupSearchViews(view: View) {
        val searchViewCategory = view.findViewById<SearchView>(R.id.liveSearchViewCategory)
        val searchViewChannel = view.findViewById<SearchView>(R.id.liveSearchViewChannel)

        val keyboardLayout = view.findViewById<android.widget.LinearLayout>(R.id.keyboard_layout)
        var keyboardManager: com.example.onyx.OnyxClasses.CustomKeyboardManager? = null
        keyboardLayout?.let { kLayout ->
            val firstAutoComplete = searchViewCategory?.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text) 
                ?: searchViewChannel?.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
            firstAutoComplete?.let { ac ->
                keyboardManager = com.example.onyx.OnyxClasses.CustomKeyboardManager(
                    requireActivity(),
                    ac,
                    kLayout,
                    null
                )
            }
        }

        val hideKeyboard = { searchView: SearchView ->
            searchView.post {
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                keyboardManager?.hideKeyboard()
            }
        }

        val showKeyboard = { searchView: SearchView ->
            searchView.post {
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                val searchAutoComplete = searchView.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
                searchAutoComplete?.requestFocus()
                
                if (searchAutoComplete != null && keyboardManager != null) {
                    imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                    keyboardManager?.searchEditText = searchAutoComplete
                    keyboardManager?.searchListener = object : com.example.onyx.OnyxClasses.OnSearchListener {
                        override fun EnterActionTrigger(query: String) {
                            if (searchView == searchViewCategory) {
                                viewModel.searchCategory(query)
                            } else {
                                viewModel.searchChannel(query)
                            }
                            hideKeyboard(searchView)
                            searchView.clearFocus()
                        }
                    }
                    keyboardManager?.showKeyboard()
                } else {
                    imm.showSoftInput(searchAutoComplete, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        view.viewTreeObserver.addOnGlobalFocusChangeListener { _, _ ->
            val inCategorySearch = searchViewCategory?.hasFocus() == true
            val inChannelSearch = searchViewChannel?.hasFocus() == true
            val inKeyboard = keyboardLayout?.hasFocus() == true
            
            if (!inCategorySearch && !inChannelSearch && !inKeyboard) {
                // Focus left the search area completely
                keyboardManager?.hideKeyboard()
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        val setupClickToShowKeyboard = { searchView: SearchView? ->
            searchView?.setOnClickListener { showKeyboard(it as SearchView) }
            val searchAutoComplete = searchView?.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
            searchAutoComplete?.setOnClickListener { showKeyboard(searchView) }
            
            searchAutoComplete?.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
                    showKeyboard(searchView)
                    false 
                } else {
                    false
                }
            }
        }

        setupClickToShowKeyboard(searchViewCategory)
        setupClickToShowKeyboard(searchViewChannel)

        searchViewCategory?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchCategory(query ?: "")
                hideKeyboard(searchViewCategory)
                searchViewCategory.clearFocus()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchCategory(newText ?: "")
                return true
            }
        })

        searchViewChannel?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchChannel(query ?: "")
                hideKeyboard(searchViewChannel)
                searchViewChannel.clearFocus()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchChannel(newText ?: "")
                return true
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlayer()
        setupCustomControls()
    }

    private fun setupCustomControls() {
        val btnFullscreen = livePlayerView.findViewById<ImageButton>(R.id.btn_fullscreen)
        val btnLiveSync = livePlayerView.findViewById<TextView>(R.id.btn_live_sync)
        val btnPlayPause = livePlayerView.findViewById<ImageButton>(R.id.exo_play_pause)
        val btnLiveFave = livePlayerView.findViewById<ImageButton>(R.id.btn_live_fave)
        val favoriteButton = view?.findViewById<View>(R.id.favoriteButton)

        btnFullscreen?.setOnClickListener {
            val nextState = if (currentPlayerState == PlayerState.NORMAL) PlayerState.FULLSCREEN else PlayerState.NORMAL
            setPlayerState(nextState)
        }

        btnLiveSync?.setOnClickListener {
            exoPlayer?.seekToDefaultPosition()
        }
        
        btnPlayPause?.setOnClickListener {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    btnPlayPause.setImageResource(R.drawable.ic_play) // Assumes you have ic_play
                } else {
                    player.play()
                    btnPlayPause.setImageResource(R.drawable.ic_pause) // Assumes you have ic_pause
                }
            }
        }

        val toggleFave = {
            currentPlayingChannel?.let { channel ->
                viewModel.toggleFavorite(channel) {
                    updateFavoriteIcons()
                }
            }
        }
        
        btnLiveFave?.setOnClickListener { toggleFave() }
        favoriteButton?.setOnClickListener { toggleFave() }
        
        livePlayerView.onDpadDownVisible = { event ->
            if (currentPlayerState == PlayerState.FULLSCREEN) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    livePlayerView.hideController()
                }
                true
            } else {
                false
            }
        }
        
        livePlayerView.onDpadDownHidden = { event ->
            if (currentPlayerState == PlayerState.FULLSCREEN) {
                true // Do nothing, but consume it
            } else {
                false
            }
        }
        
        livePlayerView.onDpadUpHidden = { event ->
            if (currentPlayerState == PlayerState.FULLSCREEN) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    livePlayerView.showController()
                }
                true
            } else {
                false
            }
        }
        
        livePlayerView.onDpadUpVisible = { event ->
            if (currentPlayerState == PlayerState.FULLSCREEN) {
                true // Consume without doing anything
            } else {
                false
            }
        }
        
        livePlayerView.onBackPress = { event ->
            if (currentPlayerState == PlayerState.FULLSCREEN) {
                if (event.action == KeyEvent.ACTION_UP) {
                    setPlayerState(PlayerState.NORMAL)
                }
                true // Consume the back press so PlayerView doesn't hide the controller
            } else {
                false // Let normal back press flow (either PlayerView hides controller or Activity handles it)
            }
        }
    }
    
    private fun setPlayerState(state: PlayerState) {
        currentPlayerState = state
        backPressedCallback.isEnabled = (state != PlayerState.NORMAL)
        
        val window = requireActivity().window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        
        val topContainer = view?.findViewById<View>(R.id.topPlayerContainer)
        val sidebarContainer = view?.findViewById<View>(R.id.sidebarContainer)
        
        when (state) {
            PlayerState.NORMAL -> {
                // Show UI
                currentChannelInfo.visibility = View.VISIBLE
                divider.visibility = View.VISIBLE
                sidebarContainer?.visibility = View.VISIBLE
                liveCategoriesRecyclerView.visibility = View.VISIBLE
                liveRecyclerView.visibility = View.VISIBLE
                
                // Show HomeActivity Sidebar if exists
                (requireActivity() as? HomeActivity)?.findViewById<View>(R.id.sideBar)?.visibility = View.VISIBLE
                
                // Show System UI
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                
                val layoutParams = livePlayerView.layoutParams as? ViewGroup.MarginLayoutParams
                layoutParams?.apply {
                    val isTv = (requireActivity().resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_TYPE_MASK) == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
                    if (isTv) {
                        height = (280 * resources.displayMetrics.density).toInt()
                        width = 0
                        if (this is android.widget.LinearLayout.LayoutParams) {
                            weight = 1.2f
                        }
                        val margin16 = (16 * resources.displayMetrics.density).toInt()
                        setMargins(margin16, margin16, margin16, margin16)
                        
                        topContainer?.layoutParams = topContainer?.layoutParams?.apply { 
                            height = ViewGroup.LayoutParams.WRAP_CONTENT 
                        }
                    } else {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        setMargins(0, 0, 0, 0)
                    }
                }
                livePlayerView.layoutParams = layoutParams
            }
            PlayerState.FULLSCREEN -> {
                // Hide EVERYTHING
                currentChannelInfo.visibility = View.GONE
                divider.visibility = View.GONE
                sidebarContainer?.visibility = View.GONE
                liveCategoriesRecyclerView.visibility = View.GONE
                liveRecyclerView.visibility = View.GONE
                
                (requireActivity() as? HomeActivity)?.findViewById<View>(R.id.sideBar)?.visibility = View.GONE
                
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                
                val layoutParams = livePlayerView.layoutParams as? ViewGroup.MarginLayoutParams
                layoutParams?.apply {
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    setMargins(0, 0, 0, 0)
                    
                    topContainer?.layoutParams = topContainer?.layoutParams?.apply { 
                        height = ViewGroup.LayoutParams.MATCH_PARENT 
                    }
                }
                livePlayerView.layoutParams = layoutParams
                livePlayerView.requestFocus()
            }
        }
    }

    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(requireContext()).build()
            livePlayerView.player = exoPlayer
            
            exoPlayer?.addListener(object : androidx.media3.common.Player.Listener {
                override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                    val upNextDetails = view?.findViewById<TextView>(R.id.upNextDetails) ?: return
                    
                    val title = mediaMetadata.title?.toString()?.trim() ?: ""
                    val subtitle = mediaMetadata.subtitle?.toString()?.trim() ?: ""
                    val description = mediaMetadata.description?.toString()?.trim() ?: ""
                    
                    val detailsList = listOf(title, subtitle, description).filter { it.isNotBlank() }
                    
                    if (detailsList.isNotEmpty()) {
                        upNextDetails.text = detailsList.joinToString("\n")
                    } else {
                        upNextDetails.text = "No Program Details Available"
                    }
                }
            })
        }
    }

    private fun playChannel(channel: IptvChannel) {
        currentPlayingChannel = channel
        sessionManager.saveLastPlayedLiveChannel(userId, channel.url)
        
        // Reset Program Details until new metadata comes in
        view?.findViewById<TextView>(R.id.upNextDetails)?.text = "Loading Program Details..."
        
        // Update Info Panel
        currentChannelName.text = channel.name
        currentChannelCategory.text = channel.group
        
        if (channel.logo.isNotEmpty()) {
            Glide.with(this)
                .load(channel.logo)
                .error(R.drawable.ic_tv)
                .into(currentChannelLogo)
        } else {
            currentChannelLogo.setImageResource(R.drawable.ic_tv)
        }

        updateFavoriteIcons()

        // Play Stream
        exoPlayer?.let { player ->
            player.setMediaItem(MediaItem.fromUri(channel.url))
            player.prepare()
            player.playWhenReady = true
        }
    }

    private fun updateFavoriteIcons() {
        val channel = currentPlayingChannel ?: return
        val isFav = viewModel.isFavorite(channel.url)
        
        val btnLiveFave = livePlayerView.findViewById<android.widget.ImageButton>(R.id.btn_live_fave)
        val favoriteButtonImg = view?.findViewById<ImageView>(R.id.favoriteButtonImg)

        if (isFav) {
            val iconRes = R.drawable.ic_tickfave
            val tint = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.fav))
            
            btnLiveFave?.setImageResource(iconRes)
            btnLiveFave?.imageTintList = tint
            
            favoriteButtonImg?.setImageResource(iconRes)
            favoriteButtonImg?.imageTintList = tint
        } else {
            val iconRes = R.drawable.ic_addfave
            val tint = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
            
            btnLiveFave?.setImageResource(iconRes)
            btnLiveFave?.imageTintList = tint
            
            favoriteButtonImg?.setImageResource(iconRes)
            favoriteButtonImg?.imageTintList = tint
        }
    }

    private fun setupRecyclerViews() {
        val isTv = (requireActivity().resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_TYPE_MASK) == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
        if (isTv) {
            liveRecyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 4)
        } else {
            liveRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }
        
        channelAdapter = LiveChannelAdapter { channel ->
            playChannel(channel)
        }
        liveRecyclerView.adapter = channelAdapter

        // Setup Favorites Recycler View
        favoritesAdapter = LiveChannelAdapter { channel ->
            playChannel(channel)
        }
        liveFavoritesRecyclerView?.layoutManager = LinearLayoutManager(context)
        liveFavoritesRecyclerView?.adapter = favoritesAdapter

        liveFavoritesToggleContainer?.setOnClickListener {
            val rv = liveFavoritesRecyclerView ?: return@setOnClickListener
            val isVisible = rv.visibility == View.VISIBLE
            if (isVisible) {
                rv.visibility = View.GONE
                liveFavoritesToggleIcon?.rotation = 0f
            } else {
                rv.visibility = View.VISIBLE
                liveFavoritesToggleIcon?.rotation = 180f
            }
        }

        val orientation = if (isTv) LinearLayoutManager.VERTICAL else LinearLayoutManager.HORIZONTAL

        liveCategoriesRecyclerView.layoutManager = LinearLayoutManager(context, orientation, false)
        categoryAdapter = LiveCategoryAdapter { category ->
            viewModel.selectCategory(category)
        }
        liveCategoriesRecyclerView.adapter = categoryAdapter
    }

    private fun observeViewModel() {
        viewModel.channels.observe(viewLifecycleOwner) { channels ->
            channelAdapter.submitList(channels)
            
            val cat = viewModel.selectedCategory.value ?: "All"
            val total = channels.size
            categoryChannelCount?.text = "Showing $total channels in $cat"
            
            if (exoPlayer?.mediaItemCount == 0 && channels.isNotEmpty()) {
                val lastUrl = sessionManager.getLastPlayedLiveChannel(userId)
                val channelToPlay = channels.find { it.url == lastUrl } ?: channels[0]
                playChannel(channelToPlay)
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
            liveCategoryCount?.text = "${categories.size} Genres"
        }

        viewModel.favoriteChannels.observe(viewLifecycleOwner) { favs ->
            favoritesAdapter?.submitList(favs)
            liveFaveCount?.text = "${favs.size}"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                liveProgressBar.visibility = View.VISIBLE
                liveRecyclerView.visibility = View.GONE
                liveCategoriesRecyclerView.visibility = View.GONE
            } else {
                liveProgressBar.visibility = View.GONE
                liveRecyclerView.visibility = View.VISIBLE
                liveCategoriesRecyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                liveErrorText.visibility = View.VISIBLE
                liveErrorText.text = error
                liveRecyclerView.visibility = View.GONE
                liveCategoriesRecyclerView.visibility = View.GONE
            } else {
                liveErrorText.visibility = View.GONE
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // If we return to the fragment and it was in fullscreen, enforce fullscreen
        if (currentPlayerState == PlayerState.FULLSCREEN) {
            setPlayerState(PlayerState.FULLSCREEN)
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.release()
        exoPlayer = null
        livePlayerView.player = null
        
        // Restore system UI if leaving fragment
        if (currentPlayerState == PlayerState.FULLSCREEN) {
            val window = requireActivity().window
            WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
            (requireActivity() as? HomeActivity)?.findViewById<View>(R.id.sideBar)?.visibility = View.VISIBLE
        }
    }
}
