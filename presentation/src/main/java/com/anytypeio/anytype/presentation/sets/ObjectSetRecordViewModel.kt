package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.ObjectStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetRecordViewModel(
    private val setObjectDetails: UpdateDetail,
    private val objectStore: ObjectStore
) : SetDataViewObjectNameViewModelBase() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    override fun onActionDone(target: Id, input: String) {
        viewModelScope.launch {
            val obj = objectStore.get(target)
            if (obj?.id != null) {
                setObjectDetails(
                    UpdateDetail.Params(
                        ctx = obj.id,
                        key = Relations.NAME,
                        value = input
                    )
                ).process(
                    failure = { Timber.e(it, "Error while updating data view record") },
                    success = { isCompleted.value = true }
                )
            } else {
                Timber.e("Couldn't find record or retrieve record id")
            }
        }
    }

    override fun onButtonClicked(target: Id, input: String) {
        viewModelScope.launch {
            val obj = objectStore.get(target)
            if (obj?.id != null) {
                if (input.isEmpty()) {
                    commands.emit(Command.OpenObject(obj.id))
                } else {
                    setObjectDetails(
                        UpdateDetail.Params(
                            ctx = obj.id,
                            key = Relations.NAME,
                            value = input
                        )
                    ).process(
                        failure = {
                            Timber.e(it, "Error while updating data view record")
                            commands.emit(Command.OpenObject(obj.id))
                        },
                        success = {
                            commands.emit(Command.OpenObject(obj.id))
                        }
                    )
                }
            } else {
                Timber.e("Couldn't find record or retrieve record id")
            }
        }
    }

    override fun onButtonClicked(input: String) {
        // Do nothing.
    }

    override fun onActionDone(input: String) {
        // Do nothing
    }

    class Factory(
        private val setObjectDetails: UpdateDetail,
        private val objectStore: ObjectStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetRecordViewModel(
                setObjectDetails = setObjectDetails,
                objectStore = objectStore
            ) as T
        }
    }

    sealed class Command {
        data class OpenObject(val ctx: Id) : Command()
    }
}