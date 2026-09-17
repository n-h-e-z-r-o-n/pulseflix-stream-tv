package com.example.onyx

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

import androidx.lifecycle.ViewModelProvider
import com.example.onyx.Database.AppDatabase

class LiveStreamViewModel(
    private val db: AppDatabase,
    private val userId: Int
) : ViewModel() {

    private val _channels = MutableLiveData<List<IptvChannel>>()
    val channels: LiveData<List<IptvChannel>> get() = _channels

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private var allChannels = mutableListOf<IptvChannel>()
    private val _favoriteChannels = MutableLiveData<List<IptvChannel>>()
    val favoriteChannels: LiveData<List<IptvChannel>> get() = _favoriteChannels

    // Optional: filter channels by category
    private val _selectedCategory = MutableLiveData<String>("All")
    val selectedCategory: LiveData<String> get() = _selectedCategory

    private val _categorySearchQuery = MutableLiveData<String>("")
    private val _channelSearchQuery = MutableLiveData<String>("")

    init {
        loadFavorites()
        fetchPlaylist("https://iptv-org.github.io/iptv/index.m3u")
    }

    private fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            val favs = db.getFavoriteLiveChannels(userId)
            withContext(Dispatchers.Main) {
                _favoriteChannels.value = favs
            }
        }
    }

    fun fetchPlaylist(urlStr: String) {
        if (allChannels.isNotEmpty()) return // Already fetched

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL(urlStr)
                val connection = url.openConnection()
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val parsedChannels = mutableListOf<IptvChannel>()
                val categorySet = mutableSetOf<String>()

                val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
                var line = reader.readLine()

                var currentName = ""
                var currentLogo = ""
                var currentGroup = "Uncategorized"

                while (line != null) {
                    line = line.trim()
                    if (line.startsWith("#EXTINF:")) {
                        // Extract Group
                        val groupMatch = Regex("group-title=\"([^\"]+)\"").find(line)
                        currentGroup = groupMatch?.groups?.get(1)?.value ?: "Uncategorized"

                        // Extract Logo
                        val logoMatch = Regex("tvg-logo=\"([^\"]+)\"").find(line)
                        currentLogo = logoMatch?.groups?.get(1)?.value ?: ""

                        // Extract Name (after the last comma)
                        val commaIndex = line.lastIndexOf(",")
                        currentName = if (commaIndex != -1 && commaIndex < line.length - 1) {
                            line.substring(commaIndex + 1).trim()
                        } else {
                            "Unknown Channel"
                        }
                    } else if (line.isNotEmpty() && !line.startsWith("#")) {
                        // It's a stream URL
                        val channel = IptvChannel(
                            name = currentName,
                            group = currentGroup,
                            logo = currentLogo,
                            url = line
                        )
                        parsedChannels.add(channel)
                        categorySet.add(currentGroup)

                        // Reset
                        currentName = ""
                        currentLogo = ""
                        currentGroup = "Uncategorized"
                    }
                    line = reader.readLine()
                }
                reader.close()

                withContext(Dispatchers.Main) {
                    allChannels = parsedChannels
                    
                    val catList = mutableListOf("All")
                    catList.addAll(categorySet.sorted())
                    _categories.value = catList
                    
                    applyFilters()
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                Log.e("LiveStreamViewModel", "Error fetching M3U", e)
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to load channels: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        applyFilters()
    }

    fun searchCategory(query: String) {
        _categorySearchQuery.value = query
        applyCategoryFilters()
    }

    fun searchChannel(query: String) {
        _channelSearchQuery.value = query
        applyFilters()
    }

    private fun applyCategoryFilters() {
        val query = _categorySearchQuery.value ?: ""
        val allCats = mutableListOf("All")
        allCats.addAll(allChannels.map { it.group }.distinct().sorted())
        
        if (query.isNotBlank()) {
            _categories.value = allCats.filter { it.contains(query, ignoreCase = true) }
        } else {
            _categories.value = allCats
        }
    }

    private fun applyFilters() {
        var filtered = allChannels.toList()

        val cat = _selectedCategory.value ?: "All"
        if (cat != "All") {
            filtered = filtered.filter { it.group == cat }
        }

        val query = _channelSearchQuery.value ?: ""
        if (query.isNotBlank()) {
            filtered = filtered.filter { it.name.contains(query, ignoreCase = true) }
        }

        _channels.value = filtered
    }

    fun isFavorite(url: String): Boolean {
        return _favoriteChannels.value?.any { it.url == url } == true
    }

    fun toggleFavorite(channel: IptvChannel, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFav = db.isFavoriteLive(userId, channel.url)
            val success = if (isFav) {
                db.removeFavoriteLive(userId, channel.url)
            } else {
                db.addFavoriteLive(userId, channel.name, channel.group, channel.logo, channel.url)
            }
            
            if (success) {
                val newFavs = db.getFavoriteLiveChannels(userId)
                withContext(Dispatchers.Main) {
                    _favoriteChannels.value = newFavs
                    onResult(!isFav) // true if added, false if removed
                }
            }
        }
    }
}

class LiveStreamViewModelFactory(
    private val db: AppDatabase,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiveStreamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LiveStreamViewModel(db, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
