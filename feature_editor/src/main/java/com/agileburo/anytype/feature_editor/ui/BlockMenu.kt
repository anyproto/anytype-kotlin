package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.presentation.model.BlockView

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-05-20.
 */


sealed class BlockMenuAction{
    data class ContentTypeAction(val id : String, val newType: ContentType): BlockMenuAction()
    data class ArchiveAction(val id : String): BlockMenuAction()
    data class DuplicateAction(val id: String): BlockMenuAction()
}

class BlockMenu(
    private val context: Context,
    private val block: BlockView,
    private val menuItemClick: (BlockMenuAction) -> Unit
) : PopupWindow(context) {

    init {
        setupView()
    }

    private fun setupView() {
        val view = LayoutInflater.from(context).inflate(getLayout(block), null)
        view.findViewById<ImageView>(getButton(block))?.isSelected = true
        isOutsideTouchable = true
        isFocusable = true
        contentView = view
        setClicks()
    }

    private fun setClicks() {
        contentView.findViewById<View>(R.id.btn_menu_p)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.P))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_h1)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.H1))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_h2)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.H2))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_h3)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.H3))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_h4)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.H4))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_bullet)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.UL))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_quote)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.Quote))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_numbered)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.NumberedList))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_checkbox)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.Check))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_code)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ContentTypeAction(block.id, ContentType.Code))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_archive)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.ArchiveAction(block.id))
            dismiss()
        }
        contentView.findViewById<View>(R.id.btn_menu_duplicate)?.setOnClickListener {
            menuItemClick.invoke(BlockMenuAction.DuplicateAction(block.id))
            dismiss()
        }
    }

    private fun getLayout(block: BlockView): Int =
        when (block) {
            is BlockView.Editable -> R.layout.popup_edit_block
            else -> R.layout.popup_non_edit_block
        }

    private fun getButton(block: BlockView) =
        when (block) {
            is BlockView.ParagraphView -> R.id.btn_menu_p
            is BlockView.HeaderView -> {
                when (block.type) {
                    BlockView.HeaderView.HeaderType.ONE -> R.id.btn_menu_h1
                    BlockView.HeaderView.HeaderType.TWO -> R.id.btn_menu_h2
                    BlockView.HeaderView.HeaderType.THREE -> R.id.btn_menu_h3
                    BlockView.HeaderView.HeaderType.FOUR -> R.id.btn_menu_h4
                }
            }
            is BlockView.BulletView -> R.id.btn_menu_bullet
            is BlockView.QuoteView -> R.id.btn_menu_quote
            is BlockView.NumberListItemView -> R.id.btn_menu_numbered
            is BlockView.CheckboxView -> R.id.btn_menu_checkbox
            is BlockView.CodeSnippetView -> R.id.btn_menu_code

            //Todo implement!
            is BlockView.LinkToPageView -> throw NotImplementedError()
            is BlockView.BookmarkView -> throw NotImplementedError()
            is BlockView.DividerView -> throw NotImplementedError()
            is BlockView.PictureView -> throw NotImplementedError()
        }
}