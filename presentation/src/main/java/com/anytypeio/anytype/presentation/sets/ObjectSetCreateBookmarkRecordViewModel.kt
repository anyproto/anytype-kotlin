package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetCreateBookmarkRecordViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val urlValidator: UrlValidator,
    private val spaceManager: SpaceManager
) : SetDataViewObjectNameViewModelBase() {

    override fun onActionDone(input: String) {
        proceedWithCreatingBookmarkObject(input)
    }

    override fun onButtonClicked(input: String) {
        proceedWithCreatingBookmarkObject(input = input)
    }

    private fun proceedWithCreatingBookmarkObject(input: String) {
        if (urlValidator.isValid(url = input)) {
            viewModelScope.launch {
                val params = CreateBookmarkObject.Params(
                    space = spaceManager.get(),
                    url = input
                )
                createBookmarkObject(params).process(
                    failure = {
                        Timber.e(it, "Error while creating bookmark object")
                        sendToast("Error while creating bookmark object")
                    },
                    success = { isCompleted.value = true }
                )
            }
        } else {
            sendToast("Url is invalid.")
        }
    }

    override fun onButtonClicked(target: Id, input: String) {
        // Do nothing.
    }

    override fun onActionDone(target: Id, input: String) {
        // Do nothing
    }

    class Factory(
        private val createBookmarkObject: CreateBookmarkObject,
        private val urlValidator: UrlValidator,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetCreateBookmarkRecordViewModel(
                createBookmarkObject = createBookmarkObject,
                urlValidator = urlValidator,
                spaceManager = spaceManager
            ) as T
        }
    }
}