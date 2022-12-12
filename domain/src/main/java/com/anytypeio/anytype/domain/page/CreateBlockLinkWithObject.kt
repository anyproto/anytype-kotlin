package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.templates.GetTemplates

/**
 * UseCase for creating a new Object with block linked to this Object
 */

class CreateBlockLinkWithObject(
    private val repo: BlockRepository,
    private val getTemplates: GetTemplates
) : ResultInteractor<CreateBlockLinkWithObject.Params, CreateBlockLinkWithObject.Result>() {

    override suspend fun doWork(params: Params): Result {

        val prefilled = buildMap {
            put(Relations.TYPE, params.type)
        }

        val template = getTemplates.run(GetTemplates.Params(params.type)).singleOrNull()?.id

        val command = Command.CreateBlockLinkWithObject(
            context = params.context,
            target = params.target,
            position = params.position,
            prefilled = prefilled,
            template = template,
            internalFlags = listOf()
        )

        val result = repo.createBlockLinkWithObject(command)

        return Result(
            id = result.blockId,
            objectId = result.objectId,
            payload = result.event
        )
    }

    /**
     * Params for creating a new object
     * @property context id of the context of the block (i.e. page, dashboard or something else)
     * @property target id of the block associated with the block we need to create
     * @property position position of the block that we need to create in relation with the target block
     * @property type assumed type of new object
     */
    data class Params(
        val context: Id,
        val target: Id,
        val position: Position,
        val type: String
    )

    /**
     * Result for this use-case
     * @property id id of the new block (link)
     * @property objectId id of the new Object linked to this new block[id]
     * @property payload payload of events
     */
    data class Result(
        val id: Id,
        val objectId: Id,
        val payload: Payload
    )
}