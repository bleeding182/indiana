package com.davidmedenjak.indiana.api

import com.squareup.moshi.Json
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.util.*

interface BitriseApi {

    @GET("v0.1/me")
    fun fetchMe(@Header("Authorization") tokenHeader: String): Flowable<UserData>

    @GET("v0.1/me/apps")
    fun fetchMyApps(): Flowable<ProjectData>

    @GET("v0.1/apps/{appSlug}/builds")
    fun fetchAppBuilds(@Path("appSlug") appSlug: String): Flowable<BuildData>

    @GET("v0.1/apps/{appSlug}/builds/{buildSlug}/artifacts")
    fun fetchBuildArtifacts(@Path("appSlug") appSlug: String, @Path("buildSlug") buildSlug: String): Flowable<ArtifactData>

    @GET("v0.1/apps/{appSlug}/builds/{buildSlug}/artifacts/{artifactSlug}")
    fun fetchArtifact(@Path("appSlug") appSlug: String, @Path("buildSlug") buildSlug: String, @Path("artifactSlug") artifactSlug: String): Flowable<ArtifactDetailsData>

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
        @Json(name = "slug") val slug: String,
        @Json(name = "abort_reason") val abortReason: String?,
        @Json(name = "commit_hash") val commitHash: String?,
        @Json(name = "commit_message") val commitMessage: String?,
        @Json(name = "is_on_hold") val isOnHold: Boolean,
        @Json(name = "environment_prepare_finished_at") val environmentPrepareFinishedAt: Date?,
        @Json(name = "finished_at") val finishedAt: Date?,
        @Json(name = "status") val status: Int,
        @Json(name = "tag") val tag: String?,
        @Json(name = "triggered_at") val triggeredAt: Date
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

data class UserData(val data: User)
data class ProjectData(val data: List<Project>)
data class BuildData(val data: List<Build>)
data class ArtifactData(val data: List<Artifact>)
data class ArtifactDetailsData(val data: ArtifactDetails)

data class User(val username: String)