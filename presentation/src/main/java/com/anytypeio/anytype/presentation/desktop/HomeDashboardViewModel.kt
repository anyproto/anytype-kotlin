package com.anytypeio.anytype.presentation.desktop

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.PAGE_CREATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.PROP_STYLE
import com.anytypeio.anytype.analytics.base.EventsDictionary.SCREEN_PROFILE
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.`object`.ObjectTypes
import com.anytypeio.anytype.domain.`object`.ObjectWrapper
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.dashboard.interactor.*
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.desktop.HomeDashboardStateMachine.Interactor
import com.anytypeio.anytype.presentation.desktop.HomeDashboardStateMachine.State
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.settings.EditorSettings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import com.anytypeio.anytype.presentation.desktop.HomeDashboardStateMachine as Machine

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
    private val searchArchivedObjects: SearchArchivedObjects,
    private val searchRecentObjects: SearchRecentObjects,
    private val searchInboxObjects: SearchInboxObjects,
    private val searchObjectSets: SearchObjectSets
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

    val archived = MutableStateFlow(emptyList<DashboardView.Document>())
    val recent = MutableStateFlow(emptyList<DashboardView>())
    val inbox = MutableStateFlow(emptyList<DashboardView>())
    val sets = MutableStateFlow(emptyList<DashboardView>())

    private val objectTypes : List<ObjectType>
    get() = stateData.value?.objectTypes ?: emptyList()

    private val views : List<DashboardView>
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
        proceedWithGettingAccount()
        proceedWithOpeningHomeDashboard()
    }

    fun onAddNewDocumentClicked() {
        val startTime = System.currentTimeMillis()
        createPage.invoke(viewModelScope, CreatePage.Params.insideDashboard()) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while creating a new page") },
                fnR = { id ->
                    val middle = System.currentTimeMillis()
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        startTime = startTime,
                        middleTime = middle,
                        renderTime = middle,
                        eventName = PAGE_CREATE,
                        props = Props(mapOf(PROP_STYLE to Block.Content.Page.Style.EMPTY))
                    )
                    machine.onEvents(listOf(Machine.Event.OnFinishedCreatingPage))
                    proceedWithOpeningDocument(id)
                }
            )
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
                AppNavigation.Command.OpenPage(
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
        if (!isLoading) {
            val view = when(tab) {
                TAB.FAVOURITE -> views.find { it is DashboardView.Document && it.target == target }
                TAB.RECENT -> recent.value.find { it is DashboardView.Document && it.target == target }
                TAB.INBOX -> inbox.value.find { it is DashboardView.Document && it.target == target }
                TAB.ARCHIVE -> archived.value.find { it.target == target }
                else -> null
            }
            if (view is DashboardView.Document && supportedLayouts.contains(view.layout)) {
                if (view.type != ObjectTypes.TEMPLATE) {
                    proceedWithOpeningDocument(target)
                } else {
                    toast("Can't open a template on Android. Coming soon")
                }
            } else {
                toast("Currently unsupported layout on Android")
            }
        }
        else {
            toast("This object is still syncing.")
        }
    }

    fun onAvatarClicked() {
        if (isProfileNavigationEnabled.value) {
            proceedWithOpeningDocument(profile)
        } else {
            toast("Profile is not ready yet. Please, try again later.")
        }
    }

    fun onArchivedClicked(target: Id) {
        navigateToArchive(target)
    }

    fun onObjectSetClicked(target: Id) {
        Timber.d("onObjectSetClicked: $target")
        proceedWithOpeningObjectSet(target)
    }

    fun onProfileClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = SCREEN_PROFILE
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenProfile))
    }

    fun onPageNavigationClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_NAVIGATION
        )
        navigation.postValue(
            EventWrapper(
                AppNavigation.Command.OpenPageNavigationScreen(
                    target = ctx
                )
            )
        )
    }

    fun onPageSearchClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_SEARCH
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenPageSearch))
    }

    private fun proceedWithObjectSearch() {
        viewModelScope.launch {
            searchArchivedObjects(Unit).process(
                success = { objects ->
                    archived.value = objects.map { ObjectWrapper.Basic(it) }.map { obj ->
                        val oType = stateData.value?.let { state ->
                            state.objectTypes.find { type -> type.url == obj.type.firstOrNull() }
                        }
                        DashboardView.Document(
                            id = obj.id,
                            target = obj.id,
                            title = obj.name,
                            isArchived = true,
                            isLoading = false,
                            emoji = obj.iconEmoji,
                            image = obj.iconImage,
                            type = obj.type.firstOrNull(),
                            typeName = oType?.name,
                            layout = obj.layout,
                            done = obj.done
                        )
                    }
                },
                failure = { Timber.e(it, "Error while searching for archived objects") }
            )
        }
        viewModelScope.launch {
            searchRecentObjects(Unit).process(
                success = { objects -> Timber.d("Found ${objects.size} recent objects")
                    recent.value = objects.map { ObjectWrapper.Basic(it) }.map { obj ->
                        val oType = stateData.value?.let { state ->
                            state.objectTypes.find { type -> type.url == obj.type.firstOrNull() }
                        }
                        if (oType?.layout == ObjectType.Layout.SET) {
                            DashboardView.ObjectSet(
                                id = obj.id,
                                target = obj.id,
                                title = obj.name,
                                isArchived = obj.isArchived ?: false,
                                isLoading = false,
                                emoji = obj.iconEmoji
                            )
                        } else {
                            DashboardView.Document(
                                id = obj.id,
                                target = obj.id,
                                title = obj.name,
                                isArchived = obj.isArchived ?: false,
                                isLoading = false,
                                emoji = obj.iconEmoji,
                                image = obj.iconImage,
                                type = obj.type.firstOrNull(),
                                typeName = oType?.name,
                                layout = obj.layout,
                                done = obj.done
                            )
                        }
                    }
                },
                failure = { Timber.e(it, "Error while searching for recent objects") }
            )
        }
        viewModelScope.launch {
            searchInboxObjects(Unit).process(
                success = { objects ->
                    inbox.value = objects.map { ObjectWrapper.Basic(it) }.map { obj ->
                        val oType = stateData.value?.let { state ->
                            state.objectTypes.find { type -> type.url == obj.type.firstOrNull() }
                        }
                        DashboardView.Document(
                            id = obj.id,
                            target = obj.id,
                            title = obj.name,
                            isArchived = obj.isArchived ?: false,
                            isLoading = false,
                            emoji = obj.iconEmoji,
                            image = obj.iconImage,
                            type = obj.type.firstOrNull(),
                            typeName = oType?.name,
                            layout = obj.layout,
                            done = obj.done
                        )
                    }
                },
                failure = { Timber.e(it, "Error while searching for inbox objects") }
            )
        }
        viewModelScope.launch {
            searchObjectSets(Unit).process(
                success = { objects ->
                    sets.value = objects.map { ObjectWrapper.Basic(it) }.map { obj ->
                        DashboardView.ObjectSet(
                            id = obj.id,
                            target = obj.id,
                            title = obj.name,
                            isArchived = obj.isArchived ?: false,
                            isLoading = false,
                            emoji = obj.iconEmoji
                        )
                    }
                },
                failure = { Timber.e(it, "Error while searching for sets") }
            )
        }
    }

    fun onCreateNewObjectSetClicked() {
        closeDashboard(viewModelScope, CloseDashboard.Param.home()) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while closing a dashobard") },
                fnR = { navigate(EventWrapper(AppNavigation.Command.OpenCreateSetScreen(ctx))) }
            )
        }
    }

    private fun toast(msg: String) {
        viewModelScope.launch { toasts.emit(msg) }
    }

    fun onResume() {}

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

    companion object {
        val supportedLayouts = listOf(
            ObjectType.Layout.BASIC,
            ObjectType.Layout.TODO,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.FILE
        )
    }

    enum class TAB { FAVOURITE, RECENT, INBOX, SETS, ARCHIVE }
}
