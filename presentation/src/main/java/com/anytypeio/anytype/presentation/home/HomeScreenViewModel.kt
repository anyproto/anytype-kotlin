package com.anytypeio.anytype.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.ext.process
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.Reducer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.CollapsedWidgetStateHolder
import com.anytypeio.anytype.presentation.widgets.DataViewListWidgetContainer
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.LinkWidgetContainer
import com.anytypeio.anytype.presentation.widgets.ListWidgetContainer
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.TreeWidgetBranchStateHolder
import com.anytypeio.anytype.presentation.widgets.TreeWidgetContainer
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetActiveViewStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetContainer
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.presentation.widgets.parseWidgets
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeScreenViewModel(
    private val configStorage: ConfigStorage,
    private val openObject: OpenObject,
    private val createWidget: CreateWidget,
    private val deleteWidget: DeleteWidget,
    private val updateWidget: UpdateWidget,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val getObject: GetObject,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val widgetEventDispatcher: Dispatcher<WidgetDispatchEvent>,
    private val objectPayloadDispatcher: Dispatcher<Payload>,
    private val interceptEvents: InterceptEvents,
    private val widgetActiveViewStateHolder: WidgetActiveViewStateHolder,
    private val collapsedWidgetStateHolder: CollapsedWidgetStateHolder,
    private val urlBuilder: UrlBuilder,
    private val createObject: CreateObject,
    private val move: Move
) : NavigationViewModel<HomeScreenViewModel.Navigation>(),
    Reducer<ObjectView, Payload>,
    WidgetActiveViewStateHolder by widgetActiveViewStateHolder,
    CollapsedWidgetStateHolder by collapsedWidgetStateHolder
{
    val views = MutableStateFlow<List<WidgetView>>(actions)
    val commands = MutableSharedFlow<Command>()
    val mode = MutableStateFlow<InteractionMode>(InteractionMode.Default)

    private val objectViewState = MutableStateFlow<ObjectViewState>(ObjectViewState.Idle)
    private val widgets = MutableStateFlow<List<Widget>>(emptyList())
    private val containers = MutableStateFlow<List<WidgetContainer>>(emptyList())
    private val treeWidgetBranchStateHolder = TreeWidgetBranchStateHolder()

    // Bundled widget containing recently opened objects
    private val recent by lazy {
        ListWidgetContainer(
            workspace = configStorage.get().workspace,
            isWidgetCollapsed = isCollapsed(Subscriptions.SUBSCRIPTION_RECENT),
            storage = storelessSubscriptionContainer,
            urlBuilder = urlBuilder,
            subscription = Subscriptions.SUBSCRIPTION_RECENT
        )
    }

    // Bundled widget containing sets
    private val sets by lazy {
        ListWidgetContainer(
            workspace = configStorage.get().workspace,
            isWidgetCollapsed = isCollapsed(Subscriptions.SUBSCRIPTION_SETS),
            storage = storelessSubscriptionContainer,
            urlBuilder = urlBuilder,
            subscription = Subscriptions.SUBSCRIPTION_SETS
        )
    }

    // Bundled widget containing objects from favorites
    private val favorites by lazy {
        ListWidgetContainer(
            workspace = configStorage.get().workspace,
            isWidgetCollapsed = isCollapsed(Subscriptions.SUBSCRIPTION_FAVORITES),
            storage = storelessSubscriptionContainer,
            urlBuilder = urlBuilder,
            subscription = Subscriptions.SUBSCRIPTION_FAVORITES
        )
    }

    // Bundled widget containing archived objects
    private val bin = WidgetView.Bin(Subscriptions.SUBSCRIPTION_ARCHIVED)

    private val bundledWidgets by lazy {
        combine(
            listOf(
                favorites.view,
                recent.view,
                sets.view
            )
        ) { array -> array }
    }

    init {

        val config = configStorage.get()

        val externalChannelEvents = interceptEvents.build(InterceptEvents.Params(config.widgets)).map {
            Payload(
                context = config.widgets,
                events = it
            )
        }

        val internalChannelEvents = objectPayloadDispatcher.flow()

        val payloads = merge(externalChannelEvents, internalChannelEvents)

        viewModelScope.launch {
            objectViewState.flatMapLatest { state ->
                when (state) {
                    is ObjectViewState.Idle -> flowOf(state)
                    is ObjectViewState.Failure -> flowOf(state)
                    is ObjectViewState.Loading -> flowOf(state)
                    is ObjectViewState.Success -> {
                        payloads.scan(state) { s, p -> s.copy(obj = reduce(s.obj, p)) }
                    }
                }
            }.map { state ->
                Timber.d("Emitting new state: ${state::class.java.simpleName}")
                when (state) {
                    is ObjectViewState.Failure -> {
                        emptyList()
                    }
                    is ObjectViewState.Idle -> {
                        emptyList()
                    }
                    is ObjectViewState.Loading -> {
                        emptyList()
                    }
                    is ObjectViewState.Success -> {
                        state.obj.blocks.parseWidgets(
                            root = state.obj.root,
                            details = state.obj.details
                        )
                    }
                }
            }.collect {
                Timber.d("Emitting list of widgets: ${it.size}")
                widgets.value = it
            }
        }

        viewModelScope.launch {
            widgets.map {
                it.map { widget ->
                    when (widget) {
                        is Widget.Link -> LinkWidgetContainer(
                            widget = widget
                        )
                        is Widget.Tree -> TreeWidgetContainer(
                            widget = widget,
                            container = storelessSubscriptionContainer,
                            expandedBranches = treeWidgetBranchStateHolder.stream(widget.id),
                            isWidgetCollapsed = isCollapsed(widget.id),
                            urlBuilder = urlBuilder,
                            dispatchers = appCoroutineDispatchers
                        )
                        is Widget.List -> DataViewListWidgetContainer(
                            widget = widget,
                            storage = storelessSubscriptionContainer,
                            getObject = getObject,
                            activeView = observeCurrentWidgetView(widget.id),
                            isWidgetCollapsed = isCollapsed(widget.id),
                            urlBuilder = urlBuilder
                        )
                    }
                }
            }.collect {
                Timber.d("Emitting list of containers: ${it.size}")
                containers.value = it
            }
        }

        viewModelScope.launch {
            containers.flatMapLatest { list ->
                Timber.d("Receiving list of containers: ${list.map { it::class }}")
                if (list.isNotEmpty()) {
                    combine(
                        list.map { m -> m.view }
                    ) { array ->
                        array.toList()
                    }
                } else {
                    flowOf(emptyList())
                }
            }.combine(bundledWidgets) { fromWidgetObject, bundled ->
                bundled.toList() + fromWidgetObject
            }.flowOn(appCoroutineDispatchers.io).collect {
                views.value = it + bin + actions
            }
        }

        proceedWithOpeningWidgetObject(widgetObject = config.widgets)
        proceedWithDispatches()
    }

    private fun proceedWithOpeningWidgetObject(widgetObject: Id) {
        viewModelScope.launch {
            openObject.stream(
                OpenObject.Params(widgetObject, false)
            ).collect { result ->
                when (result) {
                    is Resultat.Failure -> {
                        objectViewState.value = ObjectViewState.Failure(result.exception)
                        Timber.e(result.exception, "Error while opening object.")
                    }
                    is Resultat.Loading -> {
                        objectViewState.value = ObjectViewState.Loading
                    }
                    is Resultat.Success -> {
                        objectViewState.value = ObjectViewState.Success(
                            obj = result.value
                        )
                    }
                }
            }
        }
    }

    private fun proceedWithDispatches() {
        viewModelScope.launch {
            widgetEventDispatcher.flow().collect { dispatch ->
                Timber.d("New dispatch: $dispatch")
                when (dispatch) {
                    is WidgetDispatchEvent.SourcePicked -> {
                        commands.emit(
                            Command.SelectWidgetType(
                                ctx = configStorage.get().widgets,
                                source = dispatch.source,
                                layout = dispatch.sourceLayout
                            )
                        )
                    }
                    is WidgetDispatchEvent.SourceChanged -> {
                        proceedWithUpdatingWidget(
                            widget = dispatch.widget,
                            source = dispatch.source,
                            type = dispatch.type
                        )
                    }
                    is WidgetDispatchEvent.TypePicked -> {
                        proceedWithCreatingWidget(
                            source = dispatch.source,
                            type = dispatch.widgetType
                        )
                    }
                }
            }
        }
    }

    private fun proceedWithCreatingWidget(source: Id, type: Int) {
        viewModelScope.launch {
            val config = configStorage.get()
            createWidget(
                CreateWidget.Params(
                    ctx = config.widgets,
                    source = source,
                    type = when(type) {
                        Command.ChangeWidgetType.TYPE_LINK -> WidgetLayout.LINK
                        Command.ChangeWidgetType.TYPE_TREE -> WidgetLayout.TREE
                        Command.ChangeWidgetType.TYPE_LIST -> WidgetLayout.LINK
                        else -> WidgetLayout.LINK
                    }
                )
            ).flowOn(appCoroutineDispatchers.io).collect { status ->
                Timber.d("Status while creating widget: $status")
                when (status) {
                    is Resultat.Failure -> {
                        sendToast("Error while creating widget: ${status.exception}")
                        Timber.e(status.exception, "Error while creating widget")
                    }
                    is Resultat.Loading -> {
                        // Do nothing?
                    }
                    is Resultat.Success -> {
                        objectPayloadDispatcher.send(status.value)
                    }
                }
            }
        }
    }

    /**
     * @param [type] type code from [Command.ChangeWidgetType]
     */
    private fun proceedWithUpdatingWidget(
        widget: Id,
        source: Id,
        type: Int
    ) {
        viewModelScope.launch {
            val config = configStorage.get()
            updateWidget(
                UpdateWidget.Params(
                    ctx = config.widgets,
                    source = source,
                    target = widget,
                    type = when(type) {
                        Command.ChangeWidgetType.TYPE_LINK -> WidgetLayout.LINK
                        Command.ChangeWidgetType.TYPE_TREE -> WidgetLayout.TREE
                        else -> throw IllegalStateException("Unexpected type: $type")
                    }
                )
            ).flowOn(appCoroutineDispatchers.io).collect { status ->
                Timber.d("Status while creating widget: $status")
                when (status) {
                    is Resultat.Failure -> {
                        sendToast("Error while creating widget: ${status.exception}")
                        Timber.e(status.exception, "Error while creating widget")
                    }
                    is Resultat.Loading -> {
                        // Do nothing?
                    }
                    is Resultat.Success -> {
                        objectPayloadDispatcher.send(status.value)
                    }
                }
            }
        }
    }

    private fun proceedWithDeletingWidget(widget: Id) {
        viewModelScope.launch {
            val config = configStorage.get()
            deleteWidget(
                DeleteWidget.Params(
                    ctx = config.widgets,
                    targets = listOf(widget)
                )
            ).flowOn(appCoroutineDispatchers.io).collect { status ->
                Timber.d("Status while deleting widget: $status")
                when (status) {
                    is Resultat.Failure -> {
                        sendToast("Error while deleting widget: ${status.exception}")
                        Timber.e(status.exception, "Error while deleting widget")
                    }
                    is Resultat.Loading -> {
                        // Do nothing?
                    }
                    is Resultat.Success -> {
                        objectPayloadDispatcher.send(status.value)
                    }
                }
            }
        }
    }

    fun onEditWidgets() {
        mode.value = InteractionMode.Edit
    }

    fun onExitEditMode() {
        mode.value = InteractionMode.Default
    }

    fun onExpand(path: TreePath) {
        treeWidgetBranchStateHolder.onExpand(linkPath = path)
    }

    fun onWidgetObjectClicked(obj: ObjectWrapper.Basic) {
        Timber.d("With id: ${obj.id}")
        if (obj.isArchived != true) {
            proceedWithOpeningObject(obj)
        } else {
            sendToast("Open bin to restore your archived object")
        }
    }

    fun onDropDownMenuAction(widget: Id, action: DropDownMenuAction) {
        when (action) {
            DropDownMenuAction.ChangeWidgetSource -> {
                proceedWithChangingSource(widget)
            }
            DropDownMenuAction.ChangeWidgetType -> {
                proceedWithChangingType(widget)
            }
            DropDownMenuAction.EditWidgets -> {
                onEditWidgets()
            }
            DropDownMenuAction.RemoveWidget -> {
                proceedWithDeletingWidget(widget)
            }
        }
    }

    fun onBundledWidgetClicked(widget: Id) {
        when(widget) {
            Subscriptions.SUBSCRIPTION_SETS -> {
                navigate(Navigation.ExpandWidget(Subscription.Sets))
            }
            Subscriptions.SUBSCRIPTION_RECENT -> {
                navigate(Navigation.ExpandWidget(Subscription.Recent))
            }
            Subscriptions.SUBSCRIPTION_ARCHIVED -> {
                navigate(Navigation.ExpandWidget(Subscription.Bin))
            }
            Subscriptions.SUBSCRIPTION_FAVORITES -> {
                navigate(Navigation.ExpandWidget(Subscription.Favorites))
            }
        }
    }

    private fun proceedWithChangingType(widget: Id) {
        Timber.d("onChangeWidgetSourceClicked, widget:[$widget]")
        val curr = widgets.value.find { it.id == widget }
        if (curr != null) {
            viewModelScope.launch {
                commands.emit(
                    Command.ChangeWidgetType(
                        ctx = configStorage.get().widgets,
                        widget = widget,
                        source = curr.source.id,
                        type = parseWidgetType(curr)
                    )
                )
            }
        } else {
            sendToast("Widget missing. Please try again later")
        }
    }

    private fun proceedWithChangingSource(widget: Id) {
        val curr = widgets.value.find { it.id == widget }
        if (curr != null) {
            viewModelScope.launch {
                commands.emit(
                    Command.ChangeWidgetSource(
                        ctx = configStorage.get().widgets,
                        widget = widget,
                        source = curr.source.id,
                        type = parseWidgetType(curr)
                    )
                )
            }
        } else {
            sendToast("Widget missing. Please try again later")
        }
    }

    private fun parseWidgetType(curr: Widget) = when (curr) {
        is Widget.Link -> Command.ChangeWidgetType.TYPE_LINK
        is Widget.Tree -> Command.ChangeWidgetType.TYPE_TREE
        is Widget.List -> Command.ChangeWidgetType.TYPE_LIST
    }

    // TODO move to a separate reducer inject into this VM's constructor
    override fun reduce(state: ObjectView, event: Payload): ObjectView {
        var curr = state
        event.events.forEach { e ->
            when (e) {
                is Event.Command.AddBlock -> {
                    curr = curr.copy(blocks = curr.blocks + e.blocks)
                }
                is Event.Command.DeleteBlock -> {
                    curr = curr.copy(
                        blocks = curr.blocks.filter { !e.targets.contains(it.id) }
                    )
                }
                is Event.Command.UpdateStructure -> {
                    curr = curr.copy(
                        blocks = curr.blocks.replace(
                            replacement = { target ->
                                target.copy(children = e.children)
                            },
                            target = { block -> block.id == e.id }
                        )
                    )
                }
                is Event.Command.Details -> {
                    curr = curr.copy(details = curr.details.process(e))
                }
                else -> {
                    Timber.d("Skipping event: $e")
                }
            }
        }
        return curr
    }

    private fun proceedWithOpeningObject(obj: ObjectWrapper.Basic) {
        when (obj.layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.FILE,
            ObjectType.Layout.BOOKMARK -> navigate(Navigation.OpenObject(obj.id))
            ObjectType.Layout.SET -> navigate(Navigation.OpenSet(obj.id))
            else -> sendToast("Unexpected layout: ${obj.layout}")
        }
    }

    fun onCreateNewObjectClicked() {
        viewModelScope.launch {
            createObject.stream(CreateObject.Param(null)).collect { result ->
                result.fold(
                    onSuccess = { navigate(Navigation.OpenObject(it.objectId)) },
                    onFailure = {
                        Timber.e(it, "Error while creating object")
                        sendToast("Error while creating object. Please, try again later")
                    }
                )
            }
        }
    }

    fun onMove(views: List<WidgetView>, from: Int, to: Int) {
        viewModelScope.launch {
            val direction = if (from < to) Position.BOTTOM else Position.TOP
            val subject = views[to].id
            val target = if (direction == Position.TOP) views[to.inc()].id else views[to.dec()].id
            move.stream(
                Move.Params(
                    context = configStorage.get().widgets,
                    targetId = target,
                    targetContext = configStorage.get().widgets,
                    blockIds = listOf(subject),
                    position = direction
                )
            ).collect { result ->
                result.fold(
                    onSuccess = { objectPayloadDispatcher.send(it) },
                    onFailure = { Timber.e(it, "Error while moving blocks") }
                )
            }
        }
    }

    sealed class Navigation {
        data class OpenObject(val ctx: Id) : Navigation()
        data class OpenSet(val ctx: Id) : Navigation()
        data class ExpandWidget(val subscription: Subscription) : Navigation()
    }

    class Factory @Inject constructor(
        private val configStorage: ConfigStorage,
        private val openObject: OpenObject,
        private val createObject: CreateObject,
        private val createWidget: CreateWidget,
        private val deleteWidget: DeleteWidget,
        private val updateWidget: UpdateWidget,
        private val appCoroutineDispatchers: AppCoroutineDispatchers,
        private val widgetEventDispatcher: Dispatcher<WidgetDispatchEvent>,
        private val objectPayloadDispatcher: Dispatcher<Payload>,
        private val interceptEvents: InterceptEvents,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val widgetActiveViewStateHolder: WidgetActiveViewStateHolder,
        private val collapsedWidgetStateHolder: CollapsedWidgetStateHolder,
        private val urlBuilder: UrlBuilder,
        private val getObject: GetObject,
        private val move: Move
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeScreenViewModel(
            configStorage = configStorage,
            openObject = openObject,
            createObject = createObject,
            createWidget = createWidget,
            deleteWidget = deleteWidget,
            updateWidget = updateWidget,
            appCoroutineDispatchers = appCoroutineDispatchers,
            widgetEventDispatcher = widgetEventDispatcher,
            objectPayloadDispatcher = objectPayloadDispatcher,
            interceptEvents = interceptEvents,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            widgetActiveViewStateHolder = widgetActiveViewStateHolder,
            collapsedWidgetStateHolder = collapsedWidgetStateHolder,
            getObject = getObject,
            urlBuilder = urlBuilder,
            move = move
        ) as T
    }

    companion object {
        val actions = listOf(
            WidgetView.Action.Library,
            WidgetView.Action.EditWidgets
        )
    }
}

/**
 * State representing session while working with an object.
 */
sealed class ObjectViewState {
    object Idle : ObjectViewState()
    object Loading : ObjectViewState()
    data class Success(val obj: ObjectView) : ObjectViewState()
    data class Failure(val e: Throwable) : ObjectViewState()
}

sealed class InteractionMode {
    object Default : InteractionMode()
    object Edit : InteractionMode()
}

sealed class Command {
    object SelectWidgetSource : Command()
    data class SelectWidgetType(
        val ctx: Id,
        val source: Id,
        val layout: Int
    ) : Command()
    data class ChangeWidgetSource(
        val ctx: Id,
        val widget: Id,
        val source: Id,
        val type: Int
    ) : Command()
    data class ChangeWidgetType(
        val ctx: Id,
        val widget: Id,
        val source: Id,
        val type: Int
    ) : Command() {
        companion object {
            const val TYPE_TREE = 0
            const val TYPE_LINK = 1
            const val TYPE_LIST = 2
        }
    }
}