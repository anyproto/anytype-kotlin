package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetRecordViewModel(
    private val updateDataViewRecord: UpdateDataViewRecord,
    private val objectSetState: StateFlow<ObjectSet>,
    private val objectSetRecordCache: ObjectSetRecordCache
) : ObjectSetCreateRecordViewModelBase() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    override fun onComplete(ctx: Id, input: String) {
        val record = objectSetRecordCache.map[ctx]
        val id = record?.get(ObjectSetConfig.ID_KEY) as String?
        if (record != null && id != null) {
            val block = objectSetState.value.blocks.first { it.content is DV }
            val update = mapOf(ObjectSetConfig.NAME_KEY to input)
            viewModelScope.launch {
                updateDataViewRecord(
                    UpdateDataViewRecord.Params(
                        context = ctx,
                        record = id,
                        target = block.id,
                        values = update
                    )
                ).process(
                    failure = { Timber.e(it, "Error while updating data view record") },
                    success = { isCompleted.value = true }
                )
            }
        } else {
            Timber.e("Couldn't find record or retrieve record id")
        }
    }

    override fun onButtonClicked(ctx: Id, input: String) {
        viewModelScope.launch {
            val record = objectSetRecordCache.map[ctx]
            val id = record?.get(ObjectSetConfig.ID_KEY) as String?
            if (record != null && id != null) {
                if (input.isEmpty()) {
                    commands.emit(Command.OpenObject(id))
                } else {
                    val block = objectSetState.value.blocks.first { it.content is DV }
                    val update = mapOf(ObjectSetConfig.NAME_KEY to input)
                    updateDataViewRecord(
                        UpdateDataViewRecord.Params(
                            context = ctx,
                            record = id,
                            target = block.id,
                            values = update
                        )
                    ).process(
                        failure = {
                            Timber.e(it, "Error while updating data view record")
                            commands.emit(Command.OpenObject(id))
                        },
                        success = {
                            commands.emit(Command.OpenObject(id))
                        }
                    )
                }
            } else {
                Timber.e("Couldn't find record or retrieve record id")
            }
        }
    }

    class Factory(
        private val updateDataViewRecord: UpdateDataViewRecord,
        private val objectSetState: StateFlow<ObjectSet>,
        private val objectSetRecordCache: ObjectSetRecordCache
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetRecordViewModel(
                objectSetState = objectSetState,
                updateDataViewRecord = updateDataViewRecord,
                objectSetRecordCache = objectSetRecordCache
            ) as T
        }
    }

    sealed class Command {
        data class OpenObject(val ctx: Id) : Command()
    }
}