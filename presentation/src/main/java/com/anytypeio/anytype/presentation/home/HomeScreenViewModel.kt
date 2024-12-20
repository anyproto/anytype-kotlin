package com.anytypeio.anytype.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.core_models.ext.process
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.domain.auth.interactor.ClearLastOpenedObject
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.bin.EmptyBin
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.Reducer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.spaces.ClearLastOpenedSpace
import com.anytypeio.anytype.domain.spaces.GetSpaceView
import com.anytypeio.anytype.domain.types.GetPinnedObjectTypes
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.widgets.GetWidgetSession
import com.anytypeio.anytype.domain.widgets.SaveWidgetSession
import com.anytypeio.anytype.domain.widgets.SetWidgetActiveView
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.PayloadDelegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.extension.sendAddWidgetEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectTypeSelectOrChangeEvent
import com.anytypeio.anytype.presentation.extension.sendDeleteWidgetEvent
import com.anytypeio.anytype.presentation.extension.sendEditWidgetsEvent
import com.anytypeio.anytype.presentation.extension.sendReorderWidgetEvent
import com.anytypeio.anytype.presentation.extension.sendSelectHomeTabEvent
import com.anytypeio.anytype.presentation.home.Command.ChangeWidgetType.Companion.UNDEFINED_LAYOUT_CODE
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.presentation.sets.prefillNewObjectDetails
import com.anytypeio.anytype.presentation.sets.resolveSetByRelationPrefilledObjectData
import com.anytypeio.anytype.presentation.sets.resolveTypeAndActiveViewTemplate
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_DEFAULT_OBJECT_TYPE
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.AllContentWidgetContainer
import com.anytypeio.anytype.presentation.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.presentation.widgets.CollapsedWidgetStateHolder
import com.anytypeio.anytype.presentation.widgets.DataViewListWidgetContainer
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.LinkWidgetContainer
import com.anytypeio.anytype.presentation.widgets.ListWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SpaceBinWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SpaceChatWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SpaceWidgetContainer
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.TreeWidgetBranchStateHolder
import com.anytypeio.anytype.presentation.widgets.TreeWidgetContainer
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetActiveViewStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetContainer
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetSessionStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.presentation.widgets.hasValidLayout
import com.anytypeio.anytype.presentation.widgets.parseActiveViews
import com.anytypeio.anytype.presentation.widgets.parseWidgets
import javax.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * TODO
 * Corner cases to handle:
 * close object object session if it was opened by one of widgets containers and not used by another.
 *      - if it was deleted
 *      - when leaving this screen
 *
 * Change subscription IDs for bundled widgets?
 */
class HomeScreenViewModel(
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
    private val createDataViewObject: CreateDataViewObject,
    private val move: Move,
    private val emptyBin: EmptyBin,
    private val unsubscriber: Unsubscriber,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val appActionManager: AppActionManager,
    private val analytics: Analytics,
    private val getWidgetSession: GetWidgetSession,
    private val saveWidgetSession: SaveWidgetSession,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val objectWatcher: ObjectWatcher,
    private val spaceManager: SpaceManager,
    private val spaceWidgetContainer: SpaceWidgetContainer,
    private val setWidgetActiveView: SetWidgetActiveView,
    private val setObjectDetails: SetObjectDetails,
    private val getSpaceView: GetSpaceView,
    private val searchObjects: SearchObjects,
    private val getPinnedObjectTypes: GetPinnedObjectTypes,
    private val userPermissionProvider: UserPermissionProvider,
    private val deepLinkToObjectDelegate: DeepLinkToObjectDelegate,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val storeOfRelations: StoreOfRelations,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val payloadDelegator: PayloadDelegator,
    private val createBlock: CreateBlock,
    private val dateProvider: DateProvider,
    private val addObjectToCollection: AddObjectToCollection,
    private val clearLastOpenedSpace: ClearLastOpenedSpace,
    private val clearLastOpenedObject: ClearLastOpenedObject,
    private val spaceBinWidgetContainer: SpaceBinWidgetContainer,
    private val featureToggles: FeatureToggles,
    private val fieldParser: FieldParser,
    private val spaceInviteResolver: SpaceInviteResolver
) : NavigationViewModel<HomeScreenViewModel.Navigation>(),
    Reducer<ObjectView, Payload>,
    WidgetActiveViewStateHolder by widgetActiveViewStateHolder,
    WidgetSessionStateHolder by widgetSessionStateHolder,
    CollapsedWidgetStateHolder by collapsedWidgetStateHolder,
    DeepLinkToObjectDelegate by deepLinkToObjectDelegate,
    Unsubscriber by unsubscriber,
    AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    private val jobs = mutableListOf<Job>()
    private val mutex = Mutex()

    val views = MutableStateFlow<List<WidgetView>>(emptyList())
    val commands = MutableSharedFlow<Command>()
    val mode = MutableStateFlow<InteractionMode>(InteractionMode.Default)

    private var isWidgetSessionRestored = false

    private val isEmptyingBinInProgress = MutableStateFlow(false)

    private val objectViewState = MutableStateFlow<ObjectViewState>(ObjectViewState.Idle)
    private val widgets = MutableStateFlow<Widgets>(null)
    private val containers = MutableStateFlow<Containers>(null)
    private val treeWidgetBranchStateHolder = TreeWidgetBranchStateHolder()

    private val allContentWidget = AllContentWidgetContainer()
    private val spaceChatWidget = SpaceChatWidgetContainer()

    private val spaceWidgetView = spaceWidgetContainer.view

    private val widgetObjectPipelineJobs = mutableListOf<Job>()

    private val openWidgetObjectsHistory : MutableSet<OpenObjectHistoryItem> = LinkedHashSet()

    private val userPermissions = MutableStateFlow<SpaceMemberPermissions?>(null)

    val hasEditAccess = userPermissions.map { it?.isOwnerOrEditor() == true }

    private val widgetObjectPipeline = spaceManager
        .observe()
        .distinctUntilChanged()
        .onEach { newConfig ->
            viewModelScope.launch {
                val openObjectState = objectViewState.value
                if (openObjectState is ObjectViewState.Success) {
                    val subscriptions = buildList {
                        widgets.value.orEmpty().forEach { widget ->
                            if (widget.config.space != newConfig.space) {
                                if (widget.source is Widget.Source.Bundled)
                                    add(widget.source.id)
                                else
                                    add(widget.id)
                            }
                        }
                    }
                    if (subscriptions.isNotEmpty()) {
                        unsubscribe(subscriptions)
                    }
                    mutex.withLock {
                        val closed = mutableSetOf<OpenObjectHistoryItem>()
                        openWidgetObjectsHistory.forEach { (previouslyOpenedWidgetObject, space) ->
                            if (previouslyOpenedWidgetObject != newConfig.widgets) {
                                closeObject
                                    .async(
                                        CloseBlock.Params(
                                            target = previouslyOpenedWidgetObject,
                                            space = space
                                        )
                                    )
                                    .fold(
                                        onSuccess = {
                                            closed.add(
                                                OpenObjectHistoryItem(
                                                    obj = previouslyOpenedWidgetObject,
                                                    space = space
                                                )
                                            )
                                        },
                                        onFailure = {
                                            Timber.e(it, "Error while closing object from history: $previouslyOpenedWidgetObject")
                                        },
                                    )
                            }
                        }
                        if (closed.isNotEmpty()) {
                            openWidgetObjectsHistory.removeAll(closed)
                        }
                    }
                }
            }
        }
        .flatMapLatest { config ->
            openObject.stream(
                OpenObject.Params(
                    obj = config.widgets,
                    saveAsLastOpened = false,
                    spaceId = SpaceId(config.space)
                )
            ).onEach { result ->
                result.fold(
                    onSuccess = { objectView ->
                        onSessionStarted().also {
                            viewModelScope.launch {
                                mutex.withLock {
                                    openWidgetObjectsHistory.add(
                                        OpenObjectHistoryItem(
                                            obj = objectView.root,
                                            space = SpaceId(config.space)
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onFailure = { e ->
                        onSessionFailed().also {
                            Timber.e(e, "Error while opening object.")
                        }
                    },
                    onLoading = {
                        widgets.value = emptyList()
                    }
                )
            }.map { result ->
                when (result) {
                    is Resultat.Failure -> ObjectViewState.Failure(result.exception)
                    is Resultat.Loading -> ObjectViewState.Loading
                    is Resultat.Success -> ObjectViewState.Success(
                        obj = result.value,
                        config = config
                    )
                }
            }
        }
        .catch {
            emit(ObjectViewState.Failure(it))
        }

    init {
        Timber.i("HomeScreenViewModel, init")
        proceedWithUserPermissions()
        proceedWithLaunchingUnsubscriber()
        proceedWithObjectViewStatePipeline()
        proceedWithWidgetContainerPipeline()
        proceedWithRenderingPipeline()
        proceedWithObservingDispatches()
        proceedWithSettingUpShortcuts()
        proceedWithViewStatePipeline()
    }

    private fun proceedWithViewStatePipeline() {
        widgetObjectPipelineJobs += viewModelScope.launch {
            if (!isWidgetSessionRestored) {
                val session = withContext(appCoroutineDispatchers.io) {
                    getWidgetSession.async(Unit).getOrNull()
                }
                if (session != null) {
                    collapsedWidgetStateHolder.set(session.collapsed)
                }
            }
            widgetObjectPipeline.collect {
                objectViewState.value = it
            }
        }
    }

    private fun proceedWithUserPermissions() {
        viewModelScope.launch {
            spaceManager
                .observe()
                .flatMapLatest { config ->
                    userPermissionProvider.observe(SpaceId(config.space))
                }.collect { permission ->
                    userPermissions.value = permission
                    when(permission) {
                        SpaceMemberPermissions.WRITER,
                        SpaceMemberPermissions.OWNER -> {
                            if (mode.value == InteractionMode.ReadOnly) {
                                mode.value = InteractionMode.Default
                            }
                        }
                        else -> {
                            mode.value = InteractionMode.ReadOnly
                        }
                    }
                }
        }
    }

    private fun proceedWithLaunchingUnsubscriber() {
        viewModelScope.launch { unsubscriber.start() }
    }

    private fun proceedWithRenderingPipeline() {
        viewModelScope.launch {
            containers.filterNotNull().flatMapLatest { list ->
                if (list.isNotEmpty()) {
                    combine(
                        flows = buildList<Flow<WidgetView>> {
                            add(spaceWidgetView)
                            if (featureToggles.isSpaceLevelChatWidgetEnabled) {
                                add(spaceChatWidget.view)
                            }
                            add(allContentWidget.view)
                            addAll(list.map { m -> m.view })
                        }
                    ) { array ->
                        array.toList()
                    }
                } else {
                    spaceWidgetView.map { view -> listOf(view) }
                }
            }.combine(hasEditAccess) { widgets, hasEditAccess ->
                buildListOfWidgets(hasEditAccess, widgets)
            }.catch {
                Timber.e(it, "Error while rendering widgets")
            }.flowOn(appCoroutineDispatchers.io).collect {
                views.value = it
            }
        }
    }

    private fun buildListOfWidgets(
        hasEditAccess: Boolean,
        widgets: List<WidgetView>
    ): List<WidgetView> {
        return buildList {
            val filtered = widgets.filterNot { view ->
                (view is WidgetView.Bin && (view.isEmpty || view.isLoading))
            }
            addAll(filtered)
            if (hasEditAccess) {
                addAll(actions)
            }
        }
    }

    private fun proceedWithWidgetContainerPipeline() {
        viewModelScope.launch {
            widgets.filterNotNull().map { widgets ->
                val currentlyDisplayedViews = views.value
                widgets.filter { widget -> widget.hasValidLayout() }.map { widget ->
                    when (widget) {
                        is Widget.Link -> LinkWidgetContainer(
                            widget = widget,
                            fieldParser = fieldParser
                        )
                        is Widget.Tree -> TreeWidgetContainer(
                            widget = widget,
                            container = storelessSubscriptionContainer,
                            expandedBranches = treeWidgetBranchStateHolder.stream(widget.id),
                            isWidgetCollapsed = isCollapsed(widget.id),
                            isSessionActive = isSessionActive,
                            urlBuilder = urlBuilder,
                            objectWatcher = objectWatcher,
                            getSpaceView = getSpaceView,
                            onRequestCache = {
                                currentlyDisplayedViews.find { view ->
                                    view.id == widget.id
                                            && view is WidgetView.Tree
                                            && view.source == widget.source
                                } as? WidgetView.Tree
                            },
                            fieldParser = fieldParser
                        )
                        is Widget.List -> if (BundledWidgetSourceIds.ids.contains(widget.source.id)) {
                            ListWidgetContainer(
                                widget = widget,
                                subscription = widget.source.id,
                                storage = storelessSubscriptionContainer,
                                isWidgetCollapsed = isCollapsed(widget.id),
                                urlBuilder = urlBuilder,
                                isSessionActive = isSessionActive,
                                objectWatcher = objectWatcher,
                                getSpaceView = getSpaceView,
                                onRequestCache = {
                                    currentlyDisplayedViews.find { view ->
                                        view.id == widget.id
                                                && view is WidgetView.ListOfObjects
                                                && view.source == widget.source
                                    } as? WidgetView.ListOfObjects
                                },
                                fieldParser = fieldParser
                            )
                        } else {
                            DataViewListWidgetContainer(
                                widget = widget,
                                storage = storelessSubscriptionContainer,
                                getObject = getObject,
                                activeView = observeCurrentWidgetView(widget.id),
                                isWidgetCollapsed = isCollapsed(widget.id),
                                isSessionActive = isSessionActive,
                                urlBuilder = urlBuilder,
                                coverImageHashProvider = coverImageHashProvider,
                                onRequestCache = {
                                    currentlyDisplayedViews.find { view ->
                                        view.id == widget.id
                                                && view is WidgetView.SetOfObjects
                                                && view.source == widget.source
                                    } as? WidgetView.SetOfObjects
                                },
                                storeOfRelations = storeOfRelations,
                                fieldParser = fieldParser
                            )
                        }
                        is Widget.View -> {
                            DataViewListWidgetContainer(
                                widget = widget,
                                storage = storelessSubscriptionContainer,
                                getObject = getObject,
                                activeView = observeCurrentWidgetView(widget.id),
                                isWidgetCollapsed = isCollapsed(widget.id),
                                isSessionActive = isSessionActive,
                                urlBuilder = urlBuilder,
                                coverImageHashProvider = coverImageHashProvider,
                                // TODO handle cached item type.
                                onRequestCache = {
                                    currentlyDisplayedViews.find { view ->
                                        view.id == widget.id
                                                && view is WidgetView.SetOfObjects
                                                && view.source == widget.source
                                    } as? WidgetView.SetOfObjects
                                },
                                storeOfRelations = storeOfRelations,
                                fieldParser = fieldParser
                            )
                        }
                    }
                } + listOf(spaceBinWidgetContainer)
            }.collect {
                Timber.d("Emitting list of containers: ${it.size}")
                containers.value = it
            }
        }
    }

    private fun proceedWithObjectViewStatePipeline() {
        val externalChannelEvents = spaceManager.observe().flatMapLatest {  config ->
            merge(
                interceptEvents.build(
                    InterceptEvents.Params(config.widgets)
                ).map { events ->
                    Payload(
                        context = config.widgets,
                        events = events
                    )
                },
                payloadDelegator.intercept(ctx = config.widgets)
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
                    details = state.obj.details,
                    config = state.config
                ).also {
                    widgetActiveViewStateHolder.init(state.obj.blocks.parseActiveViews())
                }
            }.collect {
                Timber.d("Emitting list of widgets: ${it.size}")
                widgets.value = it
            }
        }
    }

    private suspend fun proceedWithClosingWidgetObject(
        widgetObject: Id,
        space: SpaceId
    ) {
        saveWidgetSession.async(
            SaveWidgetSession.Params(
                WidgetSession(
                    collapsed = collapsedWidgetStateHolder.get(),
                    widgetsToActiveViews = emptyMap()
                )
            )
        )
        val subscriptions = buildList {
            addAll(
                widgets.value.orEmpty().map { widget ->
                    if (widget.source is Widget.Source.Bundled)
                        widget.source.id
                    else
                        widget.id
                }
            )
            add(SpaceWidgetContainer.SPACE_WIDGET_SUBSCRIPTION)
        }
        if (subscriptions.isNotEmpty()) unsubscribe(subscriptions)

        closeObject.stream(
            CloseBlock.Params(
                target = widgetObject,
                space = space
            )
        ).collect { status ->
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

    private fun proceedWithObservingDispatches() {
        viewModelScope.launch {
            widgetEventDispatcher
                .flow()
                .withLatestFrom(spaceManager.observe()) { dispatch, config ->
                    when (dispatch) {
                        is WidgetDispatchEvent.SourcePicked.Default -> {
                            if (dispatch.sourceLayout == ObjectType.Layout.DATE.code) {
                                proceedWithCreatingWidget(
                                    ctx = config.widgets,
                                    source = dispatch.source,
                                    type = Command.ChangeWidgetType.TYPE_LINK,
                                    target = dispatch.target
                                )
                            } else {
                                commands.emit(
                                    Command.SelectWidgetType(
                                        ctx = config.widgets,
                                        source = dispatch.source,
                                        layout = dispatch.sourceLayout,
                                        target = dispatch.target,
                                        isInEditMode = isInEditMode()
                                    )
                                )
                            }
                        }
                        is WidgetDispatchEvent.SourcePicked.Bundled -> {
                            commands.emit(
                                Command.SelectWidgetType(
                                    ctx = config.widgets,
                                    source = dispatch.source,
                                    layout = ObjectType.Layout.SET.code,
                                    target = dispatch.target,
                                    isInEditMode = isInEditMode()
                                )
                            )
                        }
                        is WidgetDispatchEvent.SourceChanged -> {
                            proceedWithUpdatingWidget(
                                ctx = config.widgets,
                                widget = dispatch.widget,
                                source = dispatch.source,
                                type = dispatch.type
                            )
                        }

                        is WidgetDispatchEvent.TypePicked -> {
                            proceedWithCreatingWidget(
                                ctx = config.widgets,
                                source = dispatch.source,
                                type = dispatch.widgetType,
                                target = dispatch.target
                            )
                        }
                        is WidgetDispatchEvent.NewWithWidgetWithNewSource -> {
                            commands.emit(
                                Command.CreateSourceForNewWidget(
                                    space = SpaceId(config.space),
                                    widgets = config.widgets
                                )
                            )
                        }
                    }
                }.collect()
        }
    }

    private fun proceedWithCreatingWidget(
        ctx: Id,
        source: Id,
        type: Int,
        target: Id?
    ) {
        viewModelScope.launch {
            createWidget(
                CreateWidget.Params(
                    ctx = ctx,
                    source = source,
                    type = when (type) {
                        Command.ChangeWidgetType.TYPE_LINK -> WidgetLayout.LINK
                        Command.ChangeWidgetType.TYPE_TREE -> WidgetLayout.TREE
                        Command.ChangeWidgetType.TYPE_LIST -> WidgetLayout.LIST
                        Command.ChangeWidgetType.TYPE_VIEW -> WidgetLayout.VIEW
                        Command.ChangeWidgetType.TYPE_COMPACT_LIST -> WidgetLayout.COMPACT_LIST
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
        ctx: Id,
        widget: Id,
        source: Id,
        type: Int
    ) {
        viewModelScope.launch {
            updateWidget(
                UpdateWidget.Params(
                    ctx = ctx,
                    source = source,
                    widget = widget,
                    type = when (type) {
                        Command.ChangeWidgetType.TYPE_LINK -> WidgetLayout.LINK
                        Command.ChangeWidgetType.TYPE_TREE -> WidgetLayout.TREE
                        Command.ChangeWidgetType.TYPE_LIST -> WidgetLayout.LIST
                        Command.ChangeWidgetType.TYPE_COMPACT_LIST -> WidgetLayout.COMPACT_LIST
                        Command.ChangeWidgetType.TYPE_VIEW -> WidgetLayout.VIEW
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
            val config = spaceManager.getConfig()
            if (config != null) {
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
            } else {
                Timber.e("Failed to get config to delete a widget")
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
                    isInEditMode = isInEditMode(),
                    space = spaceManager.get()
                )
            )
        }
    }

    fun onEditWidgets() {
        viewModelScope.launch {
            if (userPermissions.value?.isOwnerOrEditor() ==  true) {
                proceedWithEnteringEditMode()
                sendEditWidgetsEvent(analytics)
            }
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

    fun onObjectCheckboxClicked(id: Id, isChecked: Boolean) {
        proceedWithTogglingObjectCheckboxState(id = id, isChecked = isChecked)
    }

    private fun proceedWithTogglingObjectCheckboxState(id: Id, isChecked: Boolean) {
        viewModelScope.launch {
            setObjectDetails.async(
                SetObjectDetails.Params(
                    ctx = id,
                    details = mapOf(
                        Relations.DONE to !isChecked
                    )
                )
            ).fold(
                onSuccess = {
                    Timber.d("Updated checkbox state")
                },
                onFailure = {
                    Timber.e(it, "Error while toggling object checkbox state")
                }
            )
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
                viewModelScope.launch {
                    navigation(
                        Navigation.ExpandWidget(
                            subscription = Subscription.Favorites,
                            space = spaceManager.get()
                        )
                    )
                }
            }
            is Widget.Source.Bundled.Sets -> {
                viewModelScope.sendSelectHomeTabEvent(
                    analytics = analytics,
                    bundled = source
                )
                // TODO switch to bundled widgets id
                viewModelScope.launch {
                    navigation(
                        Navigation.ExpandWidget(
                            subscription = Subscription.Sets,
                            space = spaceManager.get()
                        )
                    )
                }
            }

            is Widget.Source.Bundled.Recent -> {
                viewModelScope.sendSelectHomeTabEvent(
                    analytics = analytics,
                    bundled = source
                )
                // TODO switch to bundled widgets id
                viewModelScope.launch {
                    navigation(
                        Navigation.ExpandWidget(
                            subscription = Subscription.Recent,
                            space = spaceManager.get()
                        )
                    )
                }
            }

            is Widget.Source.Bundled.RecentLocal -> {
                viewModelScope.sendSelectHomeTabEvent(
                    analytics = analytics,
                    bundled = source
                )
                // TODO switch to bundled widgets id
                viewModelScope.launch {
                    navigation(
                        Navigation.ExpandWidget(
                            subscription = Subscription.RecentLocal,
                            space = spaceManager.get()
                        )
                    )
                }
            }

            is Widget.Source.Bundled.Collections -> {
                viewModelScope.sendSelectHomeTabEvent(
                    analytics = analytics,
                    bundled = source
                )
                // TODO switch to bundled widgets id
                viewModelScope.launch {
                    navigation(
                        Navigation.ExpandWidget(
                            subscription = Subscription.Collections,
                            space = spaceManager.get()
                        )
                    )
                }
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
        viewModelScope.launch {
            // TODO DROID-2341 get space from widget views for better consistency
            val space = spaceManager.get()
            when (widget) {
                Subscriptions.SUBSCRIPTION_SETS -> {
                    navigation(
                        Navigation.ExpandWidget(
                            subscription = Subscription.Sets,
                            space = space
                        )
                    )
                }
                Subscriptions.SUBSCRIPTION_RECENT -> {
                    navigation(
                        Navigation.ExpandWidget(
                            subscription = Subscription.Recent,
                            space = space
                        )
                    )
                }
                Subscriptions.SUBSCRIPTION_ARCHIVED -> {
                    navigation(
                        Navigation.ExpandWidget(
                            subscription = Subscription.Bin,
                            space = space
                        )
                    )
                }
                Subscriptions.SUBSCRIPTION_FAVORITES -> {
                    navigation(
                        Navigation.ExpandWidget(
                            subscription = Subscription.Favorites,
                            space = space
                        )
                    )
                }
                WidgetView.SpaceChat.id -> {
                    proceedWithSpaceChatWidgetHeaderClick()
                }
                WidgetView.AllContent.ALL_CONTENT_WIDGET_ID -> {
                    if (mode.value == InteractionMode.Edit) {
                        return@launch
                    }
                    navigation(
                        Navigation.OpenAllContent(
                            space = space
                        )
                    )
                }
                else -> {
                    Timber.w("Skipping widget click: $widget")
                }
            }
        }
    }

    private suspend fun proceedWithSpaceChatWidgetHeaderClick() {
        if (mode.value == InteractionMode.Edit) {
            return
        }
        val view = views.value.find { it is WidgetView.SpaceWidget.View }
        if (view != null) {
            val spaceView = (view as WidgetView.SpaceWidget.View)
            val chat = spaceView.space.getValue<Id?>(Relations.CHAT_ID)
            val space = spaceView.space.targetSpaceId
            if (chat != null && space != null) {
                navigation(
                    Navigation.OpenDiscussion(
                        space = space,
                        ctx = chat
                    )
                )
            } else {
                Timber.w("Chat or space not found - not able to open space chat")
            }
        } else {
            Timber.w("Space widget not found")
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
                    isInEditMode = isInEditMode(),
                    space = spaceManager.get()
                )
            )
        }
    }

    private fun proceedWithChangingType(widget: Id) {
        Timber.d("onChangeWidgetSourceClicked, widget:[$widget]")
        val curr = widgets.value.orEmpty().find { it.id == widget }
        val sourceId = curr?.source?.id
        if (curr != null && sourceId != null) {
            viewModelScope.launch {
                val config = spaceManager.getConfig()
                if (config != null) {
                    commands.emit(
                        Command.ChangeWidgetType(
                            ctx = config.widgets,
                            widget = widget,
                            source = sourceId,
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
                } else {
                    Timber.e("Failed to get config to change widget type")
                }
            }
        } else {
            sendToast("Widget missing. Please try again later")
        }
    }

    private fun proceedWithChangingSource(widget: Id) {
        val curr = widgets.value.orEmpty().find { it.id == widget }
        if (curr != null) {
            viewModelScope.launch {
                val config = spaceManager.getConfig()
                if (config != null) {
                    commands.emit(
                        Command.ChangeWidgetSource(
                            ctx = config.widgets,
                            widget = widget,
                            source = curr.source.id,
                            type = parseWidgetType(curr),
                            isInEditMode = isInEditMode(),
                            space = config.space
                        )
                    )
                } else {
                    Timber.e("Failed to get config to change widget source")
                }
            }
        } else {
            sendToast("Widget missing. Please try again later")
        }
    }

    private fun parseWidgetType(curr: Widget) = when (curr) {
        is Widget.Link -> Command.ChangeWidgetType.TYPE_LINK
        is Widget.Tree -> Command.ChangeWidgetType.TYPE_TREE
        is Widget.View -> Command.ChangeWidgetType.TYPE_VIEW
        is Widget.List -> {
            if (curr.isCompact)
                Command.ChangeWidgetType.TYPE_COMPACT_LIST
            else
                Command.ChangeWidgetType.TYPE_LIST
        }
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
                    interceptWidgetDeletion(e)
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
                is Event.Command.LinkGranularChange -> {
                    curr = curr.copy(
                        blocks = curr.blocks.map { block ->
                            if (block.id == e.id) {
                                val content = block.content
                                if (content is Block.Content.Link) {
                                    block.copy(
                                        content = content.copy(
                                            target = e.target
                                        )
                                    )
                                } else {
                                    block
                                }
                            } else {
                                block
                            }
                        }
                    )
                }
                is Event.Command.Widgets.SetWidget -> {
                    Timber.d("Set widget event: $e")
                    curr = curr.copy(
                        blocks = curr.blocks.map { block ->
                            if (block.id == e.widget) {
                                val content = block.content
                                if (content is Block.Content.Widget) {
                                    block.copy(
                                        content = content.copy(
                                            layout = e.layout ?: content.layout,
                                            limit = e.limit ?: content.limit,
                                            activeView = e.activeView ?: content.activeView
                                        )
                                    )
                                } else {
                                    block
                                }
                            } else {
                                block
                            }
                        }
                    )
                }
                else -> {
                    Timber.d("Skipping event: $e")
                }
            }
        }
        return curr
    }

    private fun interceptWidgetDeletion(
        e: Event.Command.DeleteBlock
    ) {
        val currentWidgets = widgets.value ?: emptyList()
        val deletedWidgets = currentWidgets.filter { widget ->
            e.targets.contains(widget.id)
        }
        val expiredSubscriptions = deletedWidgets.map { widget ->
            if (widget.source is Widget.Source.Bundled)
                widget.source.id
            else
                widget.id
        }
        if (deletedWidgets.isNotEmpty()) {
            viewModelScope.launch {
                collapsedWidgetStateHolder.onWidgetDeleted(
                    widgets = deletedWidgets.map { it.id }
                )
                unsubscriber.unsubscribe(expiredSubscriptions)
            }
        }
    }

    fun onStart() {
        Timber.d("onStart")
    }

    fun onResume(deeplink: DeepLinkResolver.Action? = null) {
        Timber.d("onResume, deeplink: ${deeplink}")
        viewModelScope.launch {
            clearLastOpenedObject.run(
                ClearLastOpenedObject.Params(
                    SpaceId(spaceManager.get())
                )
            )
        }
        when (deeplink) {
            is DeepLinkResolver.Action.Import.Experience -> {
                viewModelScope.launch {
                    commands.emit(
                        Command.Deeplink.GalleryInstallation(
                            deepLinkType = deeplink.type,
                            deepLinkSource = deeplink.source
                        )
                    )
                }
            }

            is DeepLinkResolver.Action.Invite -> {
                viewModelScope.launch {
                    delay(1000)
                    commands.emit(Command.Deeplink.Invite(deeplink.link))
                }
            }
            is DeepLinkResolver.Action.Unknown -> {
                if (BuildConfig.DEBUG) {
                    sendToast("Could not resolve deeplink")
                }
            }
            is DeepLinkResolver.Action.DeepLinkToObject -> {
                viewModelScope.launch {
                    onDeepLinkToObjectAwait(
                        obj = deeplink.obj,
                        space = deeplink.space,
                        switchSpaceIfObjectFound = true
                    ).collect { result ->
                        when(result) {
                            is DeepLinkToObjectDelegate.Result.Error -> {
                                val link = deeplink.invite
                                if (link != null && result is DeepLinkToObjectDelegate.Result.Error.PermissionNeeded) {
                                    commands.emit(
                                        Command.Deeplink.Invite(
                                            link = spaceInviteResolver.createInviteLink(
                                                contentId = link.cid,
                                                encryptionKey = link.key
                                            )
                                        )
                                    )
                                } else {
                                    commands.emit(Command.Deeplink.DeepLinkToObjectNotWorking)
                                }
                            }
                            is DeepLinkToObjectDelegate.Result.Success -> {
                                proceedWithNavigation(result.obj.navigation())
                            }
                        }
                    }
                }
            }
            is DeepLinkResolver.Action.DeepLinkToMembership -> {
                viewModelScope.launch {
                    commands.emit(
                        Command.Deeplink.MembershipScreen(
                            tierId = deeplink.tierId
                        )
                    )
                }
            }
            else -> {
                Timber.d("No deep link")
            }
        }
    }

    fun onStop() {
        Timber.d("onStop")
        viewModelScope.launch {
            saveWidgetSession.async(
                SaveWidgetSession.Params(
                    WidgetSession(
                        collapsed = collapsedWidgetStateHolder.get(),
                        widgetsToActiveViews = emptyMap()
                    )
                )
            )
        }
    }

    private fun proceedWithExitingEditMode() {
        mode.value = InteractionMode.Default
    }

    private fun proceedWithEnteringEditMode() {
        mode.value = InteractionMode.Edit
    }

    private fun proceedWithOpeningObject(obj: ObjectWrapper.Basic) {
        proceedWithNavigation(obj.navigation())
    }

    private fun proceedWithNavigation(navigation: OpenObjectNavigation) {
        when(navigation) {
            is OpenObjectNavigation.OpenDataView -> {
                navigate(
                    Navigation.OpenSet(
                        ctx = navigation.target,
                        space = navigation.space,
                        view = null
                    )
                )
            }
            is OpenObjectNavigation.OpenEditor -> {
                navigate(
                    Navigation.OpenObject(
                        ctx = navigation.target,
                        space = navigation.space
                    )
                )
            }
            is OpenObjectNavigation.OpenDiscussion -> {
                navigate(
                    Navigation.OpenDiscussion(
                        ctx = navigation.target,
                        space = navigation.space
                    )
                )
            }
            is OpenObjectNavigation.UnexpectedLayoutError -> {
                sendToast("Unexpected layout: ${navigation.layout}")
            }
            OpenObjectNavigation.NonValidObject -> {
                sendToast("Object id is missing")
            }
            is OpenObjectNavigation.OpenDataObject -> {
                navigate(
                    destination = Navigation.OpenDateObject(
                        ctx = navigation.target,
                        space = navigation.space
                    )
                )
            }
        }
    }

    fun onCreateNewObjectClicked(objType: ObjectWrapper.Type? = null) {
        Timber.d("onCreateNewObjectClicked, type:[${objType?.uniqueKey}]")
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val params = objType?.uniqueKey.getCreateObjectParams(
                space = SpaceId(spaceManager.get()),
                objType?.defaultTemplateId
            )
            createObject.stream(params).collect { createObjectResponse ->
                createObjectResponse.fold(
                    onSuccess = { result ->
                        val spaceParams = provideParams(spaceManager.get())
                        sendAnalyticsObjectCreateEvent(
                            analytics = analytics,
                            route = EventsDictionary.Routes.navigation,
                            startTime = startTime,
                            view = EventsDictionary.View.viewHome,
                            objType = objType ?: storeOfObjectTypes.getByKey(result.typeKey.key),
                            spaceParams = spaceParams
                        )
                        if (objType != null) {
                            sendAnalyticsObjectTypeSelectOrChangeEvent(
                                analytics = analytics,
                                startTime = startTime,
                                sourceObject = objType.sourceObject,
                                containsFlagType = true,
                                route = EventsDictionary.Routes.longTap,
                                spaceParams = spaceParams
                            )
                        }
                        proceedWithOpeningObject(result.obj)
                    },
                    onFailure = {
                        Timber.e(it, "Error while creating object")
                        sendToast("Error while creating object. Please, try again later")
                    }
                )
            }
        }
    }

    fun onCreateNewObjectLongClicked() {
        viewModelScope.launch {
            val space = spaceManager.get()
            if (space.isNotEmpty()) {
                commands.emit(Command.OpenObjectCreateDialog(SpaceId(space)))
            }
        }
    }

    fun onMove(views: List<WidgetView>, from: Int, to: Int) {
        viewModelScope.launch {
            val config = spaceManager.getConfig()
            if (config != null) {
                val direction = if (from < to) Position.BOTTOM else Position.TOP
                val subject = views[to]
                val target = if (direction == Position.TOP) views[to.inc()].id else views[to.dec()].id
                move.stream(
                    Move.Params(
                        context = config.widgets,
                        targetId = target,
                        targetContext = config.widgets,
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
            else
                Timber.e("Failed to get config for move operation")
        }
    }

    private fun proceedWithSettingUpShortcuts() {
        spaceManager
            .observe()
            .flatMapLatest { config ->
                getPinnedObjectTypes.flow(
                    GetPinnedObjectTypes.Params(space = SpaceId(config.space))
                ).map { pinned ->
                    config to pinned
                }
            }
            .onEach { (config, pinned) ->
                val defaultObjectType = getDefaultObjectType.async(SpaceId(config.space)).getOrNull()?.type
                val keys = buildSet {
                    pinned.take(MAX_PINNED_TYPE_COUNT_FOR_APP_ACTIONS).forEach { typeId ->
                        val wrapper = storeOfObjectTypes.get(typeId.id)
                        val uniqueKey = wrapper?.uniqueKey
                        if (uniqueKey != null) {
                            add(wrapper.uniqueKey)
                        } else {
                            Timber.w("Could not found unique key for a pinned type: ${typeId.id}")
                        }
                    }
                    if (defaultObjectType != null && size < MAX_TYPE_COUNT_FOR_APP_ACTIONS && !contains(defaultObjectType.key)) {
                        add(defaultObjectType.key)
                    }
                    if (size < MAX_TYPE_COUNT_FOR_APP_ACTIONS && !contains(ObjectTypeUniqueKeys.NOTE)) {
                        add(ObjectTypeUniqueKeys.NOTE)
                    }
                    if (size < MAX_TYPE_COUNT_FOR_APP_ACTIONS && !contains(ObjectTypeUniqueKeys.PAGE)) {
                        add(ObjectTypeUniqueKeys.PAGE)
                    }
                    if (size < MAX_TYPE_COUNT_FOR_APP_ACTIONS && !contains(ObjectTypeUniqueKeys.TASK)) {
                        add(ObjectTypeUniqueKeys.TASK)
                    }
                }
                searchObjects(
                    SearchObjects.Params(
                        space = SpaceId(config.space),
                        keys = buildList {
                            add(Relations.ID)
                            add(Relations.UNIQUE_KEY)
                            add(Relations.NAME)
                        },
                        filters = buildList {
                            add(
                                DVFilter(
                                    relation = Relations.LAYOUT,
                                    value = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                                    condition = DVFilterCondition.EQUAL
                                )
                            )
                            add(
                                DVFilter(
                                    relation = Relations.UNIQUE_KEY,
                                    value = keys.toList(),
                                    condition = DVFilterCondition.IN
                                )
                            )
                        }
                    )
                ).process(
                    success = { wrappers ->
                        val types = wrappers
                            .filter { type -> type.notDeletedNorArchived }
                            .map { ObjectWrapper.Type(it.map) }
                            .sortedBy { keys.indexOf(it.uniqueKey) }

                        val actions = types.mapNotNull { type ->
                            if (type.map.containsKey(Relations.UNIQUE_KEY)) {
                                AppActionManager.Action.CreateNew(
                                    type = TypeKey(type.uniqueKey),
                                    name = type.name.orEmpty()
                                )
                            } else {
                                null
                            }
                        }
                        appActionManager.setup(actions = actions)
                    },
                    failure = {
                        Timber.e(it, "Error while searching for types")
                    }
                )
            }
            .launchIn(viewModelScope)
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

    override fun onChangeCurrentWidgetView(widget: Id, view: Id) {
        widgetActiveViewStateHolder.onChangeCurrentWidgetView(
            widget = widget,
            view = view
        ).also {
            viewModelScope.launch {
                val config = spaceManager.getConfig()
                if (config != null)
                    setWidgetActiveView.stream(
                        SetWidgetActiveView.Params(
                            ctx = config.widgets,
                            widget = widget,
                            view = view,
                        )
                    ).collect { result ->
                        result.fold(
                            onSuccess = { objectPayloadDispatcher.send(it) },
                            onFailure = { Timber.e(it, "Error while updating active view") }
                        )
                    }
                else
                    Timber.e("Failed to get config to set active widget view")
            }
        }
    }

    fun onSpaceShareIconClicked(spaceView: ObjectWrapper.SpaceView) {
        viewModelScope.launch {
            val space = spaceView.targetSpaceId
            if (space != null) {
                commands.emit(Command.ShareSpace(SpaceId(space)))
            } else {
                sendToast("Space not found")
            }
        }
    }

    fun onSpaceSettingsClicked() {
        viewModelScope.launch {
            commands.emit(
                Command.OpenSpaceSettings(
                    spaceId = SpaceId(spaceManager.get())
                )
            )
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            if (spaceManager.getState() is SpaceManager.State.Space) {
                // Proceed with releasing resources before exiting
                openWidgetObjectsHistory.forEach { (obj, space) ->
                    closeObject
                        .async(
                            CloseBlock.Params(
                                target = obj,
                                space = space
                            )
                        )
                        .onSuccess {
                            Timber.d("Closed object from widget object session history: $obj")
                        }
                        .onFailure {
                            Timber.e(it, "Error while closing object from history")
                        }
                }
                spaceManager.clear()
                clearLastOpenedSpace.async(Unit).fold(
                    onSuccess = {
                        Timber.d("Cleared last opened space before opening vault")
                    },
                    onFailure = {
                        Timber.e(it, "Error while clearing last opened space before opening vault")
                    }
                )
            }
            commands.emit(Command.OpenVault)
        }
    }

    fun onBackLongClicked() {
        navigate(destination = Navigation.OpenSpaceSwitcher)
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("onCleared")
        try {
            GlobalScope.launch(appCoroutineDispatchers.io) {
                unsubscriber.unsubscribe(listOf(HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION))
                val config = spaceManager.getConfig()
                if (config != null) {
                    proceedWithClosingWidgetObject(
                        widgetObject = config.widgets,
                        space = SpaceId(config.space)
                    )
                }
                jobs.cancel()
                widgetObjectPipelineJobs.cancel()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while closing widget object")
        }
    }

    fun onSearchIconClicked() {
        viewModelScope.launch {
            commands.emit(
                Command.OpenGlobalSearchScreen(space = spaceManager.get())
            )
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.searchScreenShow,
            props = Props(mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.navigation))
        )
    }

    fun onSeeAllObjectsClicked(gallery: WidgetView.Gallery) {
        val source = gallery.source
        val view = gallery.view
        if (view != null && source is Widget.Source.Default) {
            val space = source.obj.spaceId
            if (space != null) {
                navigate(
                    Navigation.OpenSet(
                        ctx = gallery.source.id,
                        space = space,
                        view = view
                    )
                )
            } else {
                Timber.e("Missing space ID")
            }
        }
    }

    fun onNewWidgetSourceTypeSelected(
        type: ObjectWrapper.Type,
        widgets: Id
    ) {
        viewModelScope.launch {
            createObject.async(
                params = CreateObject.Param(
                    space = SpaceId(spaceManager.get()),
                    type = TypeKey(type.uniqueKey)
                )
            ).fold(
                onSuccess = { response ->
                    proceedWithCreatingWidget(
                        ctx = widgets,
                        source = response.objectId,
                        target = null,
                        type = if (type.recommendedLayout?.isDataView() == true) {
                            Command.ChangeWidgetType.TYPE_VIEW
                        } else {
                            Command.ChangeWidgetType.TYPE_TREE
                        }
                    )
                    proceedWithNavigation(response.obj.navigation())
                },
                onFailure = {
                    Timber.e(it, "Error while creating source for widget")
                }
            )
        }
    }

    fun onCreateObjectForWidget(
        type: ObjectWrapper.Type,
        source: Id
    ) {
        viewModelScope.launch {
            createObject.async(
                params = CreateObject.Param(
                    space = SpaceId(spaceManager.get()),
                    type = TypeKey(type.uniqueKey)
                )
            ).fold(
                onSuccess = { result ->
                    proceedWithCreatingLinkToNewObject(source, result)
                    proceedWithNavigation(result.obj.navigation())
                },
                onFailure = {
                    Timber.e(it, "Error while creating object")
                }
            )
        }
    }

    private suspend fun proceedWithCreatingLinkToNewObject(
        source: Id,
        result: CreateObject.Result
    ) {
        createBlock.async(
            params = CreateBlock.Params(
                context = source,
                target = "",
                position = Position.NONE,
                prototype = Block.Prototype.Link(
                    target = result.objectId
                )
            )
        ).fold(
            onSuccess = {
                Timber.d("Link to new object inside widget's source has been created successfully")
            },
            onFailure = {
                Timber.e(it, "Error while creating block")
            }
        )
    }

    fun onCreateObjectInsideWidget(widget: Id) {
        when(val target = widgets.value.orEmpty().find { it.id == widget }) {
            is Widget.Tree -> {
                val source = target.source
                if (source is Widget.Source.Default) {
                    if (!source.obj.layout.isDataView()) {
                        viewModelScope.launch {
                            createObject.async(
                                params = CreateObject.Param(
                                    space = SpaceId(target.config.space),
                                    type = TypeKey(ObjectTypeUniqueKeys.PAGE)
                                )
                            ).fold(
                                onSuccess = { result ->
                                    proceedWithCreatingLinkToNewObject(source.id, result)
                                    proceedWithNavigation(result.obj.navigation())
                                },
                                onFailure = {
                                    Timber.e(it, "Error while creating object")
                                }
                            )
                        }
                    }
                }
            }
            is Widget.List -> {
                // TODO
            }
            is Widget.View -> {
                // TODO
            }
            is Widget.Link -> {
                // Do nothing.
            }
            else -> {
                Timber.e("Could not found widget.")
            }
        }
    }

    fun onCreateDataViewObject(widget: WidgetId, view: ViewId?) {
        Timber.d("onCreateDataViewObject")
        viewModelScope.launch {
            val target = widgets.value.orEmpty().find { it.id == widget }
            if (target != null) {
                val widgetSource = target.source
                if (widgetSource is Widget.Source.Default) {
                    getObject.async(
                        params = GetObject.Params(
                            target = target.source.id,
                            space = SpaceId(target.config.space)
                        )
                    ).fold(
                        onSuccess = { obj ->
                            val dv = obj.blocks.find { it.content is DV }?.content as? DV
                            val viewer = if (view.isNullOrEmpty())
                                dv?.viewers?.firstOrNull()
                            else
                                dv?.viewers?.find { it.id == view }

                            if (widgetSource.obj.layout == ObjectType.Layout.COLLECTION) {
                                if (dv != null && viewer != null) {
                                    proceedWithAddingObjectToCollection(
                                        viewer = viewer,
                                        dv = dv,
                                        collection = widgetSource.obj.id
                                    )
                                }
                            } else if (widgetSource.obj.layout == ObjectType.Layout.SET) {
                                val dataViewSource = widgetSource.obj.setOf.firstOrNull()

                                if (dataViewSource != null) {
                                    val dataViewSourceObj =
                                        ObjectWrapper.Basic(obj.details[dataViewSource].orEmpty())
                                    if (dv != null && viewer != null) {
                                        when (val layout = dataViewSourceObj.layout) {
                                            ObjectType.Layout.OBJECT_TYPE -> {
                                                proceedWithCreatingDataViewObject(
                                                    dataViewSourceObj,
                                                    viewer,
                                                    dv
                                                )
                                            }

                                            ObjectType.Layout.RELATION -> {
                                                proceedWithCreatingDataViewObject(
                                                    viewer,
                                                    dv,
                                                    dataViewSourceObj
                                                )
                                            }

                                            else -> {
                                                Timber.w("Unexpected layout of data view source: $layout")
                                            }
                                        }
                                    } else {
                                        Timber.w("Could not found data view or target view inside this data view")
                                    }
                                } else {
                                    Timber.w("Missing data view source")
                                }
                            }
                        }
                    )
                }
            } else {
                Timber.w("onCreateDataViewObject's target not found")
            }
        }
    }

    private suspend fun proceedWithCreatingDataViewObject(
        viewer: Block.Content.DataView.Viewer,
        dv: DV,
        dataViewSourceObj: ObjectWrapper.Basic
    ) {
        val (defaultObjectType, defaultTemplate) = resolveTypeAndActiveViewTemplate(
            viewer,
            storeOfObjectTypes
        )
        val prefilled = viewer.resolveSetByRelationPrefilledObjectData(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            dataViewRelationLinks = dv.relationLinks,
            objSetByRelation = ObjectWrapper.Relation(dataViewSourceObj.map)
        )
        createDataViewObject.async(
            params = CreateDataViewObject.Params.SetByRelation(
                filters = viewer.filters,
                template = defaultTemplate,
                type = TypeKey(defaultObjectType?.uniqueKey ?: VIEW_DEFAULT_OBJECT_TYPE),
                prefilled = prefilled
            ).also {
                Timber.d("Calling with params: $it")
            }
        ).fold(
            onSuccess = {
                Timber.d("Successfully created object with id: ${it.objectId}")
            },
            onFailure = {
                Timber.e(it, "Error while creating data view object for widget")
            }
        )
    }

    private suspend fun proceedWithCreatingDataViewObject(
        dataViewSourceObj: ObjectWrapper.Basic,
        viewer: Block.Content.DataView.Viewer,
        dv: DV
    ) {
        val dataViewSourceType = dataViewSourceObj.uniqueKey
        val (_, defaultTemplate) = resolveTypeAndActiveViewTemplate(
            viewer,
            storeOfObjectTypes
        )
        val prefilled = viewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dataViewRelationLinks = dv.relationLinks,
            dateProvider = dateProvider
        )
        createDataViewObject.async(
            params = CreateDataViewObject.Params.SetByType(
                type = TypeKey(dataViewSourceType ?: VIEW_DEFAULT_OBJECT_TYPE),
                filters = viewer.filters,
                template = defaultTemplate,
                prefilled = prefilled
            ).also {
                Timber.d("Calling with params: $it")
            }
        ).fold(
            onSuccess = {
                Timber.d("Successfully created object with id: ${it.objectId}")
            },
            onFailure = {
                Timber.e(it, "Error while creating data view object for widget")
            }
        )
    }

    private suspend fun proceedWithAddingObjectToCollection(
        viewer: Block.Content.DataView.Viewer,
        dv: DV,
        collection: Id
    ) {
        val prefilled = viewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            dataViewRelationLinks = dv.relationLinks
        )

        val (defaultObjectType, defaultTemplate) = resolveTypeAndActiveViewTemplate(
            viewer,
            storeOfObjectTypes
        )

        val defaultObjectTypeUniqueKey = TypeKey(defaultObjectType?.uniqueKey ?: VIEW_DEFAULT_OBJECT_TYPE)

        val createObjectParams = CreateDataViewObject.Params.Collection(
            template = defaultTemplate,
            type = defaultObjectTypeUniqueKey,
            filters = viewer.filters,
            prefilled = prefilled
        )

        createDataViewObject.async(params = createObjectParams).fold(
            onFailure = {
                Timber.e("Error creating object for collection")
            },
            onSuccess = { result ->
                Timber.d("Successfully created object with id: ${result.objectId}")
                addObjectToCollection.async(
                    AddObjectToCollection.Params(
                        ctx = collection,
                        targets = listOf(result.objectId)
                    )
                ).fold(
                    onSuccess = {
                        Timber.d("Successfully added object to collection")
                    },
                    onFailure = {
                        Timber.e(it, "Error while adding object to collection")
                    }
                )
                val wrapper = ObjectWrapper.Basic(result.struct.orEmpty())
                if (wrapper.isValid) {
                    proceedWithNavigation(wrapper.navigation())
                }
            }
        )
    }

    sealed class Navigation {
        data class OpenObject(val ctx: Id, val space: Id) : Navigation()
        data class OpenDiscussion(val ctx: Id, val space: Id) : Navigation()
        data class OpenSet(val ctx: Id, val space: Id, val view: Id?) : Navigation()
        data class ExpandWidget(val subscription: Subscription, val space: Id) : Navigation()
        data object OpenSpaceSwitcher: Navigation()
        data class OpenAllContent(val space: Id) : Navigation()
        data class OpenDateObject(val ctx: Id, val space: Id) : Navigation()
    }

    class Factory @Inject constructor(
        private val openObject: OpenObject,
        private val closeObject: CloseBlock,
        private val createObject: CreateObject,
        private val createDataViewObject: CreateDataViewObject,
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
        private val getDefaultObjectType: GetDefaultObjectType,
        private val appActionManager: AppActionManager,
        private val analytics: Analytics,
        private val getWidgetSession: GetWidgetSession,
        private val saveWidgetSession: SaveWidgetSession,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val storeOfRelations: StoreOfRelations,
        private val objectWatcher: ObjectWatcher,
        private val setWidgetActiveView: SetWidgetActiveView,
        private val spaceManager: SpaceManager,
        private val spaceWidgetContainer: SpaceWidgetContainer,
        private val setObjectDetails: SetObjectDetails,
        private val getSpaceView: GetSpaceView,
        private val searchObjects: SearchObjects,
        private val getPinnedObjectTypes: GetPinnedObjectTypes,
        private val userPermissionProvider: UserPermissionProvider,
        private val deepLinkToObjectDelegate: DeepLinkToObjectDelegate,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        private val payloadDelegator: PayloadDelegator,
        private val createBlock: CreateBlock,
        private val dateProvider: DateProvider,
        private val coverImageHashProvider: CoverImageHashProvider,
        private val addObjectToCollection: AddObjectToCollection,
        private val clearLastOpenedSpace: ClearLastOpenedSpace,
        private val clearLastOpenedObject: ClearLastOpenedObject,
        private val spaceBinWidgetContainer: SpaceBinWidgetContainer,
        private val featureToggles: FeatureToggles,
        private val fieldParser: FieldParser,
        private val spaceInviteResolver: SpaceInviteResolver
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeScreenViewModel(
            openObject = openObject,
            closeObject = closeObject,
            createObject = createObject,
            createDataViewObject = createDataViewObject,
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
            getDefaultObjectType = getDefaultObjectType,
            appActionManager = appActionManager,
            analytics = analytics,
            getWidgetSession = getWidgetSession,
            saveWidgetSession = saveWidgetSession,
            spaceGradientProvider = spaceGradientProvider,
            storeOfObjectTypes = storeOfObjectTypes,
            storeOfRelations = storeOfRelations,
            objectWatcher = objectWatcher,
            setWidgetActiveView = setWidgetActiveView,
            spaceManager = spaceManager,
            spaceWidgetContainer = spaceWidgetContainer,
            setObjectDetails = setObjectDetails,
            getSpaceView = getSpaceView,
            searchObjects = searchObjects,
            getPinnedObjectTypes = getPinnedObjectTypes,
            userPermissionProvider = userPermissionProvider,
            deepLinkToObjectDelegate = deepLinkToObjectDelegate,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            coverImageHashProvider = coverImageHashProvider,
            payloadDelegator = payloadDelegator,
            createBlock = createBlock,
            dateProvider = dateProvider,
            addObjectToCollection = addObjectToCollection,
            clearLastOpenedSpace = clearLastOpenedSpace,
            clearLastOpenedObject = clearLastOpenedObject,
            spaceBinWidgetContainer = spaceBinWidgetContainer,
            featureToggles = featureToggles,
            fieldParser = fieldParser,
            spaceInviteResolver = spaceInviteResolver
        ) as T
    }

    companion object {
        val actions = listOf(
            WidgetView.Action.EditWidgets
        )

        const val HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION = "subscription.home-screen.profile-object"
    }
}

/**
 * State representing session while working with an object.
 */
sealed class ObjectViewState {
    data object Idle : ObjectViewState()
    data object Loading : ObjectViewState()
    data class Success(val obj: ObjectView, val config: Config) : ObjectViewState()
    data class Failure(val e: Throwable) : ObjectViewState()
}

sealed class InteractionMode {
    data object Default : InteractionMode()
    data object Edit : InteractionMode()
    data object ReadOnly: InteractionMode()
}

data class OpenObjectHistoryItem(
    val obj: Id,
    val space: Space
)

sealed class Command {

    /**
     * [target] optional target, below which new widget will be created
     */
    data class SelectWidgetSource(
        val target: Id? = null,
        val isInEditMode: Boolean,
        val space: Id
    ) : Command()

    data class OpenSpaceSettings(val spaceId: SpaceId) : Command()

    data class OpenObjectCreateDialog(val space: SpaceId) : Command()

    data class OpenGlobalSearchScreen(val space: Id) : Command()

    data object OpenVault: Command()

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
        val isInEditMode: Boolean,
        val space: Id
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
            const val TYPE_COMPACT_LIST = 3
            const val TYPE_VIEW = 4
            const val UNDEFINED_LAYOUT_CODE = -1
        }
    }

    data class CreateSourceForNewWidget(val space: SpaceId, val widgets: Id) : Command()
    data class CreateObjectForWidget(
        val space: SpaceId,
        val widget: Id,
        val source: Id
    ) : Command()

    data class ShareSpace(val space: SpaceId) : Command()

    sealed class Deeplink : Command() {
        data object DeepLinkToObjectNotWorking: Deeplink()
        data class Invite(val link: String) : Deeplink()
        data class GalleryInstallation(
            val deepLinkType: String,
            val deepLinkSource: String
        ) : Deeplink()
        data class MembershipScreen(val tierId: String?) : Deeplink()
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

sealed class OpenObjectNavigation {
    data class OpenEditor(val target: Id, val space: Id) : OpenObjectNavigation()
    data class OpenDataView(val target: Id, val space: Id): OpenObjectNavigation()
    data class UnexpectedLayoutError(val layout: ObjectType.Layout?): OpenObjectNavigation()
    data object NonValidObject: OpenObjectNavigation()
    data class OpenDiscussion(val target: Id, val space: Id): OpenObjectNavigation()
    data class OpenDataObject(val target: Id, val space: Id): OpenObjectNavigation()
}

fun ObjectWrapper.Basic.navigation() : OpenObjectNavigation {
    if (!isValid) return OpenObjectNavigation.NonValidObject
    return when (layout) {
        ObjectType.Layout.BASIC,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.TODO,
        ObjectType.Layout.BOOKMARK,
        ObjectType.Layout.PARTICIPANT -> {
            OpenObjectNavigation.OpenEditor(
                target = id,
                space = requireNotNull(spaceId)
            )
        }
        in SupportedLayouts.fileLayouts -> {
            OpenObjectNavigation.OpenEditor(
                target = id,
                space = requireNotNull(spaceId)
            )
        }
        ObjectType.Layout.PROFILE -> {
            val identityLink = getValue<Id>(Relations.IDENTITY_PROFILE_LINK)
            if (identityLink.isNullOrEmpty()) {
                OpenObjectNavigation.OpenEditor(
                    target = id,
                    space = requireNotNull(spaceId)
                )
            } else {
                OpenObjectNavigation.OpenEditor(
                    target = identityLink,
                    space = requireNotNull(spaceId)
                )
            }
        }
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION -> {
            OpenObjectNavigation.OpenDataView(
                target = id,
                space = requireNotNull(spaceId)
            )
        }
        ObjectType.Layout.CHAT_DERIVED -> {
            OpenObjectNavigation.OpenDiscussion(
                target = id,
                space = requireNotNull(spaceId)
            )
        }
        ObjectType.Layout.DATE -> {
            OpenObjectNavigation.OpenDataObject(
                target = id,
                space = requireNotNull(spaceId)
            )
        }
        else -> {
            OpenObjectNavigation.UnexpectedLayoutError(layout)
        }
    }
}

fun ObjectType.Layout.navigation(
    target: Id,
    space: Id
) : OpenObjectNavigation {
    return when (this) {
        ObjectType.Layout.BASIC,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.TODO,
        ObjectType.Layout.BOOKMARK,
        ObjectType.Layout.PARTICIPANT -> {
            OpenObjectNavigation.OpenEditor(
                target = target,
                space = space
            )
        }
        in SupportedLayouts.fileLayouts -> {
            OpenObjectNavigation.OpenEditor(
                target = target,
                space = space
            )
        }
        ObjectType.Layout.PROFILE -> {
            OpenObjectNavigation.OpenEditor(
                target = target,
                space = space
            )
        }
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION -> {
            OpenObjectNavigation.OpenDataView(
                target = target,
                space = space
            )
        }
        ObjectType.Layout.CHAT_DERIVED -> {
            OpenObjectNavigation.OpenDiscussion(
                target = target,
                space = space
            )
        }
        ObjectType.Layout.DATE -> {
            OpenObjectNavigation.OpenDataObject(
                target = target,
                space = space
            )
        }
        else -> {
            OpenObjectNavigation.UnexpectedLayoutError(this)
        }
    }
}

const val MAX_TYPE_COUNT_FOR_APP_ACTIONS = 4
const val MAX_PINNED_TYPE_COUNT_FOR_APP_ACTIONS = 3