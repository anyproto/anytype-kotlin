package com.agileburo.anytype.core_ui.features.page

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import kotlinx.android.synthetic.main.item_block_bookmark.view.*
import kotlinx.android.synthetic.main.item_block_bulleted.view.*
import kotlinx.android.synthetic.main.item_block_checkbox.view.*
import kotlinx.android.synthetic.main.item_block_code_snippet.view.*
import kotlinx.android.synthetic.main.item_block_contact.view.*
import kotlinx.android.synthetic.main.item_block_file.view.*
import kotlinx.android.synthetic.main.item_block_header_one.view.*
import kotlinx.android.synthetic.main.item_block_header_three.view.*
import kotlinx.android.synthetic.main.item_block_header_two.view.*
import kotlinx.android.synthetic.main.item_block_highlight.view.*
import kotlinx.android.synthetic.main.item_block_numbered.view.*
import kotlinx.android.synthetic.main.item_block_page.view.*
import kotlinx.android.synthetic.main.item_block_task.view.*
import kotlinx.android.synthetic.main.item_block_text.view.*
import kotlinx.android.synthetic.main.item_block_title.view.*
import kotlinx.android.synthetic.main.item_block_toggle.view.*

/**
 * Viewholder for rendering different type of blocks (i.e its UI-models).
 * @see BlockView
 * @see BlockAdapter
 */
sealed class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    class Text(view: View) : BlockViewHolder(view) {

        private val content = itemView.textContent

        fun bind(item: BlockView.Text) {
            content.text = item.text
        }
    }

    class Title(view: View) : BlockViewHolder(view) {

        private val title = itemView.title

        fun bind(item: BlockView.Title) {
            title.text = item.text
        }
    }

    class HeaderOne(view: View) : BlockViewHolder(view) {

        private val header = itemView.headerOne

        fun bind(item: BlockView.HeaderOne) {
            header.text = item.text
        }
    }

    class HeaderTwo(view: View) : BlockViewHolder(view) {

        private val header = itemView.headerTwo

        fun bind(item: BlockView.HeaderTwo) {
            header.text = item.text
        }
    }

    class HeaderThree(view: View) : BlockViewHolder(view) {

        private val header = itemView.headerThree

        fun bind(item: BlockView.HeaderThree) {
            header.text = item.text
        }
    }

    class Code(view: View) : BlockViewHolder(view) {

        private val code = itemView.snippet

        fun bind(item: BlockView.Code) {
            code.text = item.snippet
        }
    }

    class Checkbox(view: View) : BlockViewHolder(view) {

        private val checkbox = itemView.checkboxIcon
        private val content = itemView.checkboxContent

        fun bind(item: BlockView.Checkbox) {
            checkbox.isSelected = item.checked
            content.text = item.text
        }
    }

    class Task(view: View) : BlockViewHolder(view) {

        private val checkbox = itemView.taskIcon
        private val content = itemView.taskContent

        fun bind(item: BlockView.Task) {
            checkbox.isSelected = item.checked
            content.text = item.text
        }
    }

    class Bulleted(view: View) : BlockViewHolder(view) {

        private val content = itemView.bulletedListContent

        fun bind(item: BlockView.Bulleted) {
            content.text = item.text
        }
    }

    class Numbered(view: View) : BlockViewHolder(view) {

        private val number = itemView.number
        private val content = itemView.numberedListContent

        fun bind(item: BlockView.Numbered) {
            number.text = item.number
            content.text = item.text
        }
    }

    class Toggle(view: View) : BlockViewHolder(view) {

        private val toggle = itemView.toggle
        private val content = itemView.toggleContent

        fun bind(item: BlockView.Toggle) {
            content.text = item.text
            toggle.rotation = if (item.toggled) EXPANDED_ROTATION else COLLAPSED_ROTATION
        }

        companion object {
            /**
             * Rotation value for a toggle icon for expanded state.
             */
            const val EXPANDED_ROTATION = 90f
            /**
             * Rotation value for a toggle icon for collapsed state.
             */
            const val COLLAPSED_ROTATION = 0f
        }
    }

    class Contact(view: View) : BlockViewHolder(view) {

        private val name = itemView.name
        private val avatar = itemView.avatar

        fun bind(item: BlockView.Contact) {
            name.text = item.name
            avatar.bind(item.name)
        }
    }

    class File(view: View) : BlockViewHolder(view) {

        private val icon = itemView.fileIcon
        private val size = itemView.fileSize
        private val name = itemView.filename

        fun bind(item: BlockView.File) {
            name.text = item.filename
            size.text = item.size
            // TODO set file icon.
        }
    }

    class Page(view: View) : BlockViewHolder(view) {

        private val icon = itemView.pageIcon
        private val title = itemView.pageTitle

        fun bind(item: BlockView.Page) {
            title.text = item.text
            if (item.isEmpty)
                icon.setImageResource(R.drawable.ic_block_empty_page)
            else if (item.emoji == null)
                icon.setBackgroundResource(R.drawable.ic_block_page_without_emoji)
        }

    }

    class Bookmark(view: View) : BlockViewHolder(view) {

        private val title = itemView.bookmarkTitle
        private val description = itemView.bookmarkDescription
        private val url = itemView.bookmarkUrl
        private val image = itemView.bookmarkImage
        private val logo = itemView.bookmarkLogo

        fun bind(item: BlockView.Bookmark) {
            title.text = item.title
            description.text = item.description
            url.text = item.url
            // TODO set logo icon and website's image
        }

    }

    class Picture(view: View) : BlockViewHolder(view) {

        fun bind(item: BlockView.Picture) {
            // TODO
        }

    }

    class Divider(view: View) : BlockViewHolder(view)

    class Highlight(view: View) : BlockViewHolder(view) {

        private val content = itemView.highlightContent

        fun bind(item: BlockView.Highlight) {
            content.text = item.text
        }

    }

    companion object {
        const val HOLDER_TEXT = 0
        const val HOLDER_TITLE = 1
        const val HOLDER_HEADER_ONE = 2
        const val HOLDER_HEADER_TWO = 3
        const val HOLDER_HEADER_THREE = 4
        const val HOLDER_CODE_SNIPPET = 5
        const val HOLDER_CHECKBOX = 6
        const val HOLDER_TASK = 7
        const val HOLDER_BULLET = 8
        const val HOLDER_NUMBERED = 9
        const val HOLDER_TOGGLE = 10
        const val HOLDER_CONTACT = 11
        const val HOLDER_FILE = 12
        const val HOLDER_PAGE = 13
        const val HOLDER_BOOKMARK = 14
        const val HOLDER_PICTURE = 15
        const val HOLDER_DIVIDER = 16
        const val HOLDER_HIGHLIGHT = 17
    }
}
