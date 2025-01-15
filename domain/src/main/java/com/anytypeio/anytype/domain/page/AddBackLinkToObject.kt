package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.ext.toObject
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.`object`.GetObject

/**
 * Add backlink from set or object itself to another object as a last block
 */
class AddBackLinkToObject(
    private val getObject: GetObject,
    private val createBlock: CreateBlock,
    private val closeBlock: CloseBlock,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<AddBackLinkToObject.Params, ObjectWrapper.Basic?>(dispatchers.io) {


    override suspend fun doWork(params: Params): ObjectWrapper.Basic? {
//        val openPageParams = GetObject.Params(
//            target = params.objectToPlaceLink,
//            saveAsLastOpened = params.saveAsLastOpened,
//            space = params.spaceId
//        )
//        getObject.run(openPageParams).fold(
//            onSuccess = { objectView ->
//                val targetBlock = objectView.blocks
//                    .firstOrNull { it.id == objectView.root }
//                    ?.children
//                    ?.last()
//
//                val objectDetails = objectView.details
//
//                require(targetBlock != null) { "Target block is missing" }
//
//                createBlock.run(
//                    CreateBlock.Params(
//                        context = params.objectToPlaceLink,
//                        target = targetBlock,
//                        position = Position.BOTTOM,
//                        prototype = Block.Prototype.Link(target = params.objectToLink)
//                    )
//                )
//
//                closeBlock.run(CloseBlock.Params(params.objectToPlaceLink, params.spaceId))
//
//                return objectDetails[params.objectToPlaceLink].toObject()
//            },
//            onFailure = {
//                throw IllegalStateException("object open error ${it.message}")
//            }
//        )
        return null
    }

    data class Params(
        val objectToLink: Id,
        val objectToPlaceLink: Id,
        val saveAsLastOpened: Boolean,
        val spaceId: SpaceId
    )
}