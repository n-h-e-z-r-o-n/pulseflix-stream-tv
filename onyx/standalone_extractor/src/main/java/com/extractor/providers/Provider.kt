package com.extractor.providers

import com.extractor.models.Server
import com.extractor.models.Video
import com.extractor.models.VideoType

interface Provider {
    val name: String
    val baseUrl: String
    
    // Simplification: We assume the user already has the ID.
    // But we might need search to verify IDs or just for completeness.
    // For now, let's stick to what's needed for extraction.
    
    suspend fun getServers(id: String, videoType: VideoType): List<Server>
    
    suspend fun getVideo(server: Server): Video
}
