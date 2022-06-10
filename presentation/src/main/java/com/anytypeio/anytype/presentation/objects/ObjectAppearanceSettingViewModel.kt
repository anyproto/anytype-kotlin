package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block.Content.Link
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.ext.getLinkAppearanceMenu
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView.Relation
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView.Section
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView.Settings
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectAppearanceSettingViewModel(
    private val storage: Editor.Storage,
    private val setLinkAppearance: SetLinkAppearance,
    private val dispatcher: Dispatcher<Payload>
) : ViewModel() {

    val objectPreviewState = MutableSharedFlow<State>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val paramsAppearance = MutableSharedFlow<BlockView.Appearance.Menu>()
    private val jobs = mutableListOf<Job>()

    init {
        viewModelScope.launch {
            paramsAppearance
                .collectLatest { onNewParams(it) }
        }
    }

    fun onStart(blockId: Id) {
        jobs += viewModelScope.launch {
            storage.document.observe().collectLatest { state ->
                val menu = state.getLinkAppearanceMenu(
                    blockId = blockId,
                    details = storage.details.current()
                )
                if (menu != null) {
                    paramsAppearance.emit(menu)
                }
            }
        }
    }

    private fun onNewParams(menu: BlockView.Appearance.Menu) {
        viewModelScope.launch {
            val views = initSettingsMenu(menu)
            objectPreviewState.emit(State.Success(views))
        }
    }

    private fun initSettingsMenu(menu: BlockView.Appearance.Menu): List<ObjectAppearanceSettingView> {
        return buildList {
            add(Settings.PreviewLayout(menu.preview))
            if (menu.icon != null) {
                add(Settings.Icon(menu.icon))
            }
            if (menu.cover != null) {
                add(Settings.Cover(menu.cover))
            }
            add(Section.FeaturedRelations)
            add(Relation.Name)
            if (menu.description != null) {
                add(Relation.Description(menu.description))
            }
        }
    }

    fun onItemClicked(item: ObjectAppearanceSettingView) {
        viewModelScope.launch {
            when (item) {
                is Settings.Cover -> {
                    commands.emit(Command.CoverScreen)
                }
                is Settings.Icon -> {
                    commands.emit(Command.IconScreen)
                }
                is Settings.PreviewLayout -> {
                    commands.emit(Command.PreviewLayoutScreen)
                }
                else -> {}
            }
        }
    }

    fun onToggleClicked(
        description: Relation.Description,
        ctx: Id,
        blockId: Id,
        isChecked: Boolean
    ) {
        val block = storage.document.get().firstOrNull { it.id == blockId }
        val content = block?.content
        if (block != null && content is Link) {
            if (isChecked != content.hasDescription) {
                val newContent = content.copy(
                    description = if (isChecked) {
                        Link.Description.ADDED
                    } else {
                        Link.Description.NONE
                    }
                )
                setLinkAppearance(ctx, blockId, newContent)
            }
        }
    }

    private fun setLinkAppearance(ctx: Id, blockId: Id, content: Link) {
        viewModelScope.launch {
            setLinkAppearance(
                SetLinkAppearance.Params(
                    contextId = ctx,
                    blockId = blockId,
                    content = content
                )
            ).proceed(
                failure = { Timber.e(it, "Error while set link appearance") },
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
        private val setLinkAppearance: SetLinkAppearance,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearanceSettingViewModel(storage, setLinkAppearance, dispatcher) as T
        }
    }
    //endregion
}