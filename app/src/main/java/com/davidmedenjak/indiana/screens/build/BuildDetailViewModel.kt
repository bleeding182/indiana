package com.davidmedenjak.indiana.screens.build

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.davidmedenjak.indiana.api.BuildArtifactApi
import com.davidmedenjak.indiana.api.BuildsApi
import com.davidmedenjak.indiana.features.artifacts.DownloadBroadcastReceiver
import com.davidmedenjak.indiana.model.V0ArtifactListElementResponseModel
import com.davidmedenjak.indiana.model.V0BuildAbortParams
import com.davidmedenjak.indiana.model.V0BuildResponseItemModel
import com.davidmedenjak.indiana.model.V0BuildTriggerParams
import com.davidmedenjak.indiana.model.V0BuildTriggerParamsBuildParams
import com.davidmedenjak.indiana.model.V0BuildTriggerParamsHookInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuildDetailViewModel @Inject constructor(
    private val application: Application,
    private val artifactApi: BuildArtifactApi,
    private val buildsApi: BuildsApi,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    lateinit var navKey: BuildDetailGraph

    var buildDetails by mutableStateOf<V0BuildResponseItemModel?>(null)
        private set

    var isLoadingBuildDetails by mutableStateOf(false)
        private set

    var buildDetailsError by mutableStateOf<String?>(null)
        private set

    var newlyStartedBuild = savedStateHandle.getMutableStateFlow<BuildDetailGraph?>("newlyStartedBuild", null)

    val pagedArtifacts = Pager(
        PagingConfig(pageSize = 15, initialLoadSize = 20)
    ) {
        BuildDetailPagingSource(
            artifactApi,
            appSlug = navKey.appSlug,
            buildSlug = navKey.buildSlug
        )
    }.flow.cachedIn(viewModelScope)

    fun loadBuildDetails() {
        if (isLoadingBuildDetails) return

        viewModelScope.launch {
            isLoadingBuildDetails = true
            buildDetailsError = null

            try {
                val response = buildsApi.buildShow(
                    appSlug = navKey.appSlug,
                    buildSlug = navKey.buildSlug
                )
                buildDetails = response.data
            } catch (e: Exception) {
                buildDetailsError = e.message
            } finally {
                isLoadingBuildDetails = false
            }
        }
    }

    fun abortBuild(reason: String? = null) {
        viewModelScope.launch {
            try {
                buildsApi.buildAbort(
                    appSlug = navKey.appSlug,
                    buildSlug = navKey.buildSlug,
                    buildAbortParams = V0BuildAbortParams(
                        abortReason = reason ?: "",
                        abortWithSuccess = false,
                        skipNotifications = false
                    )
                )
                // Reload build details to reflect the abort
                loadBuildDetails()
            } catch (e: Exception) {
                buildDetailsError = "Failed to abort build: ${e.message}"
            }
        }
    }

    fun restartBuild() {
        val currentBuild = buildDetails ?: return

        viewModelScope.launch {
            try {
                val triggerParams = V0BuildTriggerParams(
                    buildParams = V0BuildTriggerParamsBuildParams(
                        branch = currentBuild.branch,
                        workflowId = currentBuild.triggeredWorkflow,
                        commitHash = currentBuild.commitHash,
                        commitMessage = "Restarted build #${currentBuild.buildNumber}",
                        tag = currentBuild.tag
                    ),
                    hookInfo = V0BuildTriggerParamsHookInfo(
                        type = "bitrise",
                    )
                )

                val result = buildsApi.buildTrigger(
                    appSlug = navKey.appSlug,
                    buildParams = triggerParams,
                )

                // Could potentially navigate to the new build, but for now just show success
                buildDetailsError = null
                newlyStartedBuild.value = navKey.copy(
                    buildTitle = result.buildNumber.toString(),
                    buildSlug = result.buildSlug!!,
                )
            } catch (e: Exception) {
                buildDetailsError = "Failed to restart build: ${e.message}"
            }
        }
    }

    suspend fun downloadArtifact(artifact: V0ArtifactListElementResponseModel) {
        val details = artifactApi.artifactShow(
            navKey.appSlug,
            buildSlug = navKey.buildSlug,
            artifactSlug = artifact.slug ?: return,
            download = null,
        ).data ?: return
        val context = application
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUrl = details.expiringDownloadUrl?.toUri() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val request = DownloadManager.Request(downloadUrl)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle(artifact.title)
            downloadManager.enqueue(request)
        } else {
            val request = DownloadManager.Request(downloadUrl)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setTitle(artifact.title)

            val downloadId = downloadManager.enqueue(request)
            ContextCompat.registerReceiver(
                context,
                DownloadBroadcastReceiver(downloadId),
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }
}

class BuildDetailPagingSource(
    val api: BuildArtifactApi,
    val appSlug: String,
    val buildSlug: String,
) : PagingSource<String, V0ArtifactListElementResponseModel>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, V0ArtifactListElementResponseModel> {
        try {
            val response = api.artifactList(
                next = params.key,
                appSlug = appSlug,
                buildSlug = buildSlug,
                limit = params.loadSize
            )

            return LoadResult.Page(
                data = response.data ?: emptyList(),
                prevKey = null,
                nextKey = response.paging?.next,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, V0ArtifactListElementResponseModel>): String? {
        return null
    }
}
