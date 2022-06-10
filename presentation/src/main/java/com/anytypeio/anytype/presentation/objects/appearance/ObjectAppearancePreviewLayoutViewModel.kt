package com.anytypeio.anytype.presentation.objects.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.ext.getLinkAppearanceMenu
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem.PreviewLayout
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectAppearancePreviewLayoutViewModel(
    private val storage: Editor.Storage,
    private val setLinkAppearance: SetLinkAppearance,
    private val dispatcher: Dispatcher<Payload>
) : ViewModel() {

    val state = MutableSharedFlow<State>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart(blockId: Id) {
        jobs += viewModelScope.launch {
            val menu = storage.document.get().getLinkAppearanceMenu(
                blockId = blockId,
                details = storage.details.current()
            )
            if (menu != null) {
                val previewLayout = menu.preview
                state.emit(
                    State.Success(
                        items = listOf(
                            ObjectAppearanceSettingView.PreviewLayout.Text(
                                isSelected = previewLayout == PreviewLayout.TEXT
                            ),
                            ObjectAppearanceSettingView.PreviewLayout.Card(
                                isSelected = previewLayout == PreviewLayout.CARD
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

    fun onItemClicked(
        item: ObjectAppearanceSettingView.PreviewLayout,
        ctx: Id,
        blockId: Id
    ) {
        val block = storage.document.get().find { it.id == blockId }
        val content = block?.content
        if (block != null && content is Block.Content.Link) {
            val newContent = when (item) {
                is ObjectAppearanceSettingView.PreviewLayout.Text ->
                    content.copy(
                        cardStyle = Block.Content.Link.CardStyle.TEXT
                    )
                is ObjectAppearanceSettingView.PreviewLayout.Card ->
                    content.copy(
                        cardStyle = Block.Content.Link.CardStyle.CARD
                    )
            }
            proceedWithFieldsUpdate(ctx, blockId, newContent)
        }
    }

    private fun proceedWithFieldsUpdate(ctx: Id, blockId: Id, content: Block.Content.Link) {
        viewModelScope.launch {
            setLinkAppearance(
                SetLinkAppearance.Params(
                    contextId = ctx,
                    blockId = blockId,
                    content = content
                )
            ).proceed(
                failure = {
                    Timber.e(it, "Error while updating preview layout visibility for block")
                },
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
        private val setLinkAppearance: SetLinkAppearance,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearancePreviewLayoutViewModel(
                storage,
                setLinkAppearance,
                dispatcher
            ) as T
        }
    }

    sealed class State {
        data class Success(val items: List<ObjectAppearanceSettingView.PreviewLayout>) : State()
        data class Error(val msg: String) : State()
        object Dismiss : State()
    }
}