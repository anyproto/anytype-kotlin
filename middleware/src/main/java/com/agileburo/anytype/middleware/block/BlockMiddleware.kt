package com.agileburo.anytype.middleware.block

import anytype.Events
import anytype.model.Models
import anytype.model.Models.Block.Content.Dashboard
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.repo.block.BlockRemote
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.interactor.Middleware
import com.google.protobuf.Value
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

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
                        fields = extractFields(block),
                        content = extractDashboard(block)
                    )
                }
        }

    override suspend fun observePages() = events
        .flow()
        .filter { event -> event.messageCase == Events.Event.MessageCase.BLOCKSHOWFULLSCREEN }
        .onEach { Timber.d("Event: $it") }
        .filter { event -> event.blockShowFullscreen.rootId == "testpage" }
        .map { event ->
            event.blockShowFullscreen.blocksList
                .filter { block -> block.contentCase == Models.Block.ContentCase.TEXT }
                .filter { block ->
                    block.text.style == Models.Block.Content.Text.Style.P
                            || block.text.style == Models.Block.Content.Text.Style.H1
                            || block.text.style == Models.Block.Content.Text.Style.H2
                            || block.text.style == Models.Block.Content.Text.Style.H3
                            || block.text.style == Models.Block.Content.Text.Style.Title
                }
                .map { block ->
                    BlockEntity(
                        id = block.id,
                        children = block.childrenIdsList.toList(),
                        fields = extractFields(block),
                        content = extractText(block)
                    )
                }
        }

    private fun extractDashboard(block: Models.Block): BlockEntity.Content.Dashboard {
        return BlockEntity.Content.Dashboard(
            type = when {
                block.dashboard.style == Dashboard.Style.Archive -> {
                    BlockEntity.Content.Dashboard.Type.ARCHIVE
                }
                block.dashboard.style == Dashboard.Style.MainScreen -> {
                    BlockEntity.Content.Dashboard.Type.MAIN_SCREEN
                }
                else -> throw IllegalStateException("Unexpected dashboard style: ${block.dashboard.style}")
            }
        )
    }

    private fun extractText(block: Models.Block): BlockEntity.Content.Text {
        return BlockEntity.Content.Text(
            text = block.text.text,
            marks = emptyList(),
            style = when (block.text.style) {
                Models.Block.Content.Text.Style.P -> BlockEntity.Content.Text.Style.P
                Models.Block.Content.Text.Style.H1 -> BlockEntity.Content.Text.Style.H1
                Models.Block.Content.Text.Style.H2 -> BlockEntity.Content.Text.Style.H2
                Models.Block.Content.Text.Style.H3 -> BlockEntity.Content.Text.Style.H3
                Models.Block.Content.Text.Style.Title -> BlockEntity.Content.Text.Style.TITLE
                Models.Block.Content.Text.Style.Quote -> BlockEntity.Content.Text.Style.QUOTE
                else -> TODO()
            }
        )
    }

    override suspend fun openDashboard(contextId: String, id: String) {
        middleware.openDashboard(contextId, id)
    }

    override suspend fun openPage(id: String) {
        middleware.openBlock(id)
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