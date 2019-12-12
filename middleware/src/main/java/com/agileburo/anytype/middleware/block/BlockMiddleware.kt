package com.agileburo.anytype.middleware.block

import anytype.Events
import anytype.model.Models
import anytype.model.Models.Block.Content.Dashboard
import anytype.model.Models.Block.Content.Page
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.model.CommandEntity
import com.agileburo.anytype.data.auth.model.ConfigEntity
import com.agileburo.anytype.data.auth.model.EventEntity
import com.agileburo.anytype.data.auth.repo.block.BlockRemote
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.interactor.Middleware
import com.google.protobuf.Value
import kotlinx.coroutines.flow.*

class BlockMiddleware(
    private val middleware: Middleware,
    private val events: EventProxy
) : BlockRemote {

    private val supportedEvents = listOf(
        Events.Event.Message.ValueCase.BLOCKSHOW,
        Events.Event.Message.ValueCase.BLOCKADD,
        Events.Event.Message.ValueCase.BLOCKSETTEXT
    )

    private val supportedTextStyles = listOf(
        Models.Block.Content.Text.Style.Paragraph,
        Models.Block.Content.Text.Style.Header1,
        Models.Block.Content.Text.Style.Header2,
        Models.Block.Content.Text.Style.Header3,
        Models.Block.Content.Text.Style.Title
    )

    private val supportedContent = listOf(
        Models.Block.ContentCase.DASHBOARD,
        Models.Block.ContentCase.PAGE,
        Models.Block.ContentCase.LAYOUT
    )

    override suspend fun getConfig(): ConfigEntity {
        return ConfigEntity(
            homeId = middleware.provideHomeDashboardId()
        )
    }

    override suspend fun observeEvents(): Flow<EventEntity> = events
        .flow()
        .filter { event ->
            event.messagesList.any { message ->
                supportedEvents.contains(message.valueCase)
            }
        }
        .map { event ->
            event.messagesList.filter { message ->
                supportedEvents.contains(message.valueCase)
            }
        }
        .flatMapConcat { event -> event.asFlow() }
        .mapNotNull { event ->
            when (event.valueCase) {
                Events.Event.Message.ValueCase.BLOCKADD -> {
                    EventEntity.Command.AddBlock(
                        blocks = event.blockAdd.blocksList.mapNotNull { block ->
                            when (block.contentCase) {
                                Models.Block.ContentCase.DASHBOARD -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList.toList(),
                                        fields = extractFields(block),
                                        content = extractDashboard(block)
                                    )
                                }
                                Models.Block.ContentCase.PAGE -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList.toList(),
                                        fields = extractFields(block),
                                        content = extractPage(block)
                                    )
                                }
                                Models.Block.ContentCase.TEXT -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList.toList(),
                                        fields = extractFields(block),
                                        content = extractText(block)
                                    )
                                }
                                Models.Block.ContentCase.LAYOUT -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList,
                                        fields = extractFields(block),
                                        content = extractLayout(block)
                                    )
                                }
                                Models.Block.ContentCase.IMAGE -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList,
                                        fields = extractFields(block),
                                        content = BlockEntity.Content.Image(
                                            path = block.image.localFilePath
                                        )
                                    )
                                }
                                else -> {
                                    null
                                }
                            }
                        }
                    )
                }
                Events.Event.Message.ValueCase.BLOCKSHOW -> {
                    EventEntity.Command.ShowBlock(
                        rootId = event.blockShow.rootId,
                        blocks = event.blockShow.blocksList.mapNotNull { block ->
                            when (block.contentCase) {
                                Models.Block.ContentCase.DASHBOARD -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList.toList(),
                                        fields = extractFields(block),
                                        content = extractDashboard(block)
                                    )
                                }
                                Models.Block.ContentCase.PAGE -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList.toList(),
                                        fields = extractFields(block),
                                        content = extractPage(block)
                                    )
                                }
                                Models.Block.ContentCase.TEXT -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList.toList(),
                                        fields = extractFields(block),
                                        content = extractText(block)
                                    )
                                }
                                Models.Block.ContentCase.LAYOUT -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList,
                                        fields = extractFields(block),
                                        content = extractLayout(block)
                                    )
                                }
                                Models.Block.ContentCase.IMAGE -> {
                                    BlockEntity(
                                        id = block.id,
                                        children = block.childrenIdsList,
                                        fields = extractFields(block),
                                        content = BlockEntity.Content.Image(
                                            path = block.image.localFilePath
                                        )
                                    )
                                }
                                else -> {
                                    null
                                }
                            }
                        }
                    )
                }
                Events.Event.Message.ValueCase.BLOCKSETTEXT -> {
                    EventEntity.Command.UpdateBlockText(
                        id = event.blockSetText.id,
                        text = event.blockSetText.text.value
                    )
                }
                else -> null
            }
        }

    override suspend fun observeBlocks() = events
        .flow()
        .filter { event ->
            event.messagesList.any { message ->
                message.valueCase == Events.Event.Message.ValueCase.BLOCKSHOW
            }
        }
        .map { event ->
            event.messagesList.filter { message ->
                message.valueCase == Events.Event.Message.ValueCase.BLOCKSHOW
            }
        }
        .flatMapConcat { event -> event.asFlow() }
        .map { event ->
            event.blockShow.blocksList
                .filter { block -> supportedContent.contains(block.contentCase) }
                .map { block ->
                    when (block.contentCase) {
                        Models.Block.ContentCase.DASHBOARD -> {
                            BlockEntity(
                                id = block.id,
                                children = block.childrenIdsList.toList(),
                                fields = extractFields(block),
                                content = extractDashboard(block)
                            )
                        }
                        Models.Block.ContentCase.PAGE -> {
                            BlockEntity(
                                id = block.id,
                                children = block.childrenIdsList.toList(),
                                fields = extractFields(block),
                                content = extractPage(block)
                            )
                        }
                        Models.Block.ContentCase.TEXT -> {
                            BlockEntity(
                                id = block.id,
                                children = block.childrenIdsList.toList(),
                                fields = extractFields(block),
                                content = extractText(block)
                            )
                        }
                        Models.Block.ContentCase.LAYOUT -> {
                            BlockEntity(
                                id = block.id,
                                children = block.childrenIdsList,
                                fields = extractFields(block),
                                content = extractLayout(block)
                            )
                        }
                        Models.Block.ContentCase.IMAGE -> {
                            BlockEntity(
                                id = block.id,
                                children = block.childrenIdsList,
                                fields = extractFields(block),
                                content = BlockEntity.Content.Image(
                                    path = block.image.localFilePath
                                )
                            )
                        }
                        else -> {
                            throw IllegalStateException("Unexpected content: ${block.contentCase}")
                        }
                    }

                }
        }

    override suspend fun observePages() = events
        .flow()
        .filter { event ->
            event.messagesList.any { message ->
                message.valueCase == Events.Event.Message.ValueCase.BLOCKSHOW
            }
        }
        .map { event ->
            event.messagesList.filter { message ->
                message.valueCase == Events.Event.Message.ValueCase.BLOCKSHOW
            }
        }
        .flatMapConcat { event -> event.asFlow() }
        .map { event ->
            event.blockShow.blocksList
                .filter { block -> block.contentCase == Models.Block.ContentCase.TEXT }
                .filter { block -> supportedTextStyles.contains(block.text.style) }
                .map { block ->
                    BlockEntity(
                        id = block.id,
                        children = block.childrenIdsList.toList(),
                        fields = extractFields(block),
                        content = extractText(block)
                    )
                }
        }

    override suspend fun openDashboard(contextId: String, id: String) {
        middleware.openDashboard(contextId, id)
    }

    override suspend fun closeDashboard(id: String) {
        middleware.closeDashboard(id)
    }

    override suspend fun createPage(parentId: String): String = middleware.createPage(parentId)

    override suspend fun openPage(id: String) {
        middleware.openBlock(id)
    }

    override suspend fun closePage(id: String) {
        middleware.closePage(id)
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

    override suspend fun update(update: CommandEntity.Update) {
        middleware.updateText(update.contextId, update.blockId, update.text)
    }

    override suspend fun create(command: CommandEntity.Create) {
        middleware.createBlock(
            command.contextId,
            command.targetId,
            command.position,
            command.block
        )
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

    private fun extractPage(block: Models.Block): BlockEntity.Content.Page {
        return BlockEntity.Content.Page(
            style = when {
                block.page.style == Page.Style.Empty -> {
                    BlockEntity.Content.Page.Style.EMPTY
                }
                block.page.style == Page.Style.Task -> {
                    BlockEntity.Content.Page.Style.TASK
                }
                block.page.style == Page.Style.Set -> {
                    BlockEntity.Content.Page.Style.SET
                }
                else -> throw IllegalStateException("Unexpected page style: ${block.page.style}")
            }
        )
    }

    private fun extractText(block: Models.Block): BlockEntity.Content.Text {
        return BlockEntity.Content.Text(
            text = block.text.text,
            marks = block.text.marks.marksList.map { mark ->
                BlockEntity.Content.Text.Mark(
                    range = IntRange(mark.range.from, mark.range.to),
                    // TODO parse parameter
                    param = null,
                    type = when (mark.type) {
                        Models.Block.Content.Text.Mark.Type.Bold -> {
                            BlockEntity.Content.Text.Mark.Type.BOLD
                        }
                        Models.Block.Content.Text.Mark.Type.Italic -> {
                            BlockEntity.Content.Text.Mark.Type.ITALIC
                        }
                        Models.Block.Content.Text.Mark.Type.Strikethrough -> {
                            BlockEntity.Content.Text.Mark.Type.STRIKETHROUGH
                        }
                        Models.Block.Content.Text.Mark.Type.Underscored -> {
                            BlockEntity.Content.Text.Mark.Type.UNDERSCORED
                        }
                        Models.Block.Content.Text.Mark.Type.Keyboard -> {
                            BlockEntity.Content.Text.Mark.Type.KEYBOARD
                        }
                        Models.Block.Content.Text.Mark.Type.TextColor -> {
                            BlockEntity.Content.Text.Mark.Type.TEXT_COLOR
                        }
                        Models.Block.Content.Text.Mark.Type.BackgroundColor -> {
                            BlockEntity.Content.Text.Mark.Type.BACKGROUND_COLOR
                        }
                        else -> throw IllegalStateException("Unexpected mark type: ${mark.type.name}")
                    }
                )
            },
            style = when (block.text.style) {
                Models.Block.Content.Text.Style.Paragraph -> BlockEntity.Content.Text.Style.P
                Models.Block.Content.Text.Style.Header1 -> BlockEntity.Content.Text.Style.H1
                Models.Block.Content.Text.Style.Header2 -> BlockEntity.Content.Text.Style.H2
                Models.Block.Content.Text.Style.Header3 -> BlockEntity.Content.Text.Style.H3
                Models.Block.Content.Text.Style.Title -> BlockEntity.Content.Text.Style.TITLE
                Models.Block.Content.Text.Style.Quote -> BlockEntity.Content.Text.Style.QUOTE
                Models.Block.Content.Text.Style.Marked -> BlockEntity.Content.Text.Style.BULLET
                Models.Block.Content.Text.Style.Numbered -> BlockEntity.Content.Text.Style.NUMBERED
                Models.Block.Content.Text.Style.Toggle -> BlockEntity.Content.Text.Style.TOGGLE
                else -> throw IllegalStateException("Unexpected text style: ${block.text.style}")
            }
        )
    }

    private fun extractLayout(block: Models.Block): BlockEntity.Content.Layout {
        return BlockEntity.Content.Layout(
            type = when {
                block.layout.style == Models.Block.Content.Layout.Style.Column -> {
                    BlockEntity.Content.Layout.Type.COLUMN
                }
                block.layout.style == Models.Block.Content.Layout.Style.Row -> {
                    BlockEntity.Content.Layout.Type.ROW
                }
                else -> throw IllegalStateException("Unexpected layout style: ${block.layout.style}")
            }
        )
    }
}