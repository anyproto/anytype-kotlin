package com.agileburo.anytype.ui.page.modals.actions

import androidx.core.os.bundleOf
import com.agileburo.anytype.core_ui.features.page.BlockView

object BlockActionToolbarFactory {

    fun newInstance(block: BlockView) = when (block) {
        is BlockView.Paragraph -> newInstance(block)
        is BlockView.Title -> TODO()
        is BlockView.ProfileTitle -> TODO()
        is BlockView.HeaderOne -> newInstance(block)
        is BlockView.HeaderTwo -> newInstance(block)
        is BlockView.HeaderThree -> newInstance(block)
        is BlockView.Highlight -> newInstance(block)
        is BlockView.Code -> newInstance(block)
        is BlockView.Checkbox -> newInstance(block)
        is BlockView.Task -> newInstance(block)
        is BlockView.Bulleted -> newInstance(block)
        is BlockView.Numbered -> newInstance(block)
        is BlockView.Toggle -> newInstance(block)
        is BlockView.Contact -> TODO()
        is BlockView.File.View -> newInstance(block)
        is BlockView.File.Upload -> newInstance(block)
        is BlockView.File.Placeholder -> newInstance(block)
        is BlockView.File.Error -> newInstance(block)
        is BlockView.Video.View -> newInstance(block)
        is BlockView.Video.Upload -> newInstance(block)
        is BlockView.Video.Placeholder -> newInstance(block)
        is BlockView.Video.Error -> newInstance(block)
        is BlockView.Page -> newInstance(block)
        is BlockView.Divider -> newInstance(block)
        is BlockView.Bookmark.Placeholder -> newInstance(block)
        is BlockView.Bookmark.View -> newInstance(block)
        is BlockView.Bookmark.Error -> newInstance(block)
        is BlockView.Picture.View -> newInstance(block)
        is BlockView.Picture.Placeholder -> newInstance(block)
        is BlockView.Picture.Error -> newInstance(block)
        is BlockView.Picture.Upload -> newInstance(block)
        BlockView.Footer -> TODO()
    }

    fun newInstance(block: BlockView.Page): PageBlockActionToolbar =
        PageBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Paragraph): ParagraphBlockActionToolbar =
        ParagraphBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.HeaderOne): HeaderOneBlockActionToolbar =
        HeaderOneBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.HeaderTwo): HeaderTwoBlockActionToolbar =
        HeaderTwoBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.HeaderThree): HeaderThreeBlockActionToolbar =
        HeaderThreeBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Checkbox): CheckBoxBlockActionToolbar =
        CheckBoxBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Code): CodeBlockActionToolbar =
        CodeBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Highlight): HighlightBlockActionToolbar =
        HighlightBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Numbered): NumberedBlockActionToolbar =
        NumberedBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Task): TaskBlockActionToolbar =
        TaskBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Toggle): ToggleBlockActionToolbar =
        ToggleBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Bulleted): BulletedBlockActionToolbar =
        BulletedBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.File): FileBlockActionToolbar =
        FileBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Picture): PictureBlockActionToolbar =
        PictureBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Video): VideoBlockActionToolbar =
        VideoBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block : BlockView.Divider): DividerBlockActionToolbar =
        DividerBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }

    fun newInstance(block: BlockView.Bookmark): BookmarkBlockActionToolbar =
        BookmarkBlockActionToolbar().apply {
            arguments = bundleOf(BlockActionToolbar.ARG_BLOCK to block)
        }
}