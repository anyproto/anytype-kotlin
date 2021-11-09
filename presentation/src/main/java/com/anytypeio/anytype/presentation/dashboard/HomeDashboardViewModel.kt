package com.anytypeio.anytype.presentation.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.PAGE_CREATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.PROP_IS_DRAFT
import com.anytypeio.anytype.analytics.base.EventsDictionary.PROP_TYPE
import com.anytypeio.anytype.analytics.base.EventsDictionary.SCREEN_DASHBOARD
import com.anytypeio.anytype.analytics.base.EventsDictionary.SCREEN_PROFILE
import com.anytypeio.anytype.analytics.base.EventsDictionary.TAB_ARCHIVE
import com.anytypeio.anytype.analytics.base.EventsDictionary.TAB_FAVORITES
import com.anytypeio.anytype.analytics.base.EventsDictionary.TAB_INBOX
import com.anytypeio.anytype.analytics.base.EventsDictionary.TAB_RECENT
import com.anytypeio.anytype.analytics.base.EventsDictionary.TAB_SETS
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.FlavourConfigProvider
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.dashboard.interactor.CloseDashboard
import com.anytypeio.anytype.domain.dashboard.interactor.OpenDashboard
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.Interactor
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.State
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.settings.EditorSettings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine as Machine

class HomeDashboardViewModel(
    private val getProfile: GetProfile,
    private val openDashboard: OpenDashboard,
    private val closeDashboard: CloseDashboard,
    private val createPage: CreatePage,
    private val getConfig: GetConfig,
    private val move: Move,
    private val interceptEvents: InterceptEvents,
    private val eventConverter: HomeDashboardEventConverter,
    private val getDebugSettings: GetDebugSettings,
    private val analytics: Analytics,
    private val searchObjects: SearchObjects,
    private val getDefaultEditorType: GetDefaultEditorType,
    private val urlBuilder: UrlBuilder,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val deleteObjects: DeleteObjects,
    private val flavourConfigProvider: FlavourConfigProvider
) : ViewStateViewModel<State>(),
    HomeDashboardEventConverter by eventConverter,
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val isProfileNavigationEnabled = MutableStateFlow(false)
    val toasts = MutableSharedFlow<String>()

    private val machine = Interactor(scope = viewModelScope)

    private val movementChannel = Channel<Movement>()
    private val movementChanges = movementChannel.consumeAsFlow()
    private val dropChannel = Channel<String>()
    private val dropChanges = dropChannel.consumeAsFlow()

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private var ctx: Id = ""
    private var profile: Id = ""

    val tabs = MutableStateFlow(listOf(TAB.FAVOURITE, TAB.RECENT, TAB.SETS, TAB.ARCHIVE))

    val archived = MutableStateFlow(emptyList<DashboardView.Document>())
    val recent = MutableStateFlow(emptyList<DashboardView>())
    val inbox = MutableStateFlow(emptyList<DashboardView>())
    val sets = MutableStateFlow(emptyList<DashboardView>())
    val shared = MutableStateFlow(emptyList<DashboardView>())

    val mode = MutableStateFlow(Mode.DEFAULT)
    val count = MutableStateFlow(0)

    val alerts = MutableSharedFlow<Alert>(replay = 0)

    private val views: List<DashboardView>
        get() = stateData.value?.blocks ?: emptyList()

    init {
        startProcessingState()
        proceedWithGettingConfig()
    }

    private fun startProcessingState() {
        viewModelScope.launch { machine.state().collect { stateData.postValue(it) } }
    }

    private fun startInterceptingEvents(context: String) {
        interceptEvents
            .build(InterceptEvents.Params(context = context))
            .onEach { Timber.d("New events on home dashboard: $it") }
            .onEach { events -> processEvents(events) }
            .launchIn(viewModelScope)
    }

    private fun processEvents(events: List<Event>) =
        events.mapNotNull { convert(it) }.let { result -> machine.onEvents(result) }

    private fun proceedWithGettingConfig() {
        getConfig(viewModelScope, Unit) { result ->
            result.either(
                fnR = { config ->
                    ctx = config.home
                    profile = config.profile
                    isProfileNavigationEnabled.value = true
                    startInterceptingEvents(context = config.home)
                    processDragAndDrop(context = config.home)
                },
                fnL = { Timber.e(it, "Error while getting config") }
            )
        }
    }

    private fun proceedWithGettingAccount() {
        getProfile(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while getting account") },
                fnR = { payload -> processEvents(payload.events) }
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
                            fnR = { Timber.d("Successfull DND for: $param") }
                        )
                    }
                }
        }
    }

    private fun proceedWithOpeningHomeDashboard() {

        machine.onEvents(listOf(Machine.Event.OnDashboardLoadingStarted))

        Timber.d("Opening home dashboard")

        viewModelScope.launch {
            openDashboard(params = null).either(
                fnR = { payload -> processEvents(payload.events).also { proceedWithObjectSearch() } },
                fnL = { Timber.e(it, "Error while opening home dashboard") }
            )
        }
    }

    fun onViewCreated() {
        Timber.d("onViewCreated, ")
        proceedWithGettingAccount()
        proceedWithOpeningHomeDashboard()
    }

    fun onAddNewDocumentClicked() {
        Timber.d("onAddNewDocumentClicked, ")
        proceedWithGettingDefaultPageType()
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
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_DOCUMENT
        )
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
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_ARCHIVE
        )
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
            if (tab == TAB.ARCHIVE) {
                proceedWithClickInArchiveTab(target)
            } else {
                val view = when (tab) {
                    TAB.FAVOURITE -> views.find { it is DashboardView.Document && it.target == target }
                    TAB.RECENT -> recent.value.find { it is DashboardView.Document && it.target == target }
                    else -> null
                }
                if (view is DashboardView.Document && SupportedLayouts.layouts.contains(view.layout)) {
                    if (view.type != ObjectType.TEMPLATE_URL) {
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
        if (isProfileNavigationEnabled.value) {
            viewModelScope.sendEvent(
                analytics = analytics,
                eventName = SCREEN_PROFILE
            )
            proceedWithOpeningDocument(profile)
        } else {
            toast("Profile is not ready yet. Please, try again later.")
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
            eventName = EventsDictionary.POPUP_SETTINGS
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenProfile))
    }

    fun onPageSearchClicked() {
        Timber.d("onPageSearchClicked, ")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_SEARCH
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenPageSearch))
    }

    private fun proceedWithObjectSearch() {
        proceedWithArchivedObjectSearch()
        proceedWithRecentObjectSearch()
        proceedWithSetsObjectSearch()
        if (flavourConfigProvider.get().enableSpaces == true) {
            tabs.value = listOf(TAB.FAVOURITE, TAB.RECENT, TAB.SETS, TAB.SHARED, TAB.ARCHIVE)
            proceedWithSharedObjectsSearch()
        }
    }

    private fun proceedWithArchivedObjectSearch() {
        viewModelScope.launch {
            val params = SearchObjects.Params(
                filters = ObjectSearchConstants.filterTabArchive,
                sorts = ObjectSearchConstants.sortTabArchive
            )
            searchObjects(params).process(
                success = { objects ->
                    archived.value = objects
                        .map { obj ->
                            val layout = obj.layout
                            val oType = stateData.value?.findOTypeById(obj.type)
                            DashboardView.Document(
                                id = obj.id,
                                target = obj.id,
                                title = obj.getProperName(),
                                isArchived = true,
                                isLoading = false,
                                emoji = obj.iconEmoji,
                                image = obj.iconImage,
                                type = obj.type.firstOrNull(),
                                typeName = oType?.name,
                                layout = obj.layout,
                                done = obj.done,
                                icon = ObjectIcon.from(
                                    obj = obj,
                                    layout = layout,
                                    builder = urlBuilder
                                )
                            )
                        }
                },
                failure = { Timber.e(it, "Error while searching for archived objects") }
            )
        }
    }

    private fun proceedWithRecentObjectSearch() {
        viewModelScope.launch {
            val params = SearchObjects.Params(
                filters = ObjectSearchConstants.filterTabHistory,
                sorts = ObjectSearchConstants.sortTabHistory,
                limit = ObjectSearchConstants.limitTabHistory
            )
            searchObjects(params).process(
                success = { objects ->
                    recent.value = objects
                        .map { obj ->
                            val oType = stateData.value?.findOTypeById(obj.type)
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
                                    image = obj.iconImage,
                                    type = obj.type.firstOrNull(),
                                    typeName = oType?.name,
                                    layout = obj.layout,
                                    done = obj.done,
                                    icon = ObjectIcon.from(
                                        obj = obj,
                                        layout = obj.layout,
                                        builder = urlBuilder
                                    )
                                )
                            }
                        }
                },
                failure = { Timber.e(it, "Error while searching for recent objects") }
            )
        }
    }

    private fun proceedWithSharedObjectsSearch() {
        viewModelScope.launch {
            val params = SearchObjects.Params(
                filters = ObjectSearchConstants.filterTabShared,
                sorts = ObjectSearchConstants.sortTabShared
            )
            searchObjects(params).process(
                success = { objects ->
                    shared.value = objects
                        .map { obj ->
                            val oType = stateData.value?.findOTypeById(obj.type)
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
                                    image = obj.iconImage,
                                    type = obj.type.firstOrNull(),
                                    typeName = oType?.name,
                                    layout = obj.layout,
                                    done = obj.done,
                                    icon = ObjectIcon.from(
                                        obj = obj,
                                        layout = obj.layout,
                                        builder = urlBuilder
                                    )
                                )
                            }
                        }
                },
                failure = { Timber.e(it, "Error while searching for recent objects") }
            )
        }
    }

    private fun proceedWithSetsObjectSearch() {
        viewModelScope.launch {
            val params = SearchObjects.Params(
                filters = ObjectSearchConstants.filterTabSets,
                sorts = ObjectSearchConstants.sortTabSets
            )
            searchObjects(params).process(
                success = { objects ->
                    sets.value = objects
                        .map { obj ->
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
                    }
                },
                failure = { Timber.e(it, "Error while searching for sets") }
            )
        }
    }

    private fun toast(msg: String) {
        viewModelScope.launch { toasts.emit(msg) }
    }

    fun onResume() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = SCREEN_DASHBOARD
        )
    }

    fun sendTabEvent(tab: CharSequence?) {
        Timber.d("sendTabEvent, tab:[$tab]")
        if (tab != null) {
            val eventName = listOf(TAB_FAVORITES, TAB_RECENT, TAB_INBOX, TAB_SETS, TAB_ARCHIVE)
                .firstOrNull { it.startsWith(tab, true) }
            if (eventName != null) {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = eventName
                )
            }
        }
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
            setObjectListIsArchived(
                SetObjectListIsArchived.Params(
                    targets = archived.value.filter { it.isSelected }.map { it.id },
                    isArchived = false
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while restoring objects from archive")
                    proceedWithArchivedObjectSearch()
                },
                success = {
                    proceedWithArchivedObjectSearch()
                }
            )
        }
    }

    fun onDeleteObjectsClicked() {
        viewModelScope.launch {
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
            deleteObjects(
                DeleteObjects.Params(
                    targets = archived.value.filter { it.isSelected }.map { it.id }
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while deleting objects")
                    proceedWithArchivedObjectSearch()
                },
                success = {
                    proceedWithArchivedObjectSearch()
                }
            )
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

        enum class TAB { FAVOURITE, RECENT, SETS, SHARED, ARCHIVE }

    //region CREATE PAGE
    private fun proceedWithGettingDefaultPageType() {
        viewModelScope.launch {
            getDefaultEditorType.invoke(Unit).proceed(
                failure = { Timber.e(it, "Error while getting default page type") },
                success = { response -> proceedWithCreatePage(type = response.type) }
            )
        }
    }

    private fun proceedWithCreatePage(type: String?) {
        val startTime = System.currentTimeMillis()
        val isDraft = true
        val params = CreatePage.Params(
            ctx = null,
            isDraft = isDraft,
            type = type,
            emoji = null
        )
        createPage.invoke(viewModelScope, params) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while creating a new page") },
                fnR = { id ->
                    val middle = System.currentTimeMillis()
                    val props = Props(mapOf(PROP_TYPE to type, PROP_IS_DRAFT to isDraft))
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        startTime = startTime,
                        middleTime = middle,
                        renderTime = middle,
                        eventName = PAGE_CREATE,
                        props = props
                    )
                    machine.onEvents(listOf(Machine.Event.OnFinishedCreatingPage))
                    proceedWithOpeningDocument(id)
                }
            )
        }
    }
    //endregion

    enum class Mode { DEFAULT, SELECTION }

    sealed class Alert {
        data class Delete(val count: Int) : Alert()
    }
}
