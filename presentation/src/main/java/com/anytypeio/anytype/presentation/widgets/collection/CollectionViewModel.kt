package com.anytypeio.anytype.presentation.widgets.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.ext.process
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrDefault
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.dashboard.interactor.SetObjectListIsFavorite
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.Reducer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendDeletionWarning
import com.anytypeio.anytype.presentation.extension.sendScreenHomeEvent
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.mapFileObjectToView
import com.anytypeio.anytype.presentation.objects.toViews
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.collection.CollectionView.FavoritesView
import com.anytypeio.anytype.presentation.widgets.collection.CollectionView.ObjectView
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.anytypeio.anytype.core_models.ObjectView as CoreObjectView

class CollectionViewModel(
    private val container: StorelessSubscriptionContainer,
    private val workspaceManager: WorkspaceManager,
    private val urlBuilder: UrlBuilder,
    private val getObjectTypes: GetObjectTypes,
    private val dispatchers: AppCoroutineDispatchers,
    private val actionObjectFilter: ActionObjectFilter,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val setObjectListIsFavorite: SetObjectListIsFavorite,
    private val deleteObjects: DeleteObjects,
    private val resourceProvider: CollectionResourceProvider,
    private val openObject: OpenObject,
    private val createObject: CreateObject,
    private val configstorage: ConfigStorage,
    interceptEvents: InterceptEvents,
    private val objectPayloadDispatcher: Dispatcher<Payload>,
    private val move: Move,
    private val analytics: Analytics,
    private val dateProvider: DateProvider,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : ViewModel(), Reducer<CoreObjectView, Payload> {

    val payloads: Flow<Payload>

    init {
        val externalChannelEvents =
            interceptEvents.build(InterceptEvents.Params(configstorage.get().home)).map {
                Payload(
                    context = configstorage.get().home,
                    events = it
                )
            }

        val internalChannelEvents = objectPayloadDispatcher.flow()
        payloads = merge(externalChannelEvents, internalChannelEvents)
    }

    val commands = MutableSharedFlow<Command>()

    private val jobs = mutableListOf<Job>()
    private val queryFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val views = MutableStateFlow<Resultat<List<CollectionView>>>(Resultat.loading())
    private val interactionMode = MutableStateFlow<InteractionMode>(InteractionMode.View)
    private var operationInProgress = MutableStateFlow(false)
    val openFileDeleteAlert = MutableStateFlow(false)
    val toasts = MutableSharedFlow<String>(replay = 0)

    private var actionMode: ActionMode = ActionMode.Edit
    private var subscription: Subscription = Subscription.None

    val uiState: StateFlow<Resultat<CollectionUiState>> =
        combine(interactionMode, views, operationInProgress) { mode, views, operationInProgress ->
            Resultat.success(
                CollectionUiState(
                    views = views,
                    showEditMode = mode == InteractionMode.Edit,
                    showWidget = mode == InteractionMode.Edit && isAnySelected(),
                    collectionName = resourceProvider.subscriptionName(subscription),
                    actionName = resourceProvider.actionModeName(
                        actionMode = actionMode,
                        isResultEmpty = when (views) {
                            is Resultat.Failure -> true
                            is Resultat.Loading -> true
                            is Resultat.Success -> {
                                views.value.none { it is ObjectView || it is FavoritesView }
                            }
                        }
                    ),
                    objectActions = actionObjectFilter.filter(subscription, selectedViews()),
                    inDragMode = subscription == Subscription.Favorites && mode == InteractionMode.Edit,
                    displayType = subscription != Subscription.Sets || subscription != Subscription.Files,
                    operationInProgress = operationInProgress
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Resultat.loading()
        )

    private suspend fun objectTypes(): StateFlow<List<ObjectWrapper.Type>> {
        val params = GetObjectTypes.Params(
            sorts = emptyList(),
            filters = ObjectSearchConstants.filterObjectTypeLibrary(
                workspaceId = workspaceManager.getCurrentWorkspace()
            ),
            keys = ObjectSearchConstants.defaultKeysObjectType
        )
        return getObjectTypes.asFlow(params).stateIn(viewModelScope)
    }

    @FlowPreview
    private fun queryFlow() = queryFlow
        .debounce(DEBOUNCE_TIMEOUT)
        .distinctUntilChanged()
        .onEach {
            if (subscription != Subscription.Bin && subscription != Subscription.Files) {
                actionMode = ActionMode.Edit
                interactionMode.value = InteractionMode.View
            }
            views.value = Resultat.loading()
        }

    fun onStop() {
        launch {
            withContext(dispatchers.io) {
                container.unsubscribe(listOf(subscription.id))
            }
            jobs.cancel()
        }
    }

    fun onStart(subscription: Subscription) {
        val isFirstLaunch = this.subscription == Subscription.None
        this.subscription = subscription
        if (isFirstLaunch && (subscription == Subscription.Bin || subscription == Subscription.Files)) {
            onStartEditMode()
        }
        subscribeObjects()
    }

    private suspend fun buildSearchParams(): StoreSearchParams {
        return StoreSearchParams(
            subscription = subscription.id,
            keys = subscription.keys,
            filters = subscription.filters(workspaceManager.getCurrentWorkspace()),
            sorts = subscription.sorts,
            limit = subscription.limit
        )
    }

    private fun subscribeObjects() {
        launch {
            when (subscription) {
                Subscription.Favorites -> {
                    favoritesSubsciptionFlow().map { it.map { it as CollectionView } }
                }

                Subscription.Files -> {
                    filesSubscriptionFlow()
                }

                else -> {
                    subscriptionFlow()
                }
            }
                .map { update -> preserveSelectedState(update) }
                .flowOn(dispatchers.io)
                .collect {
                    views.value = Resultat.success(it)
                }
        }
    }

    private fun preserveSelectedState(update: List<CollectionView>): List<CollectionView> {
        val curr = currentViews()
            .filterIsInstance<CollectionObjectView>().associateBy { it.obj.id }

        return update.map {
            when (it) {
                is ObjectView -> it.copy(isSelected = curr[it.obj.id]?.isSelected ?: false)
                is FavoritesView -> it.copy(
                    isSelected = curr[it.obj.id]?.isSelected ?: false
                )
                else -> it
            }
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun subscriptionFlow() =
        combine(
            container.subscribe(buildSearchParams()),
            queryFlow(),
            objectTypes()
        ) { objs, query, types ->
            val result = objs.filter { obj ->
                obj.getProperName().contains(query, true)
            }.toViews(urlBuilder, types).map { ObjectView(it) }.tryAddSections()
            if (result.isEmpty() && query.isNotEmpty())
                listOf(CollectionView.EmptySearch(query))
            else
                result
        }

    private fun List<ObjectView>.tryAddSections() =
        if (subscription == Subscription.Recent)
            this.groupBy { dateProvider.getRelativeTimeSpanString(it.obj.lastModifiedDate) }
                .flatMap { (key, value) ->
                    buildList<CollectionView> {
                        add(CollectionView.SectionView(key.toString()))
                        addAll(value)
                    }
                }
        else this

    private fun List<ObjectWrapper.Basic>.toOrder(favs: Map<Id, FavoritesOrder>): List<ObjectWrapper.Basic> {
        if (favs.size != this.size) {
            Timber.e("Favorite order size is not equal to the list size")
        }
        val orderedFavorites = MutableList<ObjectWrapper.Basic?>(this.size) { null }
        for (item in this) {
            val order = favs[item.id]?.order
            if (order == null) {
                Timber.e("Favorite order is null for ${item.id}")
            } else if (order < 0 || order >= this.size) {
                Timber.e("Favorite order is out of bounds for ${item.id}")
            } else {
                orderedFavorites[order] = item
            }
        }
        return orderedFavorites.filterNotNull()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private suspend fun favoritesSubsciptionFlow() =
        combine(
            container.subscribe(buildSearchParams()),
            queryFlow(),
            objectTypes(),
            openObject.asFlow(OpenObject.Params(configstorage.get().home, false))
                .flatMapLatest { payloads.scan(it) { s, p -> reduce(s, p) } },
        ) { objs, query, types, favorotiesObj ->
            val result = prepareFavorites(favorotiesObj, objs, query, types)
            if (result.isEmpty() && query.isNotEmpty())
                listOf(CollectionView.EmptySearch(query))
            else
                result
        }

    private fun prepareFavorites(
        favoritesObj: CoreObjectView,
        objs: List<ObjectWrapper.Basic>,
        query: String,
        types: List<ObjectWrapper.Type>
    ): List<CollectionObjectView> {
        val favs = favoritesObj.blocks.parseFavorites(
            root = favoritesObj.root,
            details = favoritesObj.details
        )

        return objs.toOrder(favs).filter { obj ->
            obj.getProperName().lowercase().contains(query.lowercase())
        }
            .toViews(urlBuilder, types)
            .map { FavoritesView(it, favs[it.id]?.blockId ?: "") }
    }

    fun onSearchTextChanged(search: String) {
        queryFlow.value = search
    }

    fun onObjectClicked(view: CollectionObjectView) {
        if (interactionMode.value == InteractionMode.Edit) {
            selectView(view)
            ensureViewMode()
        } else {
            openObject(view.obj)
        }
    }

    private fun openObject(view: DefaultObjectView) {
        launch {
            val target = view.id
            when (view.layout) {
                ObjectType.Layout.PROFILE,
                ObjectType.Layout.BASIC,
                ObjectType.Layout.TODO,
                ObjectType.Layout.NOTE,
                ObjectType.Layout.FILE,
                ObjectType.Layout.IMAGE,
                ObjectType.Layout.BOOKMARK -> {
                    commands.emit(Command.LaunchDocument(id = target))
                }
                ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> {
                    commands.emit(Command.LaunchObjectSet(target = target))
                }
                else -> {
                    Timber.e("Unexpected layout: ${view.layout}")
                }
            }
        }
    }

    private fun ensureViewMode() {
        if (isNoneSelected() && (subscription != Subscription.Bin && subscription != Subscription.Files)) {
            actionMode = ActionMode.Edit
            interactionMode.value = InteractionMode.View
        }
    }

    fun onObjectLongClicked(view: CollectionObjectView) {
        if (interactionMode.value != InteractionMode.Edit) {
            interactionMode.value = InteractionMode.Edit
        }
        onObjectClicked(view)
    }

    private fun selectView(view: CollectionObjectView) {
        val views = currentViews()
        val indexOf = views.indexOf(view as CollectionView)
        if (indexOf != -1) {
            when (view) {
                is ObjectView -> {
                    views[indexOf] = view.copy(isSelected = !view.isSelected)
                }
                is FavoritesView -> {
                    views[indexOf] = view.copy(isSelected = !view.isSelected)
                }
                else -> {
                    Timber.e("Unexpected view type: ${view::class}")
                }
            }
            alterActionState(views)
            this.views.value = Resultat.success(views)
        }
    }

    private fun alterActionState(views: List<CollectionView>) {
        val isSubscriptionBinOrFiles =
            subscription == Subscription.Bin || subscription == Subscription.Files

        actionMode = when {
            isAllSelected(views) && isSubscriptionBinOrFiles -> ActionMode.UnselectAll
            isAllSelected(views) -> ActionMode.Done
            isSubscriptionBinOrFiles -> ActionMode.SelectAll
            else -> ActionMode.Done
        }
    }

    fun onActionClicked() {
        when (actionMode) {
            ActionMode.Edit -> onStartEditMode()
            ActionMode.SelectAll -> onSelectAll()
            ActionMode.UnselectAll -> onUnselectAll()
            ActionMode.Done -> onDone()
        }
    }

    fun onMove(currentViews: List<CollectionView>, from: Int, to: Int) {
        if (from == to) return
        Timber.d("## from:[$from], to:[$to]")
        launch {
            val currentViews = currentViews.filterIsInstance<FavoritesView>()
            val direction = if (from < to) Position.BOTTOM else Position.TOP
            val subject = currentViews[to].blockId
            val target =
                if (direction == Position.TOP) {
                    currentViews[to.inc()].blockId
                } else {
                    currentViews[to.dec()].blockId
                }

            val param = Move.Params(
                context = configstorage.get().home,
                targetContext = configstorage.get().home,
                position = direction,
                blockIds = listOf(subject),
                targetId = target
            )

            move.stream(param).collect {
                it.fold(
                    onSuccess = {
                        sendEvent(
                            analytics = analytics,
                            eventName = EventsDictionary.reorderObjects,
                            props = Props(
                                mapOf(
                                    EventsPropertiesKey.route to EventsDictionary.Routes.home
                                )
                            )
                        )
                        Timber.d("successful DND for: $param")
                    },
                    onFailure = {
                        Timber.e(it, "Error while DND for: $param")
                    }
                )
            }
        }
    }

    private fun onUnselectAll() {
        actionMode = ActionMode.SelectAll
        unselectViews()
    }

    private fun onDone() {
        actionMode = when (subscription) {
            Subscription.Bin, Subscription.Files -> ActionMode.SelectAll
            else -> {
                interactionMode.value = InteractionMode.View
                ActionMode.Edit
            }
        }
        unselectViews()
    }

    private fun onStartEditMode() {
        actionMode = when (subscription) {
            Subscription.Bin, Subscription.Files -> ActionMode.SelectAll
            else -> ActionMode.Done
        }
        interactionMode.value = InteractionMode.Edit
    }

    private fun unselectViews() {
        changeSelectionStatus(false)
    }

    private fun changeSelectionStatus(isSelected: Boolean) {
        views.value = Resultat.success(
            currentViews()
                .map {
                    when (it) {
                        is FavoritesView -> it.copy(isSelected = isSelected)
                        is ObjectView -> it.copy(isSelected = isSelected)
                        else -> it
                    }
                })
    }

    private fun onSelectAll() {
        actionMode = when (subscription) {
            Subscription.Bin, Subscription.Files -> ActionMode.UnselectAll
            else -> ActionMode.Done
        }
        changeSelectionStatus(true)
    }

    fun onBackPressed(isExpanded: Boolean) {
        if (interactionMode.value == InteractionMode.Edit
            && subscription != Subscription.Bin
            && subscription != Subscription.Files
        ) {
            onDone()
            return
        }
        if (!(subscription in arrayOf(Subscription.Bin, Subscription.Files) && isExpanded)) {
            onPrevClicked()
        }
    }

    fun onActionWidgetClicked(action: ObjectAction) {
        proceed(action, selectedViews())
    }

    fun proceed(action: ObjectAction, views: List<CollectionObjectView>) {
        val objIds = views.toObjIds()
        when (action) {
            ObjectAction.ADD_TO_FAVOURITE -> addToFavorite(objIds)
            ObjectAction.REMOVE_FROM_FAVOURITE -> removeFromFavorite(objIds)
            ObjectAction.DELETE -> deleteFromBin(objIds)
            ObjectAction.MOVE_TO_BIN -> changeObjectListBinStatus(objIds, true)
            ObjectAction.RESTORE -> changeObjectListBinStatus(objIds, false)
            ObjectAction.DELETE_FILES -> deleteFiles()
            else -> {
                Timber.e("Unexpected action: $action")
            }
        }
    }

    private fun List<CollectionObjectView>.toObjIds() = this.map { it.obj.id }

    private fun deleteFiles() {
        openFileDeleteAlert.value = true
    }

    private fun changeObjectListBinStatus(ids: List<Id>, isArchived: Boolean) {
        launch {
            setObjectListIsArchived.stream(SetObjectListIsArchived.Params(ids, isArchived))
                .collect { it.progressiveFold() }
        }
    }

    private fun deleteFromBin(ids: List<Id>) {
        launch {
            analytics.sendDeletionWarning()
        }
        launch {
            commands.emit(Command.ConfirmRemoveFromBin(ids.size))
        }
    }

    private fun removeFromFavorite(ids: List<Id>) {
        changeFavoriteState(ids, false)
    }

    private fun addToFavorite(ids: List<Id>) {
        changeFavoriteState(ids, true)
    }

    private fun changeFavoriteState(views: List<Id>, isFavorite: Boolean) {
        launch {
            setObjectListIsFavorite.stream(SetObjectListIsFavorite.Params(views, isFavorite))
                .collect { it.progressiveFold() }
        }
    }

    fun omBottomSheet(isExpanded: Boolean) {
        if (isExpanded) {
            if (interactionMode.value == InteractionMode.View) onStartEditMode()
        } else {
            if (interactionMode.value == InteractionMode.Edit) onDone()
        }
    }

    fun onDeletionAccepted() {

        val selected = selectedViews().toObjIds()

        launch {
            deleteObjects.stream(DeleteObjects.Params(selected))
                .collect { it.progressiveFold() }
        }
    }

    fun onDeletionFilesAccepted() {
        val selected = selectedViews().toObjIds()
        val setArchivedParams = SetObjectListIsArchived.Params(
            targets = selected,
            isArchived = true
        )
        operationInProgress.value = true
        launch {
            setObjectListIsArchived.stream(params = setArchivedParams)
                .collect { archivedResult ->
                    archivedResult.progressiveFold(
                        onSuccess = { proceedWithFileDeletion(selected) },
                        onFailure = { exception ->
                            operationInProgress.value = false
                            toasts.emit("Error while deleting files")
                            Timber.e(exception, "Error while setting file objects as archived")
                        }
                    )
                }
        }
    }

    private suspend fun proceedWithFileDeletion(ids: List<Id>) {
        val params = DeleteObjects.Params(ids)
        deleteObjects.stream(params = params)
            .collect { result ->
                result.progressiveFold(
                    onSuccess = {
                        openFileDeleteAlert.value = false
                        operationInProgress.value = false
                    },
                    onFailure = { exception ->
                        operationInProgress.value = false
                        toasts.emit("Error while deleting files")
                        Timber.e(exception, "Error while deleting file objects")
                    }
                )
            }
    }

    fun onFileDeleteAlertDismiss() {
        openFileDeleteAlert.value = false
    }

    private fun isNoneSelected() =
        currentViews().none { it is CollectionObjectView && it.isSelected }

    private fun isAnySelected() =
        currentViews().any { it is CollectionObjectView && it.isSelected }

    private fun isAllSelected(views: List<CollectionView>) = views.filterIsInstance<CollectionObjectView>().all { it.isSelected }
    private fun isNoneSelected(views: List<CollectionView>) = views.filterIsInstance<CollectionObjectView>().none { it.isSelected }

    private fun selectedViews() =
        currentViews()
            .filterIsInstance<CollectionObjectView>()
            .filter { it.isSelected }

    private fun currentViews() = views.value.getOrDefault(listOf()).toMutableList()

    private inline fun launch(crossinline block: suspend CoroutineScope.() -> Unit) {
        jobs += viewModelScope.launch { block() }
    }

    private inline fun <T> Resultat<T>.progressiveFold(
        onSuccess: (value: T) -> Unit = {},
        onFailure: (exception: Throwable) -> Unit = {},
        onLoading: () -> Unit = {},
    ) {
        return when (this) {
            is Resultat.Failure -> {
                operationInProgress.value = false
                Timber.d(exception)
                onFailure(exception)
            }
            is Resultat.Loading -> {
                operationInProgress.value = true
                onLoading()
            }
            is Resultat.Success -> {
                operationInProgress.value = false
                onSuccess(value)
            }
        }
    }

    override fun reduce(state: CoreObjectView, event: Payload): CoreObjectView {
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

    fun onHomeClicked() {
        launch {
            analytics.sendScreenHomeEvent()
            commands.emit(Command.ToDesktop)
        }
    }

    fun onPrevClicked() {
        launch {
            analytics.sendScreenHomeEvent()
            commands.emit(Command.Exit)
        }
    }

    fun onSearchClicked() {
        launch {
            commands.emit(Command.ToSearch)
        }
    }

    fun onAddClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.createObjectCollectionsNavBar,
            props = Props(mapOf(EventsPropertiesKey.context to null))
        )

        val startTime = System.currentTimeMillis()
        launch {
            createObject.execute(CreateObject.Param(type = null))
                .fold(
                    onSuccess = { result ->
                        sendAnalyticsObjectCreateEvent(
                            analytics = analytics,
                            type = result.type,
                            storeOfObjectTypes = storeOfObjectTypes,
                            route = EventsDictionary.Routes.objCreateHome,
                            startTime = startTime
                        )
                        commands.emit(Command.LaunchDocument(result.objectId))
                    },
                    onFailure = { e -> Timber.e(e, "Error while creating a new page") }
                )
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun filesSubscriptionFlow(): Flow<List<CollectionView>> {
        return combine(
            container.subscribe(buildSearchParams()),
            queryFlow()
        ) { objects, query ->
            val result = filterAndMapObjects(objects, query)
            if (result.isEmpty() && query.isNotEmpty()) {
                listOf(CollectionView.EmptySearch(query))
            } else {
                result
            }
        }
    }

    private fun filterAndMapObjects(
        objects: List<ObjectWrapper.Basic>,
        query: String
    ): List<CollectionView> {
        return objects
            .filter { it.getProperName().contains(query, true) }
            .map { it.mapFileObjectToView() }
            .tryAddSections()
    }

    override fun onCleared() {
        // TODO close object if it was opened.
        super.onCleared()
    }

    class Factory @Inject constructor(
        private val container: StorelessSubscriptionContainer,
        private val workspaceManager: WorkspaceManager,
        private val urlBuilder: UrlBuilder,
        private val getObjectTypes: GetObjectTypes,
        private val dispatchers: AppCoroutineDispatchers,
        private val actionObjectFilter: ActionObjectFilter,
        private val setObjectListIsArchived: SetObjectListIsArchived,
        private val setObjectListIsFavorite: SetObjectListIsFavorite,
        private val deleteObjects: DeleteObjects,
        private val resourceProvider: CollectionResourceProvider,
        private val openObject: OpenObject,
        private val createObject: CreateObject,
        private val configStorage: ConfigStorage,
        private val interceptEvents: InterceptEvents,
        private val objectPayloadDispatcher: Dispatcher<Payload>,
        private val move: Move,
        private val analytics: Analytics,
        private val dateProvider: DateProvider,
        private val storeOfObjectTypes: StoreOfObjectTypes
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CollectionViewModel(
                container = container,
                workspaceManager = workspaceManager,
                urlBuilder = urlBuilder,
                getObjectTypes = getObjectTypes,
                dispatchers = dispatchers,
                actionObjectFilter = actionObjectFilter,
                setObjectListIsArchived = setObjectListIsArchived,
                setObjectListIsFavorite = setObjectListIsFavorite,
                deleteObjects = deleteObjects,
                resourceProvider = resourceProvider,
                openObject = openObject,
                createObject = createObject,
                configstorage = configStorage,
                interceptEvents = interceptEvents,
                objectPayloadDispatcher = objectPayloadDispatcher,
                move = move,
                analytics = analytics,
                dateProvider = dateProvider,
                storeOfObjectTypes = storeOfObjectTypes
            ) as T
        }
    }

    sealed class Command {
        data class ConfirmRemoveFromBin(val count: Int) : Command()
        data class LaunchDocument(val id: Id) : Command()
        data class OpenCollection(val subscription: Subscription) : Command()
        data class LaunchObjectSet(val target: Id) : Command()

        object ToDesktop : Command()
        object ToSearch : Command()
        object Exit : Command()
    }
}

private const val DEBOUNCE_TIMEOUT = 100L