package com.davidmedenjak.indiana.api

import com.squareup.moshi.Json
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface BitriseApi {

    @GET("v0.1/me")
    fun fetchMe(@Header("Authorization") tokenHeader: String): Flowable<Data<User>>

    @GET("v0.1/me/apps")
    fun fetchMyApps(): Flowable<Data<List<Project>>>

    @GET("v0.1/apps/{appSlug}/builds")
    fun fetchAppBuilds(@Path("appSlug") appSlug: String): Flowable<Data<List<Build>>>

    @GET("v0.1/apps/{appSlug}/builds/{buildSlug}/artifacts")
    fun fetchBuildArtifacts(@Path("appSlug") appSlug: String, @Path("buildSlug") buildSlug: String): Flowable<Data<List<Artifact>>>

    @GET("v0.1/apps/{appSlug}/builds/{buildSlug}/artifacts/{artifactSlug}")
    fun fetchArtifact(@Path("appSlug") appSlug: String, @Path("buildSlug") buildSlug: String,@Path("artifactSlug") artifactSlug: String): Flowable<Data<ArtifactDetails>>

}

data class Project(
        @Json(name = "is_disabled") val isDisabled: Boolean,
        @Json(name = "project_type") val projectType: String,
        @Json(name = "provider") val provider: String,
        @Json(name = "repo_owner") val repoOwner: String,
        @Json(name = "repo_slug") val repoSlug: String,
        @Json(name = "repo_url") val repoUrl: String,
        @Json(name = "slug") val slug: String,
        @Json(name = "title") val title: String
)

data class Build(
        @Json(name = "branch") val branch: String,
        @Json(name = "build_number") val buildNumber: Int,
        @Json(name = "slug") val slug: String
)


data class Artifact(
        @Json(name = "title") val title: String,
        @Json(name = "slug") val slug: String
)

data class ArtifactDetails(
        @Json(name = "title") val title: String,
        @Json(name = "slug") val slug: String,
        @Json(name = "expiring_download_url") val downloadUrl: String
)

data class Data<T>(val data: T)

data class User(val username: String)