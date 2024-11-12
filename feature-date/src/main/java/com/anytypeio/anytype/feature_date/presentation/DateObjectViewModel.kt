package com.anytypeio.anytype.feature_date.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.relations.RelationListWithValue
import com.anytypeio.anytype.feature_date.models.DateObjectBottomMenu
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectSheetState
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val relationListWithValue: RelationListWithValue
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val uiTopToolbarState =
        MutableStateFlow<DateObjectTopToolbarState>(DateObjectTopToolbarState.Hidden)
    val uiBottomMenu = MutableStateFlow(DateObjectBottomMenu())
    val uiHeaderState = MutableStateFlow<DateObjectHeaderState>(DateObjectHeaderState.Hidden)
    val uiHorizontalListState =
        MutableStateFlow<DateObjectHorizontalListState>(DateObjectHorizontalListState.Empty)
    val uiVerticalListState =
        MutableStateFlow<DateObjectVerticalListState>(DateObjectVerticalListState.Empty)
    val uiSheetState = MutableStateFlow<DateObjectSheetState>(DateObjectSheetState.Empty)
    val commands = MutableSharedFlow<Command>()

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
        proceedWithObservingPermissions()
        proceedWithGettingObject()
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupUiStateFlow() {
//        viewModelScope.launch {
//            restartSubscription.flatMapLatest {
//                loadData()
//            }.collectLatest { items ->
//                if (items.isEmpty()) {
//                    uiItemsState.value = UiItemsState.Empty
//                    uiContentState.value = UiContentState.Empty
//                } else {
//                    uiItemsState.value = UiItemsState.Content(items)
//                }
//            }
//        }
    }

    private fun proceedWithGettingObject() {
        val params = GetObject.Params(
            target = vmParams.objectId,
            space = vmParams.spaceId
        )
        viewModelScope.launch {
            getObject.async(params).fold(
                onSuccess = { obj ->
                    uiTopToolbarState.value = DateObjectTopToolbarState.Content(
                        syncStatus = SpaceSyncStatus.SYNCING
                    )
                    val root = ObjectWrapper.Basic(obj.details[vmParams.objectId].orEmpty())
                    uiHeaderState.value = DateObjectHeaderState.Content(
                        title = root.name.orEmpty()
                    )
                    uiHorizontalListState.value = DateObjectHorizontalListState.Content(
                        items = listOf(
                            UiHorizontalListItem.Settings(),
                            UiHorizontalListItem.MentionedIn()
                        ),
                        selectedItem = null
                    )
                },
                onFailure = { e -> Timber.e("Error getting date object") }
            )
        }
    }

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

    companion object {
        const val DEFAULT_DEBOUNCE_DURATION = 300L

        //INITIAL STATE
        const val DEFAULT_SEARCH_LIMIT = 25
    }
}