package com.davidmedenjak.indiana.api

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type

class StreamingKotlinxSerializationConverterFactory(
    private val json: Json
) : Converter.Factory() {
    
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val loader = json.serializersModule.serializer(type)
        return StreamResponseBodyConverter(json, loader)
    }
    
    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        val saver = json.serializersModule.serializer(type)
        return StreamRequestBodyConverter(json, saver)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class StreamResponseBodyConverter<T>(
    private val json: Json,
    private val loader: DeserializationStrategy<T>
) : Converter<ResponseBody, T> {
    override fun convert(value: ResponseBody): T {
        val stream = value.byteStream()
        return json.decodeFromStream(loader, stream)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class StreamRequestBodyConverter<T>(
    private val json: Json,
    private val saver: SerializationStrategy<T>
) : Converter<T, RequestBody> {
    override fun convert(value: T): RequestBody {
        val stream = ByteArrayOutputStream()
        json.encodeToStream(saver, value, stream)
        return RequestBody.create(null, stream.toByteArray())
    }
}
