package com.agileburo.anytype.presentation.page.editor

import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.download.DownloadFile
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
    private val setupBookmark: SetupBookmark,
    private val paste: Clipboard.Paste,
    private val undo: Undo,
    private val redo: Redo,
    val memory: Editor.Memory,
    val stores: Editor.Storage,
    val proxies: Editor.Proxer,
    val textInteractor: Interactor.TextInteractor
) {

    private val defaultOnSuccess: suspend (Pair<Id, Payload>) -> Unit = { (id, payload) ->
        stores.focus.update(id)
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
                        success = defaultOnSuccess
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
                            if (focus != null) stores.focus.update(focus)
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
                            index = intent.index
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { (id, payload) ->
                            stores.focus.update(intent.target)
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
                            stores.focus.update(intent.previous)
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
                        params = Clipboard.Paste.Params(
                            context = intent.context,
                            focus = intent.focus,
                            range = intent.range,
                            blocks = emptyList(),
                            html = intent.html,
                            text = intent.text,
                            selected = intent.selected
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { response ->
                            Timber.d("response: $response")
                            proxies.payloads.send(response.payload)
                        }
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