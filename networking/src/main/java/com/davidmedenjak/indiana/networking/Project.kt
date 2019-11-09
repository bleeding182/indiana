package com.davidmedenjak.indiana.networking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
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

@JsonClass(generateAdapter = true)
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
    @Json(name = "triggered_at") val triggeredAt: Date,
    @Json(name = "triggered_workflow") val workflow: String
)

@JsonClass(generateAdapter = true)
data class Artifact(
    @Json(name = "title") val title: String,
    @Json(name = "slug") val slug: String
)

@JsonClass(generateAdapter = true)
data class ArtifactDetails(
    @Json(name = "title") val title: String,
    @Json(name = "slug") val slug: String,
    @Json(name = "expiring_download_url") val downloadUrl: String
)

@JsonClass(generateAdapter = true)
data class UserData(val data: User)

@JsonClass(generateAdapter = true)
data class ProjectData(val data: List<Project>)

@JsonClass(generateAdapter = true)
data class BuildData(val data: List<Build>)

@JsonClass(generateAdapter = true)
data class ArtifactData(val data: List<Artifact>)

@JsonClass(generateAdapter = true)
data class ArtifactDetailsData(val data: ArtifactDetails)

@JsonClass(generateAdapter = true)
data class User(val username: String)