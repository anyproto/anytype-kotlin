package com.anytypeio.anytype.presentation.`object`

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.page.ArchiveDocument
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectMenuViewModel(
    private val archiveDocument: ArchiveDocument
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)

    val actions = MutableStateFlow(
        listOf(
            ObjectAction.DELETE,
            ObjectAction.ADD_TO_FAVOURITE,
            ObjectAction.SEARCH_ON_PAGE,
            ObjectAction.USE_AS_TEMPLATE
        )
    )

    fun onActionClick(ctx: Id, action: ObjectAction) {
        when (action) {
            ObjectAction.DELETE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = true)
            }
            ObjectAction.RESTORE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = false)
            }
            ObjectAction.ADD_TO_FAVOURITE -> TODO()
            ObjectAction.MOVE_TO -> TODO()
            ObjectAction.SEARCH_ON_PAGE -> TODO()
            ObjectAction.USE_AS_TEMPLATE -> TODO()
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
        private val archiveDocument: ArchiveDocument
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ObjectMenuViewModel(
                archiveDocument = archiveDocument
            ) as T
        }
    }

    companion object {
        const val ARCHIVE_OBJECT_SUCCESS_MSG = "Object archived!"
        const val RESTORE_OBJECT_SUCCESS_MSG = "Object restored!"
        const val ARCHIVE_OBJECT_ERR_MSG =
            "Error while changing is-archived status for this object. Please, try again later."
    }
}