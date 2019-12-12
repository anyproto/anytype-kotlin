package com.agileburo.anytype.presentation.page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.block.interactor.CreateBlock
import com.agileburo.anytype.domain.block.interactor.UpdateBlock
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.event.interactor.ObserveEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.ext.asMap
import com.agileburo.anytype.domain.ext.asRender
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class PageViewModel(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val updateBlock: UpdateBlock,
    private val createBlock: CreateBlock,
    private val observeEvents: ObserveEvents
) : ViewStateViewModel<PageViewModel.ViewState>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val textChanges = Channel<Pair<String, String>>()

    /**
     * Currently opened page id.
     */
    private var pageId: String = ""
    private var blocks: List<Block> = emptyList()

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    init {
        startHandlingTextChanges()
        startObservingEvents()
    }

    private fun startObservingEvents() {
        viewModelScope.launch {
            observeEvents.build().collect { event -> handleEvent(event) }
        }
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is Event.Command.ShowBlock -> {
                blocks = event.blocks
                stateData.postValue(
                    ViewState.Success(
                        blocks = event.blocks.asMap().asRender(pageId).mapNotNull { block ->
                            when {
                                block.content is Block.Content.Text -> block.toView()
                                block.content is Block.Content.Image -> block.toView()
                                else -> null
                            }
                        }
                    )
                )
            }
            is Event.Command.AddBlock -> {
                blocks = blocks + event.blocks
                dispatchBlocksToView()
            }
            is Event.Command.UpdateBlockText -> {
                val new = blocks.map { block ->
                    if (block.id != event.id)
                        block
                    else
                        block.copy(
                            content = (block.content as? Block.Content.Text)?.copy(text = event.text)
                                ?: block.content
                        )
                }
                blocks = new
            }
        }
    }

    private fun dispatchBlocksToView() {
        stateData.postValue(
            ViewState.Success(
                blocks = blocks.mapNotNull { block ->
                    if (block.content is Block.Content.Text)
                        block.toView()
                    else
                        null
                }
            )
        )
    }

    private fun startHandlingTextChanges() {
        viewModelScope.launch {
            textChanges
                .consumeAsFlow()
                .debounce(500L)
                .map { (blockId, text) ->
                    UpdateBlock.Params(
                        contextId = pageId,
                        blockId = blockId,
                        text = text
                    )
                }
                .collect {
                    updateBlock.invoke(this, it) { result ->
                        result.either(
                            fnL = { e -> Timber.e(e, "Error while updating text: $it") },
                            fnR = { Timber.d("Text has been updated") }
                        )
                    }
                }
        }
    }

    fun open(id: String) {

        pageId = id

        Timber.d("Opening a page with id: $id")

        stateData.postValue(ViewState.Loading)

        openPage.invoke(viewModelScope, OpenPage.Params(id)) { result ->
            result.either(
                fnR = { Timber.d("Page has been opened") },
                fnL = { e -> Timber.e(e, "Error while openining page with id: $id") }
            )
        }
    }

    fun onSystemBackPressed() {
        closePage.invoke(viewModelScope, ClosePage.Params(pageId)) { result ->
            result.either(
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.Exit)) },
                fnL = { e -> Timber.e(e, "Error while closing the test page") }
            )
        }
    }

    fun onTextChanged(id: String, text: String) {
        viewModelScope.launch { textChanges.send(Pair(id, text)) }
    }

    fun onAddTextBlockClicked() {
        createBlock.invoke(
            viewModelScope, CreateBlock.Params.empty(
                contextId = pageId,
                targetId = blocks.last().id
            )
        ) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while creating a block") },
                fnR = { Timber.d("Request to create a block has been dispatched") }
            )
        }
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Success(val blocks: List<BlockView>) : ViewState()
        data class Error(val message: String) : ViewState()
    }
}

