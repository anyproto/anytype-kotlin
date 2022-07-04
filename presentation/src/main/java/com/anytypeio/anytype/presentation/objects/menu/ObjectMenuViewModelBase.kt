package com.anytypeio.anytype.presentation.objects.menu

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddToFavoritesEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsMoveToBinEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRemoveFromFavoritesEvent
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class ObjectMenuViewModelBase(
    private val setObjectIsArchived: SetObjectIsArchived,
    private val addToFavorite: AddToFavorite,
    private val removeFromFavorite: RemoveFromFavorite,
    protected val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val menuOptionsProvider: ObjectMenuOptionsProvider,
) : BaseViewModel() {

    private val jobs = mutableListOf<Job>()
    val isDismissed = MutableStateFlow(false)
    val isObjectArchived = MutableStateFlow(false)
    val commands = MutableSharedFlow<Command>(replay = 0)
    val actions = MutableStateFlow(emptyList<ObjectAction>())

    private val _options = MutableStateFlow(
        ObjectMenuOptionsProvider.Options(
            hasIcon = false,
            hasCover = false,
            hasLayout = false,
            hasRelations = false,
        )
    )
    val options: Flow<ObjectMenuOptionsProvider.Options> = _options

    abstract fun onIconClicked(ctx: Id)
    abstract fun onCoverClicked(ctx: Id)
    abstract fun onLayoutClicked(ctx: Id)
    abstract fun onRelationsClicked()

    fun onHistoryClicked() {
        throw IllegalStateException("History isn't supported yet")
    }

    fun onStop() {
        jobs.forEach(Job::cancel)
        jobs.clear()
    }

    fun onStart(
        ctx: Id,
        isFavorite: Boolean,
        isArchived: Boolean,
        isProfile: Boolean,
        isLocked: Boolean
    ) {
        actions.value = buildActions(
            ctx = ctx,
            isArchived = isArchived,
            isFavorite = isFavorite,
            isProfile = isProfile
        )
        jobs += viewModelScope.launch {
            menuOptionsProvider.provide(ctx, isLocked).collect(_options)
        }
    }

    abstract fun onActionClicked(ctx: Id, action: ObjectAction)

    abstract fun buildActions(
        ctx: Id,
        isArchived: Boolean,
        isFavorite: Boolean,
        isProfile: Boolean
    ): List<ObjectAction>

    protected fun proceedWithRemovingFromFavorites(ctx: Id) {
        viewModelScope.launch {
            removeFromFavorite(
                RemoveFromFavorite.Params(
                    target = ctx
                )
            ).process(
                failure = { Timber.e(it, "Error while removing from favorite.") },
                success = {
                    sendAnalyticsRemoveFromFavoritesEvent(analytics)
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
                    sendAnalyticsAddToFavoritesEvent(analytics)
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
                        sendAnalyticsMoveToBinEvent(analytics)
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

