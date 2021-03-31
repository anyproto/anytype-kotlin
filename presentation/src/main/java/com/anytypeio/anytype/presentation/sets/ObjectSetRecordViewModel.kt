package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetRecordViewModel(
    private val updateDataViewRecord: UpdateDataViewRecord,
    private val objectSetState: StateFlow<ObjectSet>,
    private val objectSetRecordCache: ObjectSetRecordCache
) : ViewModel() {

    val isCompleted = MutableStateFlow(false)

    fun onComplete(ctx: Id, text: String) {
        val record = objectSetRecordCache.map[ctx]
        val id = record?.get(ObjectSetConfig.ID_KEY) as String?
        if (record != null && id != null) {
            val block = objectSetState.value.blocks.first { it.content is DV }
            val name = text.ifEmpty { DEFAULT_NAME }
            viewModelScope.launch {
                updateDataViewRecord(
                    UpdateDataViewRecord.Params(
                        context = ctx,
                        record = id,
                        target = block.id,
                        values = record.toMutableMap().apply {
                            put(ObjectSetConfig.NAME_KEY, name)
                        }
                    )
                ).process(
                    failure = { Timber.e(it, "Error while updating data view record") },
                    success = { isCompleted.value = true }
                )
            }
        } else {
            Timber.d("Couldn't found record or retrieve record id")
        }
    }

    class Factory(
        private val updateDataViewRecord: UpdateDataViewRecord,
        private val objectSetState: StateFlow<ObjectSet>,
        private val objectSetRecordCache: ObjectSetRecordCache
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ObjectSetRecordViewModel(
                objectSetState = objectSetState,
                updateDataViewRecord = updateDataViewRecord,
                objectSetRecordCache = objectSetRecordCache
            ) as T
        }
    }

    companion object {
        const val DEFAULT_NAME = "Untitled"
    }
}