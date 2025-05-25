package com.davidmedenjak.indiana.screens.project

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.davidmedenjak.indiana.api.BuildsApi
import com.davidmedenjak.indiana.model.V0BuildResponseItemModel
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val api: BuildsApi,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    lateinit var navKey: ProjectDetailGraph

    val pagedBuilds = Pager(
        PagingConfig(pageSize = 15, initialLoadSize = 20)
    ) {
        ProjectDetailPagingSource(api, navKey.slug)
    }.flow.cachedIn(viewModelScope)
}

class ProjectDetailPagingSource(
    val api: BuildsApi,
    val slug: String,
) : PagingSource<String, V0BuildResponseItemModel>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, V0BuildResponseItemModel> {
        try {
            val response = api.buildList(next = params.key, appSlug = slug, limit = params.loadSize)

            return LoadResult.Page(
                data = response.data ?: emptyList(),
                prevKey = null,
                nextKey = response.paging?.next,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, V0BuildResponseItemModel>): String? {
        return null
    }
}
