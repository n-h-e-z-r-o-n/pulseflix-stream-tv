package com.extractor.models

data class Video(
    val source: String,
    val subtitles: List<Subtitle> = emptyList()
)

data class Subtitle(
    val language: String,
    val url: String
)

data class Server(
    val id: String,
    val name: String,
    val url: String? = null
)

// Simple classes to hold basic search/movie data if needed, 
// though the user asked specifically for link extraction given an ID.
data class VideoType(
    val isMovie: Boolean
)
