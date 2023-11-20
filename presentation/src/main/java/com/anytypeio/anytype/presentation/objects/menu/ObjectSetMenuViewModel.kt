package com.anytypeio.anytype.presentation.objects.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.DebugGoroutinesShareDownloader
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ObjectSetMenuViewModel(
    setObjectIsArchived: SetObjectIsArchived,
    addToFavorite: AddToFavorite,
    removeFromFavorite: RemoveFromFavorite,
    addBackLinkToObject: AddBackLinkToObject,
    duplicateObject: DuplicateObject,
    delegator: Delegator<Action>,
    urlBuilder: UrlBuilder,
    dispatcher: Dispatcher<Payload>,
    menuOptionsProvider: ObjectMenuOptionsProvider,
    private val objectState: StateFlow<ObjectState>,
    private val analytics: Analytics,
    private val addObjectToCollection: AddObjectToCollection,
    private val debugGoroutinesShareDownloader: DebugGoroutinesShareDownloader
) : ObjectMenuViewModelBase(
    setObjectIsArchived = setObjectIsArchived,
    addToFavorite = addToFavorite,
    removeFromFavorite = removeFromFavorite,
    addBackLinkToObject = addBackLinkToObject,
    duplicateObject = duplicateObject,
    delegator = delegator,
    urlBuilder = urlBuilder,
    dispatcher = dispatcher,
    analytics = analytics,
    menuOptionsProvider = menuOptionsProvider,
    addObjectToCollection = addObjectToCollection,
    debugGoroutinesShareDownloader = debugGoroutinesShareDownloader
) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val setObjectIsArchived: SetObjectIsArchived,
        private val addToFavorite: AddToFavorite,
        private val removeFromFavorite: RemoveFromFavorite,
        private val addBackLinkToObject: AddBackLinkToObject,
        private val duplicateObject: DuplicateObject,
        private val delegator: Delegator<Action>,
        private val urlBuilder: UrlBuilder,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val objectState: StateFlow<ObjectState>,
        private val menuOptionsProvider: ObjectMenuOptionsProvider,
        private val addObjectToCollection: AddObjectToCollection,
        private val debugGoroutinesShareDownloader: DebugGoroutinesShareDownloader
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetMenuViewModel(
                setObjectIsArchived = setObjectIsArchived,
                addToFavorite = addToFavorite,
                removeFromFavorite = removeFromFavorite,
                addBackLinkToObject = addBackLinkToObject,
                duplicateObject = duplicateObject,
                delegator = delegator,
                urlBuilder = urlBuilder,
                analytics = analytics,
                objectState = objectState,
                dispatcher = dispatcher,
                menuOptionsProvider = menuOptionsProvider,
                addObjectToCollection = addObjectToCollection,
                debugGoroutinesShareDownloader = debugGoroutinesShareDownloader
            ) as T
        }
    }

    override fun onIconClicked(ctx: Id) {
        val dataViewState = objectState.value.dataViewState() ?: return
        viewModelScope.launch {
            if (dataViewState.objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetIcons)
            }
        }
    }

    override fun onCoverClicked(ctx: Id) {
        val dataViewState = objectState.value.dataViewState() ?: return
        viewModelScope.launch {
            if (dataViewState.objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetCover)
            }
        }
    }

    override fun onLayoutClicked(ctx: Id) {
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
        isTemplate: Boolean
    ): List<ObjectAction> = buildList {
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
        val dataViewState = objectState.value.dataViewState()
        if (dataViewState != null && !dataViewState.objectRestrictions.contains(ObjectRestriction.DUPLICATE)) {
            add(ObjectAction.DUPLICATE)
        }
        add(ObjectAction.LINK_TO)
    }

    override fun onActionClicked(ctx: Id, action: ObjectAction) {
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
                proceedWithDuplication(ctx = ctx, details = objectState.value.dataViewState()?.details)
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
        }
    }
}