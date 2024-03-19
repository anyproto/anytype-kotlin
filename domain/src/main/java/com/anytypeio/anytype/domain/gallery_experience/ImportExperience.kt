package com.anytypeio.anytype.domain.gallery_experience

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ImportExperience @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<ImportExperience.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload {
        val command = Command.ImportExperience(
            space = params.spaceId,
            url = params.url,
            title = params.title,
            isNewSpace = params.isNewSpace
        )
        return repo.importExperience(command)
    }

    data class Params(
        val spaceId: SpaceId,
        val url: String,
        val title: String,
        val isNewSpace: Boolean
    )
}