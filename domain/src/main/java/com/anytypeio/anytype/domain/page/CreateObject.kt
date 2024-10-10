package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import javax.inject.Inject

/**
 * Use case for creating a new object
 */
class CreateObject @Inject constructor(
    private val repo: BlockRepository,
    private val getDefaultObjectType: GetDefaultObjectType,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateObject.Param, CreateObject.Result>(dispatchers.io) {

    override suspend fun doWork(params: Param): Result {
        if (params.type == null) {
            val defType = getDefaultObjectType.run(params.space)
            return createObject(
                space = params.space,
                typeKey = defType.type,
                template = defType.defaultTemplate,
                internalFlags = params.internalFlags,
                prefilled = emptyMap()
            )
        } else {
            return createObject(
                space = params.space,
                typeKey = params.type,
                template = params.template,
                internalFlags = params.internalFlags,
                prefilled = params.prefilled
            )
        }
    }

    private suspend fun createObject(
        space: SpaceId,
        typeKey: TypeKey,
        template: Id?,
        internalFlags: List<InternalFlags>,
        prefilled: Struct
    ): Result {

        val command = Command.CreateObject(
            space = space,
            typeKey = typeKey,
            template = template,
            prefilled = prefilled,
            internalFlags = internalFlags,
        )

        val result = repo.createObject(command)

        return Result(
            objectId = result.id,
            event = result.event,
            appliedTemplate = template,
            typeKey = typeKey,
            obj = ObjectWrapper.Basic(result.details)
        )
    }

    data class Param(
        val space: SpaceId,
        val type: TypeKey? = null,
        val template: Id? = null,
        val internalFlags: List<InternalFlags> = emptyList(),
        val prefilled: Struct = emptyMap()
    )

    data class Result(
        val objectId: Id,
        val event: Payload,
        val appliedTemplate: String? = null,
        val typeKey: TypeKey,
        val obj: ObjectWrapper.Basic
    )
}