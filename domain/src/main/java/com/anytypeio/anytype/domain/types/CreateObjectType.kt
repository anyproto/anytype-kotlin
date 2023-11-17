package com.anytypeio.anytype.domain.types

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class CreateObjectType(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateObjectType.Params, Struct?>(dispatchers.io) {
    override suspend fun doWork(params: Params): Struct? {
        return repo.createType(
            space = params.space,
            name = params.name,
            emojiUnicode = params.emojiUnicode
        )
    }

    class Params(
        val space: Id,
        val name: String,
        val emojiUnicode: String? = null
    )
}