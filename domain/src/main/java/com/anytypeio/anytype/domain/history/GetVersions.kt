package com.anytypeio.anytype.domain.history

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GetVersions @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository,
    private val logger: Logger
) {

    suspend fun doWork(params: Params): Flow<PagingData<Version>> =
        withContext(dispatchers.io) {
            Pager(
                config = PagingConfig(
                    pageSize = 10
                ),
                pagingSourceFactory = {
                    VersionsPagingSource(
                        repo = repo,
                        objectId = params.objectId,
                        logger = logger
                    )
                }
            ).flow
        }

//        val command = Command.VersionHistory.GetVersions(
//            objectId = params.objectId,
//            lastVersion = params.lastVersion,
//            limit = params.limit
//        )
//        return repo.getVersions(command)

    data class Params(
        val objectId: Id,
        val lastVersion: Id? = null,
        val limit: Int = 10
    )
}