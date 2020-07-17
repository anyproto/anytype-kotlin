package com.agileburo.anytype.ui.page.modals.actions

import androidx.core.os.bundleOf
import com.agileburo.anytype.core_ui.features.page.BlockDimensions
import com.agileburo.anytype.core_ui.features.page.BlockView

object BlockActionToolbarFactory {

    fun newInstance(block: BlockView, dimensions: BlockDimensions) = when (block) {
        is BlockView.Paragraph -> newInstance(block, dimensions)
        is BlockView.Title -> TODO()
        is BlockView.ProfileTitle -> TODO()
        is BlockView.HeaderOne -> newInstance(block, dimensions)
        is BlockView.HeaderTwo -> newInstance(block, dimensions)
        is BlockView.HeaderThree -> newInstance(block, dimensions)
        is BlockView.Highlight -> newInstance(block, dimensions)
        is BlockView.Code -> newInstance(block, dimensions)
        is BlockView.Checkbox -> newInstance(block, dimensions)
        is BlockView.Task -> newInstance(block, dimensions)
        is BlockView.Bulleted -> newInstance(block, dimensions)
        is BlockView.Numbered -> newInstance(block, dimensions)
        is BlockView.Toggle -> newInstance(block, dimensions)
        is BlockView.Contact -> TODO()
        is BlockView.File.View -> newInstance(block, dimensions)
        is BlockView.File.Upload -> newInstance(block, dimensions)
        is BlockView.File.Placeholder -> newInstance(block, dimensions)
        is BlockView.File.Error -> newInstance(block, dimensions)
        is BlockView.Video.View -> newInstance(block, dimensions)
        is BlockView.Video.Upload -> newInstance(block, dimensions)
        is BlockView.Video.Placeholder -> newInstance(block, dimensions)
        is BlockView.Video.Error -> newInstance(block, dimensions)
        is BlockView.Page -> newInstance(block, dimensions)
        is BlockView.Divider -> newInstance(block, dimensions)
        is BlockView.Bookmark.Placeholder -> newInstance(block, dimensions)
        is BlockView.Bookmark.View -> newInstance(block, dimensions)
        is BlockView.Bookmark.Error -> newInstance(block, dimensions)
        is BlockView.Picture.View -> newInstance(block, dimensions)
        is BlockView.Picture.Placeholder -> newInstance(block, dimensions)
        is BlockView.Picture.Error -> newInstance(block, dimensions)
        is BlockView.Picture.Upload -> newInstance(block, dimensions)
        BlockView.Footer -> TODO()
    }

    fun newInstance(block: BlockView.Page, dimensions: BlockDimensions): PageBlockActionToolbar =
        PageBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Paragraph, dimensions: BlockDimensions): ParagraphBlockActionToolbar =
        ParagraphBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.HeaderOne, dimensions: BlockDimensions): HeaderOneBlockActionToolbar =
        HeaderOneBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.HeaderTwo, dimensions: BlockDimensions): HeaderTwoBlockActionToolbar =
        HeaderTwoBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(
        block: BlockView.HeaderThree,
        dimensions: BlockDimensions
    ): HeaderThreeBlockActionToolbar =
        HeaderThreeBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Checkbox, dimensions: BlockDimensions): CheckBoxBlockActionToolbar =
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

    fun newInstance(block: BlockView.Highlight, dimensions: BlockDimensions): HighlightBlockActionToolbar =
        HighlightBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Numbered, dimensions: BlockDimensions): NumberedBlockActionToolbar =
        NumberedBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Task, dimensions: BlockDimensions): TaskBlockActionToolbar =
        TaskBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Toggle, dimensions: BlockDimensions): ToggleBlockActionToolbar =
        ToggleBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Bulleted, dimensions: BlockDimensions): BulletedBlockActionToolbar =
        BulletedBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.File, dimensions: BlockDimensions): FileBlockActionToolbar =
        FileBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Picture, dimensions: BlockDimensions): PictureBlockActionToolbar =
        PictureBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Video, dimensions: BlockDimensions): VideoBlockActionToolbar =
        VideoBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Divider, dimensions: BlockDimensions): DividerBlockActionToolbar =
        DividerBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }

    fun newInstance(block: BlockView.Bookmark, dimensions: BlockDimensions): BookmarkBlockActionToolbar =
        BookmarkBlockActionToolbar().apply {
            arguments = bundleOf(
                BlockActionToolbar.ARG_BLOCK to block,
                BlockActionToolbar.ARG_BLOCK_DIMENSIONS to dimensions
            )
        }
}