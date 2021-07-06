package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for getting compatible object types with current smart block type.
 */
class GetCompatibleObjectTypes(
    private val repo: BlockRepository
) : BaseUseCase<List<ObjectType>, GetCompatibleObjectTypes.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.getObjectTypes().filter { oType ->
            oType.smartBlockTypes.contains(params.smartBlockType) && !oType.isArchived
        }
    }

    /**
     * @property [smartBlockType] target smart block type for filtering purposes.
     */
    class Params(
        val smartBlockType: SmartBlockType
    )
}