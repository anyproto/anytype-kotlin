package com.agileburo.anytype.middleware.block

import anytype.Events
import anytype.model.Models
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.repo.block.BlockRemote
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.interactor.Middleware
import com.google.protobuf.Value
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class BlockMiddleware(
    private val middleware: Middleware,
    private val events: EventProxy
) : BlockRemote {

    override suspend fun observeBlocks() = events
        .flow()
        .filter { event -> event.messageCase == Events.Event.MessageCase.BLOCKSHOWFULLSCREEN }
        .map { event ->
            event.blockShowFullscreen.blocksList
                .filter { block -> block.contentCase == Models.Block.ContentCase.DASHBOARD }
                .map { block ->
                    BlockEntity(
                        id = block.id,
                        children = block.childrenIdsList.toList(),
                        fields = extractFields(block)
                    )
                }
        }

    override suspend fun openDashboard(contextId: String, id: String) {
        middleware.openDashboard(contextId, id)
    }

    private fun extractFields(block: Models.Block): BlockEntity.Fields {
        return BlockEntity.Fields().also { fields ->
            block.fields.fieldsMap.mapValues { (key, value) ->
                fields.map[key] = when (val case = value.kindCase) {
                    Value.KindCase.NUMBER_VALUE -> value.numberValue
                    Value.KindCase.STRING_VALUE -> value.stringValue
                    else -> throw IllegalStateException("$case is not supported.")
                }
            }
        }
    }
}