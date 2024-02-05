package com.anytypeio.anytype.domain.media

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class UploadFile @Inject constructor(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
) : ResultInteractor<UploadFile.Params, Id>(dispatchers.io) {

    override suspend fun doWork(params: Params) : Id = repo.uploadFile(
        command = Command.UploadFile(
            path = params.path,
            type = params.type,
            space = params.space
        )
    )

    data class Params(
        val path: String,
        val space: SpaceId,
        val type: Block.Content.File.Type = Block.Content.File.Type.FILE,
    )
}