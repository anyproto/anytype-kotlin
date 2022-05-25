package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.ext.getAppearanceParamsOfBlockLink
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.appearance.getObjectAppearanceIconState
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectAppearanceSettingViewModel(
    private val storage: Editor.Storage,
    private val updateFields: UpdateFields,
    private val dispatcher: Dispatcher<Payload>
) : ViewModel() {

    val objectPreviewState = MutableSharedFlow<State>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val paramsAppearance = MutableSharedFlow<BlockView.Appearance.Params?>()
    private val jobs = mutableListOf<Job>()

    init {
        viewModelScope.launch {
            paramsAppearance
                .filterNotNull()
                .collectLatest { onNewParams(it) }
        }
    }

    fun onStart(blockId: Id) {
        jobs += viewModelScope.launch {
            storage.document.observe().collectLatest { state ->
                val params = state.getAppearanceParamsOfBlockLink(
                    blockId = blockId,
                    details = storage.details.current()
                )
                paramsAppearance.emit(params)
            }
        }
    }

    private fun onNewParams(params: BlockView.Appearance.Params) {
        viewModelScope.launch {
            val views = initSettingsMenu(params)
            objectPreviewState.emit(State.Success(views))
        }
    }

    private fun initSettingsMenu(params: BlockView.Appearance.Params): List<ObjectAppearanceSettingView> {
        val menus = mutableListOf<ObjectAppearanceSettingView>()
        menus.add(ObjectAppearanceSettingView.Settings.PreviewLayout(params.style))
        if (params.canHaveIcon) {
            val iconState = params.getObjectAppearanceIconState()
            menus.add(ObjectAppearanceSettingView.Settings.Icon(iconState))
        }
        if (params.canHaveCover) {
            menus.add(ObjectAppearanceSettingView.Settings.Cover(params.withCover))
        }
        menus.add(ObjectAppearanceSettingView.Section.FeaturedRelations)
        menus.add(ObjectAppearanceSettingView.Relation.Name(params.withName))
        if (params.canHaveDescription) {
            menus.add(ObjectAppearanceSettingView.Relation.Description(params.withDescription))
        }
        return menus
    }

    fun onItemClicked(item: ObjectAppearanceSettingView) {
        viewModelScope.launch {
            when (item) {
                is ObjectAppearanceSettingView.Settings.Cover -> {
                    commands.emit(Command.CoverScreen)
                }
                is ObjectAppearanceSettingView.Settings.Icon -> {
                    commands.emit(Command.IconScreen)
                }
                is ObjectAppearanceSettingView.Settings.PreviewLayout -> {
                    commands.emit(Command.PreviewLayoutScreen)
                }
                else -> {}
            }
        }
    }

    fun onToggleClicked(
        item: ObjectAppearanceSettingView,
        ctx: Id,
        blockId: Id,
        isChecked: Boolean
    ) {
        val block = storage.document.get().firstOrNull { it.id == blockId }
        if (block != null) {
            when (item) {
                is ObjectAppearanceSettingView.Relation.Description -> {
                    if (isChecked != block.fields.withDescription) {
                        val fields = block.fields.copy(
                            map = block.fields.map.toMutableMap().apply {
                                put(Block.Fields.WITH_DESCRIPTION_KEY, isChecked)
                            }
                        )
                        proceedWithFieldsUpdate(ctx, blockId, fields)
                    }
                }
                is ObjectAppearanceSettingView.Relation.Name -> {
                    if (isChecked != block.fields.withName) {
                        val fields = block.fields.copy(
                            map = block.fields.map.toMutableMap().apply {
                                put(Block.Fields.WITH_NAME_KEY, isChecked)
                            }
                        )
                        proceedWithFieldsUpdate(ctx, blockId, fields)
                    }
                }
                else -> throw UnsupportedOperationException("Wrong item type:$item")
            }
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
                success = { dispatcher.send(it) }
            )
        }
    }

    //region STATE
    sealed class State {
        data class Success(val data: List<ObjectAppearanceSettingView>) : State()
        data class Error(val msg: String) : State()
    }

    sealed class Command {
        object IconScreen : Command()
        object CoverScreen : Command()
        object PreviewLayoutScreen : Command()
    }
    //endregion

    //region FACTORY
    class Factory(
        private val storage: Editor.Storage,
        private val updateFields: UpdateFields,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearanceSettingViewModel(storage, updateFields, dispatcher) as T
        }
    }
    //endregion
}