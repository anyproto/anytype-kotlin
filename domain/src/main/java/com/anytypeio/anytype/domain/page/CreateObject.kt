package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

/**
 * Use case for creating a new object
 */
class CreateObject @Inject constructor(
    private val repo: BlockRepository,
    private val getDefaultPageType: GetDefaultPageType,
    private val getTemplates: GetTemplates,
    private val spaceManager: SpaceManager,
    private val configStorage: ConfigStorage,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateObject.Param, CreateObject.Result>(dispatchers.io) {

    override suspend fun doWork(params: Param): Result {

        val type = params.type ?: getDefaultPageType.run(Unit).type?.key

        val internalFlags = buildList {
            add(InternalFlags.ShouldSelectType)
            add(InternalFlags.ShouldSelectTemplate)
            add(InternalFlags.ShouldEmptyDelete)
        }

        val command = Command.CreateObject(
            template = null,
            prefilled = emptyMap(),
            internalFlags = internalFlags,
            space = spaceManager.get(),
            type = type.orEmpty()
        )

        val result = repo.createObject(command)

        return Result(
            objectId = result.id,
            event = result.event,
            appliedTemplate = null,
            type = type
        )
    }

    data class Param(
        val type: String?
    )

    data class Result(
        val objectId: Id,
        val event: Payload,
        val appliedTemplate: String? = null,
        val type: String? = null
    )
}