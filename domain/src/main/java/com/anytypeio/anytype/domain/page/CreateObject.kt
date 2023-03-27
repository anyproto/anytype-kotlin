package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.templates.GetTemplates

/**
 * Use case for creating a new object
 */
class CreateObject(
    private val repo: BlockRepository,
    private val getDefaultPageType: GetDefaultPageType,
    private val getTemplates: GetTemplates,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateObject.Param, CreateObject.Result>(dispatchers.io) {

    override suspend fun doWork(params: Param): Result {

        val type = params.type ?: getDefaultPageType.run(Unit).type

        val objectTemplates = if (type != null) {
            getTemplates.run(GetTemplates.Params(type = type))
        } else {
            null
        }

        val template = objectTemplates?.singleOrNull()?.id

        val internalFlags = buildList {
            if (objectTemplates != null && objectTemplates.size > 1) {
                add(InternalFlags.ShouldSelectTemplate)
            } else {
                if (template == null) {
                    add(InternalFlags.ShouldSelectType)
                }
            }
            add(InternalFlags.ShouldEmptyDelete)
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