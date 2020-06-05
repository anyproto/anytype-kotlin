package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.withLatestFrom
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.block.interactor.DragAndDrop
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.config.GetDebugSettings
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.BuildConfig
import com.agileburo.anytype.presentation.desktop.HomeDashboardStateMachine.Interactor
import com.agileburo.anytype.presentation.desktop.HomeDashboardStateMachine.State
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.presentation.settings.EditorSettings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import com.agileburo.anytype.presentation.desktop.HomeDashboardStateMachine as Machine

class HomeDashboardViewModel(
    private val getCurrentAccount: GetCurrentAccount,
    private val openDashboard: OpenDashboard,
    private val closeDashboard: CloseDashboard,
    private val createPage: CreatePage,
    private val getConfig: GetConfig,
    private val dragAndDrop: DragAndDrop,
    private val interceptEvents: InterceptEvents,
    private val eventConverter: HomeDashboardEventConverter,
    private val getDebugSettings: GetDebugSettings
) : ViewStateViewModel<State>(),
    HomeDashboardEventConverter by eventConverter,
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val machine = Interactor(scope = viewModelScope)

    private val movementChannel = Channel<Movement>()
    private val movementChanges = movementChannel.consumeAsFlow()
    private val dropChannel = Channel<String>()
    private val dropChanges = dropChannel.consumeAsFlow()

    private val _profile = MutableLiveData<ProfileView>()
    val profile: LiveData<ProfileView> = _profile

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

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
            .onEach { Timber.d("New events: $it") }
            .onEach { events -> processEvents(events) }
            .launchIn(viewModelScope)
    }

    private fun processEvents(events: List<Event>) = events.forEach {
        convert(it)?.let { result -> machine.onEvent(result) }
    }

    private fun proceedWithGettingConfig() {
        getConfig(viewModelScope, Unit) { result ->
            result.either(
                fnR = { config ->
                    startInterceptingEvents(context = config.home)
                    processDragAndDrop(context = config.home)
                },
                fnL = { Timber.e(it, "Error while getting config") }
            )
        }
    }

    private fun proceedWithGettingAccount() {
        getCurrentAccount(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while getting account") },
                fnR = { account ->
                    Timber.d("account: $account")
                    _profile.postValue(
                        ProfileView(
                            name = account.name,
                            avatar = account.avatar
                        )
                    )
                }
            )
        }
    }

    private fun processDragAndDrop(context: String) {
        viewModelScope.launch {
            dropChanges
                .withLatestFrom(movementChanges) { a, b -> Pair(a, b) }
                .mapLatest { (subject, movement) ->
                    DragAndDrop.Params(
                        context = context,
                        targetContext = context,
                        position = movement.direction,
                        blockIds = listOf(subject),
                        targetId = movement.target
                    )
                }
                .collect { param ->
                    dragAndDrop(viewModelScope, param) { result ->
                        result.either(
                            fnL = { Timber.e(it, "Error while DND for: $param") },
                            fnR = { Timber.d("Successfull DND for: $param") }
                        )
                    }
                }
        }
    }

    private fun proceedWithOpeningHomeDashboard() {

        machine.onEvent(Machine.Event.OnDashboardLoadingStarted)

        Timber.d("Opening home dashboard")

        viewModelScope.launch {
            openDashboard(params = null).either(
                fnR = { payload -> processEvents(payload.events) },
                fnL = { Timber.e(it, "Error while opening home dashboard") }
            )
        }
    }

    fun onViewCreated() {
        proceedWithGettingAccount()
        proceedWithOpeningHomeDashboard()
    }

    fun onAddNewDocumentClicked() {
        createPage.invoke(viewModelScope, CreatePage.Params.insideDashboard()) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while creating a new page") },
                fnR = { id ->
                    machine.onEvent(Machine.Event.OnFinishedCreatingPage)
                    proceedWithOpeningDocument(id)
                }
            )
        }.also {
            machine.onEvent(Machine.Event.OnStartedCreatingPage)
        }
    }

    /**
     * @param alteredViews set of views in order altered by a block dragging action
     * @param from position of the block being dragged
     * @param to target position
     */
    fun onItemMoved(
        alteredViews: List<DashboardView>,
        from: Int,
        to: Int
    ) {
        viewModelScope.launch {
            movementChannel.send(
                Movement(
                    direction = if (from < to) Position.BOTTOM else Position.TOP,
                    subject = (alteredViews[to] as DashboardView.Document).id,
                    target = (alteredViews[from] as DashboardView.Document).id
                )
            )
        }
    }

    fun onItemDropped(view: DashboardView) {
        viewModelScope.launch {
            dropChannel.send((view as DashboardView.Document).id)
        }
    }

    private fun proceedWithOpeningDocument(id: String) {
        closeDashboard(viewModelScope, CloseDashboard.Param.home()) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while closing a dashobard") },
                fnR = { proceedToPage(id) }
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

    private fun navigateToPage(id: String, editorSettings: EditorSettings? = null) =
        navigation.postValue(
            EventWrapper(
                AppNavigation.Command.OpenPage(
                    id = id,
                    editorSettings = editorSettings
                )
            )
        )

    private fun getEditorSettingsAndOpenPage(id: String) =
        viewModelScope.launch {
            getDebugSettings(Unit).proceed(
                failure = { Timber.e(it, "Error getting debug settings") },
                success = { navigateToPage(id, it.toView()) }
            )
        }

    fun onDocumentClicked(target: Id) {
        proceedWithOpeningDocument(target)
    }

    fun onProfileClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenProfile))
    }

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
}
