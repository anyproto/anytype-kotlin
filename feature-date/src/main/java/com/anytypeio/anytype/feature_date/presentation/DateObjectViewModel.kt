package com.anytypeio.anytype.feature_date.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.RelationListWithValue
import com.anytypeio.anytype.feature_date.models.DateObjectBottomMenu
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectSheetState
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
import com.anytypeio.anytype.feature_date.models.UiContentState
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.feature_date.models.UiVerticalListItem
import com.anytypeio.anytype.feature_date.models.toUiHorizontalListItems
import com.anytypeio.anytype.feature_date.models.toUiVerticalListItem
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentScreen
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewState: @see [com.anytypeio.anytype.feature_date.models.UiContentState]
 * Factory: @see [DateObjectViewModelFactory]
 * Screen: @see [com.anytypeio.anytype.feature_date.ui.DateObjectScreen]
 * Models: @see [com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState]
 */
class DateObjectViewModel(
    private val vmParams: VmParams,
    private val getObject: GetObject,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val relationListWithValue: RelationListWithValue,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val uiTopToolbarState =
        MutableStateFlow<DateObjectTopToolbarState>(DateObjectTopToolbarState.Empty)
    val uiBottomMenu = MutableStateFlow(DateObjectBottomMenu())
    val uiHeaderState = MutableStateFlow<DateObjectHeaderState>(DateObjectHeaderState.Empty)
    val uiHorizontalListState =
        MutableStateFlow<DateObjectHorizontalListState>(DateObjectHorizontalListState.empty())
    val uiVerticalListState =
        MutableStateFlow<DateObjectVerticalListState>(DateObjectVerticalListState.empty())
    val uiSheetState = MutableStateFlow<DateObjectSheetState>(DateObjectSheetState.Empty)
    val commands = MutableSharedFlow<Command>()
    val uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())

    private val _activeRelationKey = MutableStateFlow<RelationKey?>(null)

    /**
     * Paging and subscription limit. If true, we can paginate after reaching bottom items.
     * Could be true only after the first subscription results (if results size == limit)
     */
    val canPaginate = MutableStateFlow(false)
    private var itemsLimit = DEFAULT_SEARCH_LIMIT
    private val restartSubscription = MutableStateFlow(0L)

    private var shouldScrollToTopItems = false

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    init {
        Timber.d("Init DateObjectViewModel, date object id: [${vmParams.objectId}], space: [${vmParams.spaceId}]")
        uiHeaderState.value = DateObjectHeaderState.Loading
        uiHorizontalListState.value = DateObjectHorizontalListState.loadingState()
        uiVerticalListState.value = DateObjectVerticalListState.loadingState()
        proceedWithObservingPermissions()
        proceedWithGettingDateObject()
        proceedWithGettingDateObjectRelationList()
    }

    fun onStart() {
        Timber.d("onStart")
        setupUiStateFlow()
        viewModelScope.launch {
            sendAnalyticsAllContentScreen(
                analytics = analytics
            )
        }
    }

    fun onStop() {
        unsubscribe()
    }

    override fun onCleared() {
        super.onCleared()
        uiContentState.value = UiContentState.Empty
        resetLimit()
    }

    //region Initialization
    private fun proceedWithObservingPermissions() {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = vmParams.spaceId)
                .collect {
                    uiBottomMenu.value =
                        DateObjectBottomMenu(isOwnerOrEditor = it?.isOwnerOrEditor() == true)
                    permission.value = it
                }
        }
    }

    private fun proceedWithGettingDateObjectRelationList() {
        val params = RelationListWithValue.Params(
            space = vmParams.spaceId,
            value = vmParams.objectId
        )
        viewModelScope.launch {
            delay(3000L)
            relationListWithValue.async(params).fold(
                onSuccess = { result ->
                    Timber.d("Got object relation list: $result")
                    val items = result.toUiHorizontalListItems(storeOfRelations)
                    initHorizontalListState(items)
                },
                onFailure = { e -> Timber.e("Error getting object relations") }
            )
        }
    }

    private fun proceedWithGettingDateObject() {
        val params = GetObject.Params(
            target = vmParams.objectId,
            space = vmParams.spaceId
        )
        viewModelScope.launch {
            delay(3000L)
            getObject.async(params).fold(
                onSuccess = { obj ->
                    uiTopToolbarState.value = DateObjectTopToolbarState.Content(
                        syncStatus = SpaceSyncStatus.SYNCING
                    )
                    val root = ObjectWrapper.Basic(obj.details[vmParams.objectId].orEmpty())
                    uiHeaderState.value = DateObjectHeaderState.Content(
                        title = root.name.orEmpty()
                    )
                },
                onFailure = { e -> Timber.e("Error getting date object") }
            )
        }
    }
    //endregion

    //region Subscription
    private fun subscriptionId() = "date_object_subscription_${vmParams.objectId}"

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupUiStateFlow() {
        viewModelScope.launch {
            restartSubscription.flatMapLatest {
                loadData()
            }.collectLatest { items ->
                uiVerticalListState.value = DateObjectVerticalListState(items)
            }
        }
    }

    private fun loadData(): Flow<List<UiVerticalListItem>> {

        val activeRelationKey = _activeRelationKey.value
        if (activeRelationKey?.key.isNullOrBlank()) {
            return emptyFlow()
        }

        val searchParams = createSearchParams(
            activeRelationKey = activeRelationKey
        )

        return storelessSubscriptionContainer.subscribe(searchParams)
            .onStart {
                uiContentState.value = if (itemsLimit == DEFAULT_SEARCH_LIMIT) {
                    UiContentState.InitLoading
                } else {
                    UiContentState.Paging
                }
                Timber.d("Restart subscription: with params: $searchParams")
            }
            .map { objWrappers ->
                handleData(objWrappers)
            }.catch { e ->
                Timber.e("Error loading data: $e")
                handleError(e)
            }
    }

    private suspend fun handleData(
        objWrappers: List<ObjectWrapper.Basic>
    ): List<UiVerticalListItem> {

        canPaginate.value = objWrappers.size == itemsLimit

        val items = objWrappers.map {
            it.toUiVerticalListItem(
                space = vmParams.spaceId,
                urlBuilder = urlBuilder,
                objectTypes = storeOfObjectTypes.getAll()
            )
        }
        uiContentState.value = if (items.isEmpty()) {
            UiContentState.Empty
        } else {
            UiContentState.Idle(scrollToTop = shouldScrollToTopItems).also {
                shouldScrollToTopItems = false
            }
        }
        return items
    }

    private fun createSearchParams(
        activeRelationKey: RelationKey
    ): StoreSearchParams {
        val filters = filtersForSearch(
            spaces = listOf(vmParams.spaceId.id),
            objectId = vmParams.objectId,
            relationKey = activeRelationKey
        )
        return StoreSearchParams(
            space = vmParams.spaceId,
            filters = filters,
            keys = defaultKeys,
            limit = itemsLimit,
            subscription = subscriptionId()
        )
    }

    /**
     * Updates the limit for the number of items fetched and triggers data reload.
     */
    fun updateLimit() {
        Timber.d("Update limit, canPaginate: ${canPaginate.value} uiContentState: ${uiContentState.value}")
        if (canPaginate.value && uiContentState.value is UiContentState.Idle) {
            itemsLimit += DEFAULT_SEARCH_LIMIT
            restartSubscription.value++
        }
    }

    private fun resetLimit() {
        Timber.d("Reset limit")
        itemsLimit = DEFAULT_SEARCH_LIMIT
    }

    private fun unsubscribe() {
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(listOf(subscriptionId()))
        }
    }
    //endregion

    //region Ui Actions
    fun onHorizontalItemClicked(item: UiHorizontalListItem) {
        when (item) {
            is UiHorizontalListItem.Item -> {
                shouldScrollToTopItems = true
                resetLimit()
                uiContentState.value = UiContentState.Empty
                _activeRelationKey.value = item.key
                restartSubscription.value++
                updateHorizontalListState(selectedItem = item)
            }
            else -> {
                // Do nothing
            }
        }
    }

    fun onVerticalItemClicked(item: UiVerticalListItem) {
    }

    fun onNextDayClicked() {
    }

    fun onPreviousDayClicked() {
    }

    fun onCalendarClicked() {}

    fun onSyncStatusClicked() {}
    //endregion

    //region Ui State
    private fun initHorizontalListState(relations: List<UiHorizontalListItem.Item>) {
        _activeRelationKey.value = relations.getOrNull(0)?.key
        restartSubscription.value++
        uiHorizontalListState.value = DateObjectHorizontalListState(
            items = buildList {
                add(UiHorizontalListItem.Settings())
                addAll(relations)
            },
            selectedRelationKey = _activeRelationKey.value
        )
    }

    private fun updateHorizontalListState(selectedItem: UiHorizontalListItem.Item) {
        uiHorizontalListState.value = uiHorizontalListState.value.copy(
            selectedRelationKey = selectedItem.key
        )
    }

    private fun handleError(e: Throwable) {
        uiContentState.value = UiContentState.Error(
            message = e.message ?: "An error occurred while loading data."
        )
    }
    //endregion

    //region VmParams + Command
    data class VmParams(
        val objectId: Id,
        val spaceId: SpaceId
    )

    sealed class Command {
        data class OpenChat(val target: Id, val space: Id) : Command()
        data class NavigateToEditor(val id: Id, val space: Id) : Command()
        data class NavigateToSetOrCollection(val id: Id, val space: Id) : Command()
        data class NavigateToBin(val space: Id) : Command()
        sealed class SendToast : Command() {
            data class Error(val message: String) : SendToast()
            data class RelationRemoved(val name: String) : SendToast()
            data class TypeRemoved(val name: String) : SendToast()
            data class UnexpectedLayout(val layout: String) : SendToast()
            data class ObjectArchived(val name: String) : SendToast()
        }

        data object OpenGlobalSearch : Command()
        data object ExitToVault : Command()
        data object Back : Command()
    }
    //endregion

    companion object {
        //INITIAL STATE
        const val DEFAULT_SEARCH_LIMIT = 25
    }
}