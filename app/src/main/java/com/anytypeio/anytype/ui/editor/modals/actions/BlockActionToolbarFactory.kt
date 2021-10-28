package com.anytypeio.anytype.ui.editor.modals.actions

import androidx.core.os.bundleOf
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types

@Deprecated("Legacy")
object BlockActionToolbarFactory {

    fun newInstance(block: BlockView, dimensions: BlockDimensions) = when (block) {
        is BlockView.Text.Paragraph -> newInstance(block, dimensions)
        is BlockView.Text.Header.One -> newInstance(block, dimensions)
        is BlockView.Text.Header.Two -> newInstance(block, dimensions)
        is BlockView.Text.Header.Three -> newInstance(block, dimensions)
        is BlockView.Text.Highlight -> newInstance(block, dimensions)
        is BlockView.Text.Checkbox -> newInstance(block, dimensions)
        is BlockView.Text.Bulleted -> newInstance(block, dimensions)
        is BlockView.Text.Numbered -> newInstance(block, dimensions)
        is BlockView.Text.Toggle -> newInstance(block, dimensions)
        is BlockView.Code -> newInstance(block, dimensions)
        is BlockView.Media.File -> newInstance(block, dimensions)
        is BlockView.Upload.File -> newInstance(block, dimensions)
        is BlockView.MediaPlaceholder.File -> newInstance(block, dimensions)
        is BlockView.Error.File -> newInstance(block, dimensions)
        is BlockView.Media.Video -> newInstance(block, dimensions)
        is BlockView.Upload.Video -> newInstance(block, dimensions)
        is BlockView.MediaPlaceholder.Video -> newInstance(block, dimensions)
        is BlockView.Error.Video -> newInstance(block, dimensions)
        is BlockView.LinkToObject.Default -> newInstance(block, dimensions)
        is BlockView.DividerLine -> newInstance(block, dimensions)
        is BlockView.DividerDots -> newInstance(block, dimensions)
        is BlockView.MediaPlaceholder.Bookmark -> newInstance(block, dimensions)
        is BlockView.Media.Bookmark -> newInstance(block, dimensions)
        is BlockView.Error.Bookmark -> newInstance(block, dimensions)
        is BlockView.Media.Picture -> newInstance(block, dimensions)
        is BlockView.MediaPlaceholder.Picture -> newInstance(block, dimensions)
        is BlockView.Error.Picture -> newInstance(block, dimensions)
        is BlockView.Upload.Picture -> newInstance(block, dimensions)
        is BlockView.Title.Basic -> TODO()
        is BlockView.Title.Profile -> TODO()
        is BlockView.Title.Archive -> TODO()
        is BlockView.Title.Todo -> TODO()
        is BlockView.Relation.Placeholder -> newInstance(block, dimensions)
        is BlockView.Relation.Related -> newInstance(block, dimensions)
        is BlockView.Description -> TODO()
        is BlockView.FeaturedRelation -> TODO()
        is BlockView.Unsupported -> TODO()
        is BlockView.TitleNote -> TODO()
        is BlockView.Latex -> TODO()
        is BlockView.LinkToObject.Archived -> TODO()
        is BlockView.LinkToObject.Deleted -> TODO()
    }

    fun newInstance(
        block: BlockView.Relation.Related,
        dimensions: BlockDimensions
    ) = when (block.getViewType()) {
        Types.HOLDER_RELATION_DEFAULT -> RelationDefaultActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }
        Types.HOLDER_RELATION_STATUS -> RelationStatusActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }
        Types.HOLDER_RELATION_TAGS -> RelationTagActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }
        Types.HOLDER_RELATION_OBJECT -> RelationObjectActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }
        Types.HOLDER_RELATION_FILE -> RelationFileActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }
        else -> throw IllegalArgumentException("Wrong BlockView.Relation type :${block.getViewType()}")
    }

    fun newInstance(
        block: BlockView.Relation.Placeholder,
        dimensions: BlockDimensions
    ): RelationPlaceholderActionToolbar =
        RelationPlaceholderActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.LinkToObject.Default, dimensions: BlockDimensions): PageBlockActionToolbar =
        PageBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Text.Paragraph,
        dimensions: BlockDimensions
    ): ParagraphBlockActionToolbar =
        ParagraphBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Text.Header.One,
        dimensions: BlockDimensions
    ): HeaderOneBlockActionToolbar =
        HeaderOneBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Text.Header.Two,
        dimensions: BlockDimensions
    ): HeaderTwoBlockActionToolbar =
        HeaderTwoBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Text.Header.Three,
        dimensions: BlockDimensions
    ): HeaderThreeBlockActionToolbar =
        HeaderThreeBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Error,
        dimensions: BlockDimensions
    ): ErrorActionToolbar =
        ErrorActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Text.Checkbox,
        dimensions: BlockDimensions
    ): CheckBoxBlockActionToolbar =
        CheckBoxBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Code, dimensions: BlockDimensions): CodeBlockActionToolbar =
        CodeBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Text.Highlight,
        dimensions: BlockDimensions
    ): HighlightBlockActionToolbar =
        HighlightBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Text.Numbered,
        dimensions: BlockDimensions
    ): NumberedBlockActionToolbar =
        NumberedBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Text.Toggle,
        dimensions: BlockDimensions
    ): ToggleBlockActionToolbar =
        ToggleBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Text.Bulleted,
        dimensions: BlockDimensions
    ): BulletedBlockActionToolbar =
        BulletedBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Media.File,
        dimensions: BlockDimensions
    ): FileBlockActionToolbar =
        FileBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.MediaPlaceholder,
        dimensions: BlockDimensions
    ): PlaceholderActionToolbar =
        PlaceholderActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Upload,
        dimensions: BlockDimensions
    ): UploadActionToolbar =
        UploadActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Media.Picture,
        dimensions: BlockDimensions
    ): PictureBlockActionToolbar =
        PictureBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Media.Video,
        dimensions: BlockDimensions
    ): VideoBlockActionToolbar =
        VideoBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.DividerLine, dimensions: BlockDimensions): DividerLineBlockActionToolbar =
        DividerLineBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.DividerDots, dimensions: BlockDimensions): DividerDotsBlockActionToolbar =
        DividerDotsBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.Media.Bookmark,
        dimensions: BlockDimensions
    ): BookmarkBlockActionToolbar =
        BookmarkBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }
}