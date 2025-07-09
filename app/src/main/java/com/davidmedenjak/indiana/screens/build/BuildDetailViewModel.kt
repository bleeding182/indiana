package com.davidmedenjak.indiana.screens.build

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.davidmedenjak.indiana.download.ArtifactClickResult
import com.davidmedenjak.indiana.download.DownloadManager
import com.davidmedenjak.indiana.download.DownloadState
import com.davidmedenjak.indiana.download.FileOpener
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import com.davidmedenjak.indiana.model.V0ArtifactListElementResponseModel
import com.davidmedenjak.indiana.model.V0BuildAbortParams
import com.davidmedenjak.indiana.model.V0BuildResponseItemModel
import com.davidmedenjak.indiana.model.V0BuildTriggerParams
import com.davidmedenjak.indiana.model.V0BuildTriggerParamsBuildParams
import com.davidmedenjak.indiana.model.V0BuildTriggerParamsHookInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuildDetailViewModel @Inject constructor(
    private val application: Application,
    private val artifactApi: BuildArtifactApi,
    private val buildsApi: BuildsApi,
    private val downloadManager: DownloadManager,
    private val fileOpener: FileOpener,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    lateinit var navKey: BuildDetailGraph

    var buildDetails by mutableStateOf<V0BuildResponseItemModel?>(null)
        private set

    var isLoadingBuildDetails by mutableStateOf(false)
        private set

    var buildDetailsError by mutableStateOf<String?>(null)
        private set

    var newlyStartedBuild =
        savedStateHandle.getMutableStateFlow<BuildDetailGraph?>("newlyStartedBuild", null)

    // Track which download should auto-open (only one at a time)
    private var autoOpenDownloadId by mutableStateOf<String?>(null)

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

    suspend fun handleArtifactClick(artifact: V0ArtifactListElementResponseModel): ArtifactClickResult {
        val result = downloadManager.handleArtifactClick(
            artifact = artifact,
            appSlug = navKey.appSlug,
            buildSlug = navKey.buildSlug,
            projectId = navKey.appSlug // Using appSlug as projectId for now
        )
        
        // If we started a new download and it's an APK, set up auto-open
        if (result is ArtifactClickResult.DownloadStarted && 
            artifact.title?.endsWith(".apk", ignoreCase = true) == true) {
            
            // Clear any existing auto-open download
            autoOpenDownloadId = null
            
            // Set this download for auto-open
            autoOpenDownloadId = result.downloadId
            
            // Start monitoring this download for completion
            viewModelScope.launch {
                monitorDownloadForAutoOpen(result.downloadId)
            }
        }
        
        return result
    }

    fun getDownloadForArtifact(artifactId: String): Flow<DownloadState?> {
        return downloadManager.getDownloadByArtifactId(artifactId)
    }

    fun getDownloadsForBuild(): Flow<List<DownloadState>> {
        return downloadManager.getDownloadsByBuild(navKey.buildSlug)
    }
    
    private suspend fun monitorDownloadForAutoOpen(downloadId: String) {
        try {
            val completedDownload = downloadManager.getDownloadById(downloadId)
                .filterIsInstance<DownloadState.Completed>()
                .first()
                
            // Check if this download is still the one we want to auto-open
            if (autoOpenDownloadId == downloadId) {
                // Auto-install the completed APK
                fileOpener.installApk(application, completedDownload.localPath)
                // Clear the auto-open tracking
                autoOpenDownloadId = null
            }
        } catch (_: Exception) {
            // If monitoring fails, just clear the auto-open tracking
            autoOpenDownloadId = null
        }
    }
    
    // Call this when the user leaves the screen to cancel auto-open
    fun onScreenLeft() {
        autoOpenDownloadId = null
    }
    
    // Check if a specific download is set for auto-open
    fun isAutoOpenDownload(downloadId: String): Boolean {
        return autoOpenDownloadId == downloadId
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
