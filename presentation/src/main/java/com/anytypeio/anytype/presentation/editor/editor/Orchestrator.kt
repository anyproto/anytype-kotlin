package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.UpdateDivider
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.DuplicateBlock
import com.anytypeio.anytype.domain.block.interactor.MergeBlocks
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.interactor.ReplaceBlock
import com.anytypeio.anytype.domain.block.interactor.SetObjectType
import com.anytypeio.anytype.domain.block.interactor.SplitBlock
import com.anytypeio.anytype.domain.block.interactor.TurnIntoDocument
import com.anytypeio.anytype.domain.block.interactor.TurnIntoStyle
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.domain.block.interactor.UpdateAlignment
import com.anytypeio.anytype.domain.block.interactor.UpdateBackgroundColor
import com.anytypeio.anytype.domain.block.interactor.UpdateBlocksMark
import com.anytypeio.anytype.domain.block.interactor.UpdateCheckbox
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.interactor.UpdateTextColor
import com.anytypeio.anytype.domain.block.interactor.UpdateTextStyle
import com.anytypeio.anytype.domain.block.interactor.UploadBlock
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.dataview.interactor.SetRelationKey
import com.anytypeio.anytype.domain.download.DownloadFile
import com.anytypeio.anytype.domain.editor.Editor.Cursor
import com.anytypeio.anytype.domain.editor.Editor.Focus
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo
import com.anytypeio.anytype.domain.page.bookmark.CreateBookmarkBlock
import com.anytypeio.anytype.domain.page.bookmark.SetupBookmark
import com.anytypeio.anytype.domain.table.CreateTable
import com.anytypeio.anytype.domain.table.FillTableRow
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.extension.sendAnalyticsChangeTextBlockStyleEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsCopyBlockEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsCreateBlockEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsDeleteBlockEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsDownloadMediaEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsDuplicateBlockEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsPasteBlockEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRedoEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsReorderBlockEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSplitBlockEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsUndoEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsUploadMediaEvent
import timber.log.Timber

class Orchestrator(
    private val createBlock: CreateBlock,
    private val replaceBlock: ReplaceBlock,
    private val updateTextColor: UpdateTextColor,
    private val updateBackgroundColor: UpdateBackgroundColor,
    private val duplicateBlock: DuplicateBlock,
    private val splitBlock: SplitBlock,
    private val updateDivider: UpdateDivider,
    private val mergeBlocks: MergeBlocks,
    private val unlinkBlocks: UnlinkBlocks,
    private val updateTextStyle: UpdateTextStyle,
    private val turnIntoStyle: TurnIntoStyle,
    private val updateCheckbox: UpdateCheckbox,
    private val downloadFile: DownloadFile,
    val updateText: UpdateText,
    private val updateAlignment: UpdateAlignment,
    private val uploadBlock: UploadBlock,
    private val setupBookmark: SetupBookmark,
    private val createBookmarkBlock: CreateBookmarkBlock,
    private val turnIntoDocument: TurnIntoDocument,
    private val updateFields: UpdateFields,
    private val createTable: CreateTable,
    private val fillTableRow: FillTableRow,
    private val move: Move,
    private val copy: Copy,
    private val paste: Paste,
    private val undo: Undo,
    private val redo: Redo,
    private val setRelationKey: SetRelationKey,
    private val updateBlocksMark: UpdateBlocksMark,
    private val setObjectType: SetObjectType,
    val memory: Editor.Memory,
    val stores: Editor.Storage,
    val proxies: Editor.Proxer,
    val textInteractor: Interactor.TextInteractor,
    private val analytics: Analytics
) {

    private val defaultOnError: suspend (Throwable) -> Unit = { Timber.e(it) }

    suspend fun start() {
        proxies.intents.stream().collect { intent ->
            when (intent) {
                is Intent.CRUD.Create -> {
                    val startTime = System.currentTimeMillis()
                    createBlock(
                        params = CreateBlock.Params(
                            context = intent.context,
                            target = intent.target,
                            prototype = intent.prototype,
                            position = intent.position
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { (id, payload) ->
                            val middlewareTime = System.currentTimeMillis()
                            stores.focus.update(Focus.id(id = id))
                            proxies.payloads.send(payload)
                            analytics.sendAnalyticsCreateBlockEvent(
                                prototype = intent.prototype,
                                startTime = startTime,
                                middlewareTime = middlewareTime
                            )
                        }
                    )
                }
                is Intent.CRUD.Replace -> {
                    val startTime = System.currentTimeMillis()
                    replaceBlock(
                        params = ReplaceBlock.Params(
                            context = intent.context,
                            target = intent.target,
                            prototype = intent.prototype
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { (id, payload) ->
                            val middlewareTime = System.currentTimeMillis()
                            stores.focus.update(Focus.id(id = id))
                            proxies.payloads.send(payload)
                            analytics.sendAnalyticsCreateBlockEvent(
                                prototype = intent.prototype,
                                startTime = startTime,
                                middlewareTime = middlewareTime
                            )
                        }
                    )
                }
                is Intent.CRUD.Duplicate -> {
                    duplicateBlock(
                        params = DuplicateBlock.Params(
                            context = intent.context,
                            blocks = intent.blocks,
                            target = intent.target
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { (ids, payload) ->
                            stores.focus.update(Focus(id = ids.last(), cursor = Cursor.End))
                            proxies.payloads.send(payload)
                            analytics.sendAnalyticsDuplicateBlockEvent(intent.blocks.size)
                        }
                    )
                }
                is Intent.CRUD.Unlink -> {
                    unlinkBlocks(
                        params = UnlinkBlocks.Params(
                            context = intent.context,
                            targets = intent.targets
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val focus = intent.previous ?: intent.next
                            if (focus != null) {
                                stores.focus.update(
                                    Focus(
                                        id = focus,
                                        cursor = Cursor.End
                                    )
                                )
                            }
                            processSideEffects(intent.effects)
                            proxies.payloads.send(payload)
                            analytics.sendAnalyticsDeleteBlockEvent(count = intent.targets.size)
                        }
                    )
                }
                is Intent.CRUD.UpdateFields -> {
                    updateFields(
                        params = UpdateFields.Params(
                            context = intent.context,
                            fields = intent.fields
                        )
                    ).proceed(
                        failure = {},
                        success = { proxies.payloads.send(it) }
                    )
                }
                is Intent.Text.Split -> {
                    val startTime = System.currentTimeMillis()
                    splitBlock(
                        params = SplitBlock.Params(
                            context = intent.context,
                            block = intent.block,
                            range = intent.range,
                            isToggled = intent.isToggled
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { (id, payload) ->
                            val middlewareTime = System.currentTimeMillis()
                            stores.focus.update(
                                Focus(
                                    id = id,
                                    cursor = Cursor.Start
                                )
                            )
                            proxies.payloads.send(payload)
                            analytics.sendAnalyticsSplitBlockEvent(
                                startTime = startTime,
                                middlewareTime = middlewareTime,
                                style = intent.style
                            )
                        }
                    )
                }
                is Intent.Text.Merge -> {
                    mergeBlocks(
                        params = MergeBlocks.Params(
                            context = intent.context,
                            pair = intent.pair
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            if (intent.previousLength != null) {
                                stores.focus.update(
                                    Focus(
                                        id = intent.previous,
                                        cursor = Cursor.Range(
                                            intent.previousLength..intent.previousLength
                                        )
                                    )
                                )
                            } else {
                                stores.focus.update(Focus.id(intent.previous))
                            }
                            proxies.payloads.send(payload)
                            analytics.sendAnalyticsDeleteBlockEvent(count = 1)
                        }
                    )
                }
                is Intent.Text.UpdateColor -> {
                    updateTextColor(
                        params = UpdateTextColor.Params(
                            context = intent.context,
                            targets = intent.targets,
                            color = intent.color
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            proxies.payloads.send(payload)
                        }
                    )
                }
                is Intent.Text.UpdateBackgroundColor -> {
                    updateBackgroundColor(
                        params = UpdateBackgroundColor.Params(
                            context = intent.context,
                            targets = intent.targets,
                            color = intent.color
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            proxies.payloads.send(payload)
                        }
                    )
                }
                is Intent.Text.UpdateMark -> {
                    updateBlocksMark(
                        params = UpdateBlocksMark.Params(
                            context = intent.context,
                            targets = intent.targets,
                            mark = intent.mark
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            proxies.payloads.send(payload)
                        }
                    )
                }
                is Intent.Text.UpdateStyle -> {
                    updateTextStyle(
                        params = UpdateTextStyle.Params(
                            context = intent.context,
                            targets = intent.targets,
                            style = intent.style
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            proxies.payloads.send(payload)
                        }
                    )
                }
                is Intent.Text.TurnInto -> {
                    turnIntoStyle(
                        params = TurnIntoStyle.Params(
                            context = intent.context,
                            targets = intent.targets,
                            style = intent.style
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            analytics.sendAnalyticsChangeTextBlockStyleEvent(
                                style = intent.style,
                                count = intent.targets.size,
                                analyticsContext = intent.analyticsContext
                            )
                            proxies.payloads.send(payload)
                        }
                    )
                }
                is Intent.Text.UpdateCheckbox -> {
                    updateCheckbox(
                        params = UpdateCheckbox.Params(
                            context = intent.context,
                            target = intent.target,
                            isChecked = intent.isChecked
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            proxies.payloads.send(payload)
                        }
                    )
                }
                is Intent.Text.UpdateText -> {
                    updateText(
                        params = UpdateText.Params(
                            context = intent.context,
                            target = intent.target,
                            text = intent.text,
                            marks = intent.marks
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = {}
                    )
                }
                is Intent.Text.Align -> {
                    updateAlignment(
                        params = UpdateAlignment.Params(
                            context = intent.context,
                            targets = intent.targets,
                            alignment = intent.alignment
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            proxies.payloads.send(payload)
                        }
                    )
                }
                is Intent.Document.Redo -> {
                    redo(
                        params = Redo.Params(
                            context = intent.context
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { result ->
                            if (result is Redo.Result.Success) {
                                proxies.payloads.send(result.payload)
                                analytics.sendAnalyticsRedoEvent()
                            } else {
                                intent.onRedoExhausted()
                            }
                        }
                    )
                }
                is Intent.Document.Undo -> {
                    undo(
                        params = Undo.Params(
                            context = intent.context
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { result ->
                            if (result is Undo.Result.Success) {
                                proxies.payloads.send(result.payload)
                                analytics.sendAnalyticsUndoEvent()
                            } else {
                                intent.onUndoExhausted()
                            }
                        }
                    )
                }
                is Intent.Document.Move -> {
                    move(
                        params = Move.Params(
                            context = intent.context,
                            targetContext = intent.targetContext,
                            targetId = intent.target,
                            blockIds = intent.blocks,
                            position = intent.position
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = {
                            proxies.payloads.send(it)
                            analytics.sendAnalyticsReorderBlockEvent(intent.blocks.size)
                        }
                    )
                }
                is Intent.Document.TurnIntoDocument -> {
                    turnIntoDocument(
                        params = TurnIntoDocument.Params(
                            context = intent.context,
                            targets = intent.targets
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { }
                    )
                }
                is Intent.Media.DownloadFile -> {
                    downloadFile(
                        params = DownloadFile.Params(
                            url = intent.url,
                            name = intent.name
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { analytics.sendAnalyticsDownloadMediaEvent(intent.type) }
                    )
                }
                is Intent.Media.Upload -> {
                    uploadBlock(
                        params = UploadBlock.Params(
                            contextId = intent.context,
                            blockId = intent.description.blockId,
                            url = intent.url,
                            filePath = intent.filePath
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            proxies.payloads.send(payload)
                            analytics.sendAnalyticsUploadMediaEvent(intent.description.type)
                        }
                    )
                }
                is Intent.Bookmark.SetupBookmark -> {
                    setupBookmark(
                        params = SetupBookmark.Params(
                            context = intent.context,
                            target = intent.target,
                            url = intent.url
                        )
                    ).proceed(
                        failure = { throwable ->
                            proxies.errors.send(throwable)
                        },
                        success = { payload ->
                            proxies.payloads.send(payload)
                        }
                    )
                }
                is Intent.Bookmark.CreateBookmark -> {
                    val startTime = System.currentTimeMillis()
                    createBookmarkBlock(
                        params = CreateBookmarkBlock.Params(
                            context = intent.context,
                            target = intent.target,
                            url = intent.url,
                            position = intent.position
                        )
                    ).proceed(
                        failure = { throwable ->
                            proxies.errors.send(throwable)
                        },
                        success = { payload ->
                            val middlewareTime = System.currentTimeMillis()
                            proxies.payloads.send(payload)
                            analytics.sendAnalyticsCreateBlockEvent(
                                prototype = Block.Prototype.Bookmark,
                                startTime = startTime,
                                middlewareTime = middlewareTime
                            )
                        }
                    )
                }
                is Intent.Clipboard.Paste -> {
                    paste(
                        params = Paste.Params(
                            context = intent.context,
                            focus = intent.focus,
                            range = intent.range,
                            selected = intent.selected
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { response ->
                            if (response.cursor >= 0)
                                stores.focus.update(
                                    stores.focus.current().copy(
                                        cursor = Cursor.Range(response.cursor..response.cursor)
                                    )
                                )
                            else if (response.blocks.isNotEmpty()) {
                                stores.focus.update(
                                    Focus(
                                        id = response.blocks.last(),
                                        cursor = Cursor.End
                                    )
                                )
                            }
                            proxies.payloads.send(response.payload)
                            analytics.sendAnalyticsPasteBlockEvent()
                        }
                    )
                }
                is Intent.Clipboard.Copy -> {
                    copy(
                        params = Copy.Params(
                            context = intent.context,
                            blocks = intent.blocks,
                            range = intent.range
                        )
                    ).proceed(
                        success = {
                            proxies.toasts.send("Copied!")
                            analytics.sendAnalyticsCopyBlockEvent()
                        },
                        failure = defaultOnError
                    )
                }
                is Intent.Divider.UpdateStyle -> {
                    val startTime = System.currentTimeMillis()
                    updateDivider(
                        params = UpdateDivider.Params(
                            context = intent.context,
                            targets = intent.targets,
                            style = intent.style
                        )
                    ).proceed(
                        success = { payload ->
                            proxies.payloads.send(payload)
                        },
                        failure = defaultOnError
                    )
                }
                is Intent.Document.SetRelationKey -> {
                    setRelationKey(
                        params = SetRelationKey.Params(
                            contextId = intent.context,
                            blockId = intent.blockId,
                            key = intent.key
                        )
                    ).process(
                        failure = defaultOnError,
                        success = { payload -> proxies.payloads.send(payload) }
                    )
                }
                is Intent.Document.SetObjectType -> {
                    setObjectType(
                        params = SetObjectType.Params(
                            context = intent.context,
                            typeId = intent.typeId
                        )
                    ).process(
                        failure = defaultOnError,
                        success = { payload ->
                            proxies.payloads.send(payload)
                        }
                    )
                }
                is Intent.Table.CreateTable -> {
                    val startTime = System.currentTimeMillis()
                    createTable(
                        params = CreateTable.Params(
                            ctx = intent.ctx,
                            target = intent.target,
                            position = intent.position,
                            rowCount = intent.rows,
                            columnCount = intent.columns
                        )
                    ).process(
                        failure = defaultOnError,
                        success = { payload ->
                            val middlewareTime = System.currentTimeMillis()
                            proxies.payloads.send(payload)
                            analytics.sendAnalyticsCreateBlockEvent(
                                prototype = Block.Prototype.SimpleTable,
                                startTime = startTime,
                                middlewareTime = middlewareTime
                            )
                        }
                    )
                }
                is Intent.Table.FillTableRow -> {
                    fillTableRow(
                        params = FillTableRow.Params(
                            ctx = intent.ctx,
                            targetIds = intent.targetIds
                        )
                    ).process(
                        failure = defaultOnError,
                        success = { payload -> proxies.payloads.send(payload) }
                    )
                }
            }
        }
    }

    private fun processSideEffects(effects: List<SideEffect>) {
        effects.forEach { effect ->
            if (effect is SideEffect.ClearMultiSelectSelection)
                memory.selections.clearSelections()
        }
    }

    private suspend fun sendEvent(event: EventAnalytics.Anytype) {
        analytics.registerEvent(event)
    }
}