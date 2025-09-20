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
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.ext.process
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
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
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.dashboard.interactor.SetObjectListIsFavorite
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.Reducer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.CopyInviteLinkToClipboard
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.spaces.ClearLastOpenedSpace
import com.anytypeio.anytype.domain.spaces.DeleteSpace
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
import com.anytypeio.anytype.presentation.extension.sendClickWidgetTitleEvent
import com.anytypeio.anytype.presentation.extension.sendDeleteWidgetEvent
import com.anytypeio.anytype.presentation.extension.sendEditWidgetsEvent
import com.anytypeio.anytype.presentation.extension.sendOpenSidebarObjectEvent
import com.anytypeio.anytype.presentation.extension.sendReorderWidgetEvent
import com.anytypeio.anytype.presentation.extension.sendScreenWidgetMenuEvent
import com.anytypeio.anytype.presentation.home.Command.ChangeWidgetType
import com.anytypeio.anytype.presentation.home.Command.ChangeWidgetType.Companion.UNDEFINED_LAYOUT_CODE
import com.anytypeio.anytype.presentation.home.Command.ShareSpace
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Navigation.ExpandWidget
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Navigation.OpenAllContent
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Navigation.OpenChat
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.navigation.leftButtonClickAnalytics
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.presentation.sets.prefillNewObjectDetails
import com.anytypeio.anytype.presentation.sets.resolveTypeAndActiveViewTemplate
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_DEFAULT_OBJECT_TYPE
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceTechInfo
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState.SpaceInvite
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.vault.ExitToVaultDelegate
import com.anytypeio.anytype.presentation.widgets.AllContentWidgetContainer
import com.anytypeio.anytype.presentation.widgets.DataViewListWidgetContainer
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.LinkWidgetContainer
import com.anytypeio.anytype.presentation.widgets.ListWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SectionWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SpaceBinWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SpaceChatWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SpaceWidgetContainer
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.TreeWidgetBranchStateHolder
import com.anytypeio.anytype.presentation.widgets.TreeWidgetContainer
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetActiveViewStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetConfig
import com.anytypeio.anytype.presentation.widgets.WidgetContainer
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.presentation.widgets.WidgetSessionStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.presentation.widgets.forceChatPosition
import com.anytypeio.anytype.presentation.widgets.parseActiveViews
import com.anytypeio.anytype.presentation.widgets.parseWidgets
import com.anytypeio.anytype.presentation.widgets.source.BundledWidgetSourceView
import com.anytypeio.anytype.presentation.widgets.toBasic
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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
    private val vmParams: HomeScreenVmParams,
    private val openObject: OpenObject,
    private val closeObject: CloseObject,
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
    private val clearLastOpenedObject: ClearLastOpenedObject,
    private val spaceBinWidgetContainer: SpaceBinWidgetContainer,
    private val featureToggles: FeatureToggles,
    private val fieldParser: FieldParser,
    private val spaceInviteResolver: SpaceInviteResolver,
    private val exitToVaultDelegate: ExitToVaultDelegate,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val getSpaceInviteLink: GetSpaceInviteLink,
    private val deleteSpace: DeleteSpace,
    private val spaceMembers: ActiveSpaceMemberSubscriptionContainer,
    private val setAsFavourite: SetObjectListIsFavorite,
    private val chatPreviews: ChatPreviewContainer,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val copyInviteLinkToClipboard: CopyInviteLinkToClipboard,
    private val userSettingsRepository: UserSettingsRepository,
    private val scope: CoroutineScope
) : NavigationViewModel<HomeScreenViewModel.Navigation>(),
    Reducer<ObjectView, Payload>,
    WidgetActiveViewStateHolder by widgetActiveViewStateHolder,
    WidgetSessionStateHolder by widgetSessionStateHolder,
    DeepLinkToObjectDelegate by deepLinkToObjectDelegate,
    Unsubscriber by unsubscriber,
    AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate,
    ExitToVaultDelegate by exitToVaultDelegate
{

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

    private val spaceWidgetView = spaceWidgetContainer.view

    private val widgetObjectPipelineJobs = mutableListOf<Job>()

    // Store Space widget object ID (from SpaceInfo) to use during cleanup when spaceManager might be empty
    private var cachedWidgetObjectId: String? = null

    private val openWidgetObjectsHistory : MutableSet<OpenObjectHistoryItem> = LinkedHashSet()

    private val userPermissions = MutableStateFlow<SpaceMemberPermissions?>(null)

    val hasEditAccess = userPermissions.map { it?.isOwnerOrEditor() == true }

    // Expanded widget IDs for persistence across app restarts
    private val expandedWidgetIds = MutableStateFlow<Set<Id>>(emptySet())

    val navPanelState = MutableStateFlow<NavPanelState>(NavPanelState.Init)

    val viewerSpaceSettingsState = MutableStateFlow<ViewerSpaceSettingsState>(ViewerSpaceSettingsState.Init)
    val uiQrCodeState = MutableStateFlow<UiSpaceQrCodeState>(UiSpaceQrCodeState.Hidden)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val widgetObjectPipeline = spaceManager
        .observe()
        .distinctUntilChanged()
        .onEach { newConfig ->
            // Cache widget object ID for cleanup when spaceManager might be empty
            cachedWidgetObjectId = newConfig.widgets
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
                                        CloseObject.Params(
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
                    spaceId = vmParams.spaceId
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
                        widgets.value = null
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
        proceedWithNavPanelState()
    }


    private fun proceedWithNavPanelState() {
        viewModelScope.launch {
            val spaceAccessType = views
                .map {
                    val space = it.firstOrNull { it is WidgetView.SpaceWidget.View }
                    if (space is WidgetView.SpaceWidget.View) {
                        space.space
                            .spaceAccessType
                    } else {
                        null
                    }
                }
                .distinctUntilChanged()
            combine(
                spaceAccessType,
                userPermissions
            ) { type, permission ->
                val spaceId = vmParams.spaceId.id
                val spaceUxType =
                    spaceViewSubscriptionContainer.get(space = SpaceId(spaceId))?.spaceUxType
                        ?: SpaceUxType.DATA
                NavPanelState.fromPermission(
                    permission = permission,
                    forceHome = false,
                    spaceAccess = type,
                    spaceUxType = spaceUxType
                )
            }.collect {
                navPanelState.value = it
            }
        }
    }

    private suspend fun mapSpaceTypesToWidgets(isOwnerOrEditor: Boolean, config: Config): List<Widget> {
        val allTypes = storeOfObjectTypes.getAll()
        val filteredObjectTypes = allTypes
            .mapNotNull { objectType ->
                if (!objectType.isValid ||
                    SupportedLayouts.excludedSpaceTypeLayouts.contains(objectType.recommendedLayout) ||
                    objectType.isArchived == true ||
                    objectType.isDeleted == true ||
                    objectType.uniqueKey == ObjectTypeIds.TEMPLATE
                ) {
                    return@mapNotNull null
                } else {
                    objectType
                }
            }

        Timber.d("Refreshing system types, isOwnerOrEditor = $isOwnerOrEditor, allTypes = ${allTypes.size}, types = ${filteredObjectTypes.size}")

        // Partition types like SpaceTypesViewModel: myTypes can be deleted, systemTypes cannot
        val (myTypes, systemTypes) = filteredObjectTypes.partition { objectType ->
            !objectType.restrictions.contains(ObjectRestriction.DELETE)
        }

        val allTypeWidgetIds = mutableListOf<Id>()

        val widgetList = buildList {
            // Add user-created types first (deletable)
            for (objectType in myTypes) {
                val widget = createWidgetViewFromType(objectType, config)
                add(widget)
                // Track all type widgets for initial collapsed state
                allTypeWidgetIds.add(widget.id)
            }

            // Add system types (not deletable)
            for (objectType in systemTypes) {
                val widget = createWidgetViewFromType(objectType, config)
                add(widget)
                // Track all type widgets for initial collapsed state
                allTypeWidgetIds.add(widget.id)
            }
        }

        return widgetList
    }

    private fun proceedWithViewStatePipeline() {
        widgetObjectPipelineJobs += viewModelScope.launch {
            widgetObjectPipeline.collect {
                objectViewState.value = it
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun proceedWithRenderingPipeline() {
        viewModelScope.launch {
            containers.filterNotNull().flatMapLatest { list ->
                combine(
                    flows = buildList<Flow<WidgetView>> {
                        add(spaceWidgetView)
                        addAll(list.map { m -> m.view })
                    }
                ) { array ->
                    array.toList()
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
                // >1, and not >0, because space widget view is always there.
                if (widgets.size > 1) {
                    //addAll(actions)
                } else {
                    add(WidgetView.EmptyState)
                }
            }
        }
    }

    private fun proceedWithWidgetContainerPipeline() {
        viewModelScope.launch {
            widgets.filterNotNull().map { widgets ->
                val currentlyDisplayedViews = views.value
                widgets.forceChatPosition().map { widget ->
                    Timber.d("Creating container for widget: ${widget.id} of type ${widget::class.simpleName}")
                    when (widget) {
                        is Widget.Chat -> SpaceChatWidgetContainer(
                            widget = widget,
                            container = chatPreviews,
                            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                            notificationPermissionManager = notificationPermissionManager
                        )
                        is Widget.Link -> LinkWidgetContainer(
                            widget = widget,
                            fieldParser = fieldParser
                        )
                        is Widget.Tree -> TreeWidgetContainer(
                            widget = widget,
                            container = storelessSubscriptionContainer,
                            expandedBranches = treeWidgetBranchStateHolder.stream(widget.id),
                            isWidgetCollapsed = combine(
                                expandedWidgetIds,
                                userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId).map { it.toSet() }
                            ) { expanded, collapsedSecs ->
                                !expanded.contains(widget.id) || isWidgetInCollapsedSection(widget, collapsedSecs)
                            },
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
                            fieldParser = fieldParser,
                            storeOfObjectTypes = storeOfObjectTypes
                        )
                        is Widget.List -> if (BundledWidgetSourceIds.ids.contains(widget.source.id)) {
                            ListWidgetContainer(
                                widget = widget,
                                subscription = widget.source.id,
                                storage = storelessSubscriptionContainer,
                                isWidgetCollapsed = combine(
                                expandedWidgetIds,
                                userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId).map { it.toSet() }
                            ) { expanded, collapsedSecs ->
                                !expanded.contains(widget.id) || isWidgetInCollapsedSection(widget, collapsedSecs)
                            },
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
                                fieldParser = fieldParser,
                                storeOfObjectTypes = storeOfObjectTypes
                            )
                        } else {
                            DataViewListWidgetContainer(
                                space = vmParams.spaceId,
                                widget = widget,
                                storage = storelessSubscriptionContainer,
                                getObject = getObject,
                                activeView = observeCurrentWidgetView(widget.id),
                                isWidgetCollapsed = combine(
                                expandedWidgetIds,
                                userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId).map { it.toSet() }
                            ) { expanded, collapsedSecs ->
                                !expanded.contains(widget.id) || isWidgetInCollapsedSection(widget, collapsedSecs)
                            },
                                isSessionActiveFlow = isSessionActive,
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
                                fieldParser = fieldParser,
                                storeOfObjectTypes = storeOfObjectTypes
                            )
                        }
                        is Widget.View -> {
                            DataViewListWidgetContainer(
                                space = vmParams.spaceId,
                                widget = widget,
                                storage = storelessSubscriptionContainer,
                                getObject = getObject,
                                activeView = observeCurrentWidgetView(widget.id),
                                isWidgetCollapsed = combine(
                                expandedWidgetIds,
                                userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId).map { it.toSet() }
                            ) { expanded, collapsedSecs ->
                                !expanded.contains(widget.id) || isWidgetInCollapsedSection(widget, collapsedSecs)
                            },
                                isSessionActiveFlow = isSessionActive,
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
                                fieldParser = fieldParser,
                                storeOfObjectTypes = storeOfObjectTypes
                            )
                        }
                        is Widget.AllObjects -> {
                            AllContentWidgetContainer(
                                widget = widget
                            )
                        }
                        is Widget.Section.ObjectType -> {
                            SectionWidgetContainer.ObjectTypes
                        }
                        is Widget.Section.Pinned -> {
                            SectionWidgetContainer.Pinned
                        }
                    }
                }
            }.collect {
                Timber.d("Emitting list of containers: ${it.size}")
                containers.value = it
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
            combine(
                storeOfObjectTypes.trackChanges(),
                objectViewState,
                hasEditAccess,
                userSettingsRepository.getExpandedWidgetIds(vmParams.spaceId)
                    .catch { e ->
                        Timber.e(e, "Failed to get expanded widget IDs, using defaults")
                        emit(emptyList())
                    },
                userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId)
                    .catch { e ->
                        Timber.e(e, "Failed to get collapsed section IDs, using defaults")
                        emit(emptyList())
                    }
            ) { _, state, isOwnerOrEditor, savedExpandedIds, savedCollapsedSections ->
                val currentCollapsedSections = savedCollapsedSections.toSet()
                val s = when (state) {
                    is ObjectViewState.Idle -> flowOf(state)
                    is ObjectViewState.Failure -> flowOf(state)
                    is ObjectViewState.Loading -> flowOf(state)
                    is ObjectViewState.Success -> {
                        payloads.scan(state) { s, p ->
                            s.copy(obj = reduce(state = s.obj, event = p))
                        }
                    }
                }
                Pair(s, Triple(isOwnerOrEditor, savedExpandedIds, currentCollapsedSections))
            }.flatMapLatest { (stateFlow, params) ->
                val (isOwnerOrEditor, savedExpandedIds, currentCollapsedSections) = params
                stateFlow.map { state ->
                    if (state is ObjectViewState.Success) {
                        buildList {
                            // Always add section headers
                            add(Widget.Section.Pinned(config = state.config))

                            // Add pinned widgets only if pinned section is not collapsed
                            val isPinnedSectionCollapsed = currentCollapsedSections.contains(Widget.Source.SECTION_PINNED)
                            if (!isPinnedSectionCollapsed) {
                                addAll(
                                    state.obj.blocks.parseWidgets(
                                        root = state.obj.root,
                                        details = state.obj.details,
                                        config = state.config,
                                        urlBuilder = urlBuilder
                                    )
                                )
                            }

                            add(Widget.Section.ObjectType(config = state.config))

                            // Add object type widgets only if object type section is not collapsed
                            val isObjectTypeSectionCollapsed = currentCollapsedSections.contains(Widget.Source.SECTION_OBJECT_TYPE)
                            val pinnedSectionStateDesc = if (isPinnedSectionCollapsed) "collapsed" else "expanded"
                            val objectTypeSectionStateDesc = if (isObjectTypeSectionCollapsed) "collapsed" else "expanded"

                            if (!isObjectTypeSectionCollapsed) {
                                val types = mapSpaceTypesToWidgets(
                                    isOwnerOrEditor = isOwnerOrEditor,
                                    config = state.config
                                )
                                addAll(types)
                                Timber.d("Section states - Pinned: $pinnedSectionStateDesc, ObjectType: $objectTypeSectionStateDesc, ObjectType widgets added: ${types.size}")
                            } else {
                                Timber.d("Section states - Pinned: $pinnedSectionStateDesc, ObjectType: $objectTypeSectionStateDesc, ObjectType widgets: 0 (section collapsed)")
                            }
                        }.also { allWidgets ->
                            // Initialize active views for all widgets
                            // First get active views from bundled widgets (parsed from blocks)
                            val bundledWidgetActiveViews = state.obj.blocks.parseActiveViews()

                            // For ObjectType widgets, preserve any existing active view state
                            // since they don't have persistent storage in blocks
                            val currentActiveViews = widgetActiveViewStateHolder.getActiveViews()
                            val objectTypeActiveViews = currentActiveViews.filterKeys { widgetId ->
                                allWidgets
                                    .filter { it !is Widget.Section }
                                    .any { widget ->
                                        widget.id == widgetId && widget.source is Widget.Source.Default &&
                                                (widget.source as Widget.Source.Default).obj.layout == ObjectType.Layout.OBJECT_TYPE
                                    }
                            }

                            // Combine bundled widget active views with preserved ObjectType active views
                            val combinedActiveViews = bundledWidgetActiveViews + objectTypeActiveViews
                            widgetActiveViewStateHolder.init(combinedActiveViews)

                            // Update collapsed widget state based on persisted expanded IDs
                            // Update expanded widget state from persistence
                            // When savedExpandedIds is empty (no cache), all widgets will be collapsed
                            expandedWidgetIds.value = savedExpandedIds.toSet()
                        }
                    } else {
                        emptyList()
                    }
                }
            }.collect { widgetList ->
                Timber.d("Emitting list of widgets: ${widgetList.size}")
                widgets.value = widgetList
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
                    widgetsToActiveViews = emptyMap()
                )
            )
        )
        val subscriptionIds = buildList {
            addAll(
                widgets.value.orEmpty().mapNotNull { widget ->
                    when (widget.source) {
                        is Widget.Source.Bundled -> widget.source.id
                        is Widget.Source.Default -> widget.source.id
                        Widget.Source.Other -> null
                    }
                }
            )
            add(SpaceWidgetContainer.SPACE_WIDGET_SUBSCRIPTION)
        }
        if (subscriptionIds.isNotEmpty()) {
            storelessSubscriptionContainer.unsubscribe(subscriptionIds)
        }

        closeObject.stream(
            CloseObject.Params(
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
                            if (WidgetConfig.isLinkOnlyLayout(dispatch.sourceLayout)) {
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
                            if (
                                dispatch.source == BundledWidgetSourceView.AllObjects.id
                                || dispatch.source == BundledWidgetSourceView.Bin.id
                                || dispatch.source == BundledWidgetSourceView.Chat.id
                            ) {
                                // Applying link layout automatically to all-objects widget
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
                                        layout = ObjectType.Layout.SET.code,
                                        target = dispatch.target,
                                        isInEditMode = isInEditMode()
                                    )
                                )
                            }
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
        Timber.d("Proceeding with widget deletion: $widget")
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
            val config = spaceManager.getConfig()
            if (config != null) {
                sendAddWidgetEvent(
                    analytics = analytics,
                    isInEditMode = isInEditMode()
                )
                commands.emit(
                    Command.SelectWidgetSource(
                        ctx = config.widgets,
                        isInEditMode = isInEditMode(),
                        space = vmParams.spaceId.id
                    )
                )
            }
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

    fun onWidgetElementClicked(widget: Id, obj: ObjectWrapper.Basic) {
        Timber.d("With id: ${obj.id}")
        if (obj.isArchived != true) {
            viewModelScope.launch {
                val isAutoCreated = widgets.value?.find { it.id == widget }?.isAutoCreated
                analytics.sendOpenSidebarObjectEvent(
                    isAutoCreated = isAutoCreated
                )
            }
            proceedWithOpeningObject(obj)
        } else {
            sendToast("Open bin to restore your archived object")
        }
    }

    fun onCreateNewTypeClicked() {
        viewModelScope.launch {
            val permission = userPermissionProvider.get(vmParams.spaceId)
            if (permission?.isOwnerOrEditor() == true) {
                commands.emit(Command.CreateNewType(vmParams.spaceId.id))
            } else {
                sendToast("You don't have permission to create new type")
            }
        }
    }

    /**
     * Creates a WidgetView from ObjectWrapper.Type based on the widget layout configuration.
     */
    private fun createWidgetViewFromType(objectType: ObjectWrapper.Type, config: Config): Widget {
        val widgetSource = Widget.Source.Default(obj = objectType.toBasic())
        val icon = objectType.objectIcon()
        val widgetLimit = objectType.widgetLimit ?: 0

        return when (objectType.widgetLayout) {
            Block.Content.Widget.Layout.TREE -> {
                Widget.Tree(
                    id = objectType.id,
                    source = widgetSource,
                    config = config,
                    icon = icon,
                    limit = widgetLimit,
                    sectionType = SectionType.TYPES
                )
            }
            Block.Content.Widget.Layout.LIST -> {
                Widget.List(
                    id = objectType.id,
                    source = widgetSource,
                    config = config,
                    icon = icon,
                    limit = widgetLimit,
                    sectionType = SectionType.TYPES
                )
            }
            Block.Content.Widget.Layout.COMPACT_LIST -> {
                Widget.List(
                    id = objectType.id,
                    source = widgetSource,
                    config = config,
                    icon = icon,
                    limit = widgetLimit,
                    isCompact = true,
                    sectionType = SectionType.TYPES
                )
            }
            Block.Content.Widget.Layout.VIEW -> {
                Widget.View(
                    id = objectType.id,
                    source = widgetSource,
                    config = config,
                    icon = icon,
                    limit = widgetLimit,
                    sectionType = SectionType.TYPES
                )
            }
            Block.Content.Widget.Layout.LINK -> {
                Widget.Link(
                    id = objectType.id,
                    source = widgetSource,
                    config = config,
                    icon = icon,
                    sectionType = SectionType.TYPES
                )
            }
            null -> {
                if (objectType.uniqueKey == ObjectTypeIds.IMAGE) {
                    // Image type widgets default to gallery view
                    Widget.View(
                        id = objectType.id,
                        source = widgetSource,
                        config = config,
                        icon = icon,
                        limit = widgetLimit,
                        sectionType = SectionType.TYPES
                    )
                } else {
                    // Default to compact list for other types
                    Widget.List(
                        id = objectType.id,
                        source = widgetSource,
                        config = config,
                        icon = icon,
                        limit = widgetLimit,
                        isCompact = true,
                        sectionType = SectionType.TYPES
                    )
                }
            }
        }
    }

    fun onWidgetMenuTriggered(widget: Id) {
        Timber.d("onWidgetMenuTriggered: $widget")
        viewModelScope.launch {
            val isAutoCreated = widgets.value?.find { it.id == widget }?.isAutoCreated
            analytics.sendScreenWidgetMenuEvent(
                isAutoCreated = isAutoCreated
            )
        }
    }

    fun onObjectCheckboxClicked(id: Id, isChecked: Boolean) {
        Timber.d("onObjectCheckboxClicked: $id to $isChecked")
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

    fun onWidgetSourceClicked(widget: Id, source: Widget.Source) {
        Timber.d("onWidgetSourceClicked: $source")
        val isAutoCreated = widgets.value?.find { it.id == widget }?.isAutoCreated
        when (source) {
            is Widget.Source.Bundled.Favorites -> {
                viewModelScope.sendClickWidgetTitleEvent(
                    analytics = analytics,
                    bundled = source,
                    isAutoCreated = isAutoCreated
                )
                // TODO switch to bundled widgets id
                viewModelScope.launch {
                    navigation(
                        ExpandWidget(
                            subscription = Subscription.Favorites,
                            space = vmParams.spaceId.id
                        )
                    )
                }
            }
            is Widget.Source.Bundled.Recent -> {
                viewModelScope.sendClickWidgetTitleEvent(
                    analytics = analytics,
                    bundled = source,
                    isAutoCreated = isAutoCreated
                )
                // TODO switch to bundled widgets id
                viewModelScope.launch {
                    navigation(
                        ExpandWidget(
                            subscription = Subscription.Recent,
                            space = vmParams.spaceId.id
                        )
                    )
                }
            }
            is Widget.Source.Bundled.RecentLocal -> {
                viewModelScope.sendClickWidgetTitleEvent(
                    analytics = analytics,
                    bundled = source,
                    isAutoCreated = isAutoCreated
                )
                // TODO switch to bundled widgets id
                viewModelScope.launch {
                    navigation(
                        ExpandWidget(
                            subscription = Subscription.RecentLocal,
                            space = vmParams.spaceId.id
                        )
                    )
                }
            }
            is Widget.Source.Default -> {
                if (source.obj.isArchived != true) {
                    dispatchSelectHomeTabCustomSourceEvent(
                        widget = widget,
                        source = source
                    )
                    proceedWithOpeningObject(source.obj)
                } else {
                    sendToast("Open bin to restore your archived object")
                }
            }
            is Widget.Source.Bundled.Bin -> {
                viewModelScope.launch {
                    navigation(
                        ExpandWidget(
                            subscription = Subscription.Bin,
                            space = vmParams.spaceId.id
                        )
                    )
                }
            }
            is Widget.Source.Bundled.AllObjects -> {
                viewModelScope.launch {
                    if (mode.value == InteractionMode.Edit) {
                        return@launch
                    }
                    navigation(
                        OpenAllContent(
                            space = vmParams.spaceId.id
                        )
                    )
                }
            }
            is Widget.Source.Bundled.Chat -> {
                viewModelScope.launch {
                    if (mode.value == InteractionMode.Edit) {
                        return@launch
                    }
                    val space = vmParams.spaceId.id
                    val view = spaceViewSubscriptionContainer.get(SpaceId(space))
                    val chat = view?.chatId
                    if (chat != null) {
                        navigation(
                            OpenChat(
                                ctx = chat,
                                space = space
                            )
                        )
                    } else {
                        Timber.w("Failed to open chat from widget: chat not found")
                    }
                }
            }
            Widget.Source.Other -> {
                Timber.w("Skipping click on 'other' widget source")
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
            is DropDownMenuAction.CreateObjectOfType -> {
                // Convert Basic wrapper to Type wrapper for ObjectType objects
                val typeWrapper = ObjectWrapper.Type(action.source.obj.map)
                onCreateNewObjectClicked(
                    objType = typeWrapper
                )
            }
        }
    }

    fun onBundledWidgetClicked(widget: Id) {
        Timber.d("onBundledWidgetClicked: $widget")
        viewModelScope.launch {
            // TODO DROID-2341 get space from widget views for better consistency
            val space = vmParams.spaceId.id
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
                Subscriptions.SUBSCRIPTION_BIN -> {
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
                BundledWidgetSourceIds.CHAT -> {
                    proceedWithSpaceChatWidgetHeaderClick()
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
                    Navigation.OpenChat(
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
            val config = spaceManager.getConfig()
            if (config != null) {
                sendAddWidgetEvent(
                    analytics = analytics,
                    isInEditMode = isInEditMode()
                )
                commands.emit(
                    Command.SelectWidgetSource(
                        ctx = config.widgets,
                        target = widget,
                        isInEditMode = isInEditMode(),
                        space = vmParams.spaceId.id
                    )
                )
            }
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
                                    if (source.obj.layout == ObjectType.Layout.OBJECT_TYPE) {
                                        UNDEFINED_LAYOUT_CODE
                                    } else {
                                        source.obj.layout?.code ?: UNDEFINED_LAYOUT_CODE
                                    }
                                }
                                Widget.Source.Other -> UNDEFINED_LAYOUT_CODE
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
        // All-objects widget has link appearance.
        is Widget.AllObjects -> Command.ChangeWidgetType.TYPE_LINK
        is Widget.Chat -> Command.ChangeWidgetType.TYPE_LINK
        is Widget.Section.ObjectType -> ChangeWidgetType.TYPE_LINK
        is Widget.Section.Pinned -> ChangeWidgetType.TYPE_LINK
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
                    if (e is Event.Command.Details.Amend) {
                        val hasTargetKeyValueChanges = Widget.Source.SOURCE_KEYS.any { key ->
                            key in e.details.keys
                        }
                        if (hasTargetKeyValueChanges) {
                            curr = curr.copy(details = curr.details.process(e))
                        } else {
                            Timber.d("Widget source reducer: Ignoring Amend event: no relevant keys in ${e.details.keys}")
                        }
                    } else {
                        curr = curr.copy(details = curr.details.process(e))
                    }
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
                // Update expanded state when widgets are deleted
                val deletedIds = deletedWidgets.map { it.id }.toSet()
                expandedWidgetIds.value = expandedWidgetIds.value - deletedIds
                saveExpandedWidgetState()
                unsubscriber.unsubscribe(expiredSubscriptions)
            }
        }
    }

    fun onStart() {
        Timber.d("onStart")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.screenWidget
        )
    }

    fun onResume(deeplink: DeepLinkResolver.Action? = null) {
        Timber.d("onResume, deeplink: ${deeplink}")
        viewModelScope.launch {
            clearLastOpenedObject.run(
                ClearLastOpenedObject.Params(
                    vmParams.spaceId
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
                        collapsed = emptyList(),  // No longer using session for collapsed state
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
            is OpenObjectNavigation.OpenChat -> {
                navigate(
                    Navigation.OpenChat(
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
            is OpenObjectNavigation.OpenDateObject -> {
                navigate(
                    destination = Navigation.OpenDateObject(
                        ctx = navigation.target,
                        space = navigation.space
                    )
                )
            }
            is OpenObjectNavigation.OpenParticipant -> {
                navigate(
                    Navigation.OpenParticipant(
                        objectId = navigation.target,
                        space = navigation.space
                    )
                )
            }
            is OpenObjectNavigation.OpenType -> {
                navigate(
                    Navigation.OpenType(
                        target = navigation.target,
                        space = navigation.space
                    )
                )
            }
            is OpenObjectNavigation.OpenBookmarkUrl -> {
                navigate(
                    Navigation.OpenBookmarkUrl(
                        url = navigation.url
                    )
                )
            }
        }
    }

    fun onCreateNewObjectLongClicked() {
        viewModelScope.launch {
            val space = vmParams.spaceId.id
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
                            add(Relations.PLURAL_NAME)
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
            val isAutoCreated = widgets.value?.find { it.id == target?.id }?.isAutoCreated
            when (val source = target?.source) {
                is Widget.Source.Bundled -> {
                    sendDeleteWidgetEvent(
                        analytics = analytics,
                        bundled = source,
                        isInEditMode = isInEditMode(),
                        isAutoCreated = isAutoCreated
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
                                isInEditMode = isInEditMode(),
                                isAutoCreated = isAutoCreated
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

    private fun dispatchSelectHomeTabCustomSourceEvent(widget: Id, source: Widget.Source) {
        viewModelScope.launch {
            val isAutoCreated = widgets.value?.find { it.id == widget }?.isAutoCreated
            val sourceObjectType = source.type
            if (sourceObjectType != null) {
                val objectTypeWrapper = storeOfObjectTypes.get(sourceObjectType)
                if (objectTypeWrapper != null) {
                    sendClickWidgetTitleEvent(
                        analytics = analytics,
                        sourceObjectTypeId = objectTypeWrapper.sourceObject.orEmpty(),
                        isCustomObjectType = objectTypeWrapper.sourceObject.isNullOrEmpty(),
                        isAutoCreated = isAutoCreated
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
                    val isAutoCreated = widgets.value?.find { it.id == subject.id }?.isAutoCreated
                    sendReorderWidgetEvent(
                        analytics = analytics,
                        bundled = source,
                        isAutoCreated = isAutoCreated
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

    fun onSpaceWidgetShareIconClicked(spaceView: ObjectWrapper.SpaceView) {
        viewModelScope.launch {
            val space = spaceView.targetSpaceId
            if (space != null) {
                commands.emit(Command.ShareSpace(SpaceId(space)))
            } else {
                sendToast("Space not found")
            }
        }
    }

    fun onNavBarShareIconClicked() {
        viewModelScope.launch {
            navPanelState.value.leftButtonClickAnalytics(analytics)
        }
        viewModelScope.launch {
            commands.emit(Command.ShareSpace(vmParams.spaceId))
        }
    }

    fun onHomeButtonClicked() {
        // Do nothing, as home button is not visible on space home screen.
    }

    fun onSpaceWidgetClicked() {
        viewModelScope.launch {
            commands.emit(
                Command.OpenSpaceSettings(
                    spaceId = vmParams.spaceId
                )
            )
        }
    }

    fun onBackClicked() {
        proceedWithCloseOpenObjects()
        viewModelScope.launch {
            commands.emit(
                Command.HandleChatSpaceBackNavigation
            )
        }
    }

    private fun proceedWithCloseOpenObjects() {
        viewModelScope.launch {
            if (spaceManager.getState() is SpaceManager.State.Space) {
                // Proceed with releasing resources before exiting
                openWidgetObjectsHistory.forEach { (obj, space) ->
                    closeObject
                        .async(
                            CloseObject.Params(
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
            }
        }
    }

    override fun onCleared() {
        Timber.d("onCleared")

        // Cancel existing jobs first to stop any ongoing work
        jobs.cancel()
        widgetObjectPipelineJobs.cancel()

        // Launch fire-and-forget cleanup coroutine
        // Using injected scope ensures proper lifecycle management
        scope.launch(appCoroutineDispatchers.io) {
            // Best-effort cleanup: never throw past this boundary
            kotlin.runCatching {
                unsubscriber.unsubscribe(listOf(HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION))
            }.onFailure { Timber.w(it, "Error unsubscribing profile object") }

            val widgetObjectId = cachedWidgetObjectId
            if (widgetObjectId != null) {
                kotlin.runCatching {
                    proceedWithClosingWidgetObject(
                        widgetObject = widgetObjectId,
                        space = vmParams.spaceId
                    )
                }.onFailure { Timber.e(it, "Error while closing widget object") }
            }
        }

        super.onCleared()
    }

    fun onSearchIconClicked() {
        viewModelScope.launch {
            commands.emit(
                Command.OpenGlobalSearchScreen(space = vmParams.spaceId.id)
            )
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.searchScreenShow,
            props = Props(mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.navigation))
        )
    }

    fun onNewWidgetSourceTypeSelected(
        type: ObjectWrapper.Type,
        widgets: Id
    ) {
        viewModelScope.launch {
            createObject.async(
                params = CreateObject.Param(
                    space = vmParams.spaceId,
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

    fun onSpaceSettingsClicked(space: SpaceId) {
        Timber.d("onSpaceSettingsClicked, space: $space")
        viewModelScope.launch {
            val permission = userPermissions.value
            if (permission?.isOwnerOrEditor() == true) {
                navigation(Navigation.OpenOwnerOrEditorSpaceSettings(space = space.id))
            } else {
                val targetSpaceView = spaceViewSubscriptionContainer.get(space)
                if (targetSpaceView != null) {
                    val config = spaceManager.getConfig(space)
                    val creatorId = targetSpaceView.creator.orEmpty()
                    val createdByScreenName : String
                    if (creatorId.isNotEmpty()) {
                        val store = spaceMembers.get(space)
                        createdByScreenName = when(store) {
                            is ActiveSpaceMemberSubscriptionContainer.Store.Data -> {
                                store.members
                                    .find { m -> m.id == creatorId }
                                    ?.let { it.globalName ?: it.identity }
                                    ?.ifEmpty { null }
                                    ?: creatorId
                            }
                            ActiveSpaceMemberSubscriptionContainer.Store.Empty -> {
                                creatorId
                            }
                        }
                    } else {
                        Timber.w("Creator ID was empty")
                        createdByScreenName = EMPTY_STRING_VALUE
                    }
                    val inviteLink = getSpaceInviteLink
                        .async(space)
                        .getOrNull()
                        ?.scheme
                    
                    viewerSpaceSettingsState.value = ViewerSpaceSettingsState.Visible(
                        name = targetSpaceView.name.orEmpty(),
                        description = targetSpaceView.description.orEmpty(),
                        icon = targetSpaceView.spaceIcon(urlBuilder),
                        techInfo = SpaceTechInfo(
                            spaceId = space,
                            networkId = config?.network.orEmpty(),
                            creationDateInMillis = targetSpaceView
                                .getValue<Double?>(Relations.CREATED_DATE)
                                ?.let { timeInSeconds -> (timeInSeconds * 1000L).toLong() },
                            createdBy = createdByScreenName,
                            isDebugVisible = false
                        ),
                        inviteLink = inviteLink
                    )
                }
            }
        }
    }

    fun onViewerSpaceSettingsUiEvent(space: SpaceId, uiEvent: UiEvent) {
        when(uiEvent) {
            is UiEvent.OnQrCodeClicked -> {
                viewModelScope.launch {
                    val currentState = viewerSpaceSettingsState.value
                    if (currentState is ViewerSpaceSettingsState.Visible) {
                        uiQrCodeState.value = SpaceInvite(
                            link = uiEvent.link,
                            spaceName = currentState.name,
                            icon = currentState.icon
                        )
                    }
                }
            }
            UiEvent.OnInviteClicked -> {
                viewModelScope.launch { commands.emit(ShareSpace(space)) }
            }
            UiEvent.OnLeaveSpaceClicked -> {
                viewModelScope.launch { commands.emit(Command.ShowLeaveSpaceWarning) }
            }
            is UiEvent.OnShareLinkClicked -> {
                viewModelScope.launch {
                    commands.emit(Command.ShareInviteLink(uiEvent.link))
                }
            }
            is UiEvent.OnCopyLinkClicked -> {
                viewModelScope.launch {
                    val params = CopyInviteLinkToClipboard.Params(uiEvent.link)
                    copyInviteLinkToClipboard.invoke(params)
                        .proceed(
                            failure = {
                                Timber.e(it, "Failed to copy invite link to clipboard")
                                sendToast("Failed to copy invite link")
                            },
                            success = {
                                Timber.d("Invite link copied to clipboard: ${uiEvent.link}")
                                sendToast("Invite link copied to clipboard")
                            }
                        )
                }
            }
            else -> {
                Timber.w("Unexpected UI event: $uiEvent")
            }
        }
    }

    fun onDismissViewerSpaceSettings() {
        viewerSpaceSettingsState.value = ViewerSpaceSettingsState.Hidden
    }

    fun onHideQrCodeScreen() {
        uiQrCodeState.value = UiSpaceQrCodeState.Hidden
    }

    fun onLeaveSpaceAcceptedClicked(space: SpaceId) {
        viewModelScope.launch {
            val permission = userPermissionProvider.get(space)
            if (permission != SpaceMemberPermissions.OWNER) {
                deleteSpace
                    .async(space)
                    .onFailure { Timber.e(it, "Error while leaving space") }
                    .onSuccess {
                        // Forcing return to the vault even if space has chat.
                        proceedWithCloseOpenObjects()
                    }
            } else {
                Timber.e("Unexpected permission when trying to leave space: $permission")
            }
        }
    }

    //region OBJECT CREATION
    fun onCreateWidgetElementClicked(widget: WidgetView) {
        Timber.d("onCreateWidgetElementClicked, widget: ${widget::class.java.simpleName}")
        viewModelScope.launch {
            when (widget) {
                is WidgetView.ListOfObjects -> {
                    if (widget.type == WidgetView.ListOfObjects.Type.Favorites) {
                        proceedWithCreatingFavoriteObject()
                    } else {
                        Timber.w("Creating object inside ListOfObjects widget is not supported yet for type: ${widget.type}")
                    }
                }
                is WidgetView.SetOfObjects -> {
                    handleDefaultWidgetSource(
                        dataViewObjectId = widget.source.id,
                        viewId = widget.tabs.find { it.isSelected }?.id,
                        navigate = true
                    )
                }
                is WidgetView.Gallery -> {
                    handleDefaultWidgetSource(
                        dataViewObjectId = widget.source.id,
                        viewId = widget.tabs.find { it.isSelected }?.id,
                        navigate = true
                    )
                }
                is WidgetView.Tree -> {
                    getDefaultObjectType.async(vmParams.spaceId).getOrNull()?.type?.let { typeKey ->
                        storeOfObjectTypes.getByKey(key = typeKey.key)?.let {
                            onCreateObjectForWidget(type = it, source = widget.source.id)
                        }
                    }
                }
                else -> {
                    Timber.w("Unexpected widget type: ${widget::class.java.simpleName}")
                }
            }
        }
    }

    private suspend fun proceedWithCreatingFavoriteObject() {
        val type = getDefaultObjectType.async(vmParams.spaceId)
            .getOrNull()
            ?.type ?: TypeKey(ObjectTypeIds.PAGE)

        proceedWithCreatingObject(
            space = vmParams.spaceId,
            type = type,
            markAsFavorite = true
        )
    }

    private suspend fun proceedWithCreatingObject(
        space: SpaceId,
        type: TypeKey,
        templateId: Id? = null,
        prefilled: Struct = mapOf(),
        markAsFavorite: Boolean = false
    ) {
        val startTime = System.currentTimeMillis()

        createObject.async(
            params = CreateObject.Param(
                space = space,
                type = type,
                template = templateId,
                prefilled = prefilled
            )
        ).fold(
            onSuccess = { result ->

                viewModelScope.launch {
                    sendAnalyticsObjectCreateEvent(
                        objType = type.key,
                        analytics = analytics,
                        route = EventsDictionary.Routes.widget,
                        startTime = startTime,
                        view = null,
                        spaceParams = provideParams(space.id)
                    )
                }

                proceedWithNavigation(result.obj.navigation())

                if (markAsFavorite) {
                    setAsFavourite.async(
                        params = SetObjectListIsFavorite.Params(
                            objectIds = listOf(result.obj.id),
                            isFavorite = true
                        )
                    )
                }
            },
            onFailure = {
                Timber.e(it, "Error while creating object")
            }
        )
    }

    private suspend fun proceedWithCreatingDataViewObject(
        dataViewSourceObj: ObjectWrapper.Basic,
        viewer: Block.Content.DataView.Viewer,
        dv: DV,
        navigate: Boolean = false
    ) {
        Timber.d("proceedWithCreatingDataViewObject, dataViewSourceObj: $dataViewSourceObj")
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
        val type = TypeKey(dataViewSourceType ?: VIEW_DEFAULT_OBJECT_TYPE)
        val space = vmParams.spaceId.id
        val startTime = System.currentTimeMillis()
        createDataViewObject.async(
            params = CreateDataViewObject.Params.SetByType(
                type = type,
                filters = viewer.filters,
                template = defaultTemplate,
                prefilled = prefilled
            ).also {
                Timber.d("Calling with params: $it")
            }
        ).fold(
            onSuccess = { result ->
                Timber.d("Successfully created object with id: ${result.objectId}")
                viewModelScope.sendAnalyticsObjectCreateEvent(
                    analytics = analytics,
                    route = EventsDictionary.Routes.widget,
                    startTime = startTime,
                    view = null,
                    objType = type.key,
                    spaceParams = provideParams(space)
                )
                if (navigate) {
                    val wrapper = ObjectWrapper.Basic(result.struct.orEmpty())
                    if (wrapper.isValid) {
                        proceedWithNavigation(wrapper.navigation())
                    }
                }
            },
            onFailure = {
                Timber.e(it, "Error while creating data view object for widget")
            }
        )
    }

    private suspend fun proceedWithAddingObjectToCollection(
        viewer: Block.Content.DataView.Viewer,
        dv: DV,
        collection: Id,
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

        val space = vmParams.spaceId.id
        val startTime = System.currentTimeMillis()

        createDataViewObject.async(params = createObjectParams).fold(
            onFailure = {
                Timber.e("Error creating object for collection")
            },
            onSuccess = { result ->
                Timber.d("Successfully created object with id: ${result.objectId}")

                viewModelScope.sendAnalyticsObjectCreateEvent(
                    analytics = analytics,
                    route = EventsDictionary.Routes.widget,
                    startTime = startTime,
                    view = null,
                    objType = defaultObjectTypeUniqueKey.key,
                    spaceParams = provideParams(space)
                )

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

    private suspend fun handleDefaultWidgetSource(
        dataViewObjectId: Id,
        viewId: ViewId?,
        navigate: Boolean
    ) {
        getObject.async(
            params = GetObject.Params(
                target = dataViewObjectId,
                space = vmParams.spaceId
            )
        ).fold(
            onSuccess = { objView ->
                Timber.d("onCreateDataViewObject:gotDataViewPreview")
                val dv = objView.blocks.find { it.content is DV }?.content as? DV
                val viewer = if (viewId.isNullOrEmpty())
                    dv?.viewers?.firstOrNull()
                else
                    dv?.viewers?.find { it.id == viewId }

                if (dv == null) {
                    Timber.w("Data view not found inside the object")
                    return@fold
                }

                if (viewer == null) {
                    Timber.w("Viewer not found inside the data view")
                    return@fold
                }

                val dataViewObject = ObjectWrapper.Basic(objView.details[objView.root].orEmpty())

                if (!dataViewObject.isValid) {
                    Timber.w("Data view object is not valid")
                    return@fold
                }

                when (dataViewObject.layout) {
                    ObjectType.Layout.COLLECTION -> {
                        proceedWithAddingObjectToCollection(
                            viewer = viewer,
                            dv = dv,
                            collection = dataViewObjectId
                        )
                    }
                    ObjectType.Layout.SET -> {
                        val dataViewSourceId = dataViewObject.setOf.firstOrNull()
                        val dataViewSourceObj = if (dataViewSourceId != null)
                            ObjectWrapper.Basic(
                                objView.details[dataViewSourceId].orEmpty()
                            )
                        else
                            null
                        if (dataViewSourceObj == null || !dataViewSourceObj.isValid) {
                            Timber.w("Data view source is missing or not valid")
                            return@fold
                        }
                        proceedWithCreatingDataViewObject(
                            dataViewSourceObj = dataViewSourceObj,
                            viewer = viewer,
                            dv = dv,
                            navigate = navigate
                        )
                    }
                    ObjectType.Layout.OBJECT_TYPE -> {
                        if (!dataViewObject.isValid) {
                            Timber.w("Data view object is not valid")
                            return@fold
                        }
                        proceedWithCreatingDataViewObject(
                            dataViewSourceObj = dataViewObject,
                            viewer = viewer,
                            dv = dv,
                            navigate = navigate
                        )
                    }
                    else -> {
                        Timber.w("Unsupported layout of data view object: ${dataViewObject.layout}")
                    }
                }
            }
        )
    }

    fun onCreateObjectForWidget(
        type: ObjectWrapper.Type,
        source: Id
    ) {
        viewModelScope.launch {
            createObject.async(
                params = CreateObject.Param(
                    space = vmParams.spaceId,
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

    fun onCreateNewObjectClicked(objType: ObjectWrapper.Type? = null) {
        Timber.d("onCreateNewObjectClicked, type:[${objType?.uniqueKey}]")
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val params = objType?.uniqueKey.getCreateObjectParams(
                space = vmParams.spaceId,
                defaultTemplate = objType?.defaultTemplateId
            )
            createObject.stream(params).collect { createObjectResponse ->
                createObjectResponse.fold(
                    onSuccess = { result ->
                        val spaceParams = provideParams(vmParams.spaceId.id)
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
    //endregion

    //region Expanded Widgets
    /**
     * Toggles widget collapse state and persists to preferences
     */
    fun onToggleWidgetExpandedState(widgetId: Id) {
        viewModelScope.launch {
            val currentExpanded = expandedWidgetIds.value
            val newExpanded = if (currentExpanded.contains(widgetId)) {
                currentExpanded - widgetId
            } else {
                currentExpanded + widgetId
            }
            expandedWidgetIds.value = newExpanded
            saveExpandedWidgetState()
        }
    }

    /**
     * Handles section header clicks to collapse/expand all widgets in the section
     */
    fun onSectionClicked(sectionId: Id) {
        viewModelScope.launch {
            when (sectionId) {
                Widget.Source.SECTION_OBJECT_TYPE -> {
                    val currentCollapsedSections = userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId).first().toSet()
                    val isCurrentlyCollapsed = currentCollapsedSections.contains(sectionId)

                    val newCollapsedSections = if (isCurrentlyCollapsed) {
                        // Expand section - remove from collapsed sections
                        currentCollapsedSections.minus(sectionId)
                    } else {
                        // Collapse section - add to collapsed sections and collapse all type widgets
                        collapseAllObjectTypeWidgets()
                        currentCollapsedSections.plus(sectionId)
                    }

                    userSettingsRepository.setCollapsedSectionIds(vmParams.spaceId, newCollapsedSections.toList())
                }
                Widget.Source.SECTION_PINNED -> {
                    val currentCollapsedSections = userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId).first().toSet()
                    val isCurrentlyCollapsed = currentCollapsedSections.contains(sectionId)

                    val newCollapsedSections = if (isCurrentlyCollapsed) {
                        // Expand section - remove from collapsed sections
                        currentCollapsedSections.minus(sectionId)
                    } else {
                        // Collapse section - add to collapsed sections and collapse all pinned widgets
                        collapseAllPinnedWidgets()
                        currentCollapsedSections.plus(sectionId)
                    }

                    userSettingsRepository.setCollapsedSectionIds(vmParams.spaceId, newCollapsedSections.toList())
                }
            }
        }
    }

    /**
     * Collapses all ObjectType widgets by removing them from expandedWidgetIds
     */
    private suspend fun collapseAllObjectTypeWidgets() {
        val currentWidgets = widgets.value.orEmpty()
        val objectTypeWidgetIds = currentWidgets
            .filter { widget ->
                widget !is Widget.Section && widget.sectionType == SectionType.TYPES
            }
            .map { it.id }

        // Remove all ObjectType widget IDs from expanded set
        expandedWidgetIds.value = expandedWidgetIds.value - objectTypeWidgetIds.toSet()
        saveExpandedWidgetState()
    }

    /**
     * Collapses all Pinned widgets by removing them from expandedWidgetIds
     */
    private suspend fun collapseAllPinnedWidgets() {
        val currentWidgets = widgets.value.orEmpty()
        val pinnedWidgetIds = currentWidgets
            .filter { widget ->
                widget !is Widget.Section && widget.sectionType == SectionType.PINNED
            }
            .map { it.id }

        // Remove all Pinned widget IDs from expanded set
        expandedWidgetIds.value = expandedWidgetIds.value - pinnedWidgetIds.toSet()
        saveExpandedWidgetState()
    }


    /**
     * Determines if a widget should be collapsed due to its section being collapsed
     */
    private fun isWidgetInCollapsedSection(widget: Widget, collapsedSections: Set<Id>): Boolean {
        return when {
            widget is Widget.Section -> false // Sections themselves are not collapsed by section state
            widget.sectionType == SectionType.PINNED -> collapsedSections.contains(Widget.Source.SECTION_PINNED)
            widget.sectionType == SectionType.TYPES -> collapsedSections.contains(Widget.Source.SECTION_OBJECT_TYPE)
            else -> false
        }
    }

    /**
     * Saves current expanded widget state to preferences
     */
    private suspend fun saveExpandedWidgetState() {
        val expandedIds = expandedWidgetIds.value.toList()
        userSettingsRepository.setExpandedWidgetIds(vmParams.spaceId, expandedIds)
        Timber.d("Saved expanded widget state: ${expandedIds.size} expanded widgets")
    }
    //endregion

    fun proceedWithExitingToVault() {
        viewModelScope.launch {
            proceedWithClearingSpaceBeforeExitingToVault()
        }
    }

    sealed class Navigation {
        data class OpenObject(val ctx: Id, val space: Id) : Navigation()
        data class OpenChat(val ctx: Id, val space: Id) : Navigation()
        data class OpenSet(val ctx: Id, val space: Id, val view: Id?) : Navigation()
        data class ExpandWidget(val subscription: Subscription, val space: Id) : Navigation()
        data class OpenAllContent(val space: Id) : Navigation()
        data class OpenDateObject(val ctx: Id, val space: Id) : Navigation()
        data class OpenParticipant(val objectId: Id, val space: Id) : Navigation()
        data class OpenType(val target: Id, val space: Id) : Navigation()
        data class OpenOwnerOrEditorSpaceSettings(val space: Id) : Navigation()
        data class OpenBookmarkUrl(val url: String) : Navigation() // Added for opening bookmark URLs from widgets
    }

    sealed class ViewerSpaceSettingsState {
        data object Init : ViewerSpaceSettingsState()
        data object Hidden: ViewerSpaceSettingsState()
        data class Visible(
            val name: String,
            val description: String,
            val icon: SpaceIconView,
            val techInfo: SpaceTechInfo,
            val inviteLink: String? = null
        ) : ViewerSpaceSettingsState()
    }

    class Factory @Inject constructor(
        private val vmParams: HomeScreenVmParams,
        private val openObject: OpenObject,
        private val closeObject: CloseObject,
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
        private val spaceInviteResolver: SpaceInviteResolver,
        private val exitToVaultDelegate: ExitToVaultDelegate,
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        private val getSpaceInviteLink: GetSpaceInviteLink,
        private val deleteSpace: DeleteSpace,
        private val activeSpaceMemberSubscriptionContainer: ActiveSpaceMemberSubscriptionContainer,
        private val setObjectListIsFavorite: SetObjectListIsFavorite,
        private val chatPreviews: ChatPreviewContainer,
        private val notificationPermissionManager: NotificationPermissionManager,
        private val copyInviteLinkToClipboard: CopyInviteLinkToClipboard,
        private val userRepo: UserSettingsRepository,
        private val scope: CoroutineScope
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeScreenViewModel(
            vmParams = vmParams,
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
            clearLastOpenedObject = clearLastOpenedObject,
            spaceBinWidgetContainer = spaceBinWidgetContainer,
            featureToggles = featureToggles,
            fieldParser = fieldParser,
            spaceInviteResolver = spaceInviteResolver,
            exitToVaultDelegate = exitToVaultDelegate,
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            getSpaceInviteLink = getSpaceInviteLink,
            deleteSpace = this@Factory.deleteSpace,
            spaceMembers = activeSpaceMemberSubscriptionContainer,
            setAsFavourite = setObjectListIsFavorite,
            chatPreviews = chatPreviews,
            notificationPermissionManager = notificationPermissionManager,
            copyInviteLinkToClipboard = copyInviteLinkToClipboard,
            userSettingsRepository = userRepo,
            scope = scope
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
        val ctx: Id,
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

    data object ShowLeaveSpaceWarning : Command()

    data object HandleChatSpaceBackNavigation : Command()

    data class ShareInviteLink(val link: String) : Command()
    data class CreateNewType(val space: Id) : Command()
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
    data class OpenEditor(val target: Id, val space: Id, val effect: SideEffect = SideEffect.None) : OpenObjectNavigation()
    data class OpenDataView(val target: Id, val space: Id, val effect: SideEffect = SideEffect.None): OpenObjectNavigation()
    data class UnexpectedLayoutError(val layout: ObjectType.Layout?): OpenObjectNavigation()
    data object NonValidObject: OpenObjectNavigation()
    data class OpenChat(val target: Id, val space: Id): OpenObjectNavigation()
    data class OpenDateObject(val target: Id, val space: Id): OpenObjectNavigation()
    data class OpenParticipant(val target: Id, val space: Id): OpenObjectNavigation()
    data class OpenType(val target: Id, val space: Id) : OpenObjectNavigation()
    data class OpenBookmarkUrl(val url: String) : OpenObjectNavigation() // For opening bookmark URLs

    sealed class SideEffect {
        data object None: SideEffect()
        data class AttachToChat(val chat: Id, val space: Id): SideEffect()
    }
}

fun ObjectWrapper.Type.navigation(
    spaceId: Id,
): OpenObjectNavigation {
    if (!isValid) return OpenObjectNavigation.NonValidObject
    return OpenObjectNavigation.OpenType(
        target = id,
        space = spaceId
    )
}

/**
 * @param [attachmentTarget] optional target, to which the object will be attached
 */
fun ObjectWrapper.Basic.navigation(
    effect: OpenObjectNavigation.SideEffect = OpenObjectNavigation.SideEffect.None,
    openBookmarkAsObject: Boolean = false,
) : OpenObjectNavigation {
    if (!isValid) return OpenObjectNavigation.NonValidObject
    return when (layout) {
        ObjectType.Layout.BOOKMARK -> {
            if (openBookmarkAsObject) {
                OpenObjectNavigation.OpenEditor(
                    target = id,
                    space = requireNotNull(spaceId),
                    effect = effect
                )
            } else {
                val url = getValue<String>(Relations.SOURCE)
                if (url.isNullOrEmpty()) {
                    OpenObjectNavigation.OpenEditor(
                        target = id,
                        space = requireNotNull(spaceId),
                        effect = effect
                    )
                } else {
                    OpenObjectNavigation.OpenBookmarkUrl(url)
                }
            }
        }
        ObjectType.Layout.BASIC,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.TODO -> {
            OpenObjectNavigation.OpenEditor(
                target = id,
                space = requireNotNull(spaceId),
                effect = effect
            )
        }
        in SupportedLayouts.fileLayouts -> {
            OpenObjectNavigation.OpenEditor(
                target = id,
                space = requireNotNull(spaceId),
                effect = effect
            )
        }
        ObjectType.Layout.PROFILE -> {
            val identityLink = getValue<Id>(Relations.IDENTITY_PROFILE_LINK)
            if (identityLink.isNullOrEmpty()) {
                OpenObjectNavigation.OpenEditor(
                    target = id,
                    space = requireNotNull(spaceId),
                    effect = effect
                )
            } else {
                OpenObjectNavigation.OpenEditor(
                    target = identityLink,
                    space = requireNotNull(spaceId),
                    effect = effect
                )
            }
        }
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION -> {
            OpenObjectNavigation.OpenDataView(
                target = id,
                space = requireNotNull(spaceId),
                effect = effect
            )
        }
        ObjectType.Layout.CHAT_DERIVED -> {
            OpenObjectNavigation.OpenChat(
                target = id,
                space = requireNotNull(spaceId)
            )
        }
        ObjectType.Layout.DATE -> {
            OpenObjectNavigation.OpenDateObject(
                target = id,
                space = requireNotNull(spaceId)
            )
        }
        ObjectType.Layout.PARTICIPANT -> {
            OpenObjectNavigation.OpenParticipant(
                target = id,
                space = requireNotNull(spaceId)
            )
        }
        ObjectType.Layout.OBJECT_TYPE -> {
            OpenObjectNavigation.OpenType(
                target = id,
                space = requireNotNull(spaceId)
            )
        }
        else -> {
            OpenObjectNavigation.UnexpectedLayoutError(layout)
        }
    }
}

data class HomeScreenVmParams(val spaceId: SpaceId)

const val MAX_TYPE_COUNT_FOR_APP_ACTIONS = 4
const val MAX_PINNED_TYPE_COUNT_FOR_APP_ACTIONS = 3