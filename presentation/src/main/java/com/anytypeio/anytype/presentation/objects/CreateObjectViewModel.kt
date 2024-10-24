package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateObjectViewModel(
    private val createObject: CreateObject,
    private val spaceManager: SpaceManager,
    private val awaitAccountStart: AwaitAccountStartManager
) : ViewModel() {

    val createObjectStatus = MutableSharedFlow<State>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart(type: Key) {
        onCreatePage(type)
    }

    private fun onCreatePage(type: Key) {
        jobs += viewModelScope.launch {
            awaitAccountStart
                .awaitStart()
                .flatMapLatest {
                    spaceManager.observe()
                }
                .take(1)
                .collect { config ->
                    proceedWithObjectCreation(type, config)
                }
        }
    }

    private suspend fun proceedWithObjectCreation(
        type: Key,
        config: Config
    ) {
        createObject.execute(
            CreateObject.Param(
                type = TypeKey(type),
                space = SpaceId(config.space)
            )
        ).fold(
            onFailure = { e ->
                Timber.e(e, "Error while creating a new object with type:$type")
            },
            onSuccess = { result ->
                createObjectStatus.emit(
                    State.Success(
                        id = result.objectId,
                        layout = result.obj.layout,
                        space = requireNotNull(result.obj.spaceId)
                    )
                )
            }
        )
    }

    fun onStop() {
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
    }

    class Factory @Inject constructor(
        private val createObject: CreateObject,
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