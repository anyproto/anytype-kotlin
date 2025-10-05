package com.anytypeio.anytype.domain.media

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Discards preloaded file by ID.
 */
class DiscardPreloadedFile @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Id, Unit>(dispatchers.io) {
    override suspend fun doWork(params: Id) {
        repo.discardPreloadedFile(
            command = Command.DiscardPreloadedFile(params)
        )
    }
}