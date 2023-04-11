package com.anytypeio.anytype.presentation.objects.appearance.choose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.block.interactor.SetLinkAppearance
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView.Cover
import com.anytypeio.anytype.presentation.util.Dispatcher

class ObjectAppearanceChooseCoverViewModel(
    storage: Editor.Storage,
    setLinkAppearance: SetLinkAppearance,
    dispatcher: Dispatcher<Payload>
) : ObjectAppearanceChooseViewModelBase<Cover>(
    storage, setLinkAppearance, dispatcher
) {
    override fun getItems(menu: BlockView.Appearance.Menu): List<Cover> {
        val coverState = menu.cover
        return listOf(
            Cover.None(
                isSelected = coverState == BlockView.Appearance.MenuItem.Cover.WITHOUT
            ),
            Cover.Visible(
                isSelected = coverState == BlockView.Appearance.MenuItem.Cover.WITH
            )
        )
    }

    override fun updateAppearance(
        item: Cover,
        oldContent: Block.Content.Link
    ): Block.Content.Link {
        val relations = oldContent.relations
        return when (item) {
            is Cover.None -> {
                oldContent.copy(
                    relations = relations - Relations.COVER
                )
            }
            is Cover.Visible -> {
                oldContent.copy(
                    relations = relations + Relations.COVER
                )
            }
        }
    }

    class Factory(
        private val storage: Editor.Storage,
        private val setLinkAppearance: SetLinkAppearance,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearanceChooseCoverViewModel(
                storage,
                setLinkAppearance,
                dispatcher
            ) as T
        }
    }
}