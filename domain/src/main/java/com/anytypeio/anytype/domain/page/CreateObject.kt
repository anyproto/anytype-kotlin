package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider

class CreateObject(
    private val repo: BlockRepository,
    private val documentEmojiProvider: DocumentEmojiIconProvider
) : BaseUseCase<CreateObject.Result, CreateObject.Params>() {

    override suspend fun run(params: Params) = try {
        repo.createDocument(
            command = Command.CreateDocument(
                context = params.context,
                target = params.target,
                position = params.position,
                emoji = null,
                type = params.type,
                layout = params.layout,
                template = params.template
            )
        ).let { (id, target, payload) ->
            Either.Right(
                Result(
                    id = id,
                    target = target,
                    payload = payload
                )
            )
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for creating a new object
     * @property context id of the context of the block (i.e. page, dashboard or something else)
     * @property target id of the block associated with the block we need to create
     * @property position position of the block that we need to create in relation with the target block
     * @property [template] id of the template for this object (optional)
     */
    data class Params(
        val context: Id,
        val target: Id,
        val position: Position,
        val type: String,
        val layout: ObjectType.Layout,
        val template: Id? = null
    )

    /**
     * Result for this use-case
     * @property id id of the new block (link)
     * @property target id of the target for this new block
     * @property payload payload of events
     */
    data class Result(
        val id: Id,
        val target: Id,
        val payload: Payload
    )
}