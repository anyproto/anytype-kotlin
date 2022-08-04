package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeComparator
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for getting compatible object types with current smart block type.
 */
@Deprecated("Part of soon-to-be-deprecated API")
class GetCompatibleObjectTypes(
    private val repo: BlockRepository
) : BaseUseCase<List<ObjectType>, GetCompatibleObjectTypes.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.getObjectTypes()
            .filter { type ->
                validateObjectType(
                    type = type,
                    targetType = params.smartBlockType,
                    excludedTypes = params.excludedTypes
                )
            }
            .sortedWith(ObjectTypeComparator())
    }

    private fun validateObjectType(
        excludedTypes: List<Id>,
        targetType: SmartBlockType,
        type: ObjectType
    ) = (!excludedTypes.contains(type.url)
            && type.smartBlockTypes.contains(targetType)
            && !type.isArchived)

    /**
     * @property [smartBlockType] target smart block type for filtering purposes.
     */
    class Params(
        val smartBlockType: SmartBlockType,
        val excludedTypes: List<Id> = emptyList()
    )
}