package com.agileburo.anytype.presentation.page.editor

import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.clipboard.Copy
import com.agileburo.anytype.domain.clipboard.Paste
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.download.DownloadFile
import com.agileburo.anytype.domain.editor.Editor.Cursor
import com.agileburo.anytype.domain.editor.Editor.Focus
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.domain.page.Redo
import com.agileburo.anytype.domain.page.Undo
import com.agileburo.anytype.domain.page.UpdateTitle
import com.agileburo.anytype.domain.page.bookmark.SetupBookmark
import com.agileburo.anytype.presentation.page.Editor
import kotlinx.coroutines.flow.collect
import timber.log.Timber

class Orchestrator(
    private val createBlock: CreateBlock,
    private val replaceBlock: ReplaceBlock,
    private val updateTextColor: UpdateTextColor,
    private val updateBackgroundColor: UpdateBackgroundColor,
    private val duplicateBlock: DuplicateBlock,
    private val splitBlock: SplitBlock,
    private val mergeBlocks: MergeBlocks,
    private val unlinkBlocks: UnlinkBlocks,
    private val updateTextStyle: UpdateTextStyle,
    private val updateCheckbox: UpdateCheckbox,
    private val updateTitle: UpdateTitle,
    private val downloadFile: DownloadFile,
    private val updateText: UpdateText,
    private val updateAlignment: UpdateAlignment,
    private val uploadBlock: UploadBlock,
    private val setupBookmark: SetupBookmark,
    private val turnIntoDocument: TurnIntoDocument,
    private val move: Move,
    private val copy: Copy,
    private val paste: Paste,
    private val undo: Undo,
    private val redo: Redo,
    val memory: Editor.Memory,
    val stores: Editor.Storage,
    val proxies: Editor.Proxer,
    val textInteractor: Interactor.TextInteractor
) {

    private val defaultOnSuccess: suspend (Pair<Id, Payload>) -> Unit = { (id, payload) ->
        stores.focus.update(Focus.id(id))
        proxies.payloads.send(payload)
    }

    private val defaultPayload: suspend (Payload) -> Unit = {
        proxies.payloads.send(it)
    }

    private val defaultOnError: suspend (Throwable) -> Unit = { Timber.e(it) }

    suspend fun start() {
        proxies.intents.stream().collect { intent ->
            when (intent) {
                is Intent.CRUD.Create -> {
                    createBlock(
                        params = CreateBlock.Params(
                            context = intent.context,
                            target = intent.target,
                            prototype = intent.prototype,
                            position = intent.position
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = defaultOnSuccess
                    )
                }
                is Intent.CRUD.Replace -> {
                    replaceBlock(
                        params = ReplaceBlock.Params(
                            context = intent.context,
                            target = intent.target,
                            prototype = intent.prototype
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = defaultOnSuccess
                    )
                }
                is Intent.CRUD.Duplicate -> {
                    duplicateBlock(
                        params = DuplicateBlock.Params(
                            context = intent.context,
                            original = intent.target
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { (id, payload) ->
                            stores.focus.update(Focus(id = id, cursor = Cursor.End))
                            proxies.payloads.send(payload)
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
                        }
                    )
                }
                is Intent.Text.Split -> {
                    splitBlock(
                        params = SplitBlock.Params(
                            context = intent.context,
                            target = intent.target,
                            index = intent.index,
                            style = intent.style
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { (id, payload) ->
                            stores.focus.update(
                                Focus(
                                    id = intent.target,
                                    cursor = Cursor.Start
                                )
                            )
                            proxies.payloads.send(payload)
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
                        }
                    )
                }
                is Intent.Text.UpdateColor -> {
                    updateTextColor(
                        params = UpdateTextColor.Params(
                            context = intent.context,
                            target = intent.target,
                            color = intent.color
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = defaultPayload
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
                        success = defaultPayload
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
                        success = defaultPayload
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
                        success = {}
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
                            targets = listOf(intent.target),
                            alignment = intent.alignment
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = defaultPayload
                    )
                }
                is Intent.Document.Redo -> {
                    redo(
                        params = Redo.Params(
                            context = intent.context
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = defaultPayload
                    )
                }
                is Intent.Document.Undo -> {
                    undo(
                        params = Undo.Params(
                            context = intent.context
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = defaultPayload
                    )
                }
                is Intent.Document.UpdateTitle -> {
                    updateTitle(
                        params = UpdateTitle.Params(
                            context = intent.context,
                            title = intent.title
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = {}
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
                        success = { proxies.payloads.send(it) }
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
                        success = {}
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
                        success = {}
                    )
                }
                is Intent.Media.Upload -> {
                    uploadBlock(
                        params = UploadBlock.Params(
                            contextId = intent.context,
                            blockId = intent.target,
                            url = intent.url,
                            filePath = intent.filePath
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = defaultPayload
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
                        success = defaultPayload
                    )
                }
                is Intent.Clipboard.Paste -> {
                    paste(
                        params = Paste.Params(
                            context = intent.context,
                            focus = intent.focus,
                            range = intent.range
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
                            Timber.d("Copy sucessful")
                        },
                        failure = defaultOnError
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
}