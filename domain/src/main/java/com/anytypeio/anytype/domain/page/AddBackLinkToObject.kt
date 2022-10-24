package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.interactor.CreateBlock

/**
 * Add backlink from set or object itself to another object as a last block
 */
class AddBackLinkToObject(
    private val openPage: OpenPage,
    private val createBlock: CreateBlock,
    private val closeBlock: CloseBlock,
) : ResultInteractor<AddBackLinkToObject.Params, ObjectWrapper.Basic>() {


    override suspend fun doWork(params: Params): ObjectWrapper.Basic {
        when (val result = openPage.run(params.objectToPlaceLink)) {
            is Result.Success -> {
                val event = result.data
                    .events
                    .firstOrNull { event -> event is Event.Command.ShowObject }

                check(event is Event.Command.ShowObject) { "Event ShowObject is missing" }

                val targetBlock = event.blocks
                    .firstOrNull { it.id == params.objectToPlaceLink }
                    ?.children
                    ?.last()

                val objectDetails = event.details.details[params.objectToPlaceLink]?.map

                require(targetBlock != null) { "Target block is missing" }
                require(objectDetails != null) { "Object details is missing" }

                createBlock.run(
                    CreateBlock.Params(
                        context = params.objectToPlaceLink,
                        target = targetBlock,
                        position = Position.BOTTOM,
                        prototype = Block.Prototype.Link(params.objectToLink)
                    )
                )

                closeBlock.run(params.objectToPlaceLink)

                return ObjectWrapper.Basic(objectDetails)
            }
            is Result.Failure -> throw IllegalStateException("object open error ${result.error}")
        }
    }

    data class Params(
        val objectToLink: Id,
        val objectToPlaceLink: Id
    )
}