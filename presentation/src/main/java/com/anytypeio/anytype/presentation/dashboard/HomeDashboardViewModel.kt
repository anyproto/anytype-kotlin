package com.anytypeio.anytype.presentation.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.Routes.tabFavorites
import com.anytypeio.anytype.analytics.base.EventsDictionary.deletionWarningShow
import com.anytypeio.anytype.analytics.base.EventsDictionary.reorderObjects
import com.anytypeio.anytype.analytics.base.EventsDictionary.searchScreenShow
import com.anytypeio.anytype.analytics.base.EventsDictionary.selectHomeTab
import com.anytypeio.anytype.analytics.base.EventsDictionary.settingsShow
import com.anytypeio.anytype.analytics.base.EventsDictionary.showHome
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds.TEMPLATE
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.dashboard.interactor.CloseDashboard
import com.anytypeio.anytype.domain.dashboard.interactor.OpenDashboard
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DeleteObjectsOld
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchivedOld
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.Interactor
import com.anytypeio.anytype.presentation.extension.getTypeName
import com.anytypeio.anytype.presentation.extension.mapToFavorites
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRemoveObjects
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRestoreFromBin
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.Subscriptions
import com.anytypeio.anytype.presentation.settings.EditorSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine as Machine

class HomeDashboardViewModel(
    private val getProfile: GetProfile,
    private val openDashboard: OpenDashboard,
    private val closeDashboard: CloseDashboard,
    private val getConfig: GetConfig,
    private val move: Move,
    private val interceptEvents: InterceptEvents,
    private val eventConverter: HomeDashboardEventConverter,
    private val getDebugSettings: GetDebugSettings,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val setObjectListIsArchived: SetObjectListIsArchivedOld,
    private val deleteObjects: DeleteObjectsOld,
    private val objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer,
    private val cancelSearchSubscription: CancelSearchSubscription,
    private val objectStore: ObjectStore,
    private val createObject: CreateObject,
    private val workspaceManager: WorkspaceManager,
    private val favoriteObjectStateMachine: Interactor,
    private val featureToggles: FeatureToggles
) : ViewModel(),
    HomeDashboardEventConverter by eventConverter,
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    val subscriptions = mutableListOf<Job>()

    val toasts = MutableSharedFlow<String>()

    private val movementChannel = Channel<Movement>()
    private val movementChanges = movementChannel.consumeAsFlow()
    private val dropChannel = Channel<String>()
    private val dropChanges = dropChannel.consumeAsFlow()

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private var ctx: Id = ""

    val tabs = MutableStateFlow(listOf(TAB.FAVOURITE, TAB.RECENT, TAB.SETS, TAB.BIN))

    val archived = MutableStateFlow(emptyList<DashboardView.Document>())
    val recent = MutableStateFlow(emptyList<DashboardView>())
    val favorites = MutableStateFlow(emptyList<DashboardView>())
    val sets = MutableStateFlow(emptyList<DashboardView>())
    val shared = MutableStateFlow(emptyList<DashboardView>())

    val mode = MutableStateFlow(Mode.DEFAULT)
    val count = MutableStateFlow(0)

    val alerts = MutableSharedFlow<Alert>(replay = 0)

    val isDeletionInProgress = MutableStateFlow(false)

    val profile = MutableStateFlow<ViewState<ObjectWrapper.Basic>>(ViewState.Init)

    private val onFavoriteDataSetChanged = MutableSharedFlow<Unit>()

    init {
        proceedWithGettingConfig()
        proceedWithFavoriteTabPipeline()
        viewModelScope.launch {
            getProfile.observe(
                subscription = Subscriptions.SUBSCRIPTION_PROFILE,
                keys = listOf(Relations.ID, Relations.NAME, Relations.ICON_IMAGE)
            ).catch {
                Timber.e(it, "Error while observing profile on dashboard")
                toast("Could not load profile: ${it.message ?: "Unknown error"}")
            }.collectLatest {
                profile.value = ViewState.Success(it)
            }
        }
    }

    private fun proceedWithFavoriteTabPipeline() {
        viewModelScope.launch {
            combine(
                favoriteObjectStateMachine.state().distinctUntilChanged(),
                onFavoriteDataSetChanged
            ) { state, _ ->
                state.childrenIdsList.mapToFavorites(
                    blocks = state.blocks,
                    objectStore = objectStore,
                    urlBuilder = urlBuilder
                )
            }.collectLatest {
                favorites.value = it
            }
        }
    }

    private fun startInterceptingEvents(context: String) {
        interceptEvents
            .build(InterceptEvents.Params(context = context))
            .onEach { Timber.d("New events on home dashboard: $it") }
            .onEach { events -> processEvents(events) }
            .launchIn(viewModelScope)
    }

    private suspend fun processEvents(events: List<Event>) =
        events.mapNotNull { convert(it) }.let { result -> favoriteObjectStateMachine.onEvents(result) }

    private fun proceedWithGettingConfig() {
        getConfig(viewModelScope, Unit) { result ->
            result.either(
                fnR = { config ->
                    ctx = config.home
                    startInterceptingEvents(context = config.home)
                    processDragAndDrop(context = config.home)
                },
                fnL = { Timber.e(it, "Error while getting config") }
            )
        }
    }

    private fun processDragAndDrop(context: String) {
        viewModelScope.launch {
            dropChanges
                .withLatestFrom(movementChanges) { a, b -> Pair(a, b) }
                .mapLatest { (subject, movement) ->
                    Move.Params(
                        context = context,
                        targetContext = context,
                        position = movement.direction,
                        blockIds = listOf(subject),
                        targetId = movement.target
                    )
                }
                .collect { param ->
                    move(viewModelScope, param) { result ->
                        result.either(
                            fnL = { Timber.e(it, "Error while DND for: $param") },
                            fnR = {
                                sendEvent(
                                    analytics = analytics,
                                    eventName = reorderObjects,
                                    props = Props(
                                        mapOf(
                                            EventsPropertiesKey.route to EventsDictionary.Routes.home
                                        )
                                    )
                                )
                                Timber.d("successful DND for: $param")
                            }
                        )
                    }
                }
        }
    }

    private fun proceedWithOpeningHomeDashboard() {
        subscriptions += viewModelScope.launch {
            objectSearchSubscriptionContainer.observe(
                subscription = Subscriptions.SUBSCRIPTION_FAVORITES,
                keys = DEFAULT_KEYS + Relations.LAST_MODIFIED_DATE,
                filters = ObjectSearchConstants.filterTabFavorites(
                    workspaceId = workspaceManager.getCurrentWorkspace()
                )
            ).collect {
                onFavoriteDataSetChanged.emit(Unit)
            }
        }
        viewModelScope.launch {
            favoriteObjectStateMachine.onEvents(listOf(Machine.Event.OnDashboardLoadingStarted))
            val startTime = System.currentTimeMillis()
            var middleTime = 0L
            openDashboard.execute(Unit).fold(
                onSuccess = { payload ->
                    middleTime = System.currentTimeMillis()
                    processEvents(payload.events)
                },
                onFailure = { Timber.e(it, "Error while opening home dashboard") }
            )
            sendEvent(
                analytics = analytics,
                eventName = showHome,
                startTime = startTime,
                middleTime = middleTime,
                renderTime = System.currentTimeMillis(),
                props = Props(mapOf(EventsPropertiesKey.tab to tabFavorites))
            )
        }
    }

    fun onStart() {
        proceedWithOpeningHomeDashboard()
        proceedWithObjectSearchWithSubscriptions()
    }

    fun onStop() {
        viewModelScope.launch {
            subscriptions.clear()
            cancelSearchSubscription(
                CancelSearchSubscription.Params(
                    subscriptions = listOf(
                        Subscriptions.SUBSCRIPTION_FAVORITES,
                        Subscriptions.SUBSCRIPTION_ARCHIVED,
                        Subscriptions.SUBSCRIPTION_RECENT,
                        Subscriptions.SUBSCRIPTION_SETS
                    )
                )
            ).process(
                failure = { Timber.e(it, "Failed to cancel tabs subscriptions") },
                success = { Timber.d("Sucessfully canceled subscriptions. Store contains ${objectStore.size} objects") }
            )
        }
    }

    fun onAddNewDocumentClicked() {
        Timber.d("onAddNewDocumentClicked, ")
        subscriptions += viewModelScope.launch {
            createObject.execute(CreateObject.Param(type = null)).fold(
                onSuccess = { result ->
                    proceedWithOpeningDocument(result.objectId)
                },
                onFailure = { e -> Timber.e(e, "Error while creating a new object") }
            )
        }
    }

    fun onLibraryClicked() {
        Timber.d("onLibraryClicked")
        if (featureToggles.isLibraryEnabled) {
            navigation.postValue(EventWrapper(AppNavigation.Command.OpenLibrary))
        } else {
            toast("Coming soon")
        }
    }

    /**
     * @param views set of views in order altered by a block dragging action
     * @param from position of the block being dragged
     * @param to target position
     */
    fun onItemMoved(
        views: List<DashboardView>,
        from: Int,
        to: Int
    ) {
        Timber.d("onItemMoved, views:[$views], from:[$from], to:[$to]")
        viewModelScope.launch {
            val direction = if (from < to) Position.BOTTOM else Position.TOP
            val subject = views[to].id
            val target = if (direction == Position.TOP) views[to.inc()].id else views[to.dec()].id
            movementChannel.send(
                Movement(
                    direction = direction,
                    subject = subject,
                    target = target
                )
            )
        }
    }

    fun onItemDropped(view: DashboardView) {
        Timber.d("onItemDropped, view:[$view]")
        viewModelScope.launch { dropChannel.send(view.id) }
    }

    private fun proceedWithOpeningDocument(id: String) {
        closeDashboard(viewModelScope, CloseDashboard.Param.home()) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while closing a dashobard") },
                fnR = { proceedToPage(id) }
            )
        }
    }

    private fun proceedWithOpeningObjectSet(id: String) {
        closeDashboard(viewModelScope, CloseDashboard.Param.home()) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while closing a dashobard") },
                fnR = { navigateToObjectSet(id) }
            )
        }
    }

    fun onNavigationDeepLink(pageId: String) {
        Timber.d("onNavigationDeepLink, pageId:[$pageId]")
        closeDashboard(viewModelScope, CloseDashboard.Param.home()) { result ->
            result.either(
                fnL = { e ->
                    Timber.e(e, "Error while closing a dashobard, dashboard is not opened")
                    proceedToPage(pageId)
                },
                fnR = { proceedToPage(pageId) }
            )
        }
    }

    private fun proceedToPage(id: String) {
        if (BuildConfig.DEBUG) {
            getEditorSettingsAndOpenPage(id)
        } else {
            navigateToPage(id)
        }
    }

    private fun navigateToPage(id: String, editorSettings: EditorSettings? = null) {
        navigation.postValue(
            EventWrapper(
                AppNavigation.Command.OpenObject(
                    id = id,
                    editorSettings = editorSettings
                )
            )
        )
    }

    private fun navigateToArchive(target: Id) {
        navigation.postValue(
            EventWrapper(
                AppNavigation.Command.OpenArchive(
                    target = target
                )
            )
        )
    }

    private fun navigateToObjectSet(target: Id) {
        navigation.postValue(
            EventWrapper(AppNavigation.Command.OpenObjectSet(target = target))
        )
    }

    private fun getEditorSettingsAndOpenPage(id: String) =
        viewModelScope.launch {
            getDebugSettings(Unit).proceed(
                failure = { Timber.e(it, "Error getting debug settings") },
                success = { navigateToPage(id, it.toView()) }
            )
        }

    fun onTabObjectClicked(target: Id, isLoading: Boolean, tab: TAB = TAB.FAVOURITE) {
        Timber.d("onTabObjectClicked, target:[$target], isLoading:[$isLoading], tab:[$tab]")
        if (!isLoading) {
            if (tab == TAB.BIN) {
                proceedWithClickInArchiveTab(target)
            } else {
                val view = when (tab) {
                    TAB.FAVOURITE -> favorites.value.find { it is DashboardView.Document && it.target == target }
                    TAB.RECENT -> recent.value.find { it is DashboardView.Document && it.target == target }
                    else -> null
                }
                if (view is DashboardView.Document && SupportedLayouts.layouts.contains(view.layout)) {
                    if (view.type != TEMPLATE) {
                        if (view.layout == ObjectType.Layout.SET) {
                            proceedWithOpeningObjectSet(target)
                        } else {
                            proceedWithOpeningDocument(target)
                        }
                    } else {
                        toast("Can't open a template on Android. Coming soon")
                    }
                } else {
                    toast("Currently unsupported layout on Android")
                }
            }
        } else {
            toast("This object is still syncing.")
        }
    }

    fun onAvatarClicked() {
        Timber.d("onAvatarClicked, ")
        profile.value.let { state ->
            when (state) {
                is ViewState.Success -> {
                    proceedWithOpeningDocument(state.data.id)
                }
                else -> {
                    toast("Profile is not ready yet. Please, try again later.")
                }
            }
        }
    }

    fun onArchivedClicked(target: Id) {
        Timber.d("onArchivedClicked, target:[$target]")
        navigateToArchive(target)
    }

    fun onObjectSetClicked(target: Id) {
        Timber.d("onObjectSetClicked: target:[$target]")
        proceedWithOpeningObjectSet(target)
    }

    fun onSettingsClicked() {
        Timber.d("onSettingsClicked, ")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = settingsShow
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenSettings))
    }

    fun onPageSearchClicked() {
        Timber.d("onPageSearchClicked, ")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = searchScreenShow
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenPageSearch))
    }

    private fun proceedWithObjectSearchWithSubscriptions() {
        subscriptions += viewModelScope.launch {
            objectSearchSubscriptionContainer.observe(
                subscription = Subscriptions.SUBSCRIPTION_RECENT,
                keys = DEFAULT_KEYS + Relations.LAST_MODIFIED_DATE,
                filters = ObjectSearchConstants.filterTabRecent(
                    workspaceId = workspaceManager.getCurrentWorkspace()
                ),
                sorts = ObjectSearchConstants.sortTabRecent,
                limit = ObjectSearchConstants.limitTabRecent,
                offset = 0
            ).catch { Timber.e(it, "Error while collecting search results") }
                .collectLatest { objects ->
                    Timber.d("Results updated: $objects")
                    recent.value = objects.objects
                        .mapNotNull { target ->
                            val obj = objectStore.get(target)
                            if (obj != null) {
                                val layout = obj.layout
                                if (layout == ObjectType.Layout.SET) {
                                    DashboardView.ObjectSet(
                                        id = obj.id,
                                        target = obj.id,
                                        title = obj.getProperName(),
                                        isArchived = obj.isArchived ?: false,
                                        isLoading = false,
                                        icon = ObjectIcon.from(
                                            obj = obj,
                                            layout = obj.layout,
                                            builder = urlBuilder
                                        )
                                    )
                                } else {
                                    DashboardView.Document(
                                        id = obj.id,
                                        target = obj.id,
                                        title = obj.getProperName(),
                                        isArchived = obj.isArchived ?: false,
                                        isLoading = false,
                                        emoji = obj.iconEmoji,
                                        image = obj.iconImage?.let { urlBuilder.thumbnail(it) },
                                        type = obj.type.firstOrNull(),
                                        typeName = obj.getTypeName(objectStore),
                                        layout = obj.layout,
                                        done = obj.done,
                                        icon = ObjectIcon.from(
                                            obj = obj,
                                            layout = obj.layout,
                                            builder = urlBuilder
                                        )
                                    )
                                }
                            } else {
                                null
                            }
                        }
                }
        }
        subscriptions += viewModelScope.launch {
            objectSearchSubscriptionContainer.observe(
                subscription = Subscriptions.SUBSCRIPTION_ARCHIVED,
                keys = DEFAULT_KEYS,
                filters = ObjectSearchConstants.filterTabArchive(
                    workspaceId = workspaceManager.getCurrentWorkspace()
                ),
                sorts = ObjectSearchConstants.sortTabArchive,
                limit = 0,
                offset = 0
            ).catch { Timber.e(it, "Error while collecting search results") }
                .collectLatest { objects ->
                    archived.value = objects.objects
                        .mapNotNull { target ->
                            val obj = objectStore.get(target)
                            if (obj != null) {
                                val layout = obj.layout
                                DashboardView.Document(
                                    id = obj.id,
                                    target = obj.id,
                                    title = obj.getProperName(),
                                    isArchived = true,
                                    isLoading = false,
                                    emoji = obj.iconEmoji,
                                    image = obj.iconImage?.let { urlBuilder.thumbnail(it) },
                                    type = obj.type.firstOrNull(),
                                    typeName = obj.getTypeName(objectStore),
                                    layout = obj.layout,
                                    done = obj.done,
                                    icon = ObjectIcon.from(
                                        obj = obj,
                                        layout = layout,
                                        builder = urlBuilder
                                    )
                                )
                            } else {
                                null
                            }
                        }
                }
        }
        subscriptions += viewModelScope.launch {
            objectSearchSubscriptionContainer.observe(
                subscription = Subscriptions.SUBSCRIPTION_SETS,
                keys = DEFAULT_KEYS,
                filters = ObjectSearchConstants.filterTabSets(
                    workspaceId = workspaceManager.getCurrentWorkspace()
                ),
                sorts = ObjectSearchConstants.sortTabSets,
                limit = 0,
                offset = 0
            ).catch { Timber.e(it, "Error while collecting search results") }
                .collectLatest { objects ->
                    sets.value = objects.objects
                        .mapNotNull { target ->
                            val obj = objectStore.get(target)
                            if (obj != null) {
                                DashboardView.ObjectSet(
                                    id = obj.id,
                                    target = obj.id,
                                    title = obj.getProperName(),
                                    isArchived = obj.isArchived ?: false,
                                    isLoading = false,
                                    icon = ObjectIcon.getEditorLinkToObjectIcon(
                                        obj = obj,
                                        layout = obj.layout,
                                        builder = urlBuilder
                                    )
                                )
                            } else {
                                null
                            }
                        }
                }
        }
    }

    private fun toast(msg: String) {
        viewModelScope.launch { toasts.emit(msg) }
    }

    fun sendTabEvent(tab: CharSequence?) {
        Timber.d("sendTabEvent, tab:[$tab]")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = selectHomeTab,
            props = Props(mapOf(EventsPropertiesKey.tab to tab))
        )
    }

    //region BIN SELECTION

    private fun proceedWithClickInArchiveTab(target: Id) {
        if (mode.value == Mode.DEFAULT) mode.value = Mode.SELECTION
        proceedWithTogglingSelectionForTarget(target)
    }

    private fun proceedWithTogglingSelectionForTarget(target: Id) {
        val updatedViews = archived.value.map { obj ->
            if (obj.id == target) {
                obj.copy(isSelected = !obj.isSelected)
            } else {
                obj
            }
        }
        val updatedCount = updatedViews.count { it.isSelected }

        archived.value = updatedViews
        count.value = updatedCount

        if (updatedCount == 0) {
            mode.value = Mode.DEFAULT
        }
    }

    fun onSelectAllClicked() {
        archived.value = archived.value.map { obj -> obj.copy(isSelected = true) }
        count.value = archived.value.size
    }

    fun onCancelSelectionClicked() {
        mode.value = Mode.DEFAULT
        archived.value = archived.value.map { obj -> obj.copy(isSelected = false) }
        count.value = 0
    }

    fun onPutBackClicked() {
        viewModelScope.launch {
            mode.value = Mode.DEFAULT
            val ids = archived.value.filter { it.isSelected }.map { it.id }
            setObjectListIsArchived(
                SetObjectListIsArchivedOld.Params(
                    targets = ids,
                    isArchived = false
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while restoring objects from archive")
                },
                success = {
                    analytics.sendAnalyticsRestoreFromBin(count = ids.size)
                    Timber.d("Object successfully archived")
                }
            )
        }
    }

    fun onDeleteObjectsClicked() {
        viewModelScope.launch {
            sendEvent(
                analytics = analytics,
                eventName = deletionWarningShow
            )
            alerts.emit(
                Alert.Delete(
                    count = archived.value.count { it.isSelected }
                )
            )
        }
    }

    fun onDeletionAccepted() {
        proceedWithObjectDeletion()
    }

    private fun proceedWithObjectDeletion() {
        viewModelScope.launch {
            mode.value = Mode.DEFAULT
            isDeletionInProgress.value = true
            val ids = archived.value.filter { it.isSelected }.map { it.id }
            deleteObjects(
                DeleteObjectsOld.Params(
                    targets = ids
                )
            ).process(
                failure = { e ->
                    isDeletionInProgress.value = false
                    Timber.e(e, "Error while deleting objects")
                },
                success = {
                    analytics.sendAnalyticsRemoveObjects(ids.size)
                    isDeletionInProgress.value = false
                }
            )
        }
    }

    //endregion

    //region SUBSCRIPTION FAVORITES
    private fun subscribeToFavoriteObjects() {
        subscriptions += viewModelScope.launch {
            objectSearchSubscriptionContainer.observe(
                subscription = Subscriptions.SUBSCRIPTION_FAVORITES,
                keys = DEFAULT_KEYS + Relations.LAST_MODIFIED_DATE,
                filters = ObjectSearchConstants.filterTabFavorites(
                    workspaceId = workspaceManager.getCurrentWorkspace()
                )
            ).collect { onFavoriteDataSetChanged.emit(Unit) }
        }
    }
    //endregion

    /**
     * Represents movements of blocks during block dragging action.
     * @param subject id of the block being dragged
     * @param target id of the target of dragging action
     * @param direction movement direction
     * @see Position
     */
    data class Movement(
        val subject: String,
        val target: String,
        val direction: Position
    )

    enum class TAB { FAVOURITE, RECENT, SETS, SHARED, BIN }

    enum class Mode { DEFAULT, SELECTION }

    sealed class Alert {
        data class Delete(val count: Int) : Alert()
    }
}

val DEFAULT_KEYS = ObjectSearchConstants.defaultKeys + Relations.DONE
