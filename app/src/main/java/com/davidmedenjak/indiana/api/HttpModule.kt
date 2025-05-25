package com.davidmedenjak.indiana.api

import com.davidmedenjak.indiana.BuildConfig
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HttpModule {

    @Singleton
    @Provides
    fun providerOkHttp() = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
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
    fun provideMoshi() = Moshi.Builder()
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        moshi: Moshi,
        okHttpClient: Provider<OkHttpClient>
    ) = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .callFactory { okHttpClient.get().newCall(it) }
        .baseUrl("https://api.bitrise.io/v0.1/")
        .build()

    @Singleton
    @Provides
    @Named("Authorized")
    fun provideAuthorizedRetrofit(
        moshi: Moshi,
        @Named("Authorized") okHttpClient: Provider<OkHttpClient>
    ) = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
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

