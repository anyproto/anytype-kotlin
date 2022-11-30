package com.anytypeio.anytype.presentation.objects.menu

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddToFavoritesEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsMoveToBinEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectLinkToEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRemoveFromFavoritesEvent
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
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
    private val addBackLinkToObject: AddBackLinkToObject,
    protected val delegator: Delegator<Action>,
    private val urlBuilder: UrlBuilder,
    protected val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val menuOptionsProvider: ObjectMenuOptionsProvider,
    private val duplicateObject: DuplicateObject
) : BaseViewModel() {

    protected val jobs = mutableListOf<Job>()
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
            hasDiagnosticsVisibility = false
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
        jobs += viewModelScope.launch {
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
        jobs += viewModelScope.launch {
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
        jobs += viewModelScope.launch {
            setObjectIsArchived(
                SetObjectIsArchived.Params(
                    context = ctx,
                    isArchived = isArchived
                )
            ).process(
                failure = {
                    Timber.e(it, MOVE_OBJECT_TO_BIN_ERR_MSG)
                    _toasts.emit(MOVE_OBJECT_TO_BIN_ERR_MSG)
                },
                success = {
                    if (isArchived) {
                        sendAnalyticsMoveToBinEvent(analytics)
                        _toasts.emit(MOVE_OBJECT_TO_BIN_SUCCESS_MSG)
                    } else {
                        _toasts.emit(RESTORE_OBJECT_SUCCESS_MSG)
                    }
                    isObjectArchived.value = true
                }
            )
        }
    }

    fun onLinkedMyselfTo(myself: Id, addTo: Id, fromName: String?) {
        jobs += viewModelScope.launch {
            addBackLinkToObject.execute(
                AddBackLinkToObject.Params(objectToLink = myself, objectToPlaceLink = addTo)
            ).fold(
                onSuccess = { obj ->
                    sendAnalyticsObjectLinkToEvent(analytics)
                    commands.emit(
                        Command.OpenSnackbar(
                            id = addTo,
                            currentObjectName = fromName,
                            targetObjectName = obj.getProperName(),
                            icon = ObjectIcon.from(obj, obj.layout, urlBuilder)
                        )
                    )
                },
                onFailure = {
                    Timber.e(it, "Error while adding link from object to object")
                }
            )
        }
    }

    protected fun proceedWithLinkTo() {
        jobs += viewModelScope.launch { commands.emit(Command.OpenLinkToChooser) }
    }

    fun proceedWithOpeningPage(id: Id) {
        viewModelScope.launch {
            delegator.delegate(Action.OpenObject(id))
        }
    }

    fun proceedWithDuplication(ctx: Id) {
        viewModelScope.launch {
            duplicateObject(ctx).process(
                failure = {
                    Timber.e(it, "Duplication error")
                    _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                },
                success = {
                    _toasts.emit("Your object is duplicated")
                    delegator.delegate(Action.Duplicate(it))
                }
            )
        }
    }

    open fun onDiagnosticsClicked(ctx: Id) {
        throw IllegalStateException("You should not call diagnostics for sets")
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
        object OpenLinkToChooser : Command()
        data class ShareDebugTree(val uri: Uri) : Command()
        data class OpenSnackbar(
            val id: Id,
            val currentObjectName: String?,
            val targetObjectName: String?,
            val icon: ObjectIcon
        ) : Command()
    }

    companion object {
        const val MOVE_OBJECT_TO_BIN_SUCCESS_MSG = "Moved to bin!"
        const val RESTORE_OBJECT_SUCCESS_MSG = "Object restored!"
        const val MOVE_OBJECT_TO_BIN_ERR_MSG =
            "Error while moving object to bin. Please, try again later."
        const val ADD_TO_FAVORITE_SUCCESS_MSG = "Object added to favorites."
        const val REMOVE_FROM_FAVORITE_SUCCESS_MSG = "Object removed from favorites."
        const val NOT_ALLOWED = "Not allowed for this object"
        const val OBJECT_IS_LOCKED_MSG = "Your object is locked"
        const val OBJECT_IS_UNLOCKED_MSG = "Your object is locked"
        const val SOMETHING_WENT_WRONG_MSG = "Something went wrong. Please, try again later."
    }
}