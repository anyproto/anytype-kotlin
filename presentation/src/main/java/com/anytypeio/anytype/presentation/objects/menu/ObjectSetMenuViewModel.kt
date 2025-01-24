package com.anytypeio.anytype.presentation.objects.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.dashboard.interactor.SetObjectListIsFavorite
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.common.PayloadDelegator
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.DebugGoroutinesShareDownloader
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetMenuViewModel(
    setObjectIsArchived: SetObjectListIsArchived,
    addBackLinkToObject: AddBackLinkToObject,
    duplicateObject: DuplicateObject,
    delegator: Delegator<Action>,
    urlBuilder: UrlBuilder,
    dispatcher: Dispatcher<Payload>,
    menuOptionsProvider: ObjectMenuOptionsProvider,
    createWidget: CreateWidget,
    spaceManager: SpaceManager,
    payloadDelegator: PayloadDelegator,
    private val objectState: StateFlow<ObjectState>,
    private val analytics: Analytics,
    private val addObjectToCollection: AddObjectToCollection,
    private val debugGoroutinesShareDownloader: DebugGoroutinesShareDownloader,
    private val deepLinkResolver: DeepLinkResolver,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    setObjectListIsFavorite: SetObjectListIsFavorite,
    fieldParser: FieldParser,
    spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    getSpaceInviteLink: GetSpaceInviteLink
) : ObjectMenuViewModelBase(
    setObjectIsArchived = setObjectIsArchived,
    addBackLinkToObject = addBackLinkToObject,
    duplicateObject = duplicateObject,
    delegator = delegator,
    urlBuilder = urlBuilder,
    dispatcher = dispatcher,
    analytics = analytics,
    menuOptionsProvider = menuOptionsProvider,
    addObjectToCollection = addObjectToCollection,
    debugGoroutinesShareDownloader = debugGoroutinesShareDownloader,
    createWidget = createWidget,
    spaceManager = spaceManager,
    analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
    payloadDelegator = payloadDelegator,
    setObjectListIsFavorite = setObjectListIsFavorite,
    fieldParser = fieldParser,
    deepLinkResolver = deepLinkResolver,
    spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
    getSpaceInviteLink = getSpaceInviteLink
) {

    init {
        Timber.i("ObjectSetMenuViewModel, init")
    }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val addBackLinkToObject: AddBackLinkToObject,
        private val duplicateObject: DuplicateObject,
        private val delegator: Delegator<Action>,
        private val urlBuilder: UrlBuilder,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val objectState: StateFlow<ObjectState>,
        private val menuOptionsProvider: ObjectMenuOptionsProvider,
        private val addObjectToCollection: AddObjectToCollection,
        private val debugGoroutinesShareDownloader: DebugGoroutinesShareDownloader,
        private val createWidget: CreateWidget,
        private val spaceManager: SpaceManager,
        private val deepLinkResolver: DeepLinkResolver,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        private val payloadDelegator: PayloadDelegator,
        private val setObjectListIsFavorite: SetObjectListIsFavorite,
        private val setObjectListIsArchived: SetObjectListIsArchived,
        private val fieldParser: FieldParser,
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        private val getSpaceInviteLink: GetSpaceInviteLink
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetMenuViewModel(
                setObjectIsArchived = setObjectListIsArchived,
                addBackLinkToObject = addBackLinkToObject,
                duplicateObject = duplicateObject,
                delegator = delegator,
                urlBuilder = urlBuilder,
                analytics = analytics,
                objectState = objectState,
                dispatcher = dispatcher,
                menuOptionsProvider = menuOptionsProvider,
                addObjectToCollection = addObjectToCollection,
                debugGoroutinesShareDownloader = debugGoroutinesShareDownloader,
                createWidget = createWidget,
                spaceManager = spaceManager,
                deepLinkResolver = deepLinkResolver,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
                payloadDelegator = payloadDelegator,
                setObjectListIsFavorite = setObjectListIsFavorite,
                fieldParser = fieldParser,
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                getSpaceInviteLink = getSpaceInviteLink
            ) as T
        }
    }

    override fun onIconClicked(ctx: Id, space: Id) {
        val dataViewState = objectState.value.dataViewState() ?: return
        viewModelScope.launch {
            if (dataViewState.objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetIcons)
            }
        }
    }

    override fun onCoverClicked(ctx: Id, space: Id) {
        val dataViewState = objectState.value.dataViewState() ?: return
        viewModelScope.launch {
            if (dataViewState.objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetCover)
            }
        }
    }

    override fun onLayoutClicked(ctx: Id, space: Id) {
        val dataViewState = objectState.value.dataViewState() ?: return
        viewModelScope.launch {
            if (dataViewState.objectRestrictions.contains(ObjectRestriction.LAYOUT_CHANGE)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetLayout)
            }
        }
    }

    override fun onRelationsClicked() {
        val dataViewState = objectState.value.dataViewState() ?: return
        viewModelScope.launch {
            if (dataViewState.objectRestrictions.contains(ObjectRestriction.RELATIONS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetRelations)
            }
        }
    }

    override fun buildActions(
        ctx: Id,
        isArchived: Boolean,
        isFavorite: Boolean,
        isTemplate: Boolean,
        isLocked: Boolean,
        isReadOnly: Boolean
    ): List<ObjectAction> = buildList {
        if (!isReadOnly) {
            if (isArchived) {
                add(ObjectAction.RESTORE)
            } else {
                add(ObjectAction.DELETE)
            }
            if (isFavorite) {
                add(ObjectAction.REMOVE_FROM_FAVOURITE)
            } else {
                add(ObjectAction.ADD_TO_FAVOURITE)
            }
            add(ObjectAction.CREATE_WIDGET)
            val dataViewState = objectState.value.dataViewState()
            if (dataViewState != null && !dataViewState.objectRestrictions.contains(
                    ObjectRestriction.DUPLICATE
                )
            ) {
                add(ObjectAction.DUPLICATE)
            }
            add(ObjectAction.LINK_TO)
        }
        add(ObjectAction.COPY_LINK)
    }

    override fun onActionClicked(ctx: Id, space: Id, action: ObjectAction) {
        when (action) {
            ObjectAction.DELETE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = true)
            }
            ObjectAction.RESTORE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = false)
            }
            ObjectAction.ADD_TO_FAVOURITE -> {
                proceedWithAddingToFavorites(ctx)
            }
            ObjectAction.REMOVE_FROM_FAVOURITE -> {
                proceedWithRemovingFromFavorites(ctx)
            }
            ObjectAction.LINK_TO -> {
                proceedWithLinkTo()
            }
            ObjectAction.DUPLICATE -> {
                proceedWithDuplication(
                    ctx = ctx,
                    space = space,
                    details = objectState.value.dataViewState()?.details
                )
            }
            ObjectAction.CREATE_WIDGET -> {
                val details = objectState.value.dataViewState()?.details?.get(ctx)
                val wrapper = ObjectWrapper.Basic(details?.map ?: emptyMap())
                proceedWithCreatingWidget(obj = wrapper)
            }
            ObjectAction.COPY_LINK -> {
                viewModelScope.launch {
                    val link = proceedWithGeneratingObjectLink(
                        space = SpaceId(space),
                        ctx = ctx
                    )
                    commands.emit(Command.ShareDeeplinkToObject(link))
                }
            }
            ObjectAction.MOVE_TO,
            ObjectAction.SEARCH_ON_PAGE,
            ObjectAction.UNDO_REDO,
            ObjectAction.LOCK,
            ObjectAction.UNLOCK,
            ObjectAction.MOVE_TO_BIN,
            ObjectAction.USE_AS_TEMPLATE,
            ObjectAction.SET_AS_DEFAULT,
            ObjectAction.DELETE_FILES -> throw IllegalStateException("$action is unsupported")
            ObjectAction.DOWNLOAD_FILE -> {
                //do nothing
            }
        }
    }
}