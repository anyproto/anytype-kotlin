package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.page.ArchiveDocument
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class ObjectMenuViewModelBase(
    private val archiveDocument: ArchiveDocument,
    private val addToFavorite: AddToFavorite,
    private val removeFromFavorite: RemoveFromFavorite,
    private val dispatcher: Dispatcher<Payload>
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)
    val commands = MutableSharedFlow<Command>(replay = 0)
    val actions = MutableStateFlow(emptyList<ObjectAction>())

    abstract fun onIconClicked()
    abstract fun onCoverClicked()
    abstract fun onLayoutClicked()
    abstract fun onRelationsClicked()
    abstract fun onHistoryClicked()
    fun onStart(isFavorite: Boolean, isArchived: Boolean, isProfile: Boolean) {
        actions.value = buildActions(
            isArchived = isArchived,
            isFavorite = isFavorite,
            isProfile = isProfile
        )
    }
    abstract fun onActionClicked(ctx: Id, action: ObjectAction)

    protected open fun buildActions(
        isArchived: Boolean,
        isFavorite: Boolean,
        isProfile: Boolean
    ): MutableList<ObjectAction> = mutableListOf<ObjectAction>().apply {
        if (!isProfile) {
            if (isArchived) {
                add(ObjectAction.RESTORE)
            } else {
                add(ObjectAction.DELETE)
            }
        }
        if (isFavorite) {
            add(ObjectAction.REMOVE_FROM_FAVOURITE)
        } else {
            add(ObjectAction.ADD_TO_FAVOURITE)
        }
        add(ObjectAction.SEARCH_ON_PAGE)
        add(ObjectAction.USE_AS_TEMPLATE)
    }

    protected fun proceedWithRemovingFromFavorites(ctx: Id) {
        viewModelScope.launch {
            removeFromFavorite(
                RemoveFromFavorite.Params(
                    target = ctx
                )
            ).process(
                failure = { Timber.e(it, "Error while removing from favorite.") },
                success = {
                    dispatcher.send(it)
                    _toasts.emit(REMOVE_FROM_FAVORITE_SUCCESS_MSG).also {
                        isDismissed.value = true
                    }
                }
            )
        }
    }

    protected fun proceedWithAddingToFavorites(ctx: Id) {
        viewModelScope.launch {
            addToFavorite(
                AddToFavorite.Params(
                    target = ctx
                )
            ).process(
                failure = { Timber.e(it, "Error while adding to favorites.") },
                success = {
                    dispatcher.send(it)
                    _toasts.emit(ADD_TO_FAVORITE_SUCCESS_MSG).also {
                        isDismissed.value = true
                    }
                }
            )
        }
    }

    fun proceedWithUpdatingArchivedStatus(ctx: Id, isArchived: Boolean) {
        viewModelScope.launch {
            archiveDocument(
                ArchiveDocument.Params(
                    context = ctx,
                    isArchived = isArchived,
                    targets = listOf(ctx)
                )
            ).process(
                failure = {
                    Timber.e(it, ARCHIVE_OBJECT_ERR_MSG)
                    _toasts.emit(ARCHIVE_OBJECT_ERR_MSG)
                },
                success = {
                    if (isArchived) {
                        _toasts.emit(ARCHIVE_OBJECT_SUCCESS_MSG)
                    } else {
                        _toasts.emit(RESTORE_OBJECT_SUCCESS_MSG)
                    }
                    isDismissed.value = true
                }
            )
        }
    }

    sealed class Command {
        object OpenObjectIcons : Command()
        object OpenSetIcons : Command()
        object OpenObjectCover : Command()
        object OpenSetCover : Command()
        object OpenObjectLayout : Command()
        object OpenSetLayout : Command()
        object OpenObjectRelations : Command()
        object OpenSetRelations : Command()
    }

    companion object {
        const val ARCHIVE_OBJECT_SUCCESS_MSG = "Object archived!"
        const val RESTORE_OBJECT_SUCCESS_MSG = "Object restored!"
        const val ARCHIVE_OBJECT_ERR_MSG =
            "Error while changing is-archived status for this object. Please, try again later."
        const val ADD_TO_FAVORITE_SUCCESS_MSG = "Object added to favorites."
        const val REMOVE_FROM_FAVORITE_SUCCESS_MSG = "Object removed from favorites."
        const val COMING_SOON_MSG = "Coming soon..."
        const val NOT_ALLOWED = "Not allowed for this object"
    }
}

class ObjectMenuViewModel(
    archiveDocument: ArchiveDocument,
    addToFavorite: AddToFavorite,
    removeFromFavorite: RemoveFromFavorite,
    storage: Editor.Storage,
    dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics
) : ObjectMenuViewModelBase(
    archiveDocument = archiveDocument,
    addToFavorite = addToFavorite,
    removeFromFavorite = removeFromFavorite,
    dispatcher = dispatcher
) {

    private val objectRestrictions = storage.objectRestrictions.current()

    override fun onIconClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenObjectIcons)
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_OBJ_MENU_ICON
        )
    }

    override fun onCoverClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenObjectCover)
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_OBJ_MENU_COVER
        )
    }

    override fun onLayoutClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.LAYOUT_CHANGE)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenObjectLayout)
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_OBJ_MENU_LAYOUT
        )
    }

    override fun onRelationsClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.RELATIONS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenObjectRelations)
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_OBJ_MENU_RELATIONS
        )
    }

    override fun onHistoryClicked() {
        viewModelScope.launch { _toasts.emit(COMING_SOON_MSG) }
    }

    override fun onActionClicked(ctx: Id, action: ObjectAction) {
        when (action) {
            ObjectAction.DELETE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = true)
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_OBJ_MENU_ARCHIVE
                )
            }
            ObjectAction.RESTORE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = false)
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_OBJ_MENU_RESTORE
                )
            }
            ObjectAction.ADD_TO_FAVOURITE -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_OBJ_MENU_FAVORITE
                )
                proceedWithAddingToFavorites(ctx)
            }
            ObjectAction.REMOVE_FROM_FAVOURITE -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_OBJ_MENU_UNFAVORITE
                )
                proceedWithRemovingFromFavorites(ctx)
            }
            else -> {
                viewModelScope.launch { _toasts.emit(COMING_SOON_MSG) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val archiveDocument: ArchiveDocument,
        private val addToFavorite: AddToFavorite,
        private val removeFromFavorite: RemoveFromFavorite,
        private val storage: Editor.Storage,
        private val analytics: Analytics,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ObjectMenuViewModel(
                archiveDocument = archiveDocument,
                addToFavorite = addToFavorite,
                removeFromFavorite = removeFromFavorite,
                storage = storage,
                analytics = analytics,
                dispatcher = dispatcher
            ) as T
        }
    }
}

class ObjectSetMenuViewModel(
    archiveDocument: ArchiveDocument,
    addToFavorite: AddToFavorite,
    removeFromFavorite: RemoveFromFavorite,
    dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    state: StateFlow<ObjectSet>
) : ObjectMenuViewModelBase(
    archiveDocument = archiveDocument,
    addToFavorite = addToFavorite,
    removeFromFavorite = removeFromFavorite,
    dispatcher = dispatcher
) {

    private val objectRestrictions = state.value.objectRestrictions

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val archiveDocument: ArchiveDocument,
        private val addToFavorite: AddToFavorite,
        private val removeFromFavorite: RemoveFromFavorite,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val state: StateFlow<ObjectSet>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ObjectSetMenuViewModel(
                archiveDocument = archiveDocument,
                addToFavorite = addToFavorite,
                removeFromFavorite = removeFromFavorite,
                analytics = analytics,
                state = state,
                dispatcher = dispatcher
            ) as T
        }
    }

    override fun onIconClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetIcons)
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_SET_MENU_ICON
        )
    }

    override fun onCoverClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetCover)
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_SET_MENU_COVER
        )
    }

    override fun onLayoutClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.LAYOUT_CHANGE)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetLayout)
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_SET_MENU_LAYOUT
        )
    }

    override fun onRelationsClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.RELATIONS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenSetRelations)
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_SET_MENU_RELATIONS
        )
    }

    override fun onHistoryClicked() {
        viewModelScope.launch { _toasts.emit(COMING_SOON_MSG) }
    }

    override fun buildActions(
        isArchived: Boolean,
        isFavorite: Boolean,
        isProfile: Boolean
    ): MutableList<ObjectAction> = mutableListOf<ObjectAction>().apply {
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
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_SET_MENU_ARCHIVE
                )
            }
            ObjectAction.RESTORE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = false)
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_SET_MENU_RESTORE
                )
            }
            ObjectAction.ADD_TO_FAVOURITE -> {
                proceedWithAddingToFavorites(ctx)
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_SET_MENU_FAVORITE
                )
            }
            ObjectAction.REMOVE_FROM_FAVOURITE -> {
                proceedWithRemovingFromFavorites(ctx)
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_SET_MENU_UNFAVORITE
                )
            }
            else -> {
                viewModelScope.launch { _toasts.emit(COMING_SOON_MSG) }
            }
        }
    }
}