package com.anytypeio.anytype.domain.media

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class FileDrop @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<FileDrop.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload = repo.fileDrop(
        command = Command.FileDrop(
            ctx = params.ctx,
            space = params.space,
            dropTarget = params.dropTarget,
            blockPosition = params.blockPosition,
            localFilePaths = params.localFilePaths
        )
    )

    data class Params(
        val space: SpaceId,
        val ctx: Id,
        val dropTarget: Id,
        val blockPosition: Position,
        val localFilePaths: List<String>
    )
}