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
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.ext.canCreateAdditionalChats
import com.anytypeio.anytype.core_models.ext.process
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.misc.navigation
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_models.ui.spaceIcon
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.core_utils.notifications.NotificationPermissionManager
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
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.CopyInviteLinkToClipboard
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.notifications.NotificationStateCalculator
import com.anytypeio.anytype.domain.notifications.SetSpaceNotificationMode
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.HasInstanceOfObjectTypeSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.spaces.ClearLastOpenedSpace
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.domain.spaces.GetSpaceView
import com.anytypeio.anytype.domain.types.GetPinnedObjectTypes
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.widgets.GetWidgetSession
import com.anytypeio.anytype.domain.widgets.ObserveWidgetSections
import com.anytypeio.anytype.domain.widgets.SaveWidgetSession
import com.anytypeio.anytype.domain.widgets.SetWidgetActiveView
import com.anytypeio.anytype.domain.widgets.UpdateObjectTypesOrderIds
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.domain.workspace.DeepLinkToObjectDelegate
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
import com.anytypeio.anytype.presentation.extension.sendOpenSidebarObjectEvent
import com.anytypeio.anytype.presentation.extension.sendReorderWidgetEvent
import com.anytypeio.anytype.presentation.home.Command.ChangeWidgetType
import com.anytypeio.anytype.presentation.home.Command.ChangeWidgetType.Companion.UNDEFINED_LAYOUT_CODE
import com.anytypeio.anytype.presentation.home.Command.ShareSpace
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Navigation.ExpandWidget
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Navigation.OpenAllContent
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Navigation.OpenChat
import com.anytypeio.anytype.presentation.multiplayer.toSpaceMemberView
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.navigation.leftButtonClickAnalytics
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.objects.getTypeForObjectAndTargetTypeForTemplate
import com.anytypeio.anytype.presentation.objects.isTemplateObject
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.presentation.sets.prefillNewObjectDetails
import com.anytypeio.anytype.presentation.sets.resolveSetByRelationPrefilledObjectData
import com.anytypeio.anytype.presentation.sets.resolveTemplateForDataViewObject
import com.anytypeio.anytype.presentation.sets.resolveTypeAndActiveViewTemplate
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_DEFAULT_OBJECT_TYPE
import com.anytypeio.anytype.presentation.spaces.SpaceTechInfo
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState.SpaceInvite
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.vault.ExitToVaultDelegate
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.TreeWidgetBranchStateHolder
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_OBJECT_TYPE
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_PINNED
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_UNREAD
import com.anytypeio.anytype.presentation.widgets.WidgetActiveViewStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetConfig
import com.anytypeio.anytype.presentation.widgets.WidgetContainer
import com.anytypeio.anytype.presentation.widgets.WidgetContainerDelegate
import com.anytypeio.anytype.presentation.widgets.WidgetContainerDelegateImpl
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.presentation.widgets.WidgetSessionStateHolder
import com.anytypeio.anytype.presentation.widgets.WidgetUiParams
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.buildWidgetSections
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import com.anytypeio.anytype.presentation.widgets.parseActiveViews
import com.anytypeio.anytype.presentation.widgets.source.BundledWidgetSourceView
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
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
    private val hasInstanceContainer: HasInstanceOfObjectTypeSubscriptionContainer,
    private val objectWatcher: ObjectWatcher,
    private val spaceManager: SpaceManager,
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
    private val participantContainer: ParticipantSubscriptionContainer,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val copyInviteLinkToClipboard: CopyInviteLinkToClipboard,
    private val userSettingsRepository: UserSettingsRepository,
    private val observeWidgetSections: ObserveWidgetSections,
    private val scope: CoroutineScope,
    private val stringResourceProvider : StringResourceProvider,
    private val updateObjectTypesOrderIds: UpdateObjectTypesOrderIds,
    private val setSpaceNotificationMode: SetSpaceNotificationMode
) : NavigationViewModel<HomeScreenViewModel.Navigation>(),
    Reducer<ObjectView, Payload>,
    WidgetActiveViewStateHolder by widgetActiveViewStateHolder,
    WidgetSessionStateHolder by widgetSessionStateHolder,
    DeepLinkToObjectDelegate by deepLinkToObjectDelegate,
    Unsubscriber by unsubscriber,
    AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate,
    ExitToVaultDelegate by exitToVaultDelegate
{

    private val mutex = Mutex()

    val commands = MutableSharedFlow<Command>()
    val mode = MutableStateFlow<InteractionMode>(InteractionMode.Default)

    private val isEmptyingBinInProgress = MutableStateFlow(false)

    private val objectViewState = MutableStateFlow<ObjectViewState>(ObjectViewState.Idle)
    private val treeWidgetBranchStateHolder = TreeWidgetBranchStateHolder()

    // Widget sections configuration
    val widgetSections: StateFlow<com.anytypeio.anytype.core_models.WidgetSections> = observeWidgetSections
        .flow(ObserveWidgetSections.Params(spaceId = vmParams.spaceId))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = com.anytypeio.anytype.core_models.WidgetSections.default()
        )

    // Separate StateFlows for different widget sections
    private val pinnedWidgets = MutableStateFlow<List<Widget>>(emptyList())
    private val typeWidgets = MutableStateFlow<List<Widget>>(emptyList())
    private val unreadWidget = MutableStateFlow<Widget.UnreadChatList?>(null)
    private val binWidget = MutableStateFlow<Widget.Bin?>(null)

    // Separate containers for pinned and type widgets
    private val pinnedContainers = MutableStateFlow<Containers>(null)
    private val typeContainers = MutableStateFlow<Containers>(null)
    private val unreadContainer = MutableStateFlow<WidgetContainer?>(null)

    // Drag-and-drop state tracking for type widgets
    private var pendingTypeWidgetOrder: List<Id>? = null

    // Lock mechanism to prevent race conditions during DnD operations
    // When a drag operation completes, we optimistically update the UI and send to middleware
    // We then lock event processing for a short period to prevent incoming events from
    // overwriting our optimistic update before middleware confirms the change
    private var typeWidgetEventLockTimestamp: Long? = null

    // Helper property for synchronous access to current widget list
    private val currentWidgets: Widgets
        get() = pinnedWidgets.value + typeWidgets.value

    // Exposed flows for UI - widget views (WidgetView models) separated by section
    @OptIn(ExperimentalCoroutinesApi::class)
    private val pinnedViewsRaw: StateFlow<List<WidgetView>> = pinnedContainers
        .flatMapLatest { containers ->
            if (containers.isNullOrEmpty()) {
                flowOf(emptyList())
            } else {
                combine(containers.map { it.view }) { array ->
                    array.toList()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val typeViewsRaw: StateFlow<List<WidgetView>> = typeContainers
        .flatMapLatest { containers ->
            if (containers.isNullOrEmpty()) {
                flowOf(emptyList())
            } else {
                combine(containers.map { it.view }) { array ->
                    array.toList()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Badge deduplication: hide all chat badges when unread section is expanded
    @OptIn(ExperimentalCoroutinesApi::class)
    val pinnedViews: StateFlow<List<WidgetView>> = combine(
        pinnedViewsRaw,
        observeCollapsedSectionIds()
    ) { pinned, collapsedList ->
        val hideAllChatBadges = !collapsedList.contains(Widget.Source.SECTION_UNREAD)
        if (hideAllChatBadges) {
            hideAllChatBadgesInWidgets(pinned)
        } else {
            pinned
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val typeViews: StateFlow<List<WidgetView>> = combine(
        typeViewsRaw,
        observeCollapsedSectionIds()
    ) { types, collapsedList ->
        val hideAllChatBadges = !collapsedList.contains(Widget.Source.SECTION_UNREAD)
        if (hideAllChatBadges) {
            hideAllChatBadgesInWidgets(types)
        } else {
            types
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Exposed flow for unread widget
    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadView: StateFlow<WidgetView?> = unreadContainer
        .flatMapLatest { container ->
            container?.view ?: flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    // Exposed flow for bin widget
    @OptIn(ExperimentalCoroutinesApi::class)
    val binView: StateFlow<WidgetView?> = binWidget
        .flatMapLatest { widget ->
            if (widget == null) {
                flowOf(null)
            } else {
                // Create container for bin widget
                widgetContainerDelegate.createContainer(widget, emptyList())?.view ?: flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    // Exposed flow for collapsed sections
    val collapsedSections: StateFlow<Set<Id>> = observeCollapsedSectionIds()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    // Store Space widget object ID (from SpaceInfo) to use during cleanup when spaceManager might be empty
    private var cachedWidgetObjectId: String? = null

    private val openWidgetObjectsHistory : MutableSet<OpenObjectHistoryItem> = LinkedHashSet()

    private val userPermissions = MutableStateFlow<SpaceMemberPermissions?>(null)

    // Expanded widget IDs for persistence across app restarts
    private val expandedWidgetIds = MutableStateFlow<Set<Id>>(emptySet())

    // State for bundled widget deletion warning
    val pendingBundledWidgetDeletion = MutableStateFlow<Id?>(null)

    val navPanelState = MutableStateFlow<NavPanelState>(NavPanelState.Init)

    val viewerSpaceSettingsState = MutableStateFlow<ViewerSpaceSettingsState>(ViewerSpaceSettingsState.Init)
    val uiQrCodeState = MutableStateFlow<UiSpaceQrCodeState>(UiSpaceQrCodeState.Hidden)

    private val _spaceViewState = MutableStateFlow<SpaceViewState>(SpaceViewState.Init)
    val spaceViewState: StateFlow<SpaceViewState> = _spaceViewState

    // Mute state derived from space notification mode
    val isMuted: StateFlow<Boolean> = spaceViewSubscriptionContainer
        .observe(vmParams.spaceId)
        .map { spaceView ->
            NotificationStateCalculator.calculateSpaceNotificationMutedState(spaceView)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

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
                        currentWidgets.orEmpty().forEach { widget ->
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
                        pinnedWidgets.value = emptyList()
                        // Check event lock before clearing type widgets during loading
                        if (isTypeWidgetEventLockActive()) {
                            Timber.d("DROID-3951, Type widget event lock is active, ignoring loading state clear")
                        } else {
                            typeWidgets.value = emptyList()
                        }
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
        proceedWithObservingDispatches()
        proceedWithSettingUpShortcuts()
        proceedWithViewStatePipeline()
        proceedWithNavigationPanelState()
        proceedWithSpaceViewSubscription()
    }

    private fun proceedWithSpaceViewSubscription() {
        viewModelScope.launch {
            combine(
                spaceViewSubscriptionContainer.observe(vmParams.spaceId),
                spaceMembers.observe(vmParams.spaceId),
                userPermissions
            ) { spaceView, spaceMembers, permissions ->
                Triple(spaceView, spaceMembers, permissions)
            }
                .collect { (spaceView, members, permissions) ->
                    val spaceMemberCount = if (members is ActiveSpaceMemberSubscriptionContainer.Store.Data) {
                        members.members.toSpaceMemberView(
                            spaceView = spaceView,
                            urlBuilder = urlBuilder,
                            isCurrentUserOwner = permissions?.isOwner() == true,
                            stringResourceProvider = stringResourceProvider
                        ).size
                    } else {
                        0
                    }
                    val spaceIcon = spaceView.spaceIcon(urlBuilder)
                    val name = spaceView.name
                    val spaceName = if (name.isNullOrEmpty()) {
                        stringResourceProvider.getUntitledObjectTitle()
                    } else {
                        name
                    }
                    _spaceViewState.value = SpaceViewState.Success(
                        spaceName = spaceName,
                        spaceIcon = spaceIcon,
                        membersCount = spaceMemberCount,
                        spaceChatId = spaceView.getSingleValue<String>(Relations.CHAT_ID),
                        spaceUxType = spaceView.spaceUxType ?: SpaceUxType.DATA,
                        spaceAccessType = spaceView.spaceAccessType ?: SpaceAccessType.PRIVATE
                    )
                }
        }
    }


    private fun proceedWithNavigationPanelState() {
        viewModelScope.launch {
            combine(
                spaceViewState,
                userPermissions
            ) { spaceView, permission ->
                if (spaceView is SpaceViewState.Success && permission != null) {
                    NavPanelState.fromPermission(
                        permission = permission,
                        forceHome = false,
                        spaceAccess = spaceView.spaceAccessType,
                        spaceUxType = spaceView.spaceUxType
                    )
                } else {
                    NavPanelState.Init
                }
            }.collect {
                navPanelState.value = it
            }
        }
    }

    private fun proceedWithViewStatePipeline() {
        viewModelScope.launch {
            widgetObjectPipeline.collect {
                objectViewState.value = it
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun proceedWithUserPermissions() {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = vmParams.spaceId)
                .collect { permission ->
                    userPermissions.value = permission
                    when (permission) {
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


    private fun proceedWithWidgetContainerPipeline() {
        buildPinnedContainerPipeline()
        buildTypeContainerPipeline()
        buildUnreadContainerPipeline()
    }

    private fun buildPinnedContainerPipeline() {
        viewModelScope.launch {
            pinnedWidgets.map { widgets ->
                // Use section-specific views for cache optimization (avoids circular dependency)
                val currentlyDisplayedViews = pinnedViews.value
                widgets.mapNotNull { widget ->
                    Timber.d("Creating pinned container for widget: ${widget.id} of type ${widget::class.simpleName}")
                    widgetContainerDelegate.createContainer(widget, currentlyDisplayedViews)
                }
            }.collect { containersList ->
                Timber.d("Emitting list of pinned containers: ${containersList.size}")
                pinnedContainers.value = containersList
            }
        }
    }

    private fun buildTypeContainerPipeline() {
        viewModelScope.launch {
            typeWidgets.map { widgets ->
                // Use section-specific views for cache optimization (avoids circular dependency)
                val currentlyDisplayedViews = typeViews.value
                widgets.mapNotNull { widget ->
                    Timber.d("Creating type container for widget: ${widget.id} of type ${widget::class.simpleName}")
                    widgetContainerDelegate.createContainer(widget, currentlyDisplayedViews)
                }
            }.collect { containersList ->
                Timber.d("Emitting list of type containers: ${containersList.size}")
                typeContainers.value = containersList
            }
        }
    }

    private fun buildUnreadContainerPipeline() {
        viewModelScope.launch {
            unreadWidget
                .map { widget ->
                    if (widget != null) {
                        Timber.d("Creating unread container for widget: ${widget.id}")
                        widgetContainerDelegate.createContainer(widget, emptyList())
                    } else {
                        null
                    }
                }
                .collect { container ->
                    Timber.d("Emitting unread container: ${container != null}")
                    unreadContainer.value = container
                }
        }
    }

    /**
     * Applies payload events to the object view state.
     * Only Success states get payload scanning; other states pass through unchanged.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Flow<ObjectViewState>.applyPayloadEvents(
        payloads: Flow<Payload>
    ): Flow<ObjectViewState> {
        return flatMapLatest { state ->
            when (state) {
                is ObjectViewState.Idle,
                is ObjectViewState.Failure,
                is ObjectViewState.Loading -> flowOf(state)

                is ObjectViewState.Success -> {
                    payloads.scan(state) { currentState, payload ->
                        currentState.copy(obj = reduce(state = currentState.obj, event = payload))
                    }
                }
            }
        }
    }

    /**
     * Observes external payload events from the space manager.
     * Merges intercepted events with delegated payloads for the current widget context.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeExternalPayloadEvents(): Flow<Payload> {
        return spaceManager.observe().flatMapLatest { config ->
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
    }

    /**
     * Observes expanded widget IDs with error handling.
     * Returns an empty list as default if fetching fails.
     */
    private fun observeExpandedWidgetIds(): Flow<List<Id>> {
        return userSettingsRepository.getExpandedWidgetIds(vmParams.spaceId)
            .distinctUntilChanged()
            .catch { e ->
                Timber.e(e, "Failed to get expanded widget IDs, using defaults")
                emit(emptyList())
            }
    }

    /**
     * Observes collapsed section IDs with error handling.
     * Returns an empty list as default if fetching fails.
     */
    private fun observeCollapsedSectionIds(): Flow<List<Id>> {
        return userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId)
            .catch { e ->
                Timber.e(e, "Failed to get collapsed section IDs, using defaults")
                emit(emptyList())
            }
    }

    /**
     * Observes both expanded widget IDs and collapsed section IDs as a single data class.
     * This provides a clean way to access both pieces of state together in combine() calls.
     */
    private fun observeWidgetPreferences(): Flow<WidgetPreferences> {
        return combine(
            observeExpandedWidgetIds(),
            observeCollapsedSectionIds()
        ) { expandedIds, collapsedSections ->
            WidgetPreferences(
                expandedWidgetIds = expandedIds,
                collapsedSectionIds = collapsedSections
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun proceedWithObjectViewStatePipeline() {
        val externalChannelEvents = observeExternalPayloadEvents()
        val internalChannelEvents = objectPayloadDispatcher.flow()
        val payloads = merge(externalChannelEvents, internalChannelEvents)
        val widgetPreferences = observeWidgetPreferences()

        viewModelScope.launch {
            combine(
                storeOfObjectTypes.trackChanges(),
                objectViewState.applyPayloadEvents(payloads).distinctUntilChanged(),
                userPermissions,
                widgetPreferences,
                spaceViewSubscriptionContainer.observe(vmParams.spaceId),
                widgetSections
            ) { values ->
                val state = values[1] as ObjectViewState
                val userPermission = values[2] as? SpaceMemberPermissions
                val preferences = values[3] as WidgetPreferences
                val spaceView = values[4] as ObjectWrapper.SpaceView
                val sectionConfig = values[5] as com.anytypeio.anytype.core_models.WidgetSections
                val params = WidgetUiParams(
                    isOwnerOrEditor = userPermission?.isOwnerOrEditor() == true,
                    expandedIds = preferences.expandedWidgetIds.toSet(),
                    collapsedSections = preferences.collapsedSectionIds.toSet()
                )
                if (state is ObjectViewState.Success) {
                    // Build widget sections from the current object state
                    val sections = buildWidgetSections(
                        state = state,
                        params = params,
                        urlBuilder = urlBuilder,
                        storeOfObjectTypes = storeOfObjectTypes,
                        spaceView = spaceView,
                        sectionConfig = sectionConfig
                    )

                    // Initialize active views for all widgets
                    val bundledWidgetActiveViews = state.obj.blocks.parseActiveViews()

                    // Preserve ObjectType active views (not stored in WidgetsObj blocks)
                    val currentActiveViews = widgetActiveViewStateHolder.getActiveViews()
                    val allWidgets = sections.pinnedWidgets + sections.typeWidgets
                    val objectTypeActiveViews = currentActiveViews.filterKeys { widgetId ->
                        allWidgets
                            .any { widget ->
                                widget.id == widgetId && widget.source is Widget.Source.Default &&
                                        (widget.source as Widget.Source.Default).obj.layout == ObjectType.Layout.OBJECT_TYPE
                            }
                    }

                    val combinedActiveViews = bundledWidgetActiveViews + objectTypeActiveViews
                    widgetActiveViewStateHolder.init(combinedActiveViews)

                    // Update expanded IDs from persistence
                    expandedWidgetIds.value = params.expandedIds
                    sections
                } else {
                    null
                }
            }.collect { sections ->
                if (sections != null) {
                     val totalWidgets = sections.pinnedWidgets.size + sections.typeWidgets.size + (if (sections.unreadWidget != null) 1 else 0) + (if (sections.binWidget != null) 1 else 0)
                    Timber.d("Emitting widget sections: pinned=${sections.pinnedWidgets.size}, types=${sections.typeWidgets.size}, unread=${sections.unreadWidget != null}, bin=${sections.binWidget != null}, total=$totalWidgets")

                    pinnedWidgets.value = sections.pinnedWidgets

                    // Check event lock before updating type widgets to prevent race conditions during DnD
                    if (isTypeWidgetEventLockActive()) {
                        Timber.d("DROID-3951, Type widget event lock is active, ignoring incoming type widget update")
                    } else {
                        typeWidgets.value = sections.typeWidgets
                    }

                    unreadWidget.value = sections.unreadWidget
                    binWidget.value = sections.binWidget
                } else {
                    pinnedWidgets.value = emptyList()

                    // Check event lock before clearing type widgets
                    if (isTypeWidgetEventLockActive()) {
                        Timber.d("DROID-3951, Type widget event lock is active, ignoring incoming type widget clear")
                    } else {
                        typeWidgets.value = emptyList()
                    }

                    unreadWidget.value = null
                    binWidget.value = null
                }
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
                currentWidgets.orEmpty().mapNotNull { widget ->
                    when (widget.source) {
                        is Widget.Source.Bundled -> widget.source.id
                        is Widget.Source.Default -> widget.source.id
                        Widget.Source.Other -> null
                    }
                }
            )
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
            val params = CreateWidget.Params(
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
            createWidget.async(params).fold(
                onFailure = {
                    sendToast("Error while creating widget: ${it.message}")
                    Timber.e(it, "Error while creating widget")
                },
                onSuccess = { payload ->
                    Timber.d("Widget created successfully, dispatching payload")
                    objectPayloadDispatcher.send(payload)
                }
            )
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

    fun proceedWithDeletingWidget(widget: Id) {
        Timber.d("Proceeding with widget deletion: $widget")
        viewModelScope.launch {
            val config = spaceManager.getConfig()
            if (config != null) {
                val target = currentWidgets.orEmpty().find { it.id == widget }
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
                val isAutoCreated = currentWidgets?.find { it.id == widget }?.isAutoCreated
                analytics.sendOpenSidebarObjectEvent(
                    isAutoCreated = isAutoCreated
                )
                proceedWithOpeningObject(obj)
            }
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

    fun onWidgetChatClicked() {
        Timber.d("onWidgetChatClicked:")
        viewModelScope.launch {
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

    fun onTypeRowClicked(typeId: Id) {
        Timber.d("onTypeRowClicked: $typeId")
        viewModelScope.launch {
            val type = storeOfObjectTypes.get(typeId)
            if (type != null) {
                proceedWithNavigation(type.navigation(vmParams.spaceId.id))
            } else {
                Timber.w("Type not found for id: $typeId")
            }
        }
    }

    fun onCreateObjectFromTypeRow(typeId: Id) {
        Timber.d("onCreateObjectFromTypeRow: $typeId")
        viewModelScope.launch {
            val type = storeOfObjectTypes.get(typeId)
            if (type != null) {
                val typeKey = TypeKey(type.uniqueKey)
                val templateId = type.defaultTemplateId?.takeIf { it.isNotEmpty() }
                proceedWithCreatingObject(
                    space = vmParams.spaceId,
                    type = typeKey,
                    templateId = templateId
                )
            } else {
                Timber.w("Type not found for id: $typeId")
            }
        }
    }

    fun onWidgetSourceClicked(widgetId: Id) {
        Timber.d("onWidgetSourceClicked:")
        val widget = currentWidgets?.find { it.id == widgetId } ?: return
        Timber.d("Widget source: ${widget.source}")
        when (val source = widget.source) {
            is Widget.Source.Bundled.Favorites -> {
                viewModelScope.sendClickWidgetTitleEvent(
                    analytics = analytics,
                    bundled = source,
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
                    bundled = source
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
                    bundled = source
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
                        widget = widgetId,
                        source = source
                    )
                    viewModelScope.launch {
                        proceedWithOpeningObject(source.obj)
                    }
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

    fun onSeeAllClicked(widgetId: Id, viewId: ViewId?) {
        Timber.d("onSeeAllClicked: widgetId=$widgetId, viewId=$viewId")
        val widget = currentWidgets?.find { it.id == widgetId } ?: return
        val source = widget.source

        if (source is Widget.Source.Default) {
            if (source.obj.isArchived != true) {
                dispatchSelectHomeTabCustomSourceEvent(
                    widget = widgetId,
                    source = source
                )
                // Check if it's a Set, Collection, or Type layout and we have a viewId
                val layout = source.obj.layout
                if (viewId != null) {
                    when (layout) {
                        ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> {
                            viewModelScope.launch {
                                navigate(
                                    Navigation.OpenSet(
                                        ctx = source.obj.id,
                                        space = vmParams.spaceId.id,
                                        view = viewId
                                    )
                                )
                            }
                        }
                        ObjectType.Layout.OBJECT_TYPE -> {
                            viewModelScope.launch {
                                navigate(
                                    Navigation.OpenType(
                                        target = source.obj.id,
                                        space = vmParams.spaceId.id,
                                        view = viewId
                                    )
                                )
                            }
                        }
                        else -> {
                            // Fall back to standard navigation without view
                            viewModelScope.launch {
                                proceedWithOpeningObject(source.obj)
                            }
                        }
                    }
                } else {
                    // Fall back to standard navigation without view
                    viewModelScope.launch {
                        proceedWithOpeningObject(source.obj)
                    }
                }
            } else {
                sendToast("Open bin to restore your archived object")
            }
        } else {
            // For non-default sources, delegate to standard handler
            onWidgetSourceClicked(widgetId)
        }
    }

    fun onBinWidgetClicked() {
        viewModelScope.launch {
            navigation(
                ExpandWidget(
                    subscription = Subscription.Bin,
                    space = vmParams.spaceId.id
                )
            )
        }
    }

    fun onDropDownMenuAction(widget: Id, action: DropDownMenuAction) {
        when (action) {
            DropDownMenuAction.ChangeWidgetType -> {
                proceedWithChangingType(widget)
            }
            DropDownMenuAction.RemoveWidget -> {
                // Check if this is a bundled widget that needs a warning
                val targetWidget = currentWidgets.orEmpty().find { it.id == widget }
                if (targetWidget?.source is Widget.Source.Bundled) {
                    // Show warning modal for bundled widgets
                    pendingBundledWidgetDeletion.value = widget
                } else {
                    // Proceed directly for non-bundled widgets
                    proceedWithDeletingWidget(widget)
                }
            }
            DropDownMenuAction.EmptyBin -> {
                proceedWithEmptyingBin()
            }
            is DropDownMenuAction.CreateObjectOfType -> {
                // Search in both pinned and type sections
                val widgetView = pinnedViews.value.find { it.id == action.widgetId }
                    ?: typeViews.value.find { it.id == action.widgetId }
                if (widgetView == null) {
                    Timber.w("Widget view not found for id: ${action.widgetId}")
                    return
                }
                onCreateWidgetElementClicked(widgetView)
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
        val spaceView = (spaceViewState.value as? SpaceViewState.Success) ?: return
        val chat = spaceView.spaceChatId
        if (chat != null) {
            navigation(
                Navigation.OpenChat(
                    space = vmParams.spaceId.id,
                    ctx = chat
                )
            )
        } else {
            Timber.w("Chat or space not found - not able to open space chat")
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
        val curr = currentWidgets.orEmpty().find { it.id == widget }
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
                                is Widget.Source.Default -> source.obj.layout?.code ?: UNDEFINED_LAYOUT_CODE
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
        val curr = currentWidgets.orEmpty().find { it.id == widget }
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
        is Widget.Link -> ChangeWidgetType.TYPE_LINK
        is Widget.Tree -> ChangeWidgetType.TYPE_TREE
        is Widget.View -> ChangeWidgetType.TYPE_VIEW
        is Widget.List -> {
            if (curr.isCompact)
                ChangeWidgetType.TYPE_COMPACT_LIST
            else
                ChangeWidgetType.TYPE_LIST
        }
        // All-objects widget has link appearance.
        is Widget.AllObjects -> ChangeWidgetType.TYPE_LINK
        is Widget.Chat -> ChangeWidgetType.TYPE_LINK
        is Widget.UnreadChatList -> ChangeWidgetType.TYPE_LINK
        is Widget.Bin -> ChangeWidgetType.UNDEFINED_LAYOUT_CODE
        is Widget.ObjectTypesGroup -> ChangeWidgetType.UNDEFINED_LAYOUT_CODE
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
        val widgetList = currentWidgets ?: emptyList()
        val deletedWidgets = widgetList.filter { widget ->
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

    private suspend fun proceedWithOpeningObject(obj: ObjectWrapper.Basic) {
        // Check if template before routing by layout
        if (obj.isTemplateObject(storeOfObjectTypes) && !obj.layout.isDataView()) {
            obj.getTypeForObjectAndTargetTypeForTemplate(storeOfObjectTypes)?.let { objType ->
                navigate(
                    Navigation.OpenTemplate(
                        template = obj.id,
                        templateTypeId = objType.id,
                        templateTypeKey = objType.uniqueKey,
                        space = vmParams.spaceId.id
                    )
                )
                return
            }
        }
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
                        space = navigation.space,
                        view = null
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

    fun onMovePinned(views: List<WidgetView>, from: Int, to: Int) {
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

    @OptIn(ExperimentalCoroutinesApi::class)
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
            val isAutoCreated = currentWidgets?.find { it.id == target?.id }?.isAutoCreated
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

    fun onBundledWidgetDeletionConfirmed() {
        val widgetId = pendingBundledWidgetDeletion.value
        if (widgetId != null) {
            proceedWithDeletingWidget(widgetId)
            pendingBundledWidgetDeletion.value = null
        }
    }

    fun onBundledWidgetDeletionCanceled() {
        pendingBundledWidgetDeletion.value = null
    }

    private fun dispatchSelectHomeTabCustomSourceEvent(widget: Id, source: Widget.Source) {
        viewModelScope.launch {
            val isAutoCreated = currentWidgets?.find { it.id == widget }?.isAutoCreated
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
                    val isAutoCreated = currentWidgets?.find { it.id == subject.id }?.isAutoCreated
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

    fun onBackClicked() {
        proceedWithCloseOpenObjects()
        viewModelScope.launch {
            val currentSpaceView = _spaceViewState.value
            val (spaceUxType, spaceChatId) = when (currentSpaceView) {
                is SpaceViewState.Success -> {
                    currentSpaceView.spaceUxType to currentSpaceView.spaceChatId
                }
                else -> {
                    // Default to DATA type if space view not loaded
                    SpaceUxType.DATA to null
                }
            }
            commands.emit(
                Command.HandleChatSpaceBackNavigation(
                    spaceUxType = spaceUxType,
                    spaceChatId = spaceChatId
                )
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
        val currentWidgets = currentWidgets.orEmpty()

        // Launch fire-and-forget cleanup coroutine
        // Note: viewModelScope automatically cancels all its coroutines
        // Using injected scope ensures proper lifecycle management
        scope.launch(appCoroutineDispatchers.io) {
            // Best-effort cleanup: never throw past this boundary
            val widgetSubscriptions = currentWidgets.mapNotNull { widget ->
                if (widget.source is Widget.Source.Bundled)
                    widget.source.id
                else
                    widget.id
            }
            Timber.d("Unsubscribing from widgets: $widgetSubscriptions")
            kotlin.runCatching {
                storelessSubscriptionContainer.unsubscribe(
                    subscriptions = widgetSubscriptions + listOf(HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION)
                )
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
            props = Props(
                mapOf(
                    EventsPropertiesKey.route to EventsDictionary.Routes.navigation,
                    EventsPropertiesKey.spaceId to vmParams.spaceId.id
                )
            )
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

    fun onManageSectionsClicked() {
        Timber.d("onManageSectionsClicked")
        viewModelScope.launch {
            commands.emit(Command.OpenManageSections)
        }
    }

    fun onMembersClicked() {
        Timber.d("onMembersClicked")
        viewModelScope.launch {
            commands.emit(ShareSpace(vmParams.spaceId))
        }
    }

    fun onMuteClicked() {
        Timber.d("onMuteClicked")
        viewModelScope.launch {
            val spaceView = spaceViewSubscriptionContainer.get(vmParams.spaceId)
            if (spaceView == null) {
                Timber.w("Space view not found for mute toggle")
                commands.emit(Command.Toast.UnableToChangeNotificationSettings)
                return@launch
            }

            val targetSpaceId = spaceView.targetSpaceId
            if (targetSpaceId == null) {
                Timber.w("Target space ID is null for mute toggle")
                commands.emit(Command.Toast.UnableToChangeNotificationSettings)
                return@launch
            }

            // Determine current mute state and toggle
            val isMuted = NotificationStateCalculator.calculateSpaceNotificationMutedState(spaceView)
            val newMode = if (isMuted) {
                NotificationState.ALL  // Unmute
            } else {
                NotificationState.DISABLE  // Mute
            }

            Timber.d("Toggling notification state: current muted=$isMuted, new mode=$newMode")

            setSpaceNotificationMode.async(
                SetSpaceNotificationMode.Params(
                    spaceViewId = targetSpaceId,
                    mode = newMode
                )
            ).fold(
                onSuccess = {
                    Timber.d("Successfully set notification mode to $newMode")
                    commands.emit(
                        if (newMode == NotificationState.DISABLE) {
                            Command.Toast.SpaceMuted
                        } else {
                            Command.Toast.SpaceUnmuted
                        }
                    )
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to set notification mode")
                    commands.emit(Command.Toast.FailedToChangeNotificationSettings)
                }
            )
        }
    }

    fun onQrCodeClicked() {
        Timber.d("onQrCodeClicked")
        viewModelScope.launch {
            val spaceView = spaceViewSubscriptionContainer.get(vmParams.spaceId)
            if (spaceView != null) {
                val inviteLink = getSpaceInviteLink
                    .async(vmParams.spaceId)
                    .getOrNull()
                    ?.scheme
                if (inviteLink != null) {
                    uiQrCodeState.value = SpaceInvite(
                        link = inviteLink,
                        spaceName = spaceView.name.orEmpty(),
                        icon = spaceView.spaceIcon(urlBuilder)
                    )
                } else {
                    Timber.w("Could not get invite link for QR code")
                    sendToast("Unable to generate QR code")
                }
            }
        }
    }

    fun onCopyInviteLinkClicked() {
        Timber.d("onCopyInviteLinkClicked")
        viewModelScope.launch {
            val inviteLink = getSpaceInviteLink
                .async(vmParams.spaceId)
                .getOrNull()
                ?.scheme
            if (inviteLink != null) {
                val params = CopyInviteLinkToClipboard.Params(inviteLink)
                copyInviteLinkToClipboard.invoke(params)
                    .proceed(
                        failure = {
                            Timber.e(it, "Failed to copy invite link to clipboard")
                            sendToast("Failed to copy invite link")
                        },
                        success = {
                            Timber.d("Invite link copied to clipboard: $inviteLink")
                            sendToast("Invite link copied to clipboard")
                        }
                    )
            } else {
                Timber.w("Could not get invite link to copy")
                sendToast("Unable to copy invite link")
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
                is WidgetView.ChatList -> {
                    handleDefaultWidgetSource(
                        dataViewObjectId = widget.source.id,
                        viewId = widget.tabs.find { it.isSelected }?.id,
                        navigate = true
                    )
                }
                is WidgetView.Tree -> {
                    getDefaultObjectType.async(vmParams.spaceId).getOrNull()?.type?.let { typeKey ->
                        storeOfObjectTypes.getByKey(key = typeKey.key)?.let {
                            onCreateObjectForTreeWidget(
                                type = it,
                                widgetId = widget.id,
                                treeWidgetSourceId = widget.source.id
                            )
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

    /**
     * Creates a new object in a Widget by clicking the "+" button.
     *
     * This method handles TWO distinct use cases:
     * 1. **Layout.SET** - "Query by Type" Widgets [Pinned section]:
     *    The [dataViewSourceObj] is the ObjectType obtained from the Set's `setOf` field via lookup.
     * 2. **Layout.OBJECT_TYPE** - Object Type Widgets [Object Types section]:
     *    The [dataViewSourceObj] IS the ObjectType view itself (no lookup needed).
     *
     * In both cases, the created object will:
     * - Have the type specified by [dataViewSourceObj] (the ObjectType)
     * - Be prefilled with values from active view filters (when filters use permitted conditions,
     *   see [com.anytypeio.anytype.core_models.PermittedConditions])
     * - Use a template with the following priority:
     *   1. Viewer's custom template (if set)
     *   2. ObjectType's default template (if viewer template is not set)
     *   3. No template (if both are null/empty)
     *
     * @param dataViewSourceObj The ObjectType for the new object. Source depends on layout:
     *                          - Layout.SET: The ObjectType from the Set's `setOf` field (obtained via lookup)
     *                          - Layout.OBJECT_TYPE: The widget's source object itself (already the ObjectType)
     * @param viewer The active view/viewer containing filters, template settings, and display configuration
     * @param dv The DataView content with relation links used for proper filter value formatting
     * @param navigate If true, navigates to the created object after successful creation
     */
    private suspend fun proceedWithCreatingDataViewObject(
        dataViewSourceObj: ObjectWrapper.Basic,
        viewer: Block.Content.DataView.Viewer,
        dv: DV,
        navigate: Boolean = false
    ) {
        Timber.d("proceedWithCreatingDataViewObject, dataViewSourceObj: $dataViewSourceObj")
        val prefilled = viewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider
        )
        val type = TypeKey(dataViewSourceObj.uniqueKey ?: VIEW_DEFAULT_OBJECT_TYPE)
        val space = vmParams.spaceId.id
        if (type.key == ObjectTypeIds.CHAT_DERIVED) {
            // Check if chat creation is allowed based on space UX type
            val currentSpaceUxType = (spaceViewState.value as? SpaceViewState.Success)?.spaceUxType
            if (currentSpaceUxType.canCreateAdditionalChats) {
                commands.emit(
                    Command.CreateChatObject(
                        space = SpaceId(space)
                    )
                )
            } else {
                Timber.d("Chat creation not allowed in $currentSpaceUxType space")
            }
        } else {
            val startTime = System.currentTimeMillis()
            createDataViewObject.async(
                params = CreateDataViewObject.Params.SetByType(
                    type = type,
                    filters = viewer.filters,
                    template = resolveTemplateForDataViewObject(
                        viewer = viewer,
                        setOfObject = dataViewSourceObj
                    ),
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
    }

    /**
     * Creates a new object in a "Set by Relation" Widget.
     *
     * This method handles Sets where the `setOf` field points to a Relation object
     * (not an ObjectType). In this case:
     * - The type comes from the viewer's `defaultObjectType` (similar to Collections)
     * - The relation itself is added to the created object with an appropriate default value
     * - Template resolution follows the standard priority (Viewer → ObjectType → null)
     *
     * @param relationObj The Relation object from the Set's `setOf` field
     * @param viewer The active view/viewer containing filters, template settings, and display configuration
     * @param dv The DataView content with relation links used for proper filter value formatting
     * @param navigate If true, navigates to the created object after successful creation
     */
    private suspend fun proceedWithCreatingSetByRelationObject(
        relationObj: ObjectWrapper.Relation,
        viewer: Block.Content.DataView.Viewer,
        dv: DV,
        navigate: Boolean = false
    ) {
        Timber.d("proceedWithCreatingSetByRelationObject, relationObj Id: ${relationObj.id}, relationObj Key: ${relationObj.uniqueKey}")

        // Get type from viewer's defaultObjectType (not from the relation)
        val (defaultObjectType, defaultTemplate) = resolveTypeAndActiveViewTemplate(
            activeView = viewer,
            storeOfObjectTypes = storeOfObjectTypes
        )

        val type = TypeKey(defaultObjectType?.uniqueKey ?: VIEW_DEFAULT_OBJECT_TYPE)

        // Get prefilled data including the relation itself with default value
        val prefilled = viewer.resolveSetByRelationPrefilledObjectData(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            objSetByRelation = relationObj
        )

        val space = vmParams.spaceId.id
        val startTime = System.currentTimeMillis()

        createDataViewObject.async(
            params = CreateDataViewObject.Params.SetByRelation(
                type = type,
                filters = viewer.filters,
                template = defaultTemplate,
                prefilled = prefilled
            ).also {
                Timber.d("Calling SetByRelation with params: $it")
            }
        ).fold(
            onSuccess = { result ->
                Timber.d("Successfully created Set by Relation object with id: ${result.objectId}")
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
                Timber.e(it, "Error while creating Set by Relation object for widget")
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
            dateProvider = dateProvider
        )

        val (defaultObjectType, defaultTemplate) = resolveTypeAndActiveViewTemplate(
            activeView = viewer,
            storeOfObjectTypes = storeOfObjectTypes
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
                        // Check if this is a Set by ObjectType or Set by Relation
                        when (dataViewSourceObj.layout) {
                            ObjectType.Layout.OBJECT_TYPE -> {
                                // Set by Type: setOf points to an ObjectType
                                proceedWithCreatingDataViewObject(
                                    dataViewSourceObj = dataViewSourceObj,
                                    viewer = viewer,
                                    dv = dv,
                                    navigate = navigate
                                )
                            }
                            ObjectType.Layout.RELATION -> {
                                // Set by Relation: setOf points to a Relation
                                proceedWithCreatingSetByRelationObject(
                                    relationObj = ObjectWrapper.Relation(dataViewSourceObj.map),
                                    viewer = viewer,
                                    dv = dv,
                                    navigate = navigate
                                )
                            }
                            else -> {
                                Timber.w("Unsupported setOf layout: ${dataViewSourceObj.layout}")
                            }
                        }
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

    fun onCreateObjectForTreeWidget(
        type: ObjectWrapper.Type,
        widgetId: Id,
        treeWidgetSourceId: Id
    ) {
        viewModelScope.launch {
            val flags = buildList {
                add(InternalFlags.ShouldSelectTemplate)
                add(InternalFlags.ShouldSelectType)
            }
            createObject.async(
                params = CreateObject.Param(
                    space = vmParams.spaceId,
                    type = TypeKey(type.uniqueKey),
                    internalFlags = flags
                )
            ).fold(
                onSuccess = { result ->
                    proceedWithCreatingLinkToNewObject(
                        source = treeWidgetSourceId,
                        result = result,
                        position = Position.BOTTOM
                    )
                    // Ensure the tree widget source remains expanded after creating the object
                    val sourcePath = "$widgetId/$treeWidgetSourceId"
                    val currentExpanded = treeWidgetBranchStateHolder.stream(widgetId).first()
                    if (!currentExpanded.contains(sourcePath)) {
                        treeWidgetBranchStateHolder.onExpand(sourcePath)
                    }
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
        result: CreateObject.Result,
        position: Position = Position.NONE
    ) {
        createBlock.async(
            params = CreateBlock.Params(
                context = source,
                target = "",
                position = position,
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
        
        // Special handling for CHAT_DERIVED: show create chat screen instead of direct creation
        if (objType?.uniqueKey == ObjectTypeIds.CHAT_DERIVED) {
            // Check if chat creation is allowed based on space UX type
            val currentSpaceUxType = (spaceViewState.value as? SpaceViewState.Success)?.spaceUxType
            if (currentSpaceUxType.canCreateAdditionalChats) {
                viewModelScope.launch {
                    commands.emit(Command.CreateChatObject(vmParams.spaceId))
                }
            } else {
                Timber.d("Chat creation not allowed in $currentSpaceUxType space")
            }
            return
        }
        
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
     * Toggles widget collapse state and persists to preferences.
     *
     * This function simply adds/removes the widget ID from the expandedIds set.
     * The actual effect (collapse vs expand) depends on the widget's section type:
     * - Pinned widgets: Adding to set = collapse, Removing from set = expand
     * - Types widgets: Adding to set = expand, Removing from set = collapse
     *
     * See [isWidgetCollapsed] for detailed explanation of the inverted semantics.
     */
    fun onToggleWidgetExpandedState(widgetId: Id) {
        Timber.d("onToggleWidgetExpandedState, widgetId: $widgetId")
        viewModelScope.launch {
            val currentExpanded = expandedWidgetIds.value
            // Simply toggle: if in set, remove it; if not in set, add it
            // The meaning (collapsed vs expanded) depends on widget section type
            val newExpanded = if (currentExpanded.contains(widgetId)) {
                currentExpanded - widgetId
            } else {
                currentExpanded + widgetId
            }

            Timber.d("Toggle widget $widgetId: inSet=${currentExpanded.contains(widgetId)} -> ${newExpanded.contains(widgetId)}, totalInSet=${newExpanded.size}")
            expandedWidgetIds.value = newExpanded
            saveExpandedWidgetState()
        }
    }

    fun onSectionPinnedClicked() {
        viewModelScope.launch {
            val currentCollapsedSections = userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId).first().toSet()
            val isCurrentlyCollapsed = currentCollapsedSections.contains(SECTION_PINNED)

            val newCollapsedSections = if (isCurrentlyCollapsed) {
                currentCollapsedSections.minus(SECTION_PINNED)
            } else {
                currentCollapsedSections.plus(SECTION_PINNED)
            }

            userSettingsRepository.setCollapsedSectionIds(vmParams.spaceId, newCollapsedSections.toList())
        }
    }

    fun onSectionTypesClicked() {
        viewModelScope.launch {
            val currentCollapsedSections = userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId).first().toSet()
            val isCurrentlyCollapsed = currentCollapsedSections.contains(SECTION_OBJECT_TYPE)

            val newCollapsedSections = if (isCurrentlyCollapsed) {
                currentCollapsedSections.minus(SECTION_OBJECT_TYPE)
            } else {
                currentCollapsedSections.plus(SECTION_OBJECT_TYPE)
            }

            userSettingsRepository.setCollapsedSectionIds(vmParams.spaceId, newCollapsedSections.toList())
        }
    }

    fun onSectionUnreadClicked() {
        viewModelScope.launch {
            val currentCollapsedSections = userSettingsRepository.getCollapsedSectionIds(vmParams.spaceId).first().toSet()
            val isCurrentlyCollapsed = currentCollapsedSections.contains(SECTION_UNREAD)

            val newCollapsedSections = if (isCurrentlyCollapsed) {
                currentCollapsedSections.minus(SECTION_UNREAD)
            } else {
                currentCollapsedSections.plus(SECTION_UNREAD)
            }

            userSettingsRepository.setCollapsedSectionIds(vmParams.spaceId, newCollapsedSections.toList())
        }
    }

    /**
     * Determines if a widget should be collapsed due to its section being collapsed
     */
    private fun isWidgetInCollapsedSection(widget: Widget, collapsedSections: Set<Id>): Boolean {
        return when {
            widget.sectionType == SectionType.PINNED -> collapsedSections.contains(Widget.Source.SECTION_PINNED)
            widget.sectionType == SectionType.UNREAD -> collapsedSections.contains(Widget.Source.SECTION_UNREAD)
            widget.sectionType == SectionType.TYPES -> collapsedSections.contains(Widget.Source.SECTION_OBJECT_TYPE)
            else -> false
        }
    }

    /**
     * Determines if a widget should be collapsed based on section type defaults and user preferences.
     *
     * Product logic:
     * - Pinned section widgets: Expanded by default
     * - Unread section widgets: Expanded by default
     * - Object Types section widgets: Collapsed by default
     *
     * IMPORTANT: expandedIds represents "widgets toggled from their default state", NOT "expanded widgets".
     * The semantics are INVERTED based on section type:
     *
     * Pinned Section (default: expanded):
     *   - Widget ID NOT in set → EXPANDED (using default)
     *   - Widget ID IN set → COLLAPSED (user toggled from default)
     *
     * Unread Section (default: expanded):
     *   - Widget ID NOT in set → EXPANDED (using default)
     *   - Widget ID IN set → COLLAPSED (user toggled from default)
     *
     * Types Section (default: collapsed):
     *   - Widget ID NOT in set → COLLAPSED (using default)
     *   - Widget ID IN set → EXPANDED (user toggled from default)
     *
     * Example scenario:
     *   expandedIds = [widgetA, widgetB]
     *   - widgetA (Pinned) → COLLAPSED (in set = toggled from default expanded)
     *   - widgetB (Types) → EXPANDED (in set = toggled from default collapsed)
     *   - widgetC (Pinned) → EXPANDED (not in set = using default)
     *   - widgetD (Types) → COLLAPSED (not in set = using default)
     */
    private fun isWidgetCollapsed(
        widget: Widget,
        expandedIds: Set<Id>,
        collapsedSections: Set<Id>
    ): Boolean {
        // First check if the entire section is collapsed
        if (isWidgetInCollapsedSection(widget, collapsedSections)) {
            return true
        }

        return when (widget.sectionType) {
            SectionType.PINNED -> {
                // Pinned widgets are expanded by default
                // Being in expandedIds means user explicitly collapsed it
                expandedIds.contains(widget.id)
            }
            SectionType.UNREAD -> {
                // Unread widgets are expanded by default
                // Being in expandedIds means user explicitly collapsed it
                expandedIds.contains(widget.id)
            }
            SectionType.TYPES -> {
                // Object Types widgets are collapsed by default
                // Being in expandedIds means user explicitly expanded it
                !expandedIds.contains(widget.id)
            }
            SectionType.RECENTLY_EDITED -> {
                // Recently edited widgets behavior - TODO: Define default behavior
                expandedIds.contains(widget.id)
            }
            SectionType.NONE -> {
                true
            }
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

    //region Type Widget Drag and Drop
    /**
     * Called when the order of type widgets changes during a drag operation.
     *
     * This method updates the UI state immediately to reflect the new order,
     * allowing for instant visual feedback during the drag operation.
     *
     * @param fromWidgetId The ID of the type widget being dragged from.
     * @param toWidgetId The ID of the type widget being dragged to.
     */
    fun onTypeWidgetOrderChanged(fromWidgetId: String?, toWidgetId: String?) {
        Timber.d("DROID-3965, onTypeWidgetOrderChanged: from=$fromWidgetId, to=$toWidgetId")

        if (fromWidgetId.isNullOrEmpty() || toWidgetId.isNullOrEmpty()) {
            Timber.d("DROID-3965, onTypeWidgetOrderChanged: One of the IDs is null or empty, ignoring")
            return
        }

        // Mark that we're starting a drag operation if we haven't already
        if (pendingTypeWidgetOrder == null) {
            Timber.d("DROID-3965, Starting type widget drag operation")
        }

        // Filter to only include actual type widgets (exclude sections, bin, etc.)
        val actualTypeWidgets = typeViews.value

        val from = actualTypeWidgets.indexOfFirst { it.id == fromWidgetId }
        val to = actualTypeWidgets.indexOfFirst { it.id == toWidgetId }

        if (from == -1 || to == -1 || from == to) {
            Timber.d("DROID-3965, onTypeWidgetOrderChanged: Invalid indices (from=$from, to=$to), ignoring")
            return
        }

        // Store the pending order for persistence on drag end (only actual type widget IDs)
        val currentOrder = actualTypeWidgets.map { it.id }.toMutableList()
        val movedItem = currentOrder.removeAt(from)
        currentOrder.add(to, movedItem)

        pendingTypeWidgetOrder = currentOrder

        Timber.d("DROID-3965, onTypeWidgetOrderChanged: New pending order: ${currentOrder.map { it.takeLast(4) + "..." }}")
    }

    /**
     * Called when the drag operation for type widgets ends.
     *
     * This method persists the order changes made during the drag operation
     * using the UpdateObjectTypesOrderIds use case and resets the local state.
     */
    fun onTypeWidgetDragEnd() {
        Timber.d("DROID-3965, onTypeWidgetDragEnd called")

        // Persist the order changes made during the drag operation
        pendingTypeWidgetOrder?.let { newOrder ->
            viewModelScope.launch {
                Timber.d("DROID-3965, Persisting type widget order: ${newOrder.map { it.takeLast(4) + "..." }}")

                // Store the current order for potential rollback
                val previousOrder = typeWidgets.value.toList()

                // Optimistically update typeWidgets immediately to keep UI in sync
                val reorderedWidgets = newOrder.mapNotNull { id ->
                    typeWidgets.value.find { it.id == id }
                }
                typeWidgets.value = reorderedWidgets
                Timber.d("DROID-4113, Optimistically updated typeWidgets to new order")

                // Activate event lock before sending to middleware to prevent race conditions
                activateTypeWidgetEventLock()

                updateObjectTypesOrderIds.async(
                    UpdateObjectTypesOrderIds.Params(
                        spaceId = vmParams.spaceId,
                        orderedIds = newOrder
                    )
                ).fold(
                    onFailure = { error ->
                        Timber.e(error, "DROID-3965, Failed to reorder type widgets: $newOrder")
                        // Rollback to previous order
                        typeWidgets.value = previousOrder
                        Timber.d("DROID-4113, Rolled back typeWidgets to previous order")
                        clearTypeWidgetDragState()
                    },
                    onSuccess = { finalOrder ->
                        Timber.d("DROID-3965, Successfully reordered type widgets with final order: $finalOrder")
                        clearTypeWidgetDragState()
                    }
                )
            }
        } ?: run {
            Timber.d("DROID-3965, No pending type widget order changes, clearing drag state")
            clearTypeWidgetDragState()
        }
    }

    /**
     * Activates the event lock for type widgets to prevent race conditions.
     * Should be called before sending a drag-and-drop order change to middleware.
     */
    private fun activateTypeWidgetEventLock() {
        typeWidgetEventLockTimestamp = System.currentTimeMillis()
        Timber.d("DROID-3951, Type widget event lock activated at $typeWidgetEventLockTimestamp")
    }

    /**
     * Checks if the type widget event lock is currently active.
     * The lock is active if it was set within the last TYPE_WIDGET_EVENT_LOCK_DURATION_MS milliseconds.
     */
    private fun isTypeWidgetEventLockActive(): Boolean {
        val lockTimestamp = typeWidgetEventLockTimestamp ?: return false
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lockTimestamp
        val isActive = elapsedTime < TYPE_WIDGET_EVENT_LOCK_DURATION_MS

        if (!isActive) {
            Timber.d("DROID-3951, Type widget event lock expired (elapsed: ${elapsedTime}ms)")
            typeWidgetEventLockTimestamp = null
        }

        return isActive
    }

    /**
     * Clears the drag-and-drop state for type widgets.
     *
     * This method is called after the middleware responds to a drag-and-drop operation.
     * It clears the pending order but DOES NOT clear the event lock timestamp.
     *
     * The lock must remain active for its full duration (TYPE_WIDGET_EVENT_LOCK_DURATION_MS)
     * because events can arrive AFTER the middleware response. These post-response events
     * need to be ignored to prevent overwriting the optimistic UI update.
     *
     * The lock will expire automatically via isTypeWidgetEventLockActive() checks.
     */
    private fun clearTypeWidgetDragState() {
        Timber.d("DROID-3965, Clearing type widget drag state (lock remains active)")
        pendingTypeWidgetOrder = null
        // NOTE: We do NOT clear typeWidgetEventLockTimestamp here
        // The lock must remain active for the full duration to block post-response events
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
        data class OpenType(val target: Id, val space: Id, val view: Id? = null) : Navigation()
        data class OpenOwnerOrEditorSpaceSettings(val space: Id) : Navigation()
        data class OpenBookmarkUrl(val url: String) : Navigation() // Added for opening bookmark URLs from widgets
        data class OpenTemplate(
            val template: Id,
            val templateTypeId: Id,
            val templateTypeKey: Key,
            val space: Id
        ) : Navigation()
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

    sealed class SpaceViewState {
        data object Init : SpaceViewState()
        data class Success(
            val spaceIcon: SpaceIconView,
            val spaceName: String,
            val membersCount: Int,
            val spaceChatId: Id? = null,
            val spaceAccessType: SpaceAccessType,
            val spaceUxType: SpaceUxType
        ) : SpaceViewState()

        data class Failure(val e: Throwable) : SpaceViewState()
    }

    // Delegate for creating widget containers
    private val widgetContainerDelegate: WidgetContainerDelegate by lazy {
        WidgetContainerDelegateImpl(
            spaceId = vmParams.spaceId,
            chatPreviews = chatPreviews,
            participantContainer = participantContainer,
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            notificationPermissionManager = notificationPermissionManager,
            fieldParser = fieldParser,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            treeWidgetBranchStateHolder = treeWidgetBranchStateHolder,
            expandedWidgetIds = expandedWidgetIds,
            userSettingsRepository = userSettingsRepository,
            isSessionActive = isSessionActive,
            urlBuilder = urlBuilder,
            objectWatcher = objectWatcher,
            getSpaceView = getSpaceView,
            storeOfObjectTypes = storeOfObjectTypes,
            hasInstanceContainer = hasInstanceContainer,
            getObject = getObject,
            coverImageHashProvider = coverImageHashProvider,
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            stringResourceProvider = stringResourceProvider,
            dispatchers = appCoroutineDispatchers,
            observeCurrentWidgetView = ::observeCurrentWidgetView,
            isWidgetCollapsed = ::isWidgetCollapsed
        )
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
        private val hasInstanceContainer: HasInstanceOfObjectTypeSubscriptionContainer,
        private val storeOfRelations: StoreOfRelations,
        private val objectWatcher: ObjectWatcher,
        private val setWidgetActiveView: SetWidgetActiveView,
        private val spaceManager: SpaceManager,
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
        private val participantContainer: ParticipantSubscriptionContainer,
        private val notificationPermissionManager: NotificationPermissionManager,
        private val copyInviteLinkToClipboard: CopyInviteLinkToClipboard,
        private val userRepo: UserSettingsRepository,
        private val observeWidgetSections: ObserveWidgetSections,
        private val scope: CoroutineScope,
        private val stringResourceProvider : StringResourceProvider,
        private val updateObjectTypesOrderIds: UpdateObjectTypesOrderIds,
        private val setSpaceNotificationMode: SetSpaceNotificationMode
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
            hasInstanceContainer = hasInstanceContainer,
            storeOfRelations = storeOfRelations,
            objectWatcher = objectWatcher,
            setWidgetActiveView = setWidgetActiveView,
            spaceManager = spaceManager,
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
            participantContainer = participantContainer,
            notificationPermissionManager = notificationPermissionManager,
            copyInviteLinkToClipboard = copyInviteLinkToClipboard,
            userSettingsRepository = userRepo,
            observeWidgetSections = observeWidgetSections,
            scope = scope,
            stringResourceProvider = stringResourceProvider,
            updateObjectTypesOrderIds = updateObjectTypesOrderIds,
            setSpaceNotificationMode = setSpaceNotificationMode
        ) as T
    }

    companion object {
        const val HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION = "subscription.home-screen.profile-object"

        // Duration in milliseconds to lock type widget event processing after a drag operation
        // This prevents incoming middleware events from overwriting optimistic UI updates
        private const val TYPE_WIDGET_EVENT_LOCK_DURATION_MS = 1500L
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

/**
 * Contains user preferences for widget display state.
 * Used to combine both expanded widget IDs and collapsed section IDs into a single flow.
 *
 * @property expandedWidgetIds List of widget IDs that have been toggled from their default state
 * @property collapsedSectionIds List of section IDs that are currently collapsed
 */
data class WidgetPreferences(
    val expandedWidgetIds: List<Id>,
    val collapsedSectionIds: List<Id>
)

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

    data class HandleChatSpaceBackNavigation(
        val spaceUxType: SpaceUxType,
        val spaceChatId: Id?
    ) : Command()

    data class ShareInviteLink(val link: String) : Command()
    data class CreateNewType(val space: Id) : Command()
    data class CreateChatObject(val space: SpaceId) : Command()
    data object OpenManageSections : Command()
    
    sealed class Toast : Command() {
        data object SpaceMuted : Toast()
        data object SpaceUnmuted : Toast()
        data object UnableToChangeNotificationSettings : Toast()
        data object FailedToChangeNotificationSettings : Toast()
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

fun ObjectWrapper.Type.navigation(
    spaceId: Id,
): OpenObjectNavigation {
    if (!isValid) return OpenObjectNavigation.NonValidObject
    return OpenObjectNavigation.OpenType(
        target = id,
        space = spaceId
    )
}

data class HomeScreenVmParams(val spaceId: SpaceId)

const val MAX_TYPE_COUNT_FOR_APP_ACTIONS = 4
const val MAX_PINNED_TYPE_COUNT_FOR_APP_ACTIONS = 3

/**
 * Hides all chat unread badges when the unread section is expanded.
 * This prevents badge duplication across sections.
 */
private fun hideAllChatBadgesInWidgets(
    widgets: List<WidgetView>
): List<WidgetView> {
    return widgets.map { widget ->
        when (widget) {
            is WidgetView.ChatList -> {
                widget.copy(
                    elements = widget.elements.map { element ->
                        if (element is WidgetView.SetOfObjects.Element.Chat) {
                            // Hide badges by setting counters to null
                            element.copy(counter = null)
                        } else {
                            element
                        }
                    }
                )
            }
            is WidgetView.SetOfObjects -> {
                widget.copy(
                    elements = widget.elements.map { element ->
                        if (element is WidgetView.SetOfObjects.Element.Chat) {
                            // Hide badges by setting counters to null
                            element.copy(counter = null)
                        } else {
                            element
                        }
                    }
                )
            }
            is WidgetView.ListOfObjects -> {
                widget.copy(
                    elements = widget.elements.map { element ->
                        if (element is WidgetView.ListOfObjects.Element.Chat) {
                            // Hide badges by setting counters to null
                            element.copy(counter = null)
                        } else {
                            element
                        }
                    }
                )
            }
            // Other widget types don't have chat elements
            else -> widget
        }
    }
}
