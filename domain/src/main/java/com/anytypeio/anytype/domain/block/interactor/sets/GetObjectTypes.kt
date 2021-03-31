package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetObjectTypes(private val repo: BlockRepository) : BaseUseCase<List<ObjectType>, Unit>() {

    override suspend fun run(
        params: Unit
    ): Either<Throwable, List<ObjectType>> = safe { repo.getTemplates() }
}