package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetCreateBookmarkRecordViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val urlValidator: UrlValidator,
    private val sideEffectDelegator: MutableSharedFlow<List<ObjectSetReducer.SideEffect>>
) : ObjectSetCreateRecordViewModelBase() {

    override fun onComplete(ctx: Id, input: String) {
        proceedWithCreatingBookmarkObject(input)
    }

    override fun onButtonClicked(ctx: Id, input: String) {
        proceedWithCreatingBookmarkObject(input)
    }

    private fun proceedWithCreatingBookmarkObject(input: String) {
        if (urlValidator.isValid(url = input)) {
            viewModelScope.launch {
                createBookmarkObject(input).process(
                    failure = {
                        Timber.e(it, "Error while creating bookmark object")
                        sendToast("Error while creating bookmark object")
                    },
                    success = {
                        // Workaround to update view after bookmark creation.
                        // Remove it when new subscription API is adapted in sets.
                        sideEffectDelegator.emit(listOf(ObjectSetReducer.SideEffect.ResetViewer))
                        isCompleted.value = true
                    }
                )
            }
        } else {
            sendToast("Url is invalid.")
        }
    }

    class Factory(
        private val createBookmarkObject: CreateBookmarkObject,
        private val urlValidator: UrlValidator,
        private val sideEffectDelegator: MutableSharedFlow<List<ObjectSetReducer.SideEffect>>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetCreateBookmarkRecordViewModel(
                createBookmarkObject = createBookmarkObject,
                urlValidator = urlValidator,
                sideEffectDelegator = sideEffectDelegator
            ) as T
        }
    }
}