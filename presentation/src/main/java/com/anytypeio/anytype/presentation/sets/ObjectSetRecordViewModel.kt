package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetRecordViewModel(
    private val setObjectDetails: SetObjectDetails
) : SetDataViewObjectNameViewModelBase() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        Timber.d("ObjectSetRecordViewModel init")
    }

    fun onTextChanged(input: String, target: Id) {
        updateDetails(input, target)
    }

    override fun onActionDone(target: Id, space: Id, input: String) {
        updateDetails(input, target,
            onSuccess = { isCompleted.value = true },
            onFailure = {}
        )
    }

    override fun onButtonClicked(target: Id, space: Id, input: String) {
        viewModelScope.launch {
            if (input.isEmpty()) {
                emitOpenObjectCommand(target, space)
            } else {
                updateDetails(input, target,
                    onSuccess = { emitOpenObjectCommand(target, space) },
                    onFailure = { emitOpenObjectCommand(target, space) }
                )
            }
        }
    }

    override fun onButtonClicked(input: String) {
        // Do nothing
    }

    override fun onActionDone(input: String) {
        // Do nothing
    }

    private fun updateDetails(
        input: String, target: Id,
        onSuccess: (suspend () -> Unit)? = null,
        onFailure: (suspend (Throwable) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = target,
                details = mapOf(Relations.NAME to input)
            )
            setObjectDetails.async(params).fold(
                onFailure = { error ->
                    Timber.e(error, "Error while updating data view record")
                    onFailure?.invoke(error)
                },
                onSuccess = { onSuccess?.invoke() }
            )
        }
    }

    private suspend fun emitOpenObjectCommand(target: Id, space: Id) {
        commands.emit(Command.OpenObject(ctx = target, space = space))
    }

    class Factory(
        private val setObjectDetails: SetObjectDetails
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetRecordViewModel(
                setObjectDetails = setObjectDetails
            ) as T
        }
    }

    sealed class Command {
        data class OpenObject(val ctx: Id, val space: Id) : Command()
    }
}