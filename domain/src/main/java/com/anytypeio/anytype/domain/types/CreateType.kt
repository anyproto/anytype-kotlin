package com.anytypeio.anytype.domain.types

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

// TODO rename to "CreateObjectType" after refactoring
class CreateType(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateType.Params, ObjectWrapper.Type>(dispatchers.io) {
    override suspend fun doWork(params: Params): ObjectWrapper.Type {
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