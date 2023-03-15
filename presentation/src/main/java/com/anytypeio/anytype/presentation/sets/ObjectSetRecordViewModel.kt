package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetRecordViewModel(
    private val setObjectDetails: UpdateDetail
) : SetDataViewObjectNameViewModelBase() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    override fun onActionDone(target: Id, input: String) {
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    target = target,
                    key = Relations.NAME,
                    value = input
                )
            ).process(
                failure = { Timber.e(it, "Error while updating data view record") },
                success = { isCompleted.value = true }
            )
        }
    }

    override fun onButtonClicked(target: Id, input: String) {
        viewModelScope.launch {
            if (input.isEmpty()) {
                commands.emit(Command.OpenObject(target))
            } else {
                setObjectDetails(
                    UpdateDetail.Params(
                        target = target,
                        key = Relations.NAME,
                        value = input
                    )
                ).process(
                    failure = {
                        Timber.e(it, "Error while updating data view record")
                        commands.emit(Command.OpenObject(target))
                    },
                    success = {
                        commands.emit(Command.OpenObject(target))
                    }
                )
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
        private val setObjectDetails: UpdateDetail
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetRecordViewModel(
                setObjectDetails = setObjectDetails
            ) as T
        }
    }

    sealed class Command {
        data class OpenObject(val ctx: Id) : Command()
    }
}