package com.anytypeio.anytype.presentation.objects.appearance.choose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.ext.getLinkAppearanceMenu
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class ObjectAppearanceChooseViewModelBase<T : ObjectAppearanceChooseSettingsView>(
    private val storage: Editor.Storage,
    private val setLinkAppearance: SetLinkAppearance,
    private val dispatcher: Dispatcher<Payload>
) : ViewModel() {

    val state = MutableSharedFlow<State<T>>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart(blockId: Id) {
        jobs += viewModelScope.launch {
            val menu = storage.document.get().getLinkAppearanceMenu(
                blockId = blockId,
                details = storage.details.current()
            )
            if (menu != null) {
                state.emit(
                    State.Success(
                        items = getItems(menu)
                    )
                )
            } else {
                Timber.e("Couldn't get appearance params for block link:$blockId")
                state.emit(State.Error("Error while getting preview settings"))
            }
        }
    }

    protected abstract fun getItems(menu: BlockView.Appearance.Menu): List<T>

    fun onItemClicked(
        item: T,
        ctx: Id,
        blockId: Id
    ) {
        val block = storage.document.get().find { it.id == blockId }
        val content = block?.content
        if (block != null && content is Block.Content.Link) {
            val newContent = updateAppearance(item, content)
            setNewLinkAppearance(ctx, blockId, newContent)
        }
    }

    protected abstract fun updateAppearance(
        item: T,
        oldContent: Block.Content.Link
    ): Block.Content.Link


    private fun setNewLinkAppearance(
        ctx: Id,
        blockId: Id,
        content: Block.Content.Link
    ) {
        viewModelScope.launch {
            setLinkAppearance(
                SetLinkAppearance.Params(
                    contextId = ctx,
                    blockId = blockId,
                    content = content
                )
            ).proceed(
                failure = {
                    Timber.e(it, "Error while updating link appearance to $content")
                },
                success = {
                    dispatcher.send(it)
                    state.emit(State.Dismiss())
                }
            )
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
    }

    sealed class State<T : ObjectAppearanceChooseSettingsView> {
        data class Success<T : ObjectAppearanceChooseSettingsView>(val items: List<T>) : State<T>()
        data class Error<T : ObjectAppearanceChooseSettingsView>(val msg: String) : State<T>()
        class Dismiss<T : ObjectAppearanceChooseSettingsView> : State<T>()
    }

}