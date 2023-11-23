package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetCreateBookmarkRecordViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val urlValidator: UrlValidator,
    private val spaceManager: SpaceManager,
    private val addObjectToCollection: AddObjectToCollection,
    private val dispatcher: Dispatcher<Payload>
) : SetDataViewObjectNameViewModelBase() {

    private var collectionId: Id? = null

    fun onStart(collectionId: Id?) {
        this.collectionId = collectionId
    }

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
                    success = {
                        addBookmarkToCollectionIfNeeded(bookmark = it)
                        isCompleted.value = true
                    }
                )
            }
        } else {
            Timber.w("Url is invalid.")
            sendToast("Url is invalid.")
        }
    }

    private suspend fun addBookmarkToCollectionIfNeeded(bookmark: Id) {
        val collection = collectionId
        if (collection != null) {
            val params = AddObjectToCollection.Params(
                ctx = collection,
                after = "",
                targets = listOf(bookmark)
            )
            addObjectToCollection.async(params).fold(
                onSuccess = { payload -> dispatcher.send(payload) },
                onFailure = {
                    Timber.e(it, "Error while adding bookmark to collection")
                    sendToast("Error while adding bookmark to collection")
                }
            )
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
        private val spaceManager: SpaceManager,
        private val addObjectToCollection: AddObjectToCollection,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetCreateBookmarkRecordViewModel(
                createBookmarkObject = createBookmarkObject,
                urlValidator = urlValidator,
                spaceManager = spaceManager,
                addObjectToCollection = addObjectToCollection,
                dispatcher = dispatcher
            ) as T
        }
    }
}