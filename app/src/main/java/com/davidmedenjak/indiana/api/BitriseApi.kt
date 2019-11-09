package com.davidmedenjak.indiana.api

import androidx.annotation.StringDef
import com.davidmedenjak.indiana.networking.*
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface BitriseApi {

    @GET("v0.1/me")
    fun fetchMe(@Header("Authorization") tokenHeader: String): Flowable<UserData>

    @GET("v0.1/me/apps")
    fun fetchMyApps(@ProjectSort.Type @Query("sort_by") sortedBy: String = ProjectSort.LAST_BUILD_AT): Flowable<ProjectData>

    @GET("v0.1/apps/{appSlug}/builds")
    fun fetchAppBuilds(@Path("appSlug") appSlug: String): Flowable<BuildData>

    @GET("v0.1/apps/{appSlug}/builds/{buildSlug}/artifacts")
    fun fetchBuildArtifacts(@Path("appSlug") appSlug: String, @Path("buildSlug") buildSlug: String): Flowable<ArtifactData>

    @GET("v0.1/apps/{appSlug}/builds/{buildSlug}/artifacts/{artifactSlug}")
    fun fetchArtifact(@Path("appSlug") appSlug: String, @Path("buildSlug") buildSlug: String, @Path("artifactSlug") artifactSlug: String): Flowable<ArtifactDetailsData>

}

object ProjectSort {
    const val CREATED_AT = "created_at"
    const val LAST_BUILD_AT = "last_build_at"

    @StringDef(CREATED_AT, LAST_BUILD_AT)
    annotation class Type
}
