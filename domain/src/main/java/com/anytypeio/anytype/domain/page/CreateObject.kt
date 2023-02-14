package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.templates.GetTemplates

/**
 * Use case for creating a new object
 */
class CreateObject(
    private val repo: BlockRepository,
    private val getDefaultEditorType: GetDefaultEditorType,
    private val getTemplates: GetTemplates
) : ResultInteractor<CreateObject.Param, CreateObject.Result>() {

    override suspend fun doWork(params: Param): Result {

        val type = params.type ?: getDefaultEditorType.run(Unit).type

        val template = if (type != null) {
            getTemplates.run(GetTemplates.Params(type = type)).singleOrNull()?.id
        } else {
            null
        }

        val internalFlags = if (template != null) {
            listOf()
        } else {
            listOf(InternalFlags.ShouldSelectType, InternalFlags.ShouldEmptyDelete)
        }

        val prefilled = buildMap {
            if (type != null) put(Relations.TYPE, type)
        }

        val command = Command.CreateObject(
            template = template,
            prefilled = prefilled,
            internalFlags = internalFlags
        )

        val result = repo.createObject(command)

        return Result(
            objectId = result.id,
            event = result.event,
            appliedTemplate = template,
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