package com.agileburo.anytype.presentation.page.editor

import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_BACKGROUND_COLOR
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_COPY
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_CREATE
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_DOWNLOAD_FILE
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_DUPLICATE
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_MERGE
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_MOVE
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_PASTE
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_REDO
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_REPLACE
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_SETUP_BOOKMARK
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_SET_ALIGN
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_SET_TEXT_COLOR
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_SPLIT
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_TURN_INTO_DOCUMENT
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_UNDO
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_UNLINK
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_UPDATE_CHECKBOX
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_UPDATE_STYLE
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_UPDATE_TEXT
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_UPDATE_TITLE
import com.agileburo.anytype.analytics.base.EventsDictionary.BLOCK_UPLOAD
import com.agileburo.anytype.analytics.event.EventAnalytics
import com.agileburo.anytype.analytics.props.Props
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
import com.agileburo.anytype.presentation.extension.getAnalyticsEvent
import com.agileburo.anytype.presentation.extension.getProps
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
    val textInteractor: Interactor.TextInteractor,
    private val analytics: Analytics
) {

    private val defaultOnSuccess: suspend (Pair<Id, Payload>) -> Unit = { (id, payload) ->
        stores.focus.update(Focus.id(id))
        proxies.payloads.send(payload)
    }

    private val defaultOnSuccessWithEvent: suspend (Triple<Id, Payload, EventAnalytics.Anytype>) -> Unit =
        { (id, payload, event) ->
            stores.focus.update(Focus.id(id))
            proxies.payloads.send(payload)
            sendEvent(event)
        }

    private val defaultPayload: suspend (Payload) -> Unit = {
        proxies.payloads.send(it)
    }

    private val defaultPayloadWithEvent: suspend (Pair<Payload, EventAnalytics.Anytype>) -> Unit =
        { (payload, event) ->
            proxies.payloads.send(payload)
            sendEvent(event)
        }

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
                            val event = intent.prototype.getAnalyticsEvent(
                                eventName = BLOCK_CREATE,
                                startTime = startTime,
                                middlewareTime = System.currentTimeMillis()
                            )
                            defaultOnSuccessWithEvent(Triple(id, payload, event))
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
                            val event = intent.prototype.getAnalyticsEvent(
                                eventName = BLOCK_REPLACE,
                                startTime = startTime,
                                middlewareTime = System.currentTimeMillis()
                            )
                            defaultOnSuccessWithEvent(Triple(id, payload, event))
                        }
                    )
                }
                is Intent.CRUD.Duplicate -> {
                    val startTime = System.currentTimeMillis()
                    duplicateBlock(
                        params = DuplicateBlock.Params(
                            context = intent.context,
                            original = intent.target
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { (id, payload) ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_DUPLICATE,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            stores.focus.update(Focus(id = id, cursor = Cursor.End))
                            proxies.payloads.send(payload)
                            sendEvent(event)
                        }
                    )
                }
                is Intent.CRUD.Unlink -> {
                    val startTime = System.currentTimeMillis()
                    unlinkBlocks(
                        params = UnlinkBlocks.Params(
                            context = intent.context,
                            targets = intent.targets
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_UNLINK,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
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
                            sendEvent(event)
                        }
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
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_SPLIT,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            stores.focus.update(
                                Focus(
                                    id = id,
                                    cursor = Cursor.Start
                                )
                            )
                            proxies.payloads.send(payload)
                            sendEvent(event)
                        }
                    )
                }
                is Intent.Text.Merge -> {
                    val startTime = System.currentTimeMillis()
                    mergeBlocks(
                        params = MergeBlocks.Params(
                            context = intent.context,
                            pair = intent.pair
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_MERGE,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
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
                            sendEvent(event)
                        }
                    )
                }
                is Intent.Text.UpdateColor -> {
                    val startTime = System.currentTimeMillis()
                    updateTextColor(
                        params = UpdateTextColor.Params(
                            context = intent.context,
                            target = intent.target,
                            color = intent.color
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_SET_TEXT_COLOR,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            defaultPayloadWithEvent(Pair(payload, event))
                        }
                    )
                }
                is Intent.Text.UpdateBackgroundColor -> {
                    val startTime = System.currentTimeMillis()
                    updateBackgroundColor(
                        params = UpdateBackgroundColor.Params(
                            context = intent.context,
                            targets = intent.targets,
                            color = intent.color
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_BACKGROUND_COLOR,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            defaultPayloadWithEvent(Pair(payload, event))
                        }
                    )
                }
                is Intent.Text.UpdateStyle -> {
                    val startTime = System.currentTimeMillis()
                    updateTextStyle(
                        params = UpdateTextStyle.Params(
                            context = intent.context,
                            targets = intent.targets,
                            style = intent.style
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_UPDATE_STYLE,
                                props = intent.style.getProps(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            defaultPayloadWithEvent(Pair(payload, event))
                        }
                    )
                }
                is Intent.Text.UpdateCheckbox -> {
                    val startTime = System.currentTimeMillis()
                    updateCheckbox(
                        params = UpdateCheckbox.Params(
                            context = intent.context,
                            target = intent.target,
                            isChecked = intent.isChecked
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_UPDATE_CHECKBOX,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            defaultPayloadWithEvent(Pair(payload, event))
                        }
                    )
                }
                is Intent.Text.UpdateText -> {
                    val startTime = System.currentTimeMillis()
                    updateText(
                        params = UpdateText.Params(
                            context = intent.context,
                            target = intent.target,
                            text = intent.text,
                            marks = intent.marks
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = {
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_UPDATE_TEXT,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            sendEvent(event)
                        }
                    )
                }
                is Intent.Text.Align -> {
                    val startTime = System.currentTimeMillis()
                    updateAlignment(
                        params = UpdateAlignment.Params(
                            context = intent.context,
                            targets = listOf(intent.target),
                            alignment = intent.alignment
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_SET_ALIGN,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            defaultPayloadWithEvent(Pair(payload, event))
                        }
                    )
                }
                is Intent.Document.Redo -> {
                    val startTime = System.currentTimeMillis()
                    redo(
                        params = Redo.Params(
                            context = intent.context
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_REDO,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            defaultPayloadWithEvent(Pair(payload, event))
                        }
                    )
                }
                is Intent.Document.Undo -> {
                    val startTime = System.currentTimeMillis()
                    undo(
                        params = Undo.Params(
                            context = intent.context
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_UNDO,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            defaultPayloadWithEvent(Pair(payload, event))
                        }
                    )
                }
                is Intent.Document.UpdateTitle -> {
                    val startTime = System.currentTimeMillis()
                    updateTitle(
                        params = UpdateTitle.Params(
                            context = intent.context,
                            title = intent.title
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = {
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_UPDATE_TITLE,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            sendEvent(event)
                        }
                    )
                }
                is Intent.Document.Move -> {
                    val startTime = System.currentTimeMillis()
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
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_MOVE,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            proxies.payloads.send(it)
                            sendEvent(event)
                        }
                    )
                }
                is Intent.Document.TurnIntoDocument -> {
                    val startTime = System.currentTimeMillis()
                    turnIntoDocument(
                        params = TurnIntoDocument.Params(
                            context = intent.context,
                            targets = intent.targets
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = {
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_TURN_INTO_DOCUMENT,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            sendEvent(event)
                        }
                    )
                }
                is Intent.Media.DownloadFile -> {
                    val startTime = System.currentTimeMillis()
                    downloadFile(
                        params = DownloadFile.Params(
                            url = intent.url,
                            name = intent.name
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = {
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_DOWNLOAD_FILE,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            sendEvent(event)
                        }
                    )
                }
                is Intent.Media.Upload -> {
                    val startTime = System.currentTimeMillis()
                    uploadBlock(
                        params = UploadBlock.Params(
                            contextId = intent.context,
                            blockId = intent.target,
                            url = intent.url,
                            filePath = intent.filePath
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { payload ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_UPLOAD,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            defaultPayloadWithEvent(Pair(payload, event))
                        }
                    )
                }
                is Intent.Bookmark.SetupBookmark -> {
                    val startTime = System.currentTimeMillis()
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
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_SETUP_BOOKMARK,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            defaultPayloadWithEvent(Pair(payload, event))
                        }
                    )
                }
                is Intent.Clipboard.Paste -> {
                    val startTime = System.currentTimeMillis()
                    paste(
                        params = Paste.Params(
                            context = intent.context,
                            focus = intent.focus,
                            range = intent.range
                        )
                    ).proceed(
                        failure = defaultOnError,
                        success = { response ->
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_PASTE,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
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
                            sendEvent(event)
                        }
                    )
                }
                is Intent.Clipboard.Copy -> {
                    val startTime = System.currentTimeMillis()
                    copy(
                        params = Copy.Params(
                            context = intent.context,
                            blocks = intent.blocks,
                            range = intent.range
                        )
                    ).proceed(
                        success = {
                            Timber.d("Copy sucessful")
                            val event = EventAnalytics.Anytype(
                                name = BLOCK_COPY,
                                props = Props.empty(),
                                duration = EventAnalytics.Duration(
                                    start = startTime,
                                    middleware = System.currentTimeMillis()
                                )
                            )
                            sendEvent(event)
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

    private suspend fun sendEvent(event: EventAnalytics.Anytype) {
        analytics.registerEvent(
            event.copy(
                duration = event.duration?.copy(
                    render = System.currentTimeMillis()
                )
            )
        )
    }
}