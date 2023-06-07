package com.anytypeio.anytype.presentation.objects.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block.Content.Link
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.ext.getLinkAppearanceMenu
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.Cover
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.FeaturedRelationsSection
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.Icon
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.PreviewLayout
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.Relation
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

    private fun initSettingsMenu(menu: BlockView.Appearance.Menu): List<ObjectAppearanceMainSettingsView> {
        return buildList {
            add(PreviewLayout(menu.preview))
            if (menu.icon != null) {
                add(Icon(menu.icon))
            }
            if (menu.cover != null) {
                add(Cover(menu.cover))
            }
            add(FeaturedRelationsSection)
            add(Relation.Name)
            if (menu.description != null) {
                add(Relation.Description(menu.description))
            }
            add(Relation.ObjectType(menu.objectType))
        }
    }

    fun onItemClicked(item: ObjectAppearanceMainSettingsView) {
        viewModelScope.launch {
            when (item) {
                is Icon -> commands.emit(Command.IconScreen)
                is PreviewLayout -> commands.emit(Command.PreviewLayoutScreen)
                is Relation.Description -> commands.emit(Command.DescriptionScreen)
                else -> throw IllegalArgumentException("Can't handle click on $item")
            }
        }
    }

    fun onToggleClicked(
        toggle: ObjectAppearanceMainSettingsView.Toggle,
        ctx: Id,
        blockId: Id,
        isChecked: Boolean
    ) {
        val block = storage.document.get().firstOrNull { it.id == blockId }
        val content = block?.content
        if (block != null && content is Link) {
            if (isChecked != toggle.checked) {
                val newContent = when (toggle) {
                    is Relation.ObjectType -> content.copy(
                        relations = if (isChecked) {
                            content.relations + Relations.TYPE
                        } else {
                            content.relations - Relations.TYPE
                        }
                    )
                }
                setLinkAppearance(ctx, blockId, newContent)
            }
        }
    }

    fun updateCoverAppearance(ctx: Id, blockId: Id, isCoverVisible: Boolean) {
        val block = storage.document.get().find { it.id == blockId }
        if (block != null && block.content is Link) {
            val content = block.content as Link
            val newContent = updateAppearance(isCoverVisible, content)
            setLinkAppearance(ctx, blockId, newContent)
        }
    }

    private fun updateAppearance(isCoverVisible: Boolean, oldContent: Link): Link {
        val relations = oldContent.relations
        val updatedRelations = if (isCoverVisible) {
            relations + Relations.COVER
        } else {
            relations - Relations.COVER
        }
        return oldContent.copy(relations = updatedRelations)
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

    sealed class State {
        data class Success(val data: List<ObjectAppearanceMainSettingsView>) : State()
        data class Error(val msg: String) : State()
    }

    sealed class Command {
        object IconScreen : Command()
        object PreviewLayoutScreen : Command()
        object DescriptionScreen : Command()
    }

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
}