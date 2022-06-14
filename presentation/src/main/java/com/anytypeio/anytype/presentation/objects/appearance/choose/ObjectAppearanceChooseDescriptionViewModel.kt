package com.anytypeio.anytype.presentation.objects.appearance.choose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.Dispatcher

class ObjectAppearanceChooseDescriptionViewModel(
    storage: Editor.Storage,
    setLinkAppearance: SetLinkAppearance,
    dispatcher: Dispatcher<Payload>
) : ObjectAppearanceChooseViewModelBase<ObjectAppearanceChooseSettingsView.Description>(
    storage, setLinkAppearance, dispatcher
) {
    override fun getItems(menu: BlockView.Appearance.Menu): List<ObjectAppearanceChooseSettingsView.Description> {
        val description = menu.description
        return listOf(
            ObjectAppearanceChooseSettingsView.Description.None(
                isSelected = description == BlockView.Appearance.MenuItem.Description.NONE
            ),
            ObjectAppearanceChooseSettingsView.Description.Added(
                isSelected = description == BlockView.Appearance.MenuItem.Description.ADDED
            ),
            ObjectAppearanceChooseSettingsView.Description.Content(
                isSelected = description == BlockView.Appearance.MenuItem.Description.CONTENT
            ),
        )
    }

    override fun updateAppearance(
        item: ObjectAppearanceChooseSettingsView.Description,
        oldContent: Block.Content.Link
    ): Block.Content.Link {
        return when (item) {
            is ObjectAppearanceChooseSettingsView.Description.None -> oldContent.copy(
                description = Block.Content.Link.Description.NONE
            )
            is ObjectAppearanceChooseSettingsView.Description.Added -> oldContent.copy(
                description = Block.Content.Link.Description.ADDED
            )
            is ObjectAppearanceChooseSettingsView.Description.Content -> oldContent.copy(
                description = Block.Content.Link.Description.CONTENT
            )
        }
    }

    class Factory(
        private val storage: Editor.Storage,
        private val setLinkAppearance: SetLinkAppearance,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearanceChooseDescriptionViewModel(
                storage,
                setLinkAppearance,
                dispatcher
            ) as T
        }
    }
}