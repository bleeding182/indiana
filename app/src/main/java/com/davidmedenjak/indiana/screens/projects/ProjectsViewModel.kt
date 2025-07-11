package com.davidmedenjak.indiana.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.davidmedenjak.indiana.api.ApplicationApi
import com.davidmedenjak.indiana.api.ApplicationApi.SortByAppList
import com.davidmedenjak.indiana.app.UserSettings
import com.davidmedenjak.indiana.db.AppDatabase
import com.davidmedenjak.indiana.db.ProjectEntity
import com.davidmedenjak.indiana.db.ProjectLastViewed
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val api: ApplicationApi,
    private val database: AppDatabase,
    private val userSettings: UserSettings,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {
    fun updateRecents(project: Project) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                val projectLastViewed = ProjectLastViewed(
                    id = project.id,
                    lastViewed = Instant.now()
                )
                database.projects().updateLastViewed(projectLastViewed)
            }
        }
    }

    val recents = database.projects().lastViewed(5)
        .map { projects ->
            projects.map {
                Project(
                    id = it.id,
                    avatar = it.avatar,
                    name = it.name,
                    projectType = it.projectType,
                )
            }
        }
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null)

    val filteredProjectTypes: MutableStateFlow<String?> =
        MutableStateFlow(userSettings.projectFiler)

    val projectTypes: StateFlow<List<String>?> = database.projects().projectTypes()
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null)

    fun setFilterProjectType(projectType: String) {
        val isSelected = filteredProjectTypes.value != projectType
        
        analytics.logEvent("filter_project_type", android.os.Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, projectType)
            putBoolean(FirebaseAnalytics.Param.VALUE, isSelected)
        })
        
        userSettings.projectFiler = projectType
        filteredProjectTypes.update {
            if (it == projectType) {
                null
            } else {
                projectType
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedProjects = filteredProjectTypes.flatMapLatest { filter ->
        Pager(PagingConfig(pageSize = 50, initialLoadSize = 50)) {
            ProjectsPagingSource(api, database, filter)
        }.flow
    }.cachedIn(viewModelScope)
}

class ProjectsPagingSource(
    val api: ApplicationApi,
    private val database: AppDatabase,
    private val filter: String?,
) : PagingSource<String, Project>() {

    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, Project> {
        try {
            val response = api.appList(
                sortBy = SortByAppList.last_build_at,
                next = params.key,
                limit = params.loadSize,
                projectType = filter
            )
            coroutineScope {
                launch(Dispatchers.IO) {
                    val projects = response.data
                        ?.map {
                            ProjectEntity(
                                id = it.slug!!,
                                avatar = it.avatarUrl,
                                name = it.title,
                                projectType = it.projectType,
                            )
                        }
                        ?: return@launch
                    database.projects().upsert(projects)
                }
            }

            return LoadResult.Page(
                data = response.data?.map {
                    Project(
                        id = it.slug!!,
                        avatar = it.avatarUrl,
                        name = it.title,
                        projectType = it.projectType,
                    )
                } ?: emptyList(),
                prevKey = null,
                nextKey = response.paging?.next,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Project>): String? {
        return null
    }
}
