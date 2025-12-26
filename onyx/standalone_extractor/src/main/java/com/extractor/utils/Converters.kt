package com.extractor.utils

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.nio.charset.Charset

class JsoupConverter(
    private val baseUri: String,
) : Converter<ResponseBody, Document?> {

    override fun convert(value: ResponseBody): Document {
        val charset = value.contentType()?.charset() ?: Charset.forName("UTF-8")
        val parser = when (value.contentType().toString()) {
            "application/xml", "text/xml" -> Parser.xmlParser()
            else -> Parser.htmlParser()
        }
        return Jsoup.parse(value.byteStream(), charset.name(), baseUri, parser)
    }
}

class JsoupConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? = when (type) {
        Document::class.java -> JsoupConverter(retrofit.baseUrl().toString())
        else -> null
    }

    companion object {
        fun create() = JsoupConverterFactory()
    }
}

class StringConverter : Converter<ResponseBody, String> {
    override fun convert(value: ResponseBody): String {
        return value.bytes().toString(Charsets.UTF_8)
    }
}

class StringConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? = when (type) {
        String::class.java -> StringConverter()
        else -> null
    }

    companion object {
        fun create() = StringConverterFactory()
    }
}
