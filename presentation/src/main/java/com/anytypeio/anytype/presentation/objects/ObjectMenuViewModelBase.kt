package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
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
    private val setObjectIsArchived: SetObjectIsArchived,
    private val addToFavorite: AddToFavorite,
    private val removeFromFavorite: RemoveFromFavorite,
    protected val dispatcher: Dispatcher<Payload>
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)
    val isObjectArchived = MutableStateFlow(false)
    val commands = MutableSharedFlow<Command>(replay = 0)
    val actions = MutableStateFlow(emptyList<ObjectAction>())

    abstract fun onIconClicked(ctx: Id)
    abstract fun onCoverClicked(ctx: Id)
    abstract fun onLayoutClicked(ctx: Id)
    abstract fun onRelationsClicked()
    abstract fun onHistoryClicked()
    fun onStart(
        ctx: Id,
        isFavorite: Boolean,
        isArchived: Boolean,
        isProfile: Boolean
    ) {
        actions.value = buildActions(
            ctx = ctx,
            isArchived = isArchived,
            isFavorite = isFavorite,
            isProfile = isProfile
        )
    }
    abstract fun onActionClicked(ctx: Id, action: ObjectAction)

    abstract fun buildActions(
        ctx: Id,
        isArchived: Boolean,
        isFavorite: Boolean,
        isProfile: Boolean
    ): MutableList<ObjectAction>

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
            setObjectIsArchived(
                SetObjectIsArchived.Params(
                    context = ctx,
                    isArchived = isArchived
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
                    isObjectArchived.value = true
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
        const val OBJECT_IS_LOCKED_MSG = "Your object is locked"
        const val OBJECT_IS_UNLOCKED_MSG = "Your object is locked"
        const val SOMETHING_WENT_WRONG_MSG = "Something went wrong. Please, try again later."
    }
}

class ObjectMenuViewModel(
    setObjectIsArchived: SetObjectIsArchived,
    addToFavorite: AddToFavorite,
    removeFromFavorite: RemoveFromFavorite,
    dispatcher: Dispatcher<Payload>,
    private val storage: Editor.Storage,
    private val analytics: Analytics,
    private val updateFields: UpdateFields
) : ObjectMenuViewModelBase(
    setObjectIsArchived = setObjectIsArchived,
    addToFavorite = addToFavorite,
    removeFromFavorite = removeFromFavorite,
    dispatcher = dispatcher
) {

    private val objectRestrictions = storage.objectRestrictions.current()

    override fun buildActions(
        ctx: Id,
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
        add(ObjectAction.UNDO_REDO)
        if (isFavorite) {
            add(ObjectAction.REMOVE_FROM_FAVOURITE)
        } else {
            add(ObjectAction.ADD_TO_FAVOURITE)
        }
        add(ObjectAction.SEARCH_ON_PAGE)

        val root = storage.document.get().find { it.id == ctx }
        if (root != null) {
            if (root.fields.isLocked == true) {
                add(ObjectAction.UNLOCK)
            } else {
                add(ObjectAction.LOCK)
            }
        }

        add(ObjectAction.USE_AS_TEMPLATE)
    }

    override fun onIconClicked(ctx: Id) {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                try {
                    if (!isThisObjectLocked(ctx)) {
                        commands.emit(Command.OpenObjectIcons)
                    } else {
                        _toasts.emit("Your object is locked.")
                    }
                } catch (e: Exception) {
                    _toasts.emit("Something went wrong. Please, try again later.")
                }
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_OBJ_MENU_ICON
        )
    }

    override fun onCoverClicked(ctx: Id) {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                try {
                    if (!isThisObjectLocked(ctx)) {
                        commands.emit(Command.OpenObjectCover)
                    } else {
                        _toasts.emit("Your object is locked.")
                    }
                } catch (e: Exception) {
                    _toasts.emit("Something went wrong. Please, try again later.")
                }
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_OBJ_MENU_COVER
        )
    }

    override fun onLayoutClicked(ctx: Id) {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.LAYOUT_CHANGE)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                try {
                    if (!isThisObjectLocked(ctx)) {
                        commands.emit(Command.OpenObjectLayout)
                    } else {
                        _toasts.emit("Your object is locked.")
                    }
                } catch (e: Exception) {
                    _toasts.emit("Something went wrong. Please, try again later.")
                }
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
            ObjectAction.UNLOCK -> {
                proceedWithUpdatingLockStatus(ctx, false)
            }
            ObjectAction.LOCK -> {
                proceedWithUpdatingLockStatus(ctx, true)
            }
            else -> {
                viewModelScope.launch { _toasts.emit(COMING_SOON_MSG) }
            }
        }
    }

    private fun proceedWithUpdatingLockStatus(
        ctx: Id,
        isLocked: Boolean
    ) {
        val root = storage.document.get().find { it.id == ctx }
        if (root != null) {
            viewModelScope.launch {
                updateFields(
                    UpdateFields.Params(
                        context = ctx,
                        fields = listOf(
                            ctx to root.fields.copy(
                                map = root.fields.map.toMutableMap().apply {
                                    put(Block.Fields.IS_LOCKED_KEY, isLocked)
                                }
                            )
                        )
                    )
                ).proceed(
                    success = {
                        dispatcher.send(it)
                        if (isLocked) {
                            _toasts.emit(OBJECT_IS_LOCKED_MSG).also {
                                isDismissed.value = true
                            }
                        } else {
                            _toasts.emit(OBJECT_IS_UNLOCKED_MSG).also {
                                isDismissed.value = true
                            }
                        }
                    },
                    failure = {
                        Timber.e(it, "Error while updating lock-status for object")
                        _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                    }
                )
            }
        }
    }

    @Throws(Exception::class)
    fun isThisObjectLocked(ctx: Id) : Boolean {
        val doc = storage.document.get().first { it.id == ctx }
        return doc.fields.isLocked ?: false
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val setObjectIsArchived: SetObjectIsArchived,
        private val addToFavorite: AddToFavorite,
        private val removeFromFavorite: RemoveFromFavorite,
        private val storage: Editor.Storage,
        private val analytics: Analytics,
        private val dispatcher: Dispatcher<Payload>,
        private val updateFields: UpdateFields
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectMenuViewModel(
                setObjectIsArchived = setObjectIsArchived,
                addToFavorite = addToFavorite,
                removeFromFavorite = removeFromFavorite,
                storage = storage,
                analytics = analytics,
                dispatcher = dispatcher,
                updateFields = updateFields
            ) as T
        }
    }
}

class ObjectSetMenuViewModel(
    setObjectIsArchived: SetObjectIsArchived,
    addToFavorite: AddToFavorite,
    removeFromFavorite: RemoveFromFavorite,
    dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    state: StateFlow<ObjectSet>
) : ObjectMenuViewModelBase(
    setObjectIsArchived = setObjectIsArchived,
    addToFavorite = addToFavorite,
    removeFromFavorite = removeFromFavorite,
    dispatcher = dispatcher
) {

    private val objectRestrictions = state.value.objectRestrictions

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val setObjectIsArchived: SetObjectIsArchived,
        private val addToFavorite: AddToFavorite,
        private val removeFromFavorite: RemoveFromFavorite,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val state: StateFlow<ObjectSet>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetMenuViewModel(
                setObjectIsArchived = setObjectIsArchived,
                addToFavorite = addToFavorite,
                removeFromFavorite = removeFromFavorite,
                analytics = analytics,
                state = state,
                dispatcher = dispatcher
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
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_SET_MENU_ICON
        )
    }

    override fun onCoverClicked(ctx: Id) {
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

    override fun onLayoutClicked(ctx: Id) {
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
        ctx: Id,
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