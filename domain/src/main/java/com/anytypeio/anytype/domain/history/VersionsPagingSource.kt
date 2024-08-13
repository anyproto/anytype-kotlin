package com.anytypeio.anytype.domain.history

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject

class VersionsPagingSource(
    private val repo: BlockRepository,
    private val objectId: String,
    private val logger: Logger
) : PagingSource<String, Version>() {

    init {
        logger.logWarning("init 1983")
    }

    override fun getRefreshKey(state: PagingState<String, Version>): String? {
        logger.logWarning("getRefreshKey")
        return state.anchorPosition?.let { position ->
            logger.logWarning("position :$position")
            state.closestPageToPosition(position)?.data?.lastOrNull()?.id
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Version> =
        try {
            val command = Command.VersionHistory.GetVersions(
                objectId = objectId,
                lastVersion = params.key,
                limit = 100
            )
            val result = repo.getVersions(command)
            LoadResult.Page(
                data = result,
                prevKey = params.key,
                nextKey = result.lastOrNull()?.id
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
}