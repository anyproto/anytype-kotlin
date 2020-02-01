package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.withLatestFrom
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.block.interactor.DragAndDrop
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.ObserveHomeDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.event.interactor.ObserveEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel.Machine.*
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.profile.ProfileView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeDashboardViewModel(
    private val loadImage: LoadImage,
    private val getCurrentAccount: GetCurrentAccount,
    private val openDashboard: OpenDashboard,
    private val closeDashboard: CloseDashboard,
    private val createPage: CreatePage,
    private val observeHomeDashboard: ObserveHomeDashboard,
    private val getConfig: GetConfig,
    private val dragAndDrop: DragAndDrop,
    private val observeEvents: ObserveEvents
) : ViewStateViewModel<State>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val machine = Interactor(scope = viewModelScope)

    private val movementChannel = Channel<Movement>()
    private val movementChanges = movementChannel.consumeAsFlow()
    private val dropChannel = Channel<String>()
    private val dropChanges = dropChannel.consumeAsFlow()

    private val _profile = MutableLiveData<ProfileView>()
    val profile: LiveData<ProfileView> = _profile
    private val _image = MutableLiveData<ByteArray>()
    val image: LiveData<ByteArray> = _image

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    init {
        startProcessingState()
        startObservingEvents()
        proceedWithGettingConfig()
    }

    private fun startProcessingState() {
        viewModelScope.launch { machine.state().collect { stateData.postValue(it) } }
    }

    private fun startObservingEvents() {
        observeEvents
            .build()
            .onEach { Timber.d("New event: $it") }
            .onEach { event ->
                if (event is Event.Command.UpdateStructure)
                    machine.onEvent(Machine.Event.OnStructureUpdated(event.children))
                else if (event is Event.Command.AddBlock)
                    machine.onEvent(Machine.Event.OnBlocksAdded(event.blocks))
            }
            .launchIn(viewModelScope)
    }

    private fun proceedWithGettingConfig() {
        getConfig.invoke(viewModelScope, Unit) { result ->
            result.either(
                fnR = {
                    startObservingHomeDashboard(id = it.homeDashboardId)
                    processDragAndDrop(context = it.homeDashboardId)
                },
                fnL = { Timber.e(it, "Error while getting config") }
            )
        }
    }

    private fun startObservingHomeDashboard(id: String) {
        observeHomeDashboard
            .build(ObserveHomeDashboard.Param(id))
            .onEach { machine.onEvent(Machine.Event.OnDashboardLoaded(it)) }
            .launchIn(viewModelScope)
    }

    private fun proceedWithGettingAccount() {
        getCurrentAccount.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while getting account") },
                fnR = { account ->
                    _profile.postValue(ProfileView(name = account.name))
                    loadAvatarImage(account)
                }
            )
        }
    }

    private fun processDragAndDrop(context: String) {
        viewModelScope.launch {
            dropChanges
                .withLatestFrom(movementChanges) { a, b -> Pair(a, b) }
                .onEach { Timber.d("Dnd request: $it") }
                .map { (subject, movement) ->
                    DragAndDrop.Params(
                        context = context,
                        targetContext = context,
                        position = movement.direction,
                        blockIds = listOf(subject),
                        targetId = movement.target
                    )
                }
                .collect { param ->
                    dragAndDrop.invoke(this, param) { result ->
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
        openDashboard.invoke(viewModelScope, null) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while opening home dashboard") },
                fnR = { Timber.d("Home dashboard opened") }
            )
        }
    }

    private fun loadAvatarImage(account: Account) {
        account.avatar?.let { image ->
            loadImage.invoke(
                scope = viewModelScope,
                params = LoadImage.Param(
                    id = image.id
                )
            ) { result ->
                result.either(
                    fnL = { Timber.e(it, "Error while loading image") },
                    fnR = { blob -> _image.postValue(blob) }
                )
            }
        } ?: Timber.d("User does not have any avatar")
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
                    navigateToPage(id)
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

    private fun navigateToPage(id: String) {
        closeDashboard.invoke(viewModelScope, CloseDashboard.Param.home()) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while closing a dashobard") },
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.OpenPage(id))) }
            )
        }
    }

    fun onDocumentClicked(id: String) {
        navigateToPage(id)
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

    /**
     * State machine for this view model consisting of [Interactor], [State], [Event] and [Reducer]
     * It reduces [Event] to the immutable [State] by applying [Reducer] fuction.
     * This [State] then will be rendered.
     */
    sealed class Machine {

        class Interactor(
            private val scope: CoroutineScope,
            private val reducer: Reducer = Reducer(),
            private val channel: Channel<Event> = Channel(),
            private val events: Flow<Event> = channel.consumeAsFlow()
        ) {
            fun onEvent(event: Event) = scope.launch { channel.send(event) }
            fun state(): Flow<State> = events.scan(State.init(), reducer.function)
        }

        /**
         * @property isInitialized whether this state is initialized
         * @property isLoading whether the data is being loaded to prepare a new state
         * @property error if present, represents an error occured in this state machine
         * @property homeDashboard current dashboard data state that should be rendered
         */
        data class State(
            val isInitialzed: Boolean,
            val isLoading: Boolean,
            val error: String?,
            val homeDashboard: HomeDashboard?
        ) : Machine() {
            companion object {
                fun init() = State(
                    isInitialzed = true,
                    isLoading = false,
                    error = null,
                    homeDashboard = null
                )
            }
        }

        sealed class Event : Machine() {
            data class OnDashboardLoaded(
                val dashboard: HomeDashboard
            ) : Event()

            data class OnBlocksAdded(
                val blocks: List<Block>
            ) : Event()

            data class OnStructureUpdated(
                val children: List<String>
            ) : Event()

            object OnDashboardLoadingStarted : Event()

            object OnStartedCreatingPage : Event()

            object OnFinishedCreatingPage : Event()
        }

        class Reducer : StateReducer<State, Event> {

            override val function: suspend (State, Event) -> State
                get() = { state, event -> reduce(state, event) }

            override suspend fun reduce(
                state: State, event: Event
            ) = when (event) {
                is Event.OnDashboardLoadingStarted -> state.copy(
                    isInitialzed = true,
                    isLoading = true,
                    error = null,
                    homeDashboard = null
                )
                is Event.OnDashboardLoaded -> state.copy(
                    isInitialzed = true,
                    isLoading = false,
                    error = null,
                    homeDashboard = event.dashboard
                )
                is Event.OnStartedCreatingPage -> state.copy(
                    isLoading = true
                )
                is Event.OnFinishedCreatingPage -> state.copy(
                    isLoading = false
                )
                is Event.OnStructureUpdated -> state.copy(
                    isInitialzed = true,
                    isLoading = false,
                    homeDashboard = state.homeDashboard?.copy(
                        children = event.children
                    )
                )
                is Event.OnBlocksAdded -> state.copy(
                    isInitialzed = true,
                    isLoading = false,
                    homeDashboard = state.homeDashboard?.let { dashboard ->
                        dashboard.copy(blocks = dashboard.blocks + event.blocks)
                    }
                )
            }
        }
    }
}
