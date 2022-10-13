package com.anytypeio.anytype.presentation.objects.appearance.choose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem.PreviewLayout
import com.anytypeio.anytype.presentation.util.Dispatcher

class ObjectAppearanceChoosePreviewLayoutViewModel(
    storage: Editor.Storage,
    setLinkAppearance: SetLinkAppearance,
    dispatcher: Dispatcher<Payload>
) : ObjectAppearanceChooseViewModelBase<ObjectAppearanceChooseSettingsView.PreviewLayout>(
    storage, setLinkAppearance, dispatcher
) {
    override fun getItems(menu: BlockView.Appearance.Menu): List<ObjectAppearanceChooseSettingsView.PreviewLayout> {
        val previewLayout = menu.preview
        return listOf(
            ObjectAppearanceChooseSettingsView.PreviewLayout.Text(
                isSelected = previewLayout == PreviewLayout.TEXT
            ),
            ObjectAppearanceChooseSettingsView.PreviewLayout.Card(
                isSelected = previewLayout == PreviewLayout.CARD
            )
        )
    }

    override fun updateAppearance(
        item: ObjectAppearanceChooseSettingsView.PreviewLayout,
        oldContent: Block.Content.Link
    ): Block.Content.Link {
        return when (item) {
            is ObjectAppearanceChooseSettingsView.PreviewLayout.Text -> {
                val oldIconSize = oldContent.iconSize
                val newIconSize = if (oldIconSize == Block.Content.Link.IconSize.MEDIUM) {
                    Block.Content.Link.IconSize.SMALL
                } else {
                    oldIconSize
                }
                oldContent.copy(
                    cardStyle = Block.Content.Link.CardStyle.TEXT,
                    iconSize = newIconSize
                )
            }
            is ObjectAppearanceChooseSettingsView.PreviewLayout.Card ->
                oldContent.copy(
                    cardStyle = Block.Content.Link.CardStyle.CARD
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
            return ObjectAppearanceChoosePreviewLayoutViewModel(
                storage,
                setLinkAppearance,
                dispatcher
            ) as T
        }
    }
}