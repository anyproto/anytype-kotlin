package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.ext.getAppearanceParamsOfBlockLink
import com.anytypeio.anytype.presentation.objects.appearance.getObjectAppearanceIconState
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectAppearanceSettingViewModel(
    private val storage: Editor.Storage,
    private val updateFields: UpdateFields,
    private val dispatcher: Dispatcher<Payload>
) : ViewModel() {

    val objectPreviewState = MutableSharedFlow<State>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart(blockId: Id) {
        jobs += viewModelScope.launch {
            val params = storage.document.get().getAppearanceParamsOfBlockLink(
                blockId = blockId,
                details = storage.details.current()
            )
            if (params != null) {
                with(params) {
                    val iconState = params.getObjectAppearanceIconState()
                    val views = listOf(
                        ObjectAppearanceSettingView.Settings.PreviewLayout(style = style),
                        ObjectAppearanceSettingView.Settings.Icon(iconState),
                        ObjectAppearanceSettingView.Settings.Cover(withCover = withCover),
                        ObjectAppearanceSettingView.Section.FeaturedRelations,
                        ObjectAppearanceSettingView.Relation.Name(withName = withName),
                        ObjectAppearanceSettingView.Relation.Description(withDescription = withDescription)
                    )
                    viewModelScope.launch {
                        objectPreviewState.emit(State.Success(views))
                    }
                }
            } else {
                Timber.e("Couldn't get appearance params for block link:$blockId")
                objectPreviewState.emit(State.Error("Error while getting preview settings"))
            }
        }
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