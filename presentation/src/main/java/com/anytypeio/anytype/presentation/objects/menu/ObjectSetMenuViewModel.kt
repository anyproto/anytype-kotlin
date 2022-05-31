package com.anytypeio.anytype.presentation.objects.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ObjectSetMenuViewModel(
    setObjectIsArchived: SetObjectIsArchived,
    addToFavorite: AddToFavorite,
    removeFromFavorite: RemoveFromFavorite,
    dispatcher: Dispatcher<Payload>,
    state: StateFlow<ObjectSet>,
    menuOptionsProvider: ObjectMenuOptionsProvider,
    private val analytics: Analytics,
) : ObjectMenuViewModelBase(
    setObjectIsArchived = setObjectIsArchived,
    addToFavorite = addToFavorite,
    removeFromFavorite = removeFromFavorite,
    dispatcher = dispatcher,
    analytics = analytics,
    menuOptionsProvider = menuOptionsProvider,
) {

    private val objectRestrictions = state.value.objectRestrictions

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val setObjectIsArchived: SetObjectIsArchived,
        private val addToFavorite: AddToFavorite,
        private val removeFromFavorite: RemoveFromFavorite,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val state: StateFlow<ObjectSet>,
        private val menuOptionsProvider: ObjectMenuOptionsProvider,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetMenuViewModel(
                setObjectIsArchived = setObjectIsArchived,
                addToFavorite = addToFavorite,
                removeFromFavorite = removeFromFavorite,
                analytics = analytics,
                state = state,
                dispatcher = dispatcher,
                menuOptionsProvider = menuOptionsProvider
            ) as T
        }
    }

    override fun onIconClicked(ctx: Id) {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetIcons)
            }
        }
    }

    override fun onCoverClicked(ctx: Id) {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetCover)
            }
        }
    }

    override fun onLayoutClicked(ctx: Id) {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.LAYOUT_CHANGE)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetLayout)
            }
        }
    }

    override fun onRelationsClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.RELATIONS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetRelations)
            }
        }
    }

    override fun onHistoryClicked() {
        viewModelScope.launch { _toasts.emit(COMING_SOON_MSG) }
    }

    override fun buildActions(
        ctx: Id,
        isArchived: Boolean,
        isFavorite: Boolean,
        isProfile: Boolean
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
            else -> {
                viewModelScope.launch { _toasts.emit(COMING_SOON_MSG) }
            }
        }
    }
}