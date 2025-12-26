package com.extractor.extractors

import android.util.Base64
import com.extractor.models.Video
import com.extractor.utils.StringConverterFactory
import com.google.gson.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

open class RabbitstreamExtractor : Extractor() {

    override val name = "Rabbitstream"
    override val mainUrl = "https://rabbitstream.net"
    protected open val embed = "ajax/v2/embed-4"
    protected open val key = "https://keys4.fun" // Fallback key source if needed

    // Deduced default based on common patterns
    private val RABBITSTREAM_SOURCE_API = "https://rabbitstream.net/ajax/v2/embed-4/getSources?id="

    override suspend fun extract(link: String): Video {
        val service = Service.build(mainUrl)
        val sourceId = link.substringAfterLast("/").substringBefore("?")

        val response = service.getSources(
            url = RABBITSTREAM_SOURCE_API + sourceId,
        )

        val sources = when (response) {
            is Service.Sources -> response
            is Service.Sources.Encrypted -> response.decrypt(
                // In a real app, you might fetch keys from an external service if they rotate
                // For now we assume the sources are decryptable or we fetch keys logic here
                // The original code uses `service.getSourceEncryptedKey(key)`
                key = service.getSourceEncryptedKey(key).rabbitstream.keys.key
            )
        }

        return Video(
            source = sources.sources.firstOrNull()?.file ?: "",
            subtitles = sources.tracks
                .filter { it.kind == "captions" }
                .map { com.extractor.models.Subtitle(it.label, it.file) }
        )
    }

    class MegacloudExtractor : RabbitstreamExtractor() {
        override val name = "Megacloud"
        override val mainUrl = "https://megacloud.tv"
        override val embed = "embed-2/ajax/e-1"
        private val scriptUrl = "$mainUrl/js/player/a/prod/e1-player.min.js"

        override suspend fun extract(link: String): Video {
            val service = Service.build(mainUrl)
            
            // Megacloud needs referer
            val response = service.getSources(
                url = "$mainUrl/$embed/getSources",
                id = link.substringAfterLast("/").substringBefore("?"),
                referer = mainUrl
            )

            val sources = when (response) {
                is Service.Sources -> response
                is Service.Sources.Encrypted -> {
                    val (key, encryptedSources) = extractRealKey(response.sources)
                    response.sources = encryptedSources
                    response.decrypt(key)
                }
            }

            return Video(
                source = sources.sources.firstOrNull()?.file ?: "",
                subtitles = sources.tracks
                    .filter { it.kind == "captions" }
                    .map { com.extractor.models.Subtitle(it.label, it.file) }
            )
        }

        private suspend fun extractRealKey(sources: String): Pair<String, String> {
            val rawKeys = getKeys()
            val sourcesArray = sources.toCharArray()
            var extractedKey = ""
            var currentIndex = 0
            
            for (index in rawKeys) {
                val start = index[0] + currentIndex
                val end = start + index[1]
                for (i in start until end) {
                    extractedKey += sourcesArray[i].toString()
                    sourcesArray[i] = ' '
                }
                currentIndex += index[1]
            }

            return extractedKey to sourcesArray.joinToString("").replace(" ", "")
        }

        private suspend fun getKeys(): List<List<Int>> {
            val service = Service.build(mainUrl)
            val script = service.getScript(scriptUrl, Date().time / 1000)

            fun matchingKey(value: String): String {
                return Regex(",$value=((?:0x)?([0-9a-fA-F]+))").find(script)?.groupValues?.get(1)
                    ?.removePrefix("0x")
                    ?: throw Exception("Failed to match the key")
            }

            return Regex("case\\s*0x[0-9a-f]+:(?![^;]*=partKey)\\s*\\w+\\s*=\\s*(\\w+)\\s*,\\s*\\w+\\s*=\\s*(\\w+);")
                .findAll(script).toList().map { match ->
                    val matchKey1 = matchingKey(match.groupValues[1])
                    val matchKey2 = matchingKey(match.groupValues[2])
                    try {
                        listOf(matchKey1.toInt(16), matchKey2.toInt(16))
                    } catch (e: NumberFormatException) {
                        emptyList()
                    }
                }.filter { it.isNotEmpty() }
        }
    }

    class DokicloudExtractor : RabbitstreamExtractor() {
        override val name = "Dokicloud"
        override val mainUrl = "https://dokicloud.one"
    }

    // --- Service & Data Classes ---

    interface Service {
        @GET
        @Headers("X-Requested-With: XMLHttpRequest")
        suspend fun getSources(@Url url: String, @Query("id") id: String, @Header("referer") referer: String): SourcesResponse

        @GET
        @Headers("X-Requested-With: XMLHttpRequest")
        suspend fun getSources(@Url url: String): SourcesResponse

        @GET
        suspend fun getSourceEncryptedKey(@Url url: String): KeysResponse

        @GET
        suspend fun getScript(@Url url: String, @Query("v") v: Long): String

        companion object {
            fun build(baseUrl: String): Service {
                return Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(StringConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder()
                            .registerTypeAdapter(SourcesResponse::class.java, SourcesResponse.Deserializer())
                            .create()
                    ))
                    .build()
                    .create(Service::class.java)
            }
        }

        sealed class SourcesResponse {
            class Deserializer : JsonDeserializer<SourcesResponse> {
                override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SourcesResponse {
                    val jsonObject = json?.asJsonObject ?: JsonObject()
                    return if (jsonObject.get("sources")?.isJsonArray == true) {
                        Gson().fromJson(json, Sources::class.java)
                    } else {
                        Gson().fromJson(json, Encrypted::class.java)
                    }
                }
            }
        }

        data class Sources(
            val sources: List<Source> = listOf(),
            val tracks: List<Track> = listOf()
        ) : SourcesResponse()

        data class Encrypted(
            var sources: String,
            val tracks: List<Track> = listOf()
        ) : SourcesResponse() {
             fun decrypt(key: String): Sources {
                fun decryptSourceUrl(decryptionKey: ByteArray, sourceUrl: String): String {
                    val cipherData = Base64.decode(sourceUrl, Base64.DEFAULT)
                    val encrypted = cipherData.copyOfRange(16, cipherData.size)
                    val aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    aesCBC.init(Cipher.DECRYPT_MODE, SecretKeySpec(decryptionKey.copyOfRange(0, 32), "AES"), IvParameterSpec(decryptionKey.copyOfRange(32, decryptionKey.size)))
                    return String(aesCBC.doFinal(encrypted), StandardCharsets.UTF_8)
                }

                fun generateKey(salt: ByteArray, secret: ByteArray): ByteArray {
                    fun md5(input: ByteArray) = MessageDigest.getInstance("MD5").digest(input)
                    var output = md5(secret + salt)
                    var currentKey = output
                    while (currentKey.size < 48) {
                        output = md5(output + secret + salt)
                        currentKey += output
                    }
                    return currentKey
                }

                val decrypted = decryptSourceUrl(
                    generateKey(Base64.decode(sources, Base64.DEFAULT).copyOfRange(8, 16), key.toByteArray()),
                    sources
                )
                return Sources(sources = Gson().fromJson(decrypted, Array<Source>::class.java).toList(), tracks = tracks)
            }
        }

        data class Source(val file: String)
        data class Track(val file: String, val label: String, val kind: String)
        
        data class KeysResponse(val rabbitstream: RabbitKeys)
        data class RabbitKeys(val keys: Keys)
        data class Keys(val key: String)
    }
}
