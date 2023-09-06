package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

/**
 * Use case for creating a new object
 */
class CreateObject @Inject constructor(
    private val repo: BlockRepository,
    private val getDefaultPageType: GetDefaultPageType,
    private val spaceManager: SpaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateObject.Param, CreateObject.Result>(dispatchers.io) {

    override suspend fun doWork(params: Param): Result {

        val type = params.type ?: getDefaultPageType.run(Unit).type

        requireNotNull(type) { "Type is undefined" }

        val internalFlags = buildList {
            add(InternalFlags.ShouldSelectType)
            add(InternalFlags.ShouldSelectTemplate)
            add(InternalFlags.ShouldEmptyDelete)
        }

        val command = Command.CreateObject(
            template = null,
            prefilled = emptyMap(),
            internalFlags = internalFlags,
            space = SpaceId(spaceManager.get()),
            type = type
        )

        val result = repo.createObject(command)

        return Result(
            objectId = result.id,
            event = result.event,
            appliedTemplate = null,
            type = type
        )
    }

    data class Param(val type: TypeKey? = null)

    data class Result(
        val objectId: Id,
        val event: Payload,
        val appliedTemplate: String? = null,
        val type: TypeKey
    )
}