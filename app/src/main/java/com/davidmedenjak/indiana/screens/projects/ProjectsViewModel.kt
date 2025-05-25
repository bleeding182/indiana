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
import com.davidmedenjak.indiana.model.V0AppResponseItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val api: ApplicationApi,
) : ViewModel() {
    val pagedProjects = Pager(
        PagingConfig(pageSize = 50, initialLoadSize = 50)
    ) {
        ProjectsPagingSource(api)
    }.flow.cachedIn(viewModelScope)
}

class ProjectsPagingSource(
    val api: ApplicationApi,
) : PagingSource<String, V0AppResponseItemModel>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, V0AppResponseItemModel> {
        try {
            val response = api.appList(
                sortBy = SortByAppList.last_build_at,
                next = params.key,
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

    override fun getRefreshKey(state: PagingState<String, V0AppResponseItemModel>): String? {
        return null
    }
}
