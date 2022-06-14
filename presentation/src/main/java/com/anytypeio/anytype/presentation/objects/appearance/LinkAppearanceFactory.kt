package com.anytypeio.anytype.presentation.objects.appearance

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.Link.CardStyle
import com.anytypeio.anytype.core_models.Block.Content.Link.IconSize
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.InEditor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem

internal class LinkAppearanceFactory(
    private val content: Block.Content.Link,
    layout: ObjectType.Layout?
) {

    private val isTodoLayout = layout == ObjectType.Layout.TODO
    private val isNoteLayout = layout == ObjectType.Layout.NOTE
    private val withDescription = content.cardStyle != CardStyle.TEXT && !isNoteLayout

    //todo Cover menu option is off. No proper design yet.
    private val canHaveCover: Boolean =
        false && !isNoteLayout && content.cardStyle != CardStyle.TEXT

    private val withCover = canHaveCover && (content.hasCover)

    internal fun createInEditorLinkAppearance(): InEditor {
        val withIcon = when {
            isNoteLayout -> false
            isTodoLayout -> true
            else -> content.iconSize != IconSize.NONE
        }
        val description = when {
            !withDescription -> InEditor.Description.NONE
            else -> when(content.description) {
                Block.Content.Link.Description.NONE -> InEditor.Description.NONE
                Block.Content.Link.Description.ADDED -> InEditor.Description.RELATION
                Block.Content.Link.Description.CONTENT -> InEditor.Description.SNIPPET
            }

        }
        return InEditor(
            showIcon = withIcon,
            isCard = content.cardStyle == CardStyle.CARD,
            description = description,
            showCover = withCover,
            showType = content.hasType
        )
    }

    internal fun createAppearanceMenuItems(): BlockView.Appearance.Menu {
        val hasIconMenuItem = !isTodoLayout && !isNoteLayout
        val preview = when (content.cardStyle) {
            CardStyle.TEXT -> MenuItem.PreviewLayout.TEXT
            CardStyle.CARD -> MenuItem.PreviewLayout.CARD
            CardStyle.INLINE -> MenuItem.PreviewLayout.INLINE
        }
        val icon = if (hasIconMenuItem) {
            when (content.iconSize) {
                IconSize.NONE -> MenuItem.Icon.NONE
                IconSize.SMALL -> MenuItem.Icon.SMALL
                IconSize.MEDIUM -> MenuItem.Icon.MEDIUM
            }
        } else null
        val cover = if (canHaveCover) {
            when (withCover) {
                true -> MenuItem.Cover.WITH
                false -> MenuItem.Cover.WITHOUT
            }

        } else null
        val description = if (withDescription) {
            when (content.description) {
                Block.Content.Link.Description.NONE -> MenuItem.Description.NONE
                Block.Content.Link.Description.ADDED -> MenuItem.Description.ADDED
                Block.Content.Link.Description.CONTENT -> MenuItem.Description.CONTENT
            }
        } else null
        val objectType = when (content.hasType) {
            true -> MenuItem.ObjectType.WITH
            false -> MenuItem.ObjectType.WITHOUT
        }
        return BlockView.Appearance.Menu(
            preview = preview,
            icon = icon,
            cover = cover,
            description = description,
            objectType,
        )
    }
}