package com.anytypeio.anytype.presentation.`object`

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.CheckIsFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.page.ArchiveDocument
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectMenuViewModel(
    private val archiveDocument: ArchiveDocument,
    private val addToFavorite: AddToFavorite,
    private val removeFromFavorite: RemoveFromFavorite,
    private val checkIsFavorite: CheckIsFavorite
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)

    val actions = MutableStateFlow(emptyList<ObjectAction>())

    fun onStart(ctx: Id) {
        viewModelScope.launch {
            checkIsFavorite(CheckIsFavorite.Params(ctx)).process(
                failure = {
                    Timber.e(it, "Error while checking is-favorite status for object")
                },
                success = { isFavorite ->
                    if (isFavorite)
                        actions.value = listOf(
                            ObjectAction.DELETE,
                            ObjectAction.REMOVE_FROM_FAVOURITE,
                            ObjectAction.SEARCH_ON_PAGE,
                            ObjectAction.USE_AS_TEMPLATE
                        )
                    else
                        actions.value = listOf(
                            ObjectAction.DELETE,
                            ObjectAction.ADD_TO_FAVOURITE,
                            ObjectAction.SEARCH_ON_PAGE,
                            ObjectAction.USE_AS_TEMPLATE
                        )
                }
            )
        }
    }

    fun onActionClick(ctx: Id, action: ObjectAction) {
        when (action) {
            ObjectAction.DELETE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = true)
            }
            ObjectAction.RESTORE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = false)
            }
            ObjectAction.ADD_TO_FAVOURITE -> {
                viewModelScope.launch {
                    addToFavorite(
                        AddToFavorite.Params(
                            target = ctx
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while adding to favorites.") },
                        success = {
                            _toasts.emit(ADD_TO_FAVORITE_SUCCESS_MSG).also {
                                isDismissed.value = true
                            }
                        }
                    )
                }
            }
            ObjectAction.REMOVE_FROM_FAVOURITE -> {
                viewModelScope.launch {
                    removeFromFavorite(
                        RemoveFromFavorite.Params(
                            target = ctx
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while removing from favorite.") },
                        success = {
                            _toasts.emit(REMOVE_FROM_FAVORITE_SUCCESS_MSG).also {
                                isDismissed.value = true
                            }
                        }
                    )
                }
            }
            else -> {
                viewModelScope.launch { _toasts.emit("TODO") }
            }
        }
    }

    private fun proceedWithUpdatingArchivedStatus(ctx: Id, isArchived: Boolean) {
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

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val archiveDocument: ArchiveDocument,
        private val addToFavorite: AddToFavorite,
        private val removeFromFavorite: RemoveFromFavorite,
        private val checkIsFavorite: CheckIsFavorite
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ObjectMenuViewModel(
                archiveDocument = archiveDocument,
                addToFavorite = addToFavorite,
                removeFromFavorite = removeFromFavorite,
                checkIsFavorite = checkIsFavorite
            ) as T
        }
    }

    companion object {
        const val ARCHIVE_OBJECT_SUCCESS_MSG = "Object archived!"
        const val RESTORE_OBJECT_SUCCESS_MSG = "Object restored!"
        const val ARCHIVE_OBJECT_ERR_MSG =
            "Error while changing is-archived status for this object. Please, try again later."
        const val ADD_TO_FAVORITE_SUCCESS_MSG = "Object added to favorites."
        const val REMOVE_FROM_FAVORITE_SUCCESS_MSG = "Object removed from favorites."
    }
}