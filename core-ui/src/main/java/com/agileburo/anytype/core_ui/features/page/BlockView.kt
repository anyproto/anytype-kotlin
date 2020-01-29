package com.agileburo.anytype.core_ui.features.page

import com.agileburo.anytype.core_ui.common.Focusable
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.ViewType
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BULLET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CHECKBOX
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CODE_SNIPPET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CONTACT
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_DIVIDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_ONE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_THREE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_TWO
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HIGHLIGHT
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_NUMBERED
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PAGE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PARAGRAPH
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TASK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TOGGLE

/**
 * UI-models for different types of blocks.
 */
sealed class BlockView : ViewType {


    /**
     * Each block view has a corresponding block id.
     */
    abstract val id: String

    /**
     * Basic interface for textual blocks' common properties.
     */
    interface Text {

        /**
         * Base text color for this text. If not present, default color will be applied.
         */
        val color: String?

        /**
         * Textual block's text.
         */
        val text: String
    }

    /**
     * UI-model for a basic paragraph block.
     * @property id block's id
     * @property text block's content text
     * @property marks markup
     * @property focused whether this block is currently focused or not
     * @property color text color
     */
    data class Paragraph(
        override val id: String,
        override val text: String,
        override val marks: List<Markup.Mark> = emptyList(),
        override var focused: Boolean = false,
        override val color: String? = null
    ) : BlockView(), Markup, Focusable, Text {
        override fun getViewType() = HOLDER_PARAGRAPH
        override val body: String = text
    }

    /**
     * UI-model for a title block.
     * @property id block's id
     * @property text text content (i.e. title text)
     * @property emoji emoji as a page's logo (if present)
     */
    data class Title(
        override val id: String,
        val text: String,
        val emoji: String? = null
    ) : BlockView() {
        override fun getViewType() = HOLDER_TITLE
    }

    /**
     * UI-model for a header block.
     * @property id block's id
     * @property text header's content (i.e. a header's text)
     * @property color text color
     */
    data class HeaderOne(
        override val id: String,
        override val text: String,
        override val color: String? = null
    ) : BlockView(), Text {
        override fun getViewType() = HOLDER_HEADER_ONE
    }

    /**
     * UI-model for a header block.
     * @property id block's id
     * @property text header's content (i.e. a header's text)
     * @property color text color
     */
    data class HeaderTwo(
        override val id: String,
        override val color: String? = null,
        override val text: String
    ) : BlockView(), Text {
        override fun getViewType() = HOLDER_HEADER_TWO
    }

    /**
     * UI-model for a header block.
     * @property id block's id
     * @property text header's content (i.e. a header's text)
     * @property color text color
     */
    data class HeaderThree(
        override val id: String,
        override val color: String? = null,
        override val text: String
    ) : BlockView(), Text {
        override fun getViewType() = HOLDER_HEADER_THREE
    }

    /**
     * UI-model for a highlight block (analogue of quote block)
     * @property id block's id
     * @property text block's content
     */
    data class Highlight(
        override val id: String,
        val text: String
    ) : BlockView() {
        override fun getViewType() = HOLDER_HIGHLIGHT
    }

    /**
     * UI-model for a code-snippet block.
     * @property id block's id
     * @property snippet blocks's content (i.e. code snippet)
     */
    data class Code(
        override val id: String,
        val snippet: String
    ) : BlockView() {
        override fun getViewType() = HOLDER_CODE_SNIPPET
    }

    /**
     * UI-model for checkbox blocks.
     * @property id block's id
     * @property text checkbox's content text
     * @property checked immutable checkbox state (whether this checkbox is checked or not)
     */
    data class Checkbox(
        override val id: String,
        override val marks: List<Markup.Mark> = emptyList(),
        override val focused: Boolean = false,
        val text: String,
        val checked: Boolean = false
    ) : BlockView(), Markup, Focusable {
        override fun getViewType() = HOLDER_CHECKBOX
        override val body: String = text
    }

    /**
     * UI-model for task blocks.
     * @property id block's id
     * @property text task's content text
     * @property checked immutable taks state (whether this task is completed or not)
     */
    data class Task(
        override val id: String,
        val text: String,
        val checked: Boolean = false
    ) : BlockView() {
        override fun getViewType() = HOLDER_TASK
    }

    /**
     * UI-model for items of a bulleted list.
     * @property id block's id
     * @property text bullet list item content
     * @property indent indentation value
     * @property color text color
     */
    data class Bulleted(
        override val id: String,
        override val marks: List<Markup.Mark> = emptyList(),
        override val focused: Boolean = false,
        override val color: String? = null,
        override val text: String,
        val indent: Int
    ) : BlockView(), Markup, Focusable, Text {
        override fun getViewType() = HOLDER_BULLET
        override val body: String = text
    }

    /**
     * UI-model for items of a numbered list.
     * @property id block's id
     * @property text numbered list item content
     * @property number number value
     * @property indent indentation value
     */
    data class Numbered(
        override val id: String,
        val text: String,
        val number: String,
        val indent: Int
    ) : BlockView() {
        override fun getViewType() = HOLDER_NUMBERED
    }

    /**
     * UI-model for a toggle block.
     * @property id block's id
     * @property text toggle block's content
     * @property indent indentation value
     * @property toggled toggle state (whether this toggle is expanded or not)
     */
    data class Toggle(
        override val id: String,
        val text: String,
        val indent: Int,
        val toggled: Boolean = false
    ) : BlockView() {
        override fun getViewType() = HOLDER_TOGGLE
    }

    /**
     * UI-model for a contact block.
     * @property id block's
     * @property name a person's name
     * @property avatar a person's avatar image
     */
    data class Contact(
        override val id: String,
        val name: String,
        val avatar: String
    ) : BlockView() {
        override fun getViewType() = HOLDER_CONTACT
    }

    /**
     * UI-model for blocks containing files.
     * @property id block's id
     * @property size a file's size
     * @property filename a filename
     */
    data class File(
        override val id: String,
        val size: String,
        val filename: String
    ) : BlockView() {
        override fun getViewType() = HOLDER_FILE
    }

    /**
     * UI-model for blocks containing page links.
     * @property id block's id
     * @property text a page's name
     * @property emoji a page's emoji (if present)
     * @property isEmpty this property determines whether this page is empty or not
     * @property isArchived this property determines whether this page is archived or not
     */
    data class Page(
        override val id: String,
        val text: String,
        val emoji: String?,
        val isEmpty: Boolean = false,
        val isArchived: Boolean = false
    ) : BlockView() {
        override fun getViewType() = HOLDER_PAGE
    }

    /**
     * UI-model for a divider block.
     * @property id block's id
     */
    data class Divider(
        override val id: String
    ) : BlockView() {
        override fun getViewType() = HOLDER_DIVIDER
    }

    /**
     * UI-model for a bookmark block
     * @property id block's id
     * @property title website's title
     * @property title website's content description
     * @property url website's url
     * @property logoUrl website's logo url
     * @property logoUrl content's main image url
     */
    data class Bookmark(
        override val id: String,
        val title: String,
        val description: String,
        val url: String,
        val logoUrl: String,
        val imageUrl: String
    ) : BlockView() {
        override fun getViewType() = HOLDER_BOOKMARK
    }

    /**
     * UI-model for a picture block
     * @property id block's id
     */
    data class Picture(
        override val id: String
    ) : BlockView() {
        override fun getViewType() = HOLDER_PICTURE
    }
}