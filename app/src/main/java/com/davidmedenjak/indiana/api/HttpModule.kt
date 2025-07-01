package com.davidmedenjak.indiana.api

import com.davidmedenjak.indiana.BuildConfig
import com.davidmedenjak.indiana.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonUnquotedLiteral
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("kotlin.Any")
    
    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is String -> encoder.encodeString(value)
            is Int -> encoder.encodeInt(value)
            is Long -> encoder.encodeLong(value)
            is Double -> encoder.encodeDouble(value)
            is Boolean -> encoder.encodeBoolean(value)
            else -> encoder.encodeString(value.toString())
        }
    }
    
    override fun deserialize(decoder: Decoder): Any {
        return try {
            decoder.decodeString()
        } catch (e: Exception) {
            try {
                decoder.decodeInt()
            } catch (e: Exception) {
                try {
                    decoder.decodeDouble()
                } catch (e: Exception) {
                    try {
                        decoder.decodeBoolean()
                    } catch (e: Exception) {
                        decoder.decodeString()
                    }
                }
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
class HttpModule {

    @Singleton
    @Provides
    fun providerOkHttp(
        sessionManager: SessionManager,
    ) = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
            addInterceptor { chain ->
                val result = chain.proceed(chain.request())
                if (result.code == 401) {
                    sessionManager.logout()
                }
                result
            }
        }
        .build()

    @Singleton
    @Provides
    @Named("Authorized")
    fun provideAuthorizedOkHttp(
        okHttpClient: OkHttpClient,
        authInterceptor: ApiTokenAuthInterceptor
    ) = okHttpClient.newBuilder()
        .addInterceptor(authInterceptor)
        .build()

    @Singleton
    @Provides
    fun provideJson() = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        serializersModule = SerializersModule {
            contextual(AnySerializer)
        }
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        json: Json,
        okHttpClient: Provider<OkHttpClient>
    ) = Retrofit.Builder()
        .addConverterFactory(StreamingKotlinxSerializationConverterFactory(json))
        .callFactory { okHttpClient.get().newCall(it) }
        .baseUrl("https://api.bitrise.io/v0.1/")
        .build()

    @Singleton
    @Provides
    @Named("Authorized")
    fun provideAuthorizedRetrofit(
        json: Json,
        @Named("Authorized") okHttpClient: Provider<OkHttpClient>
    ) = Retrofit.Builder()
        .addConverterFactory(StreamingKotlinxSerializationConverterFactory(json))
        .callFactory { okHttpClient.get().newCall(it) }
        .baseUrl("https://api.bitrise.io/v0.1/")
        .build()

    @Reusable
    @Provides
    fun UserApi(
        @Named("Authorized") retrofit: Retrofit
    ): UserApi = retrofit.create(UserApi::class.java)

    @Reusable
    @Provides
    fun ApplicationApi(
        @Named("Authorized") retrofit: Retrofit
    ): ApplicationApi = retrofit.create(ApplicationApi::class.java)

    @Reusable
    @Provides
    fun BuildsApi(
        @Named("Authorized") retrofit: Retrofit
    ): BuildsApi = retrofit.create(BuildsApi::class.java)

    @Reusable
    @Provides
    fun BuildArtifactApi(
        @Named("Authorized") retrofit: Retrofit
    ): BuildArtifactApi = retrofit.create(BuildArtifactApi::class.java)

    @Reusable
    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

}

