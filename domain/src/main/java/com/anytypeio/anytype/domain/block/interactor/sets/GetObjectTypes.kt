package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.base.CacheUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetObjectTypes(
    private val repo: BlockRepository
) : CacheUseCase<List<ObjectType>, GetObjectTypes.Params>() {

    override suspend fun run(
        params: Params
    ): Either<Throwable, List<ObjectType>> = safe {
        val objectTypes = repo.getObjectTypes()
        if (params.filterArchivedObjects) {
            objectTypes.filter { !it.isArchived }
        } else {
            objectTypes
        }
    }

    /**
     * @param filterArchivedObjects if true, filter object types by not archived types
     */
    class Params(
        val filterArchivedObjects: Boolean
    )
}