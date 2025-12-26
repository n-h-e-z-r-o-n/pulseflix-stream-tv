package com.extractor.extractors

import com.extractor.models.Video

abstract class Extractor {
    abstract val name: String
    abstract val mainUrl: String
    
    abstract suspend fun extract(link: String): Video
}
