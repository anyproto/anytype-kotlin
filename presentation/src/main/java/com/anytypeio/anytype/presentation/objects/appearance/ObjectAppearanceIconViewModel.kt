package com.anytypeio.anytype.presentation.objects.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Fields.Companion.ICON_SIZE_KEY
import com.anytypeio.anytype.core_models.Block.Fields.Companion.ICON_WITH_KEY
import com.anytypeio.anytype.core_models.Block.Fields.Companion.WITH_DESCRIPTION_KEY
import com.anytypeio.anytype.core_models.Block.Fields.Companion.WITH_NAME_KEY
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.ext.getAppearanceParamsOfBlockLink
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_LARGE
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_MEDIUM
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_SMALL
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView.Icon
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectAppearanceIconViewModel(
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
                val iconState = params.getObjectAppearanceIconState()
                state.emit(
                    State.Success(
                        items = listOf(
                            Icon.None(isSelected = iconState == ObjectAppearanceIconState.NONE),
                            // TODO small icons will be handled later
                            //Icon.Small(isSelected = iconState == ObjectAppearanceIconState.SMALL),
                            Icon.Medium(isSelected = iconState == ObjectAppearanceIconState.MEDIUM)
                            // TODO large icons will be handled later
                            //Icon.Large(isSelected = iconState == ObjectAppearanceIconState.LARGE)
                        )
                    )
                )
            } else {
                Timber.e("Couldn't get appearance params for block link:$blockId")
                state.emit(State.Error("Error while getting preview settings"))
            }
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
    }

    fun onItemClicked(item: ObjectAppearanceSettingView, ctx: Id, blockId: Id) {
        val block = storage.document.get().firstOrNull { it.id == blockId }
        if (block != null) {
            val fields = when (item) {
                is Icon.Large ->
                    block.fields.copy(
                        map = block.fields.map.toMutableMap().apply {
                            put(ICON_SIZE_KEY, LINK_ICON_SIZE_LARGE)
                            put(ICON_WITH_KEY, true)
                        }
                    )
                is Icon.Medium ->
                    block.fields.copy(
                        map = block.fields.map.toMutableMap().apply {
                            put(ICON_SIZE_KEY, LINK_ICON_SIZE_MEDIUM)
                            put(ICON_WITH_KEY, true)
                        }
                    )
                is Icon.Small ->
                    block.fields.copy(
                        map = block.fields.map.toMutableMap().apply {
                            put(ICON_SIZE_KEY, LINK_ICON_SIZE_SMALL)
                            put(ICON_WITH_KEY, true)
                        }
                    )
                is Icon.None ->
                    block.fields.copy(
                        map = block.fields.map.toMutableMap().apply {
                            put(ICON_WITH_KEY, false)
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
                failure = { Timber.e(it, "Error while updating icon size for object") },
                success = {
                    dispatcher.send(it)
                    state.emit(State.Dismiss)
                }
            )
        }
    }

    class Factory(
        private val storage: Editor.Storage,
        private val updateFields: UpdateFields,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearanceIconViewModel(storage, updateFields, dispatcher) as T
        }
    }

    sealed class State {
        data class Success(val items: List<Icon>) : State()
        object Dismiss : State()
        data class Error(val msg: String) : State()
    }
}