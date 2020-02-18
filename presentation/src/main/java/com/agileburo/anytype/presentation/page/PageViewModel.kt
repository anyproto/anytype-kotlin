package com.agileburo.anytype.presentation.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.replace
import com.agileburo.anytype.core_utils.ext.withLatestFrom
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content
import com.agileburo.anytype.domain.block.model.Block.Prototype
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.ext.*
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.page.ControlPanelMachine.Interactor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class PageViewModel(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val updateBlock: UpdateBlock,
    private val createBlock: CreateBlock,
    private val interceptEvents: InterceptEvents,
    private val updateCheckbox: UpdateCheckbox,
    private val unlinkBlocks: UnlinkBlocks,
    private val duplicateBlock: DuplicateBlock,
    private val updateTextStyle: UpdateTextStyle,
    private val updateTextColor: UpdateTextColor,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val mergeBlocks: MergeBlocks,
    private val splitBlock: SplitBlock
) : ViewStateViewModel<PageViewModel.ViewState>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val controlPanelInteractor = Interactor(viewModelScope)
    val controlPanelViewState = MutableLiveData<ControlPanelState>()

    private val renderingChannel = Channel<List<Block>>()
    private val renderings = renderingChannel.consumeAsFlow()

    private val focusChannel = ConflatedBroadcastChannel(EMPTY_FOCUS_ID)
    private val focusChanges = focusChannel.asFlow()

    private val textChannel = Channel<Triple<Id, String, List<Content.Text.Mark>>>()
    private val textChanges = textChannel.consumeAsFlow()

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

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

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
                .collect { event -> handleEvents(event) }
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

    private fun handleEvents(events: List<Event>) {

        events.forEach { event ->
            Timber.d("Handling event: $event")
            when (event) {
                is Event.Command.ShowBlock -> {
                    blocks = event.blocks
                }
                is Event.Command.AddBlock -> {
                    blocks = blocks + event.blocks
                    viewModelScope.launch { focusChannel.send(event.blocks.last().id) }
                }
                is Event.Command.UpdateStructure -> {
                    blocks = blocks.map { block ->
                        if (block.id == event.id)
                            block.copy(children = event.children)
                        else
                            block
                    }
                }
                is Event.Command.DeleteBlock -> {
                    blocks = blocks.filter { !event.targets.contains(it.id) }
                }
                is Event.Command.GranularChange -> {
                    blocks = blocks.map { block ->
                        if (block.id == event.id) {
                            val content = block.content.asText()
                            block.copy(
                                content = content.copy(
                                    style = event.style ?: content.style,
                                    color = event.color ?: content.color,
                                    text = event.text ?: content.text,
                                    marks = event.marks ?: content.marks
                                )
                            )
                        } else
                            block
                    }
                }
            }
        }

        viewModelScope.launch { renderingChannel.send(blocks) }
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
                fnL = {
                    throwable -> Timber.e("Error update marks:${throwable.message}")
                },
                fnR = {
                    val newContent = targetContent.copy(marks = it)
                    val newBlock = targetBlock.copy(content = newContent)
                    rerenderingBlocks(newBlock)
                    proceedWithUpdatingBlock(
                        params = UpdateBlock.Params(
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

        renderingChannel.send(blocks)

        proceedWithUpdatingBlock(
            params = UpdateBlock.Params(
                contextId = context,
                blockId = newBlock.id,
                text = newContent.text,
                marks = newContent.marks
            )
        )
    }

    private fun rerenderingBlocks(block: Block) =
        viewModelScope.launch {
            val update = blocks.map {
                if (it.id != block.id)
                    it
                else
                    block
            }
            blocks = update
            renderingChannel.send(blocks)
        }

    private fun processRendering() {
        viewModelScope.launch {
            renderings.withLatestFrom(focusChanges) { models, focus ->

                val render = models.asMap().asRender(context)

                val numbers = render.numbers()

                render.mapNotNull { block ->
                    when (block.content) {
                        is Content.Text -> {
                            block.toView(
                                focused = block.id == focus,
                                numbers = numbers
                            )
                        }
                        is Content.Image -> {
                            block.toView()
                        }
                        is Content.Link -> block.toView()
                        else -> null
                    }
                }
            }.collect { dispatchToUI(it) }
        }
    }

    private fun dispatchToUI(views: List<BlockView>) {
        stateData.postValue(ViewState.Success(views))
    }

    private fun startHandlingTextChanges() {
        textChanges
            .debounce(TEXT_CHANGES_DEBOUNCE_DURATION)
            .map { (id, text, marks) ->

                val update = blocks.map { block ->
                    if (block.id == id) {
                        block.copy(
                            content = block.content.asText().copy(
                                text = text,
                                marks = marks.filter { it.range.first != it.range.last }
                            )
                        )
                    } else
                        block
                }

                blocks = update

                UpdateBlock.Params(
                    contextId = context,
                    blockId = id,
                    text = text,
                    marks = marks.filter { it.range.first != it.range.last }
                )
            }
            .onEach { params -> proceedWithUpdatingBlock(params) }
            .launchIn(viewModelScope)
    }

    private fun proceedWithUpdatingBlock(params: UpdateBlock.Params) {
        Timber.d("Starting updating block with params: $params")
        updateBlock.invoke(viewModelScope, params) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while updating text: $params") },
                fnR = { Timber.d("Text has been updated") }
            )
        }
    }

    fun open(id: String) {

        context = id

        stateData.postValue(ViewState.Loading)

        openPage.invoke(viewModelScope, OpenPage.Params(id)) { result ->
            result.either(
                fnR = { Timber.d("Page with id $id has been opened") },
                fnL = { Timber.e(it, "Error while openining page with id: $id") }
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
                    proceedWithUpdatingBlock(
                        params = UpdateBlock.Params(
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

    fun onBottomSheetHidden() {
        proceedWithExiting()
    }

    private fun proceedWithExiting() {
        closePage.invoke(viewModelScope, ClosePage.Params(context)) { result ->
            result.either(
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.Exit)) },
                fnL = { Timber.e(it, "Error while closing the test page") }
            )
        }
    }

    fun onTextChanged(id: String, text: String, marks: List<Content.Text.Mark>) {
        Timber.d("onTextChanged: $id\nNew text: $text\nMarks: $marks")
        viewModelScope.launch { textChannel.send(Triple(id, text, marks)) }
    }

    fun onSelectionChanged(id: String, selection: IntRange) {
        viewModelScope.launch { selectionChannel.send(Pair(id, selection)) }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnSelectionChanged(selection))
    }

    fun onBlockFocusChanged(id: String, hasFocus: Boolean) {
        Timber.d("Focus changed ($hasFocus): $id")
        if (hasFocus) {
            viewModelScope.launch { focusChannel.send(id) }
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
            proceedWithMergingBlocks(previous, id)
        } else {
            Timber.d("Skipping merge on non-empty-block-backspace-pressed event")
        }
    }

    private fun proceedWithMergingBlocks(id: String, previous: String) {
        mergeBlocks.invoke(
            scope = viewModelScope,
            params = MergeBlocks.Params(
                context = context,
                pair = Pair(id, previous)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while merging blocks: $id, $previous") },
                fnR = { Timber.d("Succesfully merged $id and $previous") }
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
                fnR = { Timber.d("Succesfully split the target block with id: $id") }
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
                fnR = { Timber.d("Request to create a block has been dispatched") }
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
        Timber.e("Not implemented")
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
        unlinkBlocks.invoke(
            scope = viewModelScope,
            params = UnlinkBlocks.Params(
                context = context,
                targets = listOf(target)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while unlinking block with id: $target") },
                fnR = { Timber.d("Succesfully unlinked block with id: $target") }
            )
        }
    }


    fun onActionDuplicateClicked() {
        viewModelScope.launch {
            focusChanges
                .take(1)
                .collect { focus ->
                    duplicateBlock.invoke(
                        scope = this,
                        params = DuplicateBlock.Params(
                            context = context,
                            original = focus
                        )
                    ) { result ->
                        result.either(
                            fnL = { Timber.e(it, "Error while duplicating block with id: $focus") },
                            fnR = { Timber.d("Succesfully duplicated block with id: $focus") }
                        )
                    }
                }
        }
    }

    fun onAddTextBlockClicked(style: Content.Text.Style) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
        proceedWithCreatingNewTextBlock(
            id = "",
            position = Position.INNER,
            style = style
        )
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
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarToggleClicked)
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
                        if (content.style == Content.Text.Style.TITLE) {
                            addNewBlockAtTheEnd()
                        }
                    }
                    is Content.Link -> {
                        addNewBlockAtTheEnd()
                    }
                    else -> {
                        Timber.d("Outside-click has been ignored.")
                    }
                }
            }
        }
    }

    fun onHideKeyboardClicked() {
        viewModelScope.launch {
            focusChannel.send(EMPTY_FOCUS_ID)
            renderingChannel.send(blocks)
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnClearFocusClicked)
    }

    fun onPageClicked(id: String) {
        proceedWithOpeningPage(
            target = blocks.first { it.id == id }.content<Content.Link>().target
        )
    }

    private fun proceedWithOpeningPage(target: Id) {
        navigate(EventWrapper(AppNavigation.Command.OpenPage(target)))
    }

    fun onAddNewPageClicked() {

        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)

        val params = CreateBlock.Params(
            context = context,
            position = Position.BOTTOM,
            target = focusChannel.value,
            prototype = Prototype.Page(style = Content.Page.Style.EMPTY)
        )

        createBlock.invoke(scope = viewModelScope, params = params) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating new page with params: $params") },
                fnR = { Timber.d("Page created!") }
            )
        }
    }

    private fun addNewBlockAtTheEnd() {
        proceedWithCreatingNewTextBlock(
            id = "",
            position = Position.INNER,
            style = Content.Text.Style.P
        )
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Success(val blocks: List<BlockView>) : ViewState()
        data class Error(val message: String) : ViewState()
        data class OpenLinkScreen(val pageId: String, val block: Block, val range: IntRange) :
            ViewState()
    }

    companion object {
        const val EMPTY_FOCUS_ID = ""
        const val TEXT_CHANGES_DEBOUNCE_DURATION = 500L
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