package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp.Params
import javax.inject.Inject

class GetDateObjectByTimestamp @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Params, Struct?>(dispatchers.io) {

    override suspend fun doWork(params: Params): Struct? {
        val command = Command.ObjectDateByTimestamp(
            space = params.space,
            timeInSeconds = params.timestampInSeconds
        )
        return repo.objectDateByTimestamp(command)
    }

    data class Params(val space: SpaceId, val timestampInSeconds: Long)
}