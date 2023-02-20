package com.anytypeio.anytype.presentation.widgets.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrDefault
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.dashboard.interactor.SetObjectListIsFavorite
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.toViews
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

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
) : ViewModel() {

    val commands = MutableSharedFlow<Command>()

    private val jobs = mutableListOf<Job>()
    private val queryFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val views = MutableStateFlow<Resultat<List<CollectionView>>>(Resultat.loading())
    private val interactionMode = MutableStateFlow<InteractionMode>(InteractionMode.View)
    private var actionMode: ActionMode = ActionMode.Edit
    private var subscription: Subscription = Subscription.None

    val uiState: StateFlow<Resultat<CollectionUiState>> =
        interactionMode.combine(views) { mode, views ->
            Resultat.success(
                CollectionUiState(
                    views,
                    mode == InteractionMode.Edit,
                    mode == InteractionMode.Edit && currentViews().any { it.isSelected },
                    resourceProvider.subscriptionName(subscription),
                    resourceProvider.actionModeName(actionMode),
                    actionObjectFilter.filter(subscription, views.getOrDefault(emptyList())),
                    subscription == Subscription.Favorites && mode == InteractionMode.Edit,
                    subscription != Subscription.Sets
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
            if (subscription != Subscription.Bin) {
                interactionMode.value = InteractionMode.View
            }
            views.value = Resultat.loading()
        }

    fun onStop() {
        launch {
            container.unsubscribe(listOf(subscription.id))
            jobs.cancel()
        }
    }

    fun onStart(subscription: Subscription) {
        if (this.subscription != subscription && subscription == Subscription.Bin) {
            onStartEditMode()
        }
        this.subscription = subscription
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

    @OptIn(FlowPreview::class)
    private fun subscribeObjects() {
        launch {
            combine(
                container.subscribe(buildSearchParams()),
                queryFlow(),
                objectTypes()
            ) { objs, query, types ->
                objs.filter { obj -> obj.getProperName().lowercase().contains(query.lowercase()) }
                    .toViews(urlBuilder, types).map { CollectionView(it) }
            }
                .flowOn(dispatchers.io)
                .collect {
                    views.value = Resultat.success(it)
                }
        }
    }


    fun onSearchTextChanged(search: String) {
        queryFlow.value = search
    }

    fun onObjectClicked(view: CollectionView) {
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
                ObjectType.Layout.SET -> {
                    commands.emit(Command.LaunchObjectSet(target = target))
                }
                else -> {
                    Timber.e("Unexpected layout: ${view.layout}")
                }
            }
        }
    }

    private fun ensureViewMode() {
        if (currentViews().none { it.isSelected } && subscription != Subscription.Bin) {
            actionMode = ActionMode.Edit
            interactionMode.value = InteractionMode.View
        }
    }

    fun onObjectLongClicked(view: CollectionView) {
        if (interactionMode.value != InteractionMode.Edit) {
            interactionMode.value = InteractionMode.Edit
        }
        onObjectClicked(view)
    }

    private fun selectView(view: CollectionView) {
        val views = currentViews()
        val indexOf = views.indexOf(view)
        if (indexOf != -1) {
            views[indexOf] = view.copy(isSelected = !view.isSelected)
            alterActionState(views)
            this.views.value = Resultat.success(views)
        }
    }

    private fun alterActionState(views: MutableList<CollectionView>) {
        actionMode = if (views.all { it.isSelected }) {
            ActionMode.UnselectAll
        } else {
            ActionMode.SelectAll
        }
    }

    private fun currentViews() = views.value.getOrDefault(listOf()).toMutableList()

    private inline fun launch(crossinline block: suspend CoroutineScope.() -> Unit) {
        jobs += viewModelScope.launch { block() }
    }

    fun onActionClicked() {
        when (actionMode) {
            ActionMode.Edit -> {
                onStartEditMode()
            }
            ActionMode.SelectAll -> {
                onSelectAll()
            }
            ActionMode.UnselectAll -> {
                onUnselectAll()
            }
            ActionMode.Done -> onDone()
        }
    }

    private fun onUnselectAll() {
        actionMode = ActionMode.SelectAll
        unselectViews()
    }

    private fun onDone() {
        unselectViews()
        if (subscription != Subscription.Bin) {
            actionMode = ActionMode.Edit
            interactionMode.value = InteractionMode.View
        }
    }

    private fun onStartEditMode() {
        actionMode = ActionMode.SelectAll
        interactionMode.value = InteractionMode.Edit
    }

    private fun unselectViews() {
        views.value = Resultat.success(currentViews().map { it.copy(isSelected = false) })
    }

    private fun onSelectAll() {
        actionMode = if (subscription == Subscription.Bin) {
            ActionMode.UnselectAll
        } else {
            ActionMode.Done
        }
        views.value = Resultat.success(currentViews().map { it.copy(isSelected = true) })
    }

    fun onBackPressed(isExpanded: Boolean) {
        if (interactionMode.value == InteractionMode.Edit && subscription != Subscription.Bin) {
            onDone()
        } else if (!(subscription == Subscription.Bin && isExpanded)) {
            launch { commands.emit(Command.Exit) }
        }
    }

    fun onActionWidgetClicked(action: ObjectAction) {
        val selected = currentViews().filter { it.isSelected }
        proceed(action, selected)
    }

    fun proceed(action: ObjectAction, views: List<CollectionView>) {
        val objIds = views.map { it.obj.id }
        when (action) {
            ObjectAction.ADD_TO_FAVOURITE -> addToFavorite(objIds)
            ObjectAction.REMOVE_FROM_FAVOURITE -> removeFromFavorite(objIds)
            ObjectAction.DELETE -> deleteFromBin(objIds)
            ObjectAction.RESTORE -> restoreFromBin(objIds)
            else -> {
                Timber.e("Unexpected action: $action")
            }
        }
    }

    private fun restoreFromBin(ids: List<Id>) {
        launch {
            setObjectListIsArchived.stream(SetObjectListIsArchived.Params(ids, false))
                .collect {
                    it.fold(
                        onSuccess = { Timber.d("Restored") },
                        onFailure = { Timber.e(it) }
                    )
                }
        }
    }

    private fun deleteFromBin(ids: List<Id>) {
        launch {
            deleteObjects.stream(DeleteObjects.Params(ids))
                .collect {
                    it.fold(
                        onSuccess = { Timber.d("Restored") },
                        onFailure = { Timber.e(it) }
                    )
                }
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
            setObjectListIsFavorite.stream(SetObjectListIsFavorite.Params(views, false))
                .collect {
                    it.fold(
                        onSuccess = { Timber.d("Favorite state changed $isFavorite") },
                        onFailure = { Timber.e(it) }
                    )
                }
        }
    }

    fun omBottomSheet(isExpanded: Boolean) {
        if (isExpanded) {
            if (interactionMode.value == InteractionMode.View) onStartEditMode()
        } else {
            if (interactionMode.value == InteractionMode.Edit) onDone()
        }
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
            ) as T
        }
    }

    sealed class Command {
        data class LaunchDocument(val id: Id) : Command()
        data class LaunchObjectSet(val target: Id) : Command()
        object Exit : Command()
    }
}

private const val DEBOUNCE_TIMEOUT = 100L