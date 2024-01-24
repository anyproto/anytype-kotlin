package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetCreateBookmarkRecordViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val urlValidator: UrlValidator,
    private val spaceManager: SpaceManager,
    private val objectState: StateFlow<ObjectState>,
    private val dispatcher: Dispatcher<Payload>,
    private val addObjectToCollection: AddObjectToCollection,
    private val session: ObjectSetSession,
    private val storeOfRelations: StoreOfRelations,
    private val dateProvider: DateProvider
) : SetDataViewObjectNameViewModelBase() {

    override fun onActionDone(input: String) {
        viewModelScope.launch {
            proceedWithCreatingBookmarkObject(input)
        }
    }

    override fun onButtonClicked(input: String) {
        viewModelScope.launch {
            proceedWithCreatingBookmarkObject(input = input)
        }
    }

    private suspend fun proceedWithCreatingBookmarkObject(input: String) {
        if (urlValidator.isValid(url = input)) {
            val state = objectState.value.dataViewState() ?: return
            when (state) {
                is ObjectState.DataView.Collection -> {
                    val viewer = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return
                    val prefilled = viewer.prefillNewObjectDetails(
                        dateProvider = dateProvider,
                        storeOfRelations = storeOfRelations,
                        dataViewRelationLinks = state.objectRelationLinks
                    )
                    createBookmark(
                        input = input,
                        details = prefilled
                    ) { addBookmarkToCollection(it) }
                }

                is ObjectState.DataView.Set -> {
                    val setOf = state.getSetOfValue(state.root)
                    if (state.isSetByRelation(setOf)) {
                        val sourceDetails = state.details[setOf.firstOrNull()]
                        if (sourceDetails != null && sourceDetails.map.isNotEmpty()) {
                            val sourceObject = ObjectWrapper.Relation(sourceDetails.map)
                            val viewer = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return
                            val details = viewer.resolveSetByRelationPrefilledObjectData(
                                objSetByRelation = sourceObject,
                                dataViewRelationLinks = state.objectRelationLinks,
                                dateProvider = dateProvider,
                                storeOfRelations = storeOfRelations
                            )
                            createBookmark(
                                input = input,
                                details = details
                            ) { isCompleted.value = true }
                        }
                    } else {
                        createBookmark(
                            input = input
                        ) { isCompleted.value = true }
                    }
                }
            }
        } else {
            sendToast("Url is invalid.")
        }
    }

    private suspend fun addBookmarkToCollection(bookmarkObj: Id) {
        val state = objectState.value.dataViewState() ?: return
        val params = AddObjectToCollection.Params(
            ctx = state.root,
            after = "",
            targets = listOf(bookmarkObj)
        )
        addObjectToCollection.async(params).fold(
            onSuccess = { payload ->
                dispatcher.send(payload)
                isCompleted.value = true
            },
            onFailure = {
                Timber.e(it, "Error while adding bookmark object to collection")
            }
        )
    }

    private suspend fun createBookmark(
        input: String,
        details: Struct = emptyMap(),
        action: suspend (Id) -> Unit
    ) {
        val params = CreateBookmarkObject.Params(
            space = spaceManager.get(),
            url = input,
            details = details
        )
        createBookmarkObject(params).process(
            failure = {
                Timber.e(it, "Error while creating bookmark object")
                sendToast("Error while creating bookmark object")
            },
            success = { objId ->
                Timber.d("Created bookmark object with id: $objId")
                action(objId)
            }
        )
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
        private val objectState: StateFlow<ObjectState>,
        private val dispatcher: Dispatcher<Payload>,
        private val addObjectToCollection: AddObjectToCollection,
        private val session: ObjectSetSession,
        private val storeOfRelations: StoreOfRelations,
        private val dateProvider: DateProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetCreateBookmarkRecordViewModel(
                createBookmarkObject = createBookmarkObject,
                urlValidator = urlValidator,
                spaceManager = spaceManager,
                objectState = objectState,
                dispatcher = dispatcher,
                addObjectToCollection = addObjectToCollection,
                session = session,
                storeOfRelations = storeOfRelations,
                dateProvider = dateProvider
            ) as T
        }
    }
}