package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.base.CacheUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetObjectTypes(
    private val repo: BlockRepository
) : CacheUseCase<List<ObjectType>, Unit>() {

    override suspend fun run(
        params: Unit
    ): Either<Throwable, List<ObjectType>> = safe { repo.getTemplates() }
}