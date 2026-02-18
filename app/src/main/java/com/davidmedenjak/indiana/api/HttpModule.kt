package com.davidmedenjak.indiana.api

import com.davidmedenjak.indiana.BuildConfig
import com.davidmedenjak.indiana.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

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
