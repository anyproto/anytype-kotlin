package com.agileburo.anytype.presentation.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.pattern.Matcher
import com.agileburo.anytype.core_ui.features.page.pattern.Pattern
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.*
import com.agileburo.anytype.core_utils.tools.Counter
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content
import com.agileburo.anytype.domain.block.model.Block.Prototype
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.download.DownloadFile
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.ext.addMark
import com.agileburo.anytype.domain.ext.asMap
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.domain.ext.textStyle
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.*
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.common.SupportCommand
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.page.ControlPanelMachine.Interactor
import com.agileburo.anytype.presentation.page.model.TextUpdate
import com.agileburo.anytype.presentation.page.render.BlockViewRenderer
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

const val EMPTY_PATH = ""

class PageViewModel(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val createPage: CreatePage,
    private val createDocument: CreateDocument,
    private val archiveDocument: ArchiveDocument,
    private val undo: Undo,
    private val redo: Redo,
    private val updateText: UpdateText,
    private val createBlock: CreateBlock,
    private val replaceBlock: ReplaceBlock,
    private val interceptEvents: InterceptEvents,
    private val updateCheckbox: UpdateCheckbox,
    private val unlinkBlocks: UnlinkBlocks,
    private val duplicateBlock: DuplicateBlock,
    private val updateTextStyle: UpdateTextStyle,
    private val updateTextColor: UpdateTextColor,
    private val updateBackgroundColor: UpdateBackgroundColor,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val mergeBlocks: MergeBlocks,
    private val splitBlock: SplitBlock,
    private val downloadFile: DownloadFile,
    private val uploadUrl: UploadUrl,
    private val documentExternalEventReducer: StateReducer<List<Block>, Event>,
    private val urlBuilder: UrlBuilder,
    private val renderer: DefaultBlockViewRenderer,
    private val counter: Counter,
    private val patternMatcher: Matcher<Pattern>
) : ViewStateViewModel<PageViewModel.ViewState>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>>,
    SupportCommand<PageViewModel.Command>,
    BlockViewRenderer by renderer,
    ToggleStateHolder by renderer,
    StateReducer<List<Block>, Event> by documentExternalEventReducer {

    private val controlPanelInteractor = Interactor(viewModelScope)
    val controlPanelViewState = MutableLiveData<ControlPanelState>()

    private val renderingChannel = Channel<List<Block>>()

    private val renderings = renderingChannel.consumeAsFlow()

    private val focusChannel = ConflatedBroadcastChannel(EMPTY_FOCUS_ID)
    private val focusChanges = focusChannel.asFlow()

    private val textChannel = Channel<TextUpdate>()
    private val textUpdateChannel = Channel<TextUpdate>()
    private val textChanges = textChannel.consumeAsFlow()
    private val textUpdateChanges = textUpdateChannel.consumeAsFlow()

    private val selectionChannel = Channel<Pair<Id, IntRange>>()
    private val selectionsChanges = selectionChannel.consumeAsFlow()

    private val markupActionChannel = Channel<MarkupAction>()
    private val markupActions = markupActionChannel.consumeAsFlow()

    /**
     * Currently opened page id.
     */
    var context: String = ""

    /**
     * Current set of blocks on this page.
     */
    var blocks: List<Block> = emptyList()

    private val _focus: MutableLiveData<Id> = MutableLiveData()
    val focus: LiveData<Id> = _focus

    /**
     * Open gallery and search media files for block with that id
     */
    private var mediaBlockId = ""

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()
    override val commands = MutableLiveData<EventWrapper<Command>>()

    init {
        startHandlingTextChanges()
        startProcessingFocusChanges()
        startProcessingControlPanelViewState()
        startObservingEvents()
        processRendering()
        processMarkupChanges()
    }

    private fun startProcessingFocusChanges() {
        viewModelScope.launch {
            focusChanges.collect { _focus.postValue(it) }
        }
    }

    private fun startObservingEvents() {
        viewModelScope.launch {
            interceptEvents
                .build()
                .filter { events -> events.any { it.context == context } }
                .map { events ->
                    events.forEach { event -> blocks = reduce(blocks, event) }
                    proceedWithInitialFocusing(events)
                }
                .collect { viewModelScope.launch { refresh() } }
        }
    }

    @Deprecated("Will be removed")
    private fun proceedWithInitialFocusing(events: List<Event>) {
        val event = events.find { event -> event is Event.Command.ShowBlock }
        if (event is Event.Command.ShowBlock) {
            event.blocks.find { block ->
                block.content is Content.Text
                        && block.content<Content.Text>().style == Content.Text.Style.TITLE
            }?.let { title ->
                updateFocus(title.id)
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnFocusChanged(
                        id = title.id, style = Content.Text.Style.TITLE
                    )
                )
            }
        }
    }

    private fun startProcessingControlPanelViewState() {
        viewModelScope.launch {
            controlPanelInteractor
                .state()
                .distinctUntilChanged()
                .collect { controlPanelViewState.postValue(it) }
        }
    }

    private fun processMarkupChanges() {
        markupActions
            .withLatestFrom(
                selectionsChanges
                    .distinctUntilChanged()
                    .filter { (_, selection) -> selection.first != selection.last }
            ) { a, b -> Pair(a, b) }
            .onEach { (action, selection) ->
                when (action.type) {
                    Markup.Type.LINK -> {
                        val block = blocks.first { it.id == selection.first }
                        val range = IntRange(
                            start = selection.second.first,
                            endInclusive = selection.second.last.dec()
                        )
                        stateData.value = ViewState.OpenLinkScreen(context, block, range)
                    }
                    else -> {
                        applyMarkup(selection, action)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun applyLinkMarkup(
        blockId: String, link: String, range: IntRange
    ) {
        val targetBlock = blocks.first { it.id == blockId }
        val targetContent = targetBlock.content as Content.Text
        val linkMark = Content.Text.Mark(
            type = Content.Text.Mark.Type.LINK,
            range = IntRange(start = range.first, endInclusive = range.last.inc()),
            param = link
        )
        val marks = targetContent.marks

        updateLinkMarks.invoke(
            viewModelScope,
            UpdateLinkMarks.Params(marks = marks, newMark = linkMark)
        ) { result ->
            result.either(
                fnL = { throwable ->
                    Timber.e("Error update marks:${throwable.message}")
                },
                fnR = {
                    val newContent = targetContent.copy(marks = it)
                    val newBlock = targetBlock.copy(content = newContent)
                    rerenderingBlocks(newBlock)
                    proceedWithUpdatingText(
                        params = UpdateText.Params(
                            contextId = context,
                            text = newBlock.content.asText().text,
                            blockId = targetBlock.id,
                            marks = it
                        )
                    )
                }
            )
        }
    }

    private suspend fun applyMarkup(
        selection: Pair<String, IntRange>,
        action: MarkupAction
    ) {
        val targetBlock = blocks.first { it.id == selection.first }
        val targetContent = targetBlock.content as Content.Text

        val mark = Content.Text.Mark(
            range = selection.second,
            type = when (action.type) {
                Markup.Type.BOLD -> Content.Text.Mark.Type.BOLD
                Markup.Type.ITALIC -> Content.Text.Mark.Type.ITALIC
                Markup.Type.STRIKETHROUGH -> Content.Text.Mark.Type.STRIKETHROUGH
                Markup.Type.TEXT_COLOR -> Content.Text.Mark.Type.TEXT_COLOR
                Markup.Type.LINK -> Content.Text.Mark.Type.LINK
                Markup.Type.BACKGROUND_COLOR -> Content.Text.Mark.Type.BACKGROUND_COLOR
                Markup.Type.KEYBOARD -> Content.Text.Mark.Type.KEYBOARD
            },
            param = action.param
        )

        val marks = targetContent.marks.addMark(mark)

        val newContent = targetContent.copy(
            marks = marks
        )

        val newBlock = targetBlock.copy(content = newContent)

        val update = blocks.map { block ->
            if (block.id != selection.first)
                block
            else
                newBlock
        }

        blocks = update

        refresh()

        proceedWithUpdatingText(
            params = UpdateText.Params(
                contextId = context,
                blockId = newBlock.id,
                text = newContent.text,
                marks = newContent.marks
            )
        )
    }

    private fun rerenderingBlocks(block: Block) =
        viewModelScope.launch {
            blocks = blocks.map {
                if (it.id != block.id)
                    it
                else
                    block
            }
            refresh()
        }

    private fun processRendering() {
        viewModelScope.launch {
            renderings.withLatestFrom(focusChanges) { models, focus ->
                models.asMap().render(
                    indent = INITIAL_INDENT,
                    anchor = context,
                    focus = focus,
                    root = models.first { it.id == context },
                    counter = counter
                )
            }.collect { dispatchToUI(it) }
        }
    }

    private fun dispatchToUI(views: List<BlockView>) {
        stateData.postValue(ViewState.Success(views))
    }

    private fun startHandlingTextChanges() {
        textChanges
            .onEach { update ->
                when {
                    update.patterns.isEmpty() -> textUpdateChannel.send(update)
                    update.patterns.contains(Pattern.NUMBERED) -> replaceBy(
                        target = update.target,
                        prototype = Prototype.Text(
                            style = Content.Text.Style.NUMBERED
                        )
                    )
                    update.patterns.contains(Pattern.DIVIDER) -> replaceBy(
                        target = update.target,
                        prototype = Prototype.Divider
                    )
                    update.patterns.contains(Pattern.CHECKBOX) -> replaceBy(
                        target = update.target,
                        prototype = Prototype.Text(
                            style = Content.Text.Style.CHECKBOX
                        )
                    )
                    update.patterns.contains(Pattern.BULLET) -> replaceBy(
                        target = update.target,
                        prototype = Prototype.Text(
                            style = Content.Text.Style.BULLET
                        )
                    )
                    update.patterns.contains(Pattern.H1) -> replaceBy(
                        target = update.target,
                        prototype = Prototype.Text(
                            style = Content.Text.Style.H1
                        )
                    )
                    update.patterns.contains(Pattern.H2) -> replaceBy(
                        target = update.target,
                        prototype = Prototype.Text(
                            style = Content.Text.Style.H2
                        )
                    )
                    update.patterns.contains(Pattern.H3) -> replaceBy(
                        target = update.target,
                        prototype = Prototype.Text(
                            style = Content.Text.Style.H3
                        )
                    )
                    update.patterns.contains(Pattern.QUOTE) -> replaceBy(
                        target = update.target,
                        prototype = Prototype.Text(
                            style = Content.Text.Style.QUOTE
                        )
                    )
                    update.patterns.contains(Pattern.TOGGLE) -> replaceBy(
                        target = update.target,
                        prototype = Prototype.Text(
                            style = Content.Text.Style.TOGGLE
                        )
                    )
                    else -> textUpdateChannel.send(update)
                }
            }
            .launchIn(viewModelScope)

        textUpdateChanges
            .debounce(TEXT_CHANGES_DEBOUNCE_DURATION)
            .map { update ->

                blocks = blocks.map { block ->
                    if (block.id == update.target) {
                        block.copy(
                            content = block.content.asText().copy(
                                text = update.text,
                                marks = update.markup.filter { it.range.first != it.range.last }
                            )
                        )
                    } else
                        block
                }

                UpdateText.Params(
                    contextId = context,
                    blockId = update.target,
                    text = update.text,
                    marks = update.markup.filter { it.range.first != it.range.last }
                )
            }
            .onEach { params -> proceedWithUpdatingText(params) }
            .launchIn(viewModelScope)
    }

    private fun proceedWithUpdatingText(params: UpdateText.Params) {
        Timber.d("Starting updating block with params: $params")
        updateText.invoke(viewModelScope, params) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while updating text: $params") },
                fnR = { Timber.d("Text has been updated") }
            )
        }
    }

    private fun replaceBy(
        target: Id,
        prototype: Prototype
    ) {
        replaceBlock.invoke(
            scope = viewModelScope,
            params = ReplaceBlock.Params(
                context = context,
                target = target,
                prototype = prototype
            ),
            onResult = { result ->
                result.either(
                    fnL = { Timber.e(it, "Error while converting $target to: $prototype") },
                    fnR = { id -> updateFocus(id) }
                )
            }
        )
    }

    fun open(id: String) {

        context = id

        stateData.postValue(ViewState.Loading)

        openPage.invoke(viewModelScope, OpenPage.Params(id)) { result ->
            result.either(
                fnR = { Timber.d("Page with id $id has been opened") },
                fnL = { Timber.e(it, "Error while opening page with id: $id") }
            )
        }
    }

    fun onAddLinkPressed(blockId: String, link: String, range: IntRange) {
        applyLinkMarkup(blockId, link, range)
    }

    fun onUnlinkPressed(blockId: String, range: IntRange) {
        val targetBlock = blocks.first { it.id == blockId }
        val targetContent = targetBlock.content as Content.Text
        val marks = targetContent.marks

        removeLinkMark.invoke(
            viewModelScope, RemoveLinkMark.Params(range = range, marks = marks)
        ) { result ->
            result.either(
                fnL = { Timber.e("Error update marks:${it.message}") },
                fnR = {
                    val newContent = targetContent.copy(marks = it)
                    val newBlock = targetBlock.copy(content = newContent)
                    rerenderingBlocks(newBlock)
                    proceedWithUpdatingText(
                        params = UpdateText.Params(
                            contextId = context,
                            text = newBlock.content.asText().text,
                            blockId = targetBlock.id,
                            marks = it
                        )
                    )
                }
            )
        }
    }

    fun onSystemBackPressed() {
        proceedWithExiting()
    }

    fun onBackButtonPressed() {
        proceedWithExiting()
    }

    fun onBottomSheetHidden() {
        proceedWithExitingToDesktop()
    }

    private fun proceedWithExiting() {
        closePage.invoke(viewModelScope, ClosePage.Params(context)) { result ->
            result.either(
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.Exit)) },
                fnL = { Timber.e(it, "Error while closing the test page") }
            )
        }
    }

    private fun proceedWithExitingToDesktop() {
        closePage.invoke(viewModelScope, ClosePage.Params(context)) { result ->
            result.either(
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop)) },
                fnL = { Timber.e(it, "Error while closing the test page") }
            )
        }
    }

    fun onTextChanged(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        val update = TextUpdate(target = id, text = text, markup = marks, patterns = emptyList())
        Timber.d("onTextChanged: $update")
        viewModelScope.launch { textChannel.send(update) }
    }

    fun onParagraphTextChanged(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        val update = TextUpdate(
            target = id,
            text = text,
            markup = marks,
            patterns = patternMatcher.match(text)
        )
        Timber.d("onParagraphTextChanged: $update")
        viewModelScope.launch { textChannel.send(update) }
    }

    fun onSelectionChanged(id: String, selection: IntRange) {
        viewModelScope.launch { selectionChannel.send(Pair(id, selection)) }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnSelectionChanged(selection))
    }

    fun onBlockFocusChanged(id: String, hasFocus: Boolean) {
        Timber.d("Focus changed ($hasFocus): $id")
        if (hasFocus) {
            updateFocus(id)
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = blocks.first { it.id == id }.textStyle()
                )
            )
        }
    }

    fun onEmptyBlockBackspaceClicked(id: String) {
        Timber.d("onEmptyBlockBackspaceClicked: $id")
        blocks.find { it.id == id }?.let { target ->
            if (!target.content.asText().isTitle())
                proceedWithUnlinking(target = id)
        }
    }

    fun onNonEmptyBlockBackspaceClicked(id: String) {
        val page = blocks.first { it.id == context }
        val index = page.children.indexOf(id)
        if (index > 1) {
            val previous = page.children[index.dec()]
            proceedWithMergingBlocks(
                previous = previous,
                id = id
            )
        } else {
            Timber.d("Skipping merge on non-empty-block-backspace-pressed event")
        }
    }

    private fun proceedWithMergingBlocks(id: String, previous: String) {
        mergeBlocks.invoke(
            scope = viewModelScope,
            params = MergeBlocks.Params(
                context = context,
                pair = Pair(previous, id)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while merging blocks: $id, $previous") },
                fnR = { updateFocus(previous) }
            )
        }
    }

    fun onSplitLineEnterClicked(
        id: String,
        index: Int
    ) {
        splitBlock.invoke(
            scope = viewModelScope,
            params = SplitBlock.Params(
                context = context,
                target = id,
                index = index
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while splitting the target block with id: $id") },
                fnR = { id -> updateFocus(id) }
            )
        }
    }

    fun onEndLineEnterClicked(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        Timber.d("On endline enter clicked")

        val target = blocks.first { it.id == id }

        val content = target.content<Content.Text>().copy(
            text = text,
            marks = marks
        )

        blocks = blocks.replace(
            replacement = { old -> old.copy(content = content) }
        ) { block -> block.id == id }

        if (content.isList()) {
            handleEndlineEnterPressedEventForListItem(content, id)
        } else {
            proceedWithCreatingNewTextBlock(
                id = id,
                style = Content.Text.Style.P
            )
        }
    }

    private fun handleEndlineEnterPressedEventForListItem(
        content: Content.Text,
        id: String
    ) {
        if (content.text.isNotEmpty()) {
            proceedWithCreatingNewTextBlock(id, content.style)
        } else {
            proceedWithUpdatingTextStyle(
                style = Content.Text.Style.P,
                target = id
            )
        }
    }

    private fun proceedWithCreatingNewTextBlock(
        id: String,
        style: Content.Text.Style,
        position: Position = Position.BOTTOM
    ) {
        createBlock.invoke(
            scope = viewModelScope,
            params = CreateBlock.Params(
                context = context,
                target = id,
                position = position,
                prototype = Prototype.Text(style = style)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a block") },
                fnR = { id -> updateFocus(id) }
            )
        }
    }

    private fun updateFocus(id: Id) {
        Timber.d("Updating focus: $id")
        viewModelScope.launch { focusChannel.send(id) }
    }

    private fun proceedWithCreatingNewDividerBlock(
        id: String,
        position: Position = Position.BOTTOM
    ) {
        createBlock.invoke(
            scope = viewModelScope,
            params = CreateBlock.Params(
                context = context,
                target = id,
                position = position,
                prototype = Prototype.Divider
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a divider block") },
                fnR = { id -> updateFocus(id) }
            )
        }
    }

    fun onMarkupActionClicked(markup: Markup.Type) {
        viewModelScope.launch {
            markupActionChannel.send(MarkupAction(type = markup))
        }
    }

    fun onMarkupTextColorAction(color: String) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnMarkupTextColorSelected)
        viewModelScope.launch {
            markupActionChannel.send(
                MarkupAction(
                    type = Markup.Type.TEXT_COLOR,
                    param = color
                )
            )
        }
    }

    fun onMarkupBackgroundColorAction(color: String) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnMarkupBackgroundColorSelected)
        viewModelScope.launch {
            markupActionChannel.send(
                MarkupAction(
                    type = Markup.Type.BACKGROUND_COLOR,
                    param = color
                )
            )
        }
    }

    fun onToolbarTextColorAction(color: String) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBlockTextColorSelected)
        focusChannel.value.let { focus ->
            updateTextColor.invoke(
                scope = viewModelScope,
                params = UpdateTextColor.Params(
                    context = context,
                    target = focus,
                    color = color
                )
            ) { result ->
                result.either(
                    fnL = { Timber.e(it, "Error while updating the whole block's text color") },
                    fnR = { Timber.d("Text color ($color) has been succesfully updated for the block: $focus") }
                )
            }
        }
    }

    fun onBlockBackgroundColorAction(color: String) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBlockBackgroundColorSelected)
        focusChannel.value.let { focus ->
            updateBackgroundColor.invoke(
                scope = viewModelScope,
                params = UpdateBackgroundColor.Params(
                    context = context,
                    targets = listOf(focus),
                    color = color
                )
            ) { result ->
                result.either(
                    fnL = {
                        Timber.e(
                            it,
                            "Error while updating background color for the block: $focus"
                        )
                    },
                    fnR = { Timber.d("Background color ($color) has been succesfully updated for the block: $focus") }
                )
            }
        }
    }

    fun onMarkupToolbarColorClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnMarkupToolbarColorClicked)
    }

    fun onActionDeleteClicked() {
        viewModelScope.launch {
            focusChanges
                .take(1)
                .collect { focus -> proceedWithUnlinking(focus) }
        }
    }

    private fun proceedWithUnlinking(target: String) {

        // TODO support nested blocks

        val parent = blocks.first { it.children.contains(target) }

        val index = parent.children.indexOf(target)

        val previous = index.dec().let { prev ->
            if (prev != -1) parent.children[prev] else null
        }

        val next = index.inc().let { nxt ->
            if (nxt <= parent.children.lastIndex) parent.children[nxt] else null
        }

        unlinkBlocks.invoke(
            scope = viewModelScope,
            params = UnlinkBlocks.Params(
                context = context,
                targets = listOf(target)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while unlinking block with id: $target") },
                fnR = {
                    if (previous != null)
                        updateFocus(id = previous)
                    else if (next != null)
                        updateFocus(id = next)
                }
            )
        }
    }

    fun onActionDuplicateClicked() {
        duplicateBlock.invoke(
            scope = viewModelScope,
            params = DuplicateBlock.Params(
                context = context,
                original = focusChannel.value
            ),
            onResult = { result ->
                result.either(
                    fnL = { Timber.e(it, "Error while duplicating block with id: $focus") },
                    fnR = { id -> updateFocus(id) }
                )
            }
        )
    }

    fun onActionUndoClicked() {
        undo.invoke(
            scope = viewModelScope,
            params = Undo.Params(
                context = context
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while un-doing user actions") },
                fnR = { Timber.d("Undo operation completed sucessfully") }
            )
        }
    }

    fun onActionRedoClicked() {
        redo.invoke(
            scope = viewModelScope,
            params = Redo.Params(
                context = context
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while re-doing user actions") },
                fnR = { Timber.d("Redo operation completed sucessfully") }
            )
        }
    }

    fun onAddTextBlockClicked(style: Content.Text.Style) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
        proceedWithCreatingNewTextBlock(
            id = focusChannel.value,
            style = style
        )
    }

    fun onAddVideoBlockClicked() {
        proceedWithCreatingEmptyFileBlock(
            id = focusChannel.value,
            type = Content.File.Type.VIDEO
        )
    }

    fun onAddLocalVideoClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_VIDEO_ALL))
    }

    fun onAddLocalPictureClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_IMAGE_ALL))
    }

    fun onTogglePlaceholderClicked(target: Id) {
        createBlock.invoke(
            scope = viewModelScope,
            params = CreateBlock.Params(
                target = target,
                position = Position.INNER,
                context = context,
                prototype = Prototype.Text(
                    style = Content.Text.Style.P
                )
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a paragraph inside toggle block") },
                fnR = { id -> updateFocus(id) }
            )
        }
    }

    fun onToggleClicked(target: Id) {
        onToggleChanged(target)
        viewModelScope.launch { refresh() }
    }

    fun onAddLocalFileClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_FILE_ALL))
    }

    fun onAddImageBlockClicked() {
        proceedWithCreatingEmptyFileBlock(
            id = focusChannel.value,
            type = Content.File.Type.IMAGE
        )
    }

    fun onAddFileBlockClicked() {
        proceedWithCreatingEmptyFileBlock(
            id = focusChannel.value,
            type = Content.File.Type.FILE
        )
    }

    private fun proceedWithCreatingEmptyFileBlock(
        id: String,
        type: Content.File.Type,
        state: Content.File.State = Content.File.State.EMPTY,
        position: Position = Position.BOTTOM
    ) {
        createBlock.invoke(
            scope = viewModelScope,
            params = CreateBlock.Params(
                context = context,
                target = id,
                position = position,
                prototype = Prototype.File(type = type, state = state)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a block") },
                fnR = { id -> updateFocus(id) }
            )
        }
    }

    fun onCheckboxClicked(id: String) {
        val target = blocks.first { it.id == id }

        val params = UpdateCheckbox.Params(
            context = context,
            target = target.id,
            isChecked = target.content.asText().toggleCheck()
        )

        updateCheckbox.invoke(viewModelScope, params) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while updating checkbox: $id") },
                fnR = { Timber.d("Checkbox block has been succesfully updated") }
            )
        }
    }

    fun onAddBlockToolbarClicked() {
        dispatch(Command.OpenAddBlockPanel)
    }

    fun onActionToolbarClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnActionToolbarClicked)
    }

    fun onTurnIntoToolbarToggleClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTurnIntoToolbarToggleClicked)
    }

    fun onColorToolbarToogleClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnColorToolbarToggleClicked)
    }

    fun onTurnIntoStyleClicked(style: Content.Text.Style) {
        proceedWithUpdatingTextStyle(style, focusChannel.value)
    }

    fun onAddDividerBlockClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
        proceedWithCreatingNewDividerBlock(id = focusChannel.value)
    }

    private fun proceedWithUpdatingTextStyle(
        style: Content.Text.Style,
        target: String
    ) {
        updateTextStyle.invoke(
            scope = viewModelScope,
            params = UpdateTextStyle.Params(
                context = context,
                target = target,
                style = style
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while updating text style") },
                fnR = { Timber.d("Text style succesfully updated") }
            )
        }
    }

    fun onOutsideClicked() {
        blocks.first { it.id == context }.let { page ->
            if (page.children.isNotEmpty()) {
                val last = blocks.first { it.id == page.children.last() }
                when (val content = last.content) {
                    is Content.Text -> {
                        when {
                            content.style == Content.Text.Style.TITLE -> addNewBlockAtTheEnd()
                            content.text.isNotEmpty() -> addNewBlockAtTheEnd()
                            else -> Timber.d("Outside-click has been ignored.")
                        }
                    }
                    is Content.Link -> {
                        addNewBlockAtTheEnd()
                    }
                    is Content.Bookmark -> {
                        addNewBlockAtTheEnd()
                    }
                    else -> {
                        Timber.d("Outside-click has been ignored.")
                    }
                }
            } else {
                addNewBlockAtTheEnd()
            }
        }
    }

    fun onHideKeyboardClicked() {
        viewModelScope.launch {
            focusChannel.send(EMPTY_FOCUS_ID)
            refresh()
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnClearFocusClicked)
    }

    private suspend fun refresh() {
        renderingChannel.send(blocks)
    }

    fun onPageClicked(id: String) {
        proceedWithOpeningPage(
            target = blocks.first { it.id == id }.content<Content.Link>().target
        )
    }

    fun onAddNewPageClicked() {

        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)

        val params = CreateDocument.Params(
            context = context,
            position = Position.BOTTOM,
            target = focusChannel.value,
            prototype = Prototype.Page(style = Content.Page.Style.EMPTY)
        )

        createDocument.invoke(scope = viewModelScope, params = params) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating new page with params: $params") },
                fnR = { (_, target) -> proceedWithOpeningPage(target) }
            )
        }
    }

    fun onArchiveThisPageClicked() {
        archiveDocument.invoke(
            scope = viewModelScope,
            params = ArchiveDocument.Params(
                context = context,
                target = context
            ),
            onResult = { result ->
                result.either(
                    fnL = { Timber.e(it, "Error while archiving page") },
                    fnR = { proceedWithExiting() }
                )
            }
        )
    }

    fun onAddBookmarkClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)

        val params = CreateBlock.Params(
            context = context,
            position = Position.BOTTOM,
            target = focusChannel.value,
            prototype = Prototype.Bookmark
        )

        createBlock.invoke(scope = viewModelScope, params = params) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a bookmark with params: $params") },
                fnR = { Timber.d("Bookmark created!") }
            )
        }
    }

    fun onBookmarkPlaceholderClicked(target: String) {
        dispatch(
            command = Command.OpenBookmarkSetter(
                context = context,
                target = target
            )
        )
    }

    fun onBookmarkMenuClicked(target: Id) {
        updateFocus(target)
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBookmarkMenuClicked)
    }

    fun onTextInputClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextInputClicked)
    }

    fun onPlusButtonPressed() {
        createPage.invoke(
            scope = viewModelScope,
            params = CreatePage.Params.insideDashboard()
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a new page on home dashboard") },
                fnR = { id -> proceedWithOpeningPage(id) }
            )
        }
    }

    fun onAddVideoUrlClicked(blockId: String, url: String) {
        //Todo add url validation
        uploadUrl.invoke(
            scope = viewModelScope,
            params = UploadUrl.Params(
                contextId = context,
                blockId = blockId,
                url = url,
                filePath = EMPTY_PATH
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while upload new url for video block") },
                fnR = { Timber.d("Upload Url Success") }
            )
        }
    }

    fun onAddVideoFileClicked(filePath: String?) {
        if (filePath == null) {
            Timber.d("Error while getting filePath")
            return
        }
        uploadUrl.invoke(
            scope = viewModelScope,
            params = UploadUrl.Params(
                contextId = context,
                blockId = mediaBlockId,
                url = "",
                filePath = filePath
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while upload new file path for video block") },
                fnR = { Timber.d("Upload File Path Success") }
            )
        }
    }

    fun onChooseVideoFileFromMedia() {
        try {
            val targetBlock = blocks.first { it.id == mediaBlockId }
            val targetContent = targetBlock.content as Content.File
            val newContent = targetContent.copy(state = Content.File.State.UPLOADING)
            val newBlock = targetBlock.copy(content = newContent)
            rerenderingBlocks(newBlock)
        } catch (e: Exception) {
            Timber.e(e, "Error while update block:$mediaBlockId state to Uploading")
            stateData.value = ViewState.Error("Can't load video for this block")
        }
    }

    fun onPageIconClicked() {
        val target = blocks.first { it.content is Content.Icon }.id
        dispatch(Command.OpenPagePicker(target))
    }

    fun onDownloadFileClicked(id: String) {
        dispatch(Command.RequestDownloadPermission(id))
    }

    fun startDownloadingFile(id: String) {
        val block = blocks.first { it.id == id }
        val file = block.content<Content.File>()
        downloadFile.invoke(
            scope = viewModelScope,
            params = DownloadFile.Params(
                url = urlBuilder.file(file.hash),
                name = file.name.orEmpty()
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while trying to download file: $file") },
                fnR = { Timber.d("Started download file: $file") }
            )
        }
    }

    fun onMediaBlockMenuClicked(id: String) {
        updateFocus(id)
        onActionToolbarClicked()
    }

    private fun addNewBlockAtTheEnd() {
        proceedWithCreatingNewTextBlock(
            id = "",
            position = Position.INNER,
            style = Content.Text.Style.P
        )
    }

    private fun proceedWithOpeningPage(target: Id) {
        navigate(EventWrapper(AppNavigation.Command.OpenPage(target)))
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Success(val blocks: List<BlockView>) : ViewState()
        data class Error(val message: String) : ViewState()
        data class OpenLinkScreen(
            val pageId: String,
            val block: Block,
            val range: IntRange
        ) : ViewState()
    }

    sealed class Command {
        data class OpenPagePicker(
            val target: String
        ) : Command()

        data class OpenGallery(
            val mediaType: String
        ) : Command()

        data class OpenBookmarkSetter(
            val target: String,
            val context: String
        ) : Command()

        object OpenAddBlockPanel : Command()

        data class RequestDownloadPermission(
            val id: String
        ) : Command()
    }

    companion object {
        const val EMPTY_FOCUS_ID = ""
        const val TEXT_CHANGES_DEBOUNCE_DURATION = 500L
        const val INITIAL_INDENT = 0
    }

    data class MarkupAction(
        val type: Markup.Type,
        val param: Any? = null
    )

    override fun onCleared() {
        super.onCleared()
        focusChannel.cancel()
        renderingChannel.cancel()
        textChannel.cancel()
        selectionChannel.cancel()
        markupActionChannel.cancel()
        controlPanelInteractor.channel.cancel()
    }
}