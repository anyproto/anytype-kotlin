package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.page.CreateObjectByTypeAndTemplate
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateObjectViewModel(
    private val createObject: CreateObjectByTypeAndTemplate,
    private val spaceManager: SpaceManager,
    private val awaitAccountStart: AwaitAccountStartManager
) : ViewModel() {

    val createObjectStatus = MutableSharedFlow<State>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart(type: Key) {
        Timber.d("CreateObjectViewModel: onStart, type:$type")
        onCreatePage(type)
    }

    private fun onCreatePage(type: Key) {
        jobs += viewModelScope.launch {
            awaitAccountStart
                .awaitStart()
                .flatMapLatest {
                    spaceManager.state()
                }
                .filter { state ->
                    state is SpaceManager.State.Space.Active || state is SpaceManager.State.NoSpace
                }
                .take(1)
                .collect { config ->
                    proceedWithObjectCreation(type, config)
                }
        }
    }

    private suspend fun proceedWithObjectCreation(
        type: Key,
        state: SpaceManager.State
    ) {
        Timber.d("CreateObjectViewModel: proceedWithObjectCreation, type:$type, state:$state")
        when(state) {
            is SpaceManager.State.Space.Active -> {
                val params = CreateObjectByTypeAndTemplate.Param(
                    typeKey = TypeKey(type),
                    space = SpaceId(state.config.space),
                    keys = ObjectSearchConstants.defaultKeysObjectType
                )
                createObject.async(params).fold(
                    onFailure = { e ->
                        Timber.e(e, "Error while creating a new object with type:$type")
                        createObjectStatus.emit(State.Error("Error while creating a new object"))
                    },
                    onSuccess = { result ->
                        when (result) {
                            CreateObjectByTypeAndTemplate.Result.ObjectTypeNotFound -> {
                                createObjectStatus.emit(State.Error("Object type not found"))
                            }
                            is CreateObjectByTypeAndTemplate.Result.Success -> {
                                createObjectStatus.emit(
                                    State.Success(
                                        id = result.objectId,
                                        layout = result.obj.layout,
                                        space = requireNotNull(result.obj.spaceId)
                                    )
                                )
                            }
                        }
                    }
                )
            }
            is SpaceManager.State.NoSpace -> {
                createObjectStatus.emit(State.Exit)
            }
            is SpaceManager.State.Space.Idle -> {
                // Do nothing.
            }
            is SpaceManager.State.Init -> {
                // Do nothing.
            }
        }
    }

    fun onStop() {
        Timber.d("CreateObjectViewModel: onStop")
        viewModelScope.launch {
            jobs.apply {
                forEach { it.cancel() }
                clear()
            }
        }
    }

    sealed class State {
        data class Success(val id: String, val layout: ObjectType.Layout?, val space: Id) : State()
        data class Error(val msg: String) : State()
        data object Exit : State()
    }

    class Factory @Inject constructor(
        private val createObject: CreateObjectByTypeAndTemplate,
        private val spaceManager: SpaceManager,
        private val awaitAccountStart: AwaitAccountStartManager
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateObjectViewModel(
                createObject = createObject,
                spaceManager = spaceManager,
                awaitAccountStart = awaitAccountStart
            ) as T
        }
    }
}