package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use case for storing in memory cache all object types(archived or not) of account
 */
class StoreObjectTypes(
    private val repo: BlockRepository,
    private val objectTypesProvider: ObjectTypesProvider
) : BaseUseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Throwable, Unit> = safe {
        val objectTypes = repo.getObjectTypes()
        objectTypesProvider.set(objectTypes)
    }
}