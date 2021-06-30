package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetCompatibleObjectTypes(
    private val repo: BlockRepository
) : BaseUseCase<List<ObjectType>, GetCompatibleObjectTypes.Params>() {

    override suspend fun run(params: Params): Either<Throwable, List<ObjectType>> = safe {
        repo.getObjectTypes()
            .filter {
                it.smartBlockTypes.contains(params.smartBlockType)
            }
    }

    class Params(
        val smartBlockType: SmartBlockType
    )
}