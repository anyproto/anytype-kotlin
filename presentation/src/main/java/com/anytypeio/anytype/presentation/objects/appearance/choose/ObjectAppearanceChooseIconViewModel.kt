package com.anytypeio.anytype.presentation.objects.appearance.choose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView.Icon
import com.anytypeio.anytype.presentation.util.Dispatcher

class ObjectAppearanceChooseIconViewModel(
    storage: Editor.Storage,
    setLinkAppearance: SetLinkAppearance,
    dispatcher: Dispatcher<Payload>
) : ObjectAppearanceChooseViewModelBase<Icon>(
    storage, setLinkAppearance, dispatcher
) {
    override fun getItems(menu: BlockView.Appearance.Menu): List<Icon> {
        return menu.iconMenus
    }

    override fun updateAppearance(
        item: Icon,
        oldContent: Block.Content.Link
    ): Block.Content.Link {
        return when (item) {
            is Icon.Medium -> oldContent.copy(
                iconSize = Block.Content.Link.IconSize.MEDIUM
            )
            is Icon.Small ->
                oldContent.copy(
                    iconSize = Block.Content.Link.IconSize.SMALL
                )
            is Icon.None ->
                oldContent.copy(
                    iconSize = Block.Content.Link.IconSize.NONE
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
            return ObjectAppearanceChooseIconViewModel(
                storage,
                setLinkAppearance,
                dispatcher
            ) as T
        }
    }
}