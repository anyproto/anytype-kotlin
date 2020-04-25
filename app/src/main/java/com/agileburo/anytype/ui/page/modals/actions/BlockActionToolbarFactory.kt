package com.agileburo.anytype.ui.page.modals.actions

import androidx.core.os.bundleOf
import com.agileburo.anytype.core_ui.features.page.BlockView

object BlockActionToolbarFactory {

    fun newInstance(block: BlockView) = when (block) {
        is BlockView.Paragraph -> newInstance(block)
        is BlockView.Title -> TODO()
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
        is BlockView.File.View -> TODO()
        is BlockView.File.Upload -> TODO()
        is BlockView.File.Placeholder -> TODO()
        is BlockView.File.Error -> TODO()
        is BlockView.Video.View -> TODO()
        is BlockView.Video.Upload -> TODO()
        is BlockView.Video.Placeholder -> TODO()
        is BlockView.Video.Error -> TODO()
        is BlockView.Page -> TODO()
        is BlockView.Divider -> TODO()
        is BlockView.Bookmark.Placeholder -> TODO()
        is BlockView.Bookmark.View -> TODO()
        is BlockView.Bookmark.Error -> TODO()
        is BlockView.Picture.View -> TODO()
        is BlockView.Picture.Placeholder -> TODO()
        is BlockView.Picture.Error -> TODO()
        is BlockView.Picture.Upload -> TODO()
        BlockView.Footer -> TODO()
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
}