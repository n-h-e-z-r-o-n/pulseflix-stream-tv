package com.extractor.providers

import com.extractor.extractors.Extractor
import com.extractor.extractors.RabbitstreamExtractor
import com.extractor.models.Server
import com.extractor.models.Video
import com.extractor.models.VideoType
import com.extractor.utils.DnsResolver
import com.extractor.utils.JsoupConverterFactory
import com.extractor.utils.StringConverterFactory
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

object SflixProvider : Provider {

    override val name = "SFlix"
    override val baseUrl = "https://sflix.to/" // Keep it updated

    private val service = Service.build()
    
    // We need a way to map server names to Extractors
    private val extractors = listOf(
        RabbitstreamExtractor(),
        RabbitstreamExtractor.MegacloudExtractor(),
        RabbitstreamExtractor.DokicloudExtractor()
    )

    override suspend fun getServers(id: String, videoType: VideoType): List<Server> {
        val document = if (videoType.isMovie) {
            service.getMovieServers(id)
        } else {
            service.getEpisodeServers(id)
        }
        
        val servers = document.select("a").map {
            Server(
                id = it.attr("data-id"),
                name = it.selectFirst("span")?.text()?.trim() ?: "Unknown"
            )
        }
        
        return servers
    }

    override suspend fun getVideo(server: Server): Video {
        // 1. Get the link from the server ID
        val linkResponse = service.getLink(server.id)
        val linkWithReferrer = "${linkResponse.link}&referrer=$baseUrl"
        
        // 2. Find matching extractor
        // Logic: Check if link matches extractor's domain
        // In the original code, RabbitstreamExtractor handles "rabbitstream.net"
        // linkResponse.link looks like "https://rabbitstream.net/embed-4/..."
        
        val extractor = extractors.find { extractor -> 
             linkResponse.link.contains(extractor.mainUrl) || 
             (extractor is RabbitstreamExtractor.MegacloudExtractor && linkResponse.link.contains("megacloud.tv")) ||
             (extractor is RabbitstreamExtractor.DokicloudExtractor && linkResponse.link.contains("dokicloud.one"))
        } ?: throw Exception("No extractor found for url: ${linkResponse.link}")

        return extractor.extract(linkWithReferrer)
    }

    // --- Service Interface ---
    
    interface Service {
        @GET("ajax/movie/episodes/{id}")
        suspend fun getMovieServers(@Path("id") movieId: String): Document

        @GET("ajax/v2/episode/servers/{id}")
        suspend fun getEpisodeServers(@Path("id") episodeId: String): Document

        @GET("ajax/sources/{id}")
        suspend fun getLink(@Path("id") id: String): LinkResponse

        data class LinkResponse(
            val type: String,
            val link: String
        )

        companion object {
            fun build(): Service {
                val client = OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .dns(DnsResolver.doh)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(SflixProvider.baseUrl)
                    .addConverterFactory(JsoupConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()

                return retrofit.create(Service::class.java)
            }
        }
    }
}
