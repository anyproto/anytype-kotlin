package com.anytypeio.anytype.presentation.objects.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.ext.getAppearanceParamsOfBlockLink
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectAppearanceCoverViewModel(
    private val storage: Editor.Storage,
    private val updateFields: UpdateFields,
    private val dispatcher: Dispatcher<Payload>
) : ViewModel() {

    val state = MutableSharedFlow<State>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart(blockId: Id) {
        jobs += viewModelScope.launch {
            val params = storage.document.get().getAppearanceParamsOfBlockLink(
                blockId = blockId,
                details = storage.details.current()
            )
            if (params != null) {
                val coverState = params.getObjectAppearanceCoverState()
                state.emit(
                    State.Success(
                        items = listOf(
                            ObjectAppearanceSettingView.Cover.None(
                                isSelected = coverState == ObjectAppearanceCoverState.NONE
                            ),
                            ObjectAppearanceSettingView.Cover.Visible(
                                isSelected = coverState == ObjectAppearanceCoverState.VISIBLE
                            )
                        )
                    )
                )
            } else {
                Timber.e("Couldn't get appearance params for block link:$blockId")
                state.emit(State.Error("Error while getting preview settings"))
            }
        }
    }

    fun onItemClicked(item: ObjectAppearanceSettingView, ctx: Id, blockId: Id) {
        val block = storage.document.get().firstOrNull { it.id == blockId }
        if (block != null) {
            val fields = when (item) {
                is ObjectAppearanceSettingView.Cover.None ->
                    block.fields.copy(
                        map = block.fields.map.toMutableMap().apply {
                            put(Block.Fields.COVER_WITH_KEY, false)
                        }
                    )
                is ObjectAppearanceSettingView.Cover.Visible ->
                    block.fields.copy(
                        map = block.fields.map.toMutableMap().apply {
                            put(Block.Fields.COVER_WITH_KEY, true)
                        }
                    )
                else -> throw UnsupportedOperationException("Wrong item type:$item")
            }
            proceedWithFieldsUpdate(ctx, blockId, fields)
        }
    }

    private fun proceedWithFieldsUpdate(ctx: Id, blockId: Id, fields: Block.Fields) {
        viewModelScope.launch {
            updateFields.invoke(
                UpdateFields.Params(
                    context = ctx,
                    fields = listOf(Pair(blockId, fields))
                )
            ).proceed(
                failure = { Timber.e(it, "Error while updating cover visibility for block") },
                success = {
                    dispatcher.send(it)
                    state.emit(State.Dismiss)
                }
            )
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
    }

    class Factory(
        private val storage: Editor.Storage,
        private val updateFields: UpdateFields,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearanceCoverViewModel(storage, updateFields, dispatcher) as T
        }
    }

    sealed class State {
        data class Success(val items: List<ObjectAppearanceSettingView.Cover>) : State()
        data class Error(val msg: String) : State()
        object Dismiss : State()
    }
}