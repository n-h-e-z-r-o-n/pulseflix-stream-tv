package com.extractor

import com.extractor.models.Server
import com.extractor.models.Video
import com.extractor.models.VideoType
import com.extractor.providers.SflixProvider

object StreamFlixExtractor {

    /**
     * Fetch the list of available servers for a given video.
     * @param isMovie true if it's a movie, false for TV show episode.
     * @param id The ID of the video (e.g. "movie/free-interstellar-08779")
     */
    suspend fun getServers(isMovie: Boolean, id: String): List<Server> {
        return SflixProvider.getServers(id, VideoType(isMovie))
    }

    /**
     * Resolve a specific server to get the video link.
     */
    suspend fun extractVideo(server: Server): Video {
        return SflixProvider.getVideo(server)
    }

    /**
     * Convenience method to try to get the first working video link.
     * Warning: This might take time as it iterates through servers.
     */
    suspend fun getFirstLink(isMovie: Boolean, id: String): Video? {
        val servers = getServers(isMovie, id)
        for (server in servers) {
            try {
                return extractVideo(server)
            } catch (e: Exception) {
                // Continue to next server
                continue
            }
        }
        return null
    }
}
