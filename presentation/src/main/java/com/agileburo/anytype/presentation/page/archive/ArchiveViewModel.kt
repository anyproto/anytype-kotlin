package com.agileburo.anytype.presentation.page.archive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.extensions.updateSelection
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.switchToLatestFrom
import com.agileburo.anytype.core_utils.ext.withLatestFrom
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.common.Document
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.editor.Editor
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.ext.asMap
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.domain.page.*
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.common.SupportCommand
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.page.editor.Command
import com.agileburo.anytype.presentation.page.editor.Orchestrator
import com.agileburo.anytype.presentation.page.editor.Proxy
import com.agileburo.anytype.presentation.page.render.BlockViewRenderer
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.selection.SelectionStateHolder
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class ArchiveViewState {
    object Loading : ArchiveViewState()
    data class Success(val blocks: List<BlockView>, val selections: Int) : ArchiveViewState()
}

class ArchiveViewModel(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val archiveDocument: ArchiveDocument,
    private val interceptEvents: InterceptEvents,
    private val renderer: DefaultBlockViewRenderer,
    private val reducer: StateReducer<List<Block>, Event>,
    private val orchestrator: Orchestrator,
    private val selectionStateHolder: SelectionStateHolder
) : ViewStateViewModel<ArchiveViewState>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>>,
    SupportCommand<Command>,
    BlockViewRenderer by renderer,
    ToggleStateHolder by renderer,
    SelectionStateHolder by orchestrator.memory.selections,
    StateReducer<List<Block>, Event> by reducer {

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()
    override val commands = MutableLiveData<EventWrapper<Command>>()

    private var eventSubscription: Job? = null

    private var mode = EditorMode.MULTI_SELECT

    private val renderCommand = Proxy.Subject<Unit>()
    private val renderizePipeline = Proxy.Subject<Document>()

    var context: String = ""
    var blocks: Document = emptyList()

    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    init {
        startObservingPayload()
        startObservingErrors()
        processRendering()
        viewModelScope.launch { orchestrator.start() }
    }

    private fun startObservingPayload() {
        viewModelScope.launch {
            orchestrator
                .proxies
                .payloads
                .stream()
                .map { payload -> processEvents(payload.events) }
                .collect { viewModelScope.launch { refresh() } }
        }
    }

    private suspend fun processEvents(events: List<Event>) {
        events.forEach { event ->
            if (event is Event.Command.ShowBlock) {
                orchestrator.stores.details.update(event.details)
            }
            if (event is Event.Command.UpdateDetails) {
                orchestrator.stores.details.add(event.target, event.details)
            }
            blocks = reduce(blocks, event)
        }
    }

    private fun startObservingErrors() {
        viewModelScope.launch {
            orchestrator.proxies.errors
                .stream()
                .collect {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    private fun processRendering() {
        renderCommand
            .stream()
            .switchToLatestFrom(orchestrator.stores.views.stream())
            .onEach { dispatchToUI(it) }
            .launchIn(viewModelScope)

        renderizePipeline
            .stream()
            .filter { it.isNotEmpty() }
            .withLatestFrom(
                orchestrator.stores.details.stream()
            ) { models, details ->
                models.asMap().render(
                    mode = mode,
                    indent = 0,
                    anchor = context,
                    focus = Editor.Focus.empty(),
                    root = models.first { it.id == context },
                    details = details
                )
            }
            .onEach { views ->
                orchestrator.stores.views.update(views)
                renderCommand.send(Unit)
            }
            .launchIn(viewModelScope)
    }

    private fun dispatchToUI(views: List<BlockView>) {
        stateData.postValue(
            ArchiveViewState.Success(
                blocks = views,
                selections = selectionStateHolder.currentSelection().size
            )
        )
    }

    fun onStart(id: Id) {
        context = id

        stateData.postValue(ArchiveViewState.Loading)

        eventSubscription = viewModelScope.launch {
            interceptEvents
                .build(InterceptEvents.Params(context))
                .map { events -> processEvents(events) }
                .collect { refresh() }
        }

        viewModelScope.launch {
            openPage(OpenPage.Params(id)).proceed(
                success = { payload ->
                    orchestrator.proxies.payloads.send(payload)
                },
                failure = { Timber.e(it, "Error while opening page with id: $id") }
            )
        }
    }

    fun onPutBackClicked() {
        val selectedBlocks = selectionStateHolder.currentSelection().toList()
        if (selectedBlocks.isNotEmpty()) {
            val targets = mutableListOf<Id>()
            selectedBlocks.forEach { id ->
                targets.add(
                    blocks.first { it.id == id }.content<Block.Content.Link>().target
                )
            }
            selectionStateHolder.clearSelections()
            viewModelScope.launch {
                archiveDocument(
                    ArchiveDocument.Params(
                        context = context,
                        targets = targets,
                        isArchived = false
                    )
                ).proceed(
                    failure = { Timber.e(it, "Error while archiving page") },
                    success = {
                        Timber.d("Success to unarchive pages!")
                        selectionStateHolder.clearSelections()
                    }
                )
            }
        }
    }

    fun onPageClicked(click: ListenerType) {
        if (click is ListenerType.Page) {
            (stateData.value as? ArchiveViewState.Success)?.let { state ->
                selectionStateHolder.toggleSelection(click.target)
                val update = state.blocks.map { block ->
                    if (block.id == click.target)
                        block.updateSelection(selectionStateHolder.isSelected(click.target))
                    else
                        block
                }
                stateData.postValue(
                    ArchiveViewState.Success(
                        update,
                        selectionStateHolder.currentSelection().size
                    )
                )
                Timber.d("Selected : ${selectionStateHolder.currentSelection().size}")
            }
        }
    }

    fun onStop() {
        eventSubscription?.cancel()
    }

    fun onBottomSheetHidden() {
        proceedWithExitingToDesktop()
    }

    private fun proceedWithExitingToDesktop() {
        closePage(viewModelScope, ClosePage.Params(context)) { result ->
            result.either(
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop)) },
                fnL = { Timber.e(it, "Error while closing the test page") }
            )
        }
    }

    private suspend fun refresh() {
        renderizePipeline.send(blocks)
    }

    override fun onCleared() {
        super.onCleared()

        orchestrator.stores.focus.cancel()
        orchestrator.stores.details.cancel()
        orchestrator.stores.textSelection.cancel()
        orchestrator.proxies.changes.cancel()
        orchestrator.proxies.saves.cancel()
        renderizePipeline.cancel()
    }
}