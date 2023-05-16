package com.anytypeio.anytype.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.core_models.ext.process
import com.anytypeio.anytype.core_utils.ext.letNotNull
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.bin.EmptyBin
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.Reducer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.widgets.GetWidgetSession
import com.anytypeio.anytype.domain.widgets.SaveWidgetSession
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.presentation.extension.sendAddWidgetEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendDeleteWidgetEvent
import com.anytypeio.anytype.presentation.extension.sendEditWidgetsEvent
import com.anytypeio.anytype.presentation.extension.sendReorderWidgetEvent
import com.anytypeio.anytype.presentation.extension.sendSelectHomeTabEvent
import com.anytypeio.anytype.presentation.home.Command.ChangeWidgetType.Companion.UNDEFINED_LAYOUT_CODE
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.BundledWidgetSourceIds
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
import com.anytypeio.anytype.presentation.widgets.WidgetSessionStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.presentation.widgets.getActiveTabViews
import com.anytypeio.anytype.presentation.widgets.parseWidgets
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class HomeScreenViewModel(
    private val configStorage: ConfigStorage,
    private val openObject: OpenObject,
    private val closeObject: CloseBlock,
    private val createWidget: CreateWidget,
    private val deleteWidget: DeleteWidget,
    private val updateWidget: UpdateWidget,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val getObject: GetObject,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val widgetEventDispatcher: Dispatcher<WidgetDispatchEvent>,
    private val objectPayloadDispatcher: Dispatcher<Payload>,
    private val interceptEvents: InterceptEvents,
    private val widgetSessionStateHolder: WidgetSessionStateHolder,
    private val widgetActiveViewStateHolder: WidgetActiveViewStateHolder,
    private val collapsedWidgetStateHolder: CollapsedWidgetStateHolder,
    private val urlBuilder: UrlBuilder,
    private val createObject: CreateObject,
    private val move: Move,
    private val emptyBin: EmptyBin,
    private val unsubscriber: Unsubscriber,
    private val getDefaultPageType: GetDefaultPageType,
    private val appActionManager: AppActionManager,
    private val analytics: Analytics,
    private val getWidgetSession: GetWidgetSession,
    private val saveWidgetSession: SaveWidgetSession,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : NavigationViewModel<HomeScreenViewModel.Navigation>(),
    Reducer<ObjectView, Payload>,
    WidgetActiveViewStateHolder by widgetActiveViewStateHolder,
    WidgetSessionStateHolder by widgetSessionStateHolder,
    CollapsedWidgetStateHolder by collapsedWidgetStateHolder,
    Unsubscriber by unsubscriber {

    val views = MutableStateFlow<List<WidgetView>>(emptyList())
    val commands = MutableSharedFlow<Command>()
    val mode = MutableStateFlow<InteractionMode>(InteractionMode.Default)

    private var isWidgetSessionRestored = false

    private val isEmptyingBinInProgress = MutableStateFlow(false)

    private val objectViewState = MutableStateFlow<ObjectViewState>(ObjectViewState.Idle)
    private val widgets = MutableStateFlow<Widgets>(null)
    private val containers = MutableStateFlow<Containers>(null)
    private val treeWidgetBranchStateHolder = TreeWidgetBranchStateHolder()

    // Bundled widget containing archived objects
    private val bin = WidgetView.Bin(Subscriptions.SUBSCRIPTION_ARCHIVED)

    val icon = MutableStateFlow<SpaceIconView>(SpaceIconView.Loading)

    init {
        val config = configStorage.get()
        proceedWithObservingSpaceIcon(config)
        proceedWithLaunchingUnsubscriber()
        proceedWithObjectViewStatePipeline(config)
        proceedWithWidgetContainerPipeline(config)
        proceedWithRenderingPipeline()
        proceedWithObservingDispatches()
        proceedWithSettingUpShortcuts()
    }

    private fun proceedWithLaunchingUnsubscriber() {
        viewModelScope.launch { unsubscriber.start() }
    }

    private fun proceedWithRenderingPipeline() {
        viewModelScope.launch {
            containers.filterNotNull().flatMapLatest { list ->
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
            }.flowOn(appCoroutineDispatchers.io).collect {
                views.value = it + listOf(WidgetView.Library, bin) + actions
            }
        }
    }

    private fun proceedWithWidgetContainerPipeline(config: Config) {
        viewModelScope.launch {
            widgets.filterNotNull().map {
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
                            isSessionActive = isSessionActive,
                            urlBuilder = urlBuilder,
                            workspace = config.workspace
                        )
                        is Widget.List -> if (BundledWidgetSourceIds.ids.contains(widget.source.id)) {
                            ListWidgetContainer(
                                widget = widget,
                                subscription = widget.source.id,
                                workspace = config.workspace,
                                storage = storelessSubscriptionContainer,
                                isWidgetCollapsed = isCollapsed(widget.id),
                                urlBuilder = urlBuilder,
                                isSessionActive = isSessionActive
                            )
                        } else {
                            DataViewListWidgetContainer(
                                widget = widget,
                                workspace = config.workspace,
                                storage = storelessSubscriptionContainer,
                                getObject = getObject,
                                activeView = observeCurrentWidgetView(widget.id),
                                isWidgetCollapsed = isCollapsed(widget.id),
                                isSessionActive = isSessionActive,
                                urlBuilder = urlBuilder
                            )
                        }
                    }
                }
            }.collect {
                Timber.d("Emitting list of containers: ${it.size}")
                containers.value = it
            }
        }
    }

    private fun proceedWithObjectViewStatePipeline(config: Config) {
        val externalChannelEvents =
            interceptEvents.build(InterceptEvents.Params(config.widgets)).map {
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
            }.filterIsInstance<ObjectViewState.Success>().map { state ->
                state.obj.blocks.parseWidgets(
                    root = state.obj.root,
                    details = state.obj.details
                )
            }.collect {
                Timber.d("Emitting list of widgets: ${it.size}")
                widgets.value = it
            }
        }
    }

    private fun proceedWithObservingSpaceIcon(config: Config) {
        viewModelScope.launch {
            storelessSubscriptionContainer.subscribe(
                StoreSearchByIdsParams(
                    subscription = HOME_SCREEN_SPACE_OBJECT_SUBSCRIPTION,
                    targets = listOf(config.workspace),
                    keys = listOf(
                        Relations.ID,
                        Relations.ICON_EMOJI,
                        Relations.ICON_IMAGE,
                        Relations.ICON_OPTION
                    )
                )
            ).map { result ->
                val obj = result.firstOrNull()
                obj?.spaceIcon(urlBuilder, spaceGradientProvider) ?: SpaceIconView.Placeholder
            }.flowOn(appCoroutineDispatchers.io).collect {
                icon.value = it
            }
        }
    }

    private fun proceedWithOpeningWidgetObject(widgetObject: Id) {
        viewModelScope.launch {
            openObject.stream(
                OpenObject.Params(widgetObject, false)
            ).collect { result ->
                when (result) {
                    is Resultat.Failure -> {
                        objectViewState.value = ObjectViewState.Failure(result.exception).also {
                            onSessionFailed()
                        }
                        Timber.e(result.exception, "Error while opening object.")
                    }
                    is Resultat.Loading -> {
                        objectViewState.value = ObjectViewState.Loading
                    }
                    is Resultat.Success -> {
                        objectViewState.value = ObjectViewState.Success(
                            obj = result.value
                        ).also {
                            onSessionStarted()
                        }
                    }
                }
            }
        }
    }

    private fun proceedWithClosingWidgetObject(widgetObject: Id) {
        viewModelScope.launch {
            saveWidgetSession.execute(
                SaveWidgetSession.Params(
                    WidgetSession(
                        collapsed = collapsedWidgetStateHolder.get(),
                        widgetsToActiveViews = views.value.getActiveTabViews()
                    )
                )
            )
            val subscriptions = widgets.value.orEmpty().map { widget ->
                if (widget.source is Widget.Source.Bundled)
                    widget.source.id
                else
                    widget.id
            }
            if (subscriptions.isNotEmpty()) unsubscribe(subscriptions)
            closeObject.stream(widgetObject).collect { status ->
                status.fold(
                    onFailure = {
                        Timber.e(it, "Error while closing widget object")
                    },
                    onSuccess = {
                        onSessionStopped().also {
                            Timber.d("Widget object closed successfully")
                        }
                    }
                )
            }
        }
    }

    private fun proceedWithObservingDispatches() {
        viewModelScope.launch {
            widgetEventDispatcher.flow().collect { dispatch ->
                Timber.d("New dispatch: $dispatch")
                when (dispatch) {
                    is WidgetDispatchEvent.SourcePicked.Default -> {
                        commands.emit(
                            Command.SelectWidgetType(
                                ctx = configStorage.get().widgets,
                                source = dispatch.source,
                                layout = dispatch.sourceLayout,
                                target = dispatch.target,
                                isInEditMode = isInEditMode()
                            )
                        )
                    }
                    is WidgetDispatchEvent.SourcePicked.Bundled -> {
                        commands.emit(
                            Command.SelectWidgetType(
                                ctx = configStorage.get().widgets,
                                source = dispatch.source,
                                layout = ObjectType.Layout.SET.code,
                                target = dispatch.target,
                                isInEditMode = isInEditMode()
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
                            type = dispatch.widgetType,
                            target = dispatch.target
                        )
                    }
                }
            }
        }
    }

    private fun proceedWithCreatingWidget(
        source: Id,
        type: Int,
        target: Id?
    ) {
        viewModelScope.launch {
            val config = configStorage.get()
            createWidget(
                CreateWidget.Params(
                    ctx = config.widgets,
                    source = source,
                    type = when (type) {
                        Command.ChangeWidgetType.TYPE_LINK -> WidgetLayout.LINK
                        Command.ChangeWidgetType.TYPE_TREE -> WidgetLayout.TREE
                        Command.ChangeWidgetType.TYPE_LIST -> WidgetLayout.LIST
                        else -> WidgetLayout.LINK
                    },
                    target = target,
                    position = if (!target.isNullOrEmpty()) Position.BOTTOM else Position.NONE
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
                    type = when (type) {
                        Command.ChangeWidgetType.TYPE_LINK -> WidgetLayout.LINK
                        Command.ChangeWidgetType.TYPE_TREE -> WidgetLayout.TREE
                        Command.ChangeWidgetType.TYPE_LIST -> WidgetLayout.LIST
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
                        launch {
                            objectPayloadDispatcher.send(status.value)
                        }
                    }
                }
            }
        }
    }

    private fun proceedWithDeletingWidget(widget: Id) {
        viewModelScope.launch {
            val config = configStorage.get()
            val target = widgets.value.orEmpty().find { it.id == widget }
            deleteWidget.stream(
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
                        objectPayloadDispatcher.send(status.value).also {
                            dispatchDeleteWidgetAnalyticsEvent(target)
                        }
                    }
                }
            }
        }
    }

    private fun proceedWithEmptyingBin() {
        viewModelScope.launch {
            emptyBin.stream(Unit).flowOn(appCoroutineDispatchers.io).collect { status ->
                Timber.d("Status while emptying bin: $status")
                when (status) {
                    is Resultat.Failure -> {
                        Timber.e(status.exception, "Error while emptying bin").also {
                            isEmptyingBinInProgress.value = false
                        }
                    }
                    is Resultat.Loading -> {
                        isEmptyingBinInProgress.value = true
                    }
                    is Resultat.Success -> {
                        when (status.value.size) {
                            0 -> sendToast("Bin already empty")
                            1 -> sendToast("One object deleted")
                            else -> "${status.value.size} objects deleted"
                        }
                        isEmptyingBinInProgress.value = false
                    }
                }
            }
        }
    }

    fun onCreateWidgetClicked() {
        viewModelScope.launch {
            sendAddWidgetEvent(
                analytics = analytics,
                isInEditMode = isInEditMode()
            )
            commands.emit(
                Command.SelectWidgetSource(
                    isInEditMode = isInEditMode()
                )
            )
        }
    }

    fun onEditWidgets() {
        proceedWithEnteringEditMode().also {
            viewModelScope.sendEditWidgetsEvent(analytics)
        }
    }

    fun onExitEditMode() {
        proceedWithExitingEditMode()
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

    fun onWidgetSourceClicked(source: Widget.Source) {
        when (source) {
            is Widget.Source.Bundled.Favorites -> {
                viewModelScope.sendSelectHomeTabEvent(
                    analytics = analytics,
                    bundled = source
                )
                // TODO switch to bundled widgets id
                navigate(Navigation.ExpandWidget(Subscription.Favorites))
            }
            is Widget.Source.Bundled.Sets -> {
                viewModelScope.sendSelectHomeTabEvent(
                    analytics = analytics,
                    bundled = source
                )
                // TODO switch to bundled widgets id
                navigate(Navigation.ExpandWidget(Subscription.Sets))
            }
            is Widget.Source.Bundled.Recent -> {
                viewModelScope.sendSelectHomeTabEvent(
                    analytics = analytics,
                    bundled = source
                )
                // TODO switch to bundled widgets id
                navigate(Navigation.ExpandWidget(Subscription.Recent))
            }
            is Widget.Source.Bundled.Collections -> {
                viewModelScope.sendSelectHomeTabEvent(
                    analytics = analytics,
                    bundled = source
                )
                // TODO switch to bundled widgets id
                navigate(Navigation.ExpandWidget(Subscription.Collections))
            }
            is Widget.Source.Default -> {
                if (source.obj.isArchived != true) {
                    dispatchSelectHomeTabCustomSourceEvent(source)
                    proceedWithOpeningObject(source.obj)
                } else {
                    sendToast("Open bin to restore your archived object")
                }
            }
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
                proceedWithEnteringEditMode()
            }
            DropDownMenuAction.RemoveWidget -> {
                proceedWithDeletingWidget(widget)
            }
            DropDownMenuAction.EmptyBin -> {
                proceedWithEmptyingBin()
            }
            DropDownMenuAction.AddBelow -> {
                proceedWithAddingWidgetBelow(widget)
            }
        }
    }

    fun onBundledWidgetClicked(widget: Id) {
        when (widget) {
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

    private fun proceedWithAddingWidgetBelow(widget: Id) {
        viewModelScope.launch {
            sendAddWidgetEvent(
                analytics = analytics,
                isInEditMode = isInEditMode()
            )
            commands.emit(
                Command.SelectWidgetSource(
                    target = widget,
                    isInEditMode = isInEditMode()
                )
            )
        }
    }

    private fun proceedWithChangingType(widget: Id) {
        Timber.d("onChangeWidgetSourceClicked, widget:[$widget]")
        val curr = widgets.value.orEmpty().find { it.id == widget }
        if (curr != null) {
            viewModelScope.launch {
                commands.emit(
                    Command.ChangeWidgetType(
                        ctx = configStorage.get().widgets,
                        widget = widget,
                        source = curr.source.id,
                        type = parseWidgetType(curr),
                        layout = when (val source = curr.source) {
                            is Widget.Source.Bundled -> UNDEFINED_LAYOUT_CODE
                            is Widget.Source.Default -> {
                                source.obj.layout?.code ?: UNDEFINED_LAYOUT_CODE
                            }
                        },
                        isInEditMode = isInEditMode()
                    )
                )
            }
        } else {
            sendToast("Widget missing. Please try again later")
        }
    }

    private fun proceedWithChangingSource(widget: Id) {
        val curr = widgets.value.orEmpty().find { it.id == widget }
        if (curr != null) {
            viewModelScope.launch {
                commands.emit(
                    Command.ChangeWidgetSource(
                        ctx = configStorage.get().widgets,
                        widget = widget,
                        source = curr.source.id,
                        type = parseWidgetType(curr),
                        isInEditMode = isInEditMode()
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
                    interceptWidgetDeletion(curr, e)
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

    private fun interceptWidgetDeletion(
        curr: ObjectView,
        e: Event.Command.DeleteBlock
    ) {
        val deletedWidgets: List<Id> = curr.blocks.mapNotNull { block ->
            if (e.targets.contains(block.id) && block.content is Block.Content.Widget)
                block.id
            else
                null
        }
        if (deletedWidgets.isNotEmpty()) {
            viewModelScope.launch {
                collapsedWidgetStateHolder.onWidgetDeleted(deletedWidgets)
                unsubscriber.unsubscribe(deletedWidgets)
            }
        }
    }

    fun onStart() {
        Timber.d("onStart")
        if (!isWidgetSessionRestored) {
            viewModelScope.launch {
                val session = withContext(appCoroutineDispatchers.io) {
                    getWidgetSession.execute(Unit).getOrNull()
                }
                if (session != null) {
                    collapsedWidgetStateHolder.set(session.collapsed)
                    widgetActiveViewStateHolder.init(session.widgetsToActiveViews)
                }
                proceedWithOpeningWidgetObject(widgetObject = configStorage.get().widgets)
            }
        } else {
            proceedWithOpeningWidgetObject(widgetObject = configStorage.get().widgets)
        }
    }

    fun onStop() {
        Timber.d("onStop")
        // Temporary workaround for app crash when user is logged out and config storage is not initialized.
        try {
            proceedWithClosingWidgetObject(widgetObject = configStorage.get().widgets)
        } catch (e: Exception) {
            Timber.e(e, "Error while closing widget object")
        }
    }

    private fun proceedWithExitingEditMode() {
        mode.value = InteractionMode.Default
    }

    private fun proceedWithEnteringEditMode() {
        mode.value = InteractionMode.Edit
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
            ObjectType.Layout.COLLECTION -> navigate(Navigation.OpenSet(obj.id))
            else -> sendToast("Unexpected layout: ${obj.layout}")
        }
    }

    fun onCreateNewObjectClicked() {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            createObject.stream(CreateObject.Param(null)).collect { result ->
                result.fold(
                    onSuccess = {
                        if (it.appliedTemplate != null) {
                            val middleTime = System.currentTimeMillis()
                            sendAnalyticsObjectCreateEvent(
                                analytics = analytics,
                                objType = it.type,
                                route = EventsDictionary.Routes.objPowerTool,
                                startTime = startTime,
                                middleTime = middleTime
                            )
                        }
                        navigate(Navigation.OpenObject(it.objectId))
                    },
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
            val subject = views[to]
            val target = if (direction == Position.TOP) views[to.inc()].id else views[to.dec()].id
            move.stream(
                Move.Params(
                    context = configStorage.get().widgets,
                    targetId = target,
                    targetContext = configStorage.get().widgets,
                    blockIds = listOf(subject.id),
                    position = direction
                )
            ).collect { result ->
                result.fold(
                    onSuccess = {
                        objectPayloadDispatcher.send(it).also {
                            dispatchReorderWidgetAnalyticEvent(subject)
                        }
                    },
                    onFailure = { Timber.e(it, "Error while moving blocks") }
                )
            }
        }
    }

    private fun proceedWithSettingUpShortcuts() {
        viewModelScope.launch {
            getDefaultPageType.execute(Unit).fold(
                onSuccess = {
                    Pair(it.name, it.type).letNotNull { name, type ->
                        appActionManager.setup(
                            AppActionManager.Action.CreateNew(
                                type = type,
                                name = name
                            )
                        )
                    }
                },
                onFailure = {
                    Timber.d("Error while setting up app shortcuts")
                }
            )
        }
    }

    private fun dispatchDeleteWidgetAnalyticsEvent(target: Widget?) {
        viewModelScope.launch {
            when (val source = target?.source) {
                is Widget.Source.Bundled -> {
                    sendDeleteWidgetEvent(
                        analytics = analytics,
                        bundled = source,
                        isInEditMode = isInEditMode()
                    )
                }
                is Widget.Source.Default -> {
                    val sourceObjectType = source.type
                    if (sourceObjectType != null) {
                        val objectTypeWrapper = storeOfObjectTypes.get(sourceObjectType)
                        if (objectTypeWrapper != null) {
                            sendDeleteWidgetEvent(
                                analytics = analytics,
                                sourceObjectTypeId = objectTypeWrapper.sourceObject.orEmpty(),
                                isCustomObjectType = objectTypeWrapper.sourceObject.isNullOrEmpty(),
                                isInEditMode = isInEditMode()
                            )
                        } else {
                            Timber.e("Failed to dispatch analytics: source type not found in types storage")
                        }
                    } else {
                        Timber.e("Failed to dispatch analytics: unknown source type")
                    }
                }
                else -> {
                    Timber.e("Error while dispatching analytics event: source not found")
                }
            }
        }
    }

    private fun isInEditMode() = mode.value == InteractionMode.Edit

    private fun dispatchSelectHomeTabCustomSourceEvent(source: Widget.Source) {
        viewModelScope.launch {
            val sourceObjectType = source.type
            if (sourceObjectType != null) {
                val objectTypeWrapper = storeOfObjectTypes.get(sourceObjectType)
                if (objectTypeWrapper != null) {
                    sendSelectHomeTabEvent(
                        analytics = analytics,
                        sourceObjectTypeId = objectTypeWrapper.sourceObject.orEmpty(),
                        isCustomObjectType = objectTypeWrapper.sourceObject.isNullOrEmpty()
                    )
                } else {
                    Timber.e("Failed to dispatch analytics: source type not found in types storage")
                }
            } else {
                Timber.e("Failed to dispatch analytics: unknown source type")
            }
        }
    }

    private fun dispatchReorderWidgetAnalyticEvent(subject: WidgetView) {
        viewModelScope.launch {
            val source = when (subject) {
                is WidgetView.Link -> subject.source
                is WidgetView.ListOfObjects -> subject.source
                is WidgetView.SetOfObjects -> subject.source
                is WidgetView.Tree -> subject.source
                else -> null
            }
            when(source) {
                is Widget.Source.Bundled -> {
                    sendReorderWidgetEvent(
                        analytics = analytics,
                        bundled = source
                    )
                }
                is Widget.Source.Default -> {
                    val sourceObjectType = source.type
                    if (sourceObjectType != null) {
                        val objectTypeWrapper = storeOfObjectTypes.get(sourceObjectType)
                        if (objectTypeWrapper != null) {
                            sendReorderWidgetEvent(
                                analytics = analytics,
                                sourceObjectTypeId = objectTypeWrapper.sourceObject.orEmpty(),
                                isCustomObjectType = objectTypeWrapper.sourceObject.isNullOrEmpty()
                            )
                        } else {
                            Timber.e("Failed to dispatch analytics: source type not found in types storage")
                        }
                    } else {
                        Timber.e("Failed to dispatch analytics: unknown source type")
                    }
                }

                else -> {
                    // Do nothing.
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            unsubscriber.unsubscribe(listOf(HOME_SCREEN_SPACE_OBJECT_SUBSCRIPTION))
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
        private val closeObject: CloseBlock,
        private val createObject: CreateObject,
        private val createWidget: CreateWidget,
        private val deleteWidget: DeleteWidget,
        private val updateWidget: UpdateWidget,
        private val appCoroutineDispatchers: AppCoroutineDispatchers,
        private val widgetEventDispatcher: Dispatcher<WidgetDispatchEvent>,
        private val objectPayloadDispatcher: Dispatcher<Payload>,
        private val interceptEvents: InterceptEvents,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val widgetSessionStateHolder: WidgetSessionStateHolder,
        private val widgetActiveViewStateHolder: WidgetActiveViewStateHolder,
        private val collapsedWidgetStateHolder: CollapsedWidgetStateHolder,
        private val urlBuilder: UrlBuilder,
        private val getObject: GetObject,
        private val move: Move,
        private val emptyBin: EmptyBin,
        private val unsubscriber: Unsubscriber,
        private val getDefaultPageType: GetDefaultPageType,
        private val appActionManager: AppActionManager,
        private val analytics: Analytics,
        private val getWidgetSession: GetWidgetSession,
        private val saveWidgetSession: SaveWidgetSession,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val storeOfObjectTypes: StoreOfObjectTypes
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeScreenViewModel(
            configStorage = configStorage,
            openObject = openObject,
            closeObject = closeObject,
            createObject = createObject,
            createWidget = createWidget,
            deleteWidget = deleteWidget,
            updateWidget = updateWidget,
            appCoroutineDispatchers = appCoroutineDispatchers,
            widgetEventDispatcher = widgetEventDispatcher,
            objectPayloadDispatcher = objectPayloadDispatcher,
            interceptEvents = interceptEvents,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            widgetSessionStateHolder = widgetSessionStateHolder,
            widgetActiveViewStateHolder = widgetActiveViewStateHolder,
            collapsedWidgetStateHolder = collapsedWidgetStateHolder,
            getObject = getObject,
            urlBuilder = urlBuilder,
            move = move,
            emptyBin = emptyBin,
            unsubscriber = unsubscriber,
            getDefaultPageType = getDefaultPageType,
            appActionManager = appActionManager,
            analytics = analytics,
            getWidgetSession = getWidgetSession,
            saveWidgetSession = saveWidgetSession,
            spaceGradientProvider = spaceGradientProvider,
            storeOfObjectTypes = storeOfObjectTypes
        ) as T
    }

    companion object {
        val actions = listOf(
            WidgetView.Action.EditWidgets
        )

        const val HOME_SCREEN_SPACE_OBJECT_SUBSCRIPTION = "subscription.home-screen.space-object"
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

    /**
     * [target] optional target, below which new widget will be created
     */
    data class SelectWidgetSource(
        val target: Id? = null,
        val isInEditMode: Boolean
    ) : Command()

    data class SelectWidgetType(
        val ctx: Id,
        val source: Id,
        val target: Id?,
        val layout: Int,
        val isInEditMode: Boolean
    ) : Command()

    data class ChangeWidgetSource(
        val ctx: Id,
        val widget: Id,
        val source: Id,
        val type: Int,
        val isInEditMode: Boolean
    ) : Command()

    data class ChangeWidgetType(
        val ctx: Id,
        val widget: Id,
        val source: Id,
        val type: Int,
        val layout: Int,
        val isInEditMode: Boolean
    ) : Command() {
        companion object {
            const val TYPE_TREE = 0
            const val TYPE_LINK = 1
            const val TYPE_LIST = 2
            const val UNDEFINED_LAYOUT_CODE = -1
        }
    }
}

/**
 * Empty list means there are no widgets.
 * Null means there are no info about widgets — due to loading or error state.
 */
typealias Widgets = List<Widget>?
/**
 * Empty list means there are no containers.
 * Null means there are no info about containers — due to loading or error state.
 */
typealias Containers = List<WidgetContainer>?