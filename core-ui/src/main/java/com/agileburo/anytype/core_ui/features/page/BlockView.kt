package com.agileburo.anytype.core_ui.features.page

import com.agileburo.anytype.core_ui.common.Checkable
import com.agileburo.anytype.core_ui.common.Focusable
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.ViewType
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BULLET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CHECKBOX
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CODE_SNIPPET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CONTACT
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_DIVIDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_UPLOAD
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FOOTER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_ONE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_THREE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_TWO
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HIGHLIGHT
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_NUMBERED
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PAGE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PARAGRAPH
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE_UPLOAD
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TASK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TOGGLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_UPLOAD

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
         * Base text color for the text.
         * If not present, default color will be applied.
         */
        val color: String?

        /**
         * Background color for the whole block as opposed to text highlight background.
         * If not present, default color will be applied.
         */
        val backgroundColor: String?

        /**
         * Textual block's text.
         */
        val text: String
    }

    /**
     * Views implementing this interface would have an indent.
     */
    interface Indentable {
        val indent: Int
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
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val indent: Int = 0
    ) : BlockView(), Markup, Focusable, Text, Indentable {
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
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val indent: Int
    ) : BlockView(), Text, Indentable {
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
        override val text: String,
        override val backgroundColor: String? = null,
        override val indent: Int
    ) : BlockView(), Text, Indentable {
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
        override val text: String,
        override val backgroundColor: String? = null,
        override val indent: Int
    ) : BlockView(), Text, Indentable {
        override fun getViewType() = HOLDER_HEADER_THREE
    }

    /**
     * UI-model for a highlight block (analogue of quote block)
     * @property id block's id
     * @property text block's content
     */
    data class Highlight(
        override val id: String,
        val text: String,
        override val indent: Int
    ) : BlockView(), Indentable {
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
     * @property isChecked immutable checkbox state (whether this checkbox is checked or not)
     */
    data class Checkbox(
        override val id: String,
        override val marks: List<Markup.Mark> = emptyList(),
        override val focused: Boolean = false,
        override val text: String,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val isChecked: Boolean = false,
        override val indent: Int
    ) : BlockView(), Markup, Focusable, Text, Checkable, Indentable {
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
        override val backgroundColor: String? = null,
        override val text: String,
        override val indent: Int
    ) : BlockView(), Markup, Focusable, Text, Indentable {
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
        override val text: String,
        override val marks: List<Markup.Mark> = emptyList(),
        override val focused: Boolean = false,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val indent: Int,
        val number: String
    ) : BlockView(), Markup, Focusable, Text, Indentable {
        override fun getViewType() = HOLDER_NUMBERED
        override val body: String = text
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
        override val text: String,
        override val marks: List<Markup.Mark>,
        override var focused: Boolean,
        override val color: String?,
        override val backgroundColor: String?,
        override val indent: Int,
        val toggled: Boolean = false,
        val isEmpty: Boolean = false
    ) : BlockView(), Markup, Focusable, Text, Indentable {
        override fun getViewType() = HOLDER_TOGGLE
        override val body: String = text
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
     * UI-models for blocks containing files.
     * @property id block's id
     */
    sealed class File(
        override val id: String
    ) : BlockView(), Indentable {

        /**
         * UI-model for block containing file, with state DONE.
         * @property id block's id
         */
        data class View(
            override val id: String,
            override val indent: Int,
            val size: Long?,
            val name: String?,
            val mime: String?,
            val hash: String?,
            val url: String
        ) : BlockView.File(id) {
            override fun getViewType() = HOLDER_FILE
        }

        /**
         * UI-model for block containing file, with state UPLOADING.
         * @property id block's id
         */
        data class Upload(
            override val id: String,
            override val indent: Int
        ) : BlockView.File(id) {
            override fun getViewType() = HOLDER_FILE_UPLOAD
        }

        /**
         * UI-model for block containing file, with state EMPTY.
         * @property id block's id
         */
        data class Placeholder(
            override val id: String,
            override val indent: Int
        ) : BlockView.File(id) {
            override fun getViewType() = HOLDER_FILE_PLACEHOLDER
        }

        /**
         * UI-model for block containing file, with state ERROR.
         * @property id block's id
         */
        data class Error(
            override val id: String,
            override val indent: Int
        ) : BlockView.File(id) {
            override fun getViewType() = HOLDER_FILE_ERROR
        }
    }

    /**
     * UI-models for blocks containing videos.
     * @property id block's id
     */
    sealed class Video(
        override val id: String
    ) : BlockView(), Indentable {

        /**
         * UI-model for block containing video, with state DONE.
         */
        data class View(
            override val id: String,
            override val indent: Int,
            val size: Long?,
            val name: String?,
            val mime: String?,
            val hash: String?,
            val url: String
        ) : BlockView.Video(id) {
            override fun getViewType() = HOLDER_VIDEO
        }

        /**
         * UI-model for block containing video, with state UPLOADING.
         * @property id block's id
         */
        data class Upload(
            override val id: String,
            override val indent: Int
        ) : BlockView.Video(id) {
            override fun getViewType() = HOLDER_VIDEO_UPLOAD
        }

        /**
         * UI-model for block containing video, with state EMPTY.
         * @property id block's id
         */
        data class Placeholder(
            override val id: String,
            override val indent: Int
        ) : BlockView.Video(id) {
            override fun getViewType() = HOLDER_VIDEO_PLACEHOLDER
        }

        /**
         * UI-model for block containing video, with state ERROR.
         * @property id block's id
         */
        data class Error(
            override val id: String,
            override val indent: Int
        ) : BlockView.Video(id) {
            override fun getViewType() = HOLDER_VIDEO_ERROR
        }
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
        override val indent: Int,
        val text: String? = null,
        val emoji: String?,
        val isEmpty: Boolean = false,
        val isArchived: Boolean = false
    ) : BlockView(), Indentable {
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
     */
    sealed class Bookmark(
        override val id: String
    ) : BlockView(), Indentable {

        /**
         * UI-model for a bookmark placeholder (used when bookmark url is not set)
         */
        data class Placeholder(
            override val id: String,
            override val indent: Int
        ) : Bookmark(id = id) {
            override fun getViewType() = HOLDER_BOOKMARK_PLACEHOLDER
        }

        /**
         * UI-model for a bookmark view.
         * @property title website's title
         * @property description website's content description
         * @property url website's url
         * @property faviconUrl website's favicon url
         * @property imageUrl content's main image url
         */
        data class View(
            override val id: String,
            override val indent: Int,
            val url: String,
            val title: String?,
            val description: String?,
            val faviconUrl: String?,
            val imageUrl: String?
        ) : Bookmark(id = id) {
            override fun getViewType() = HOLDER_BOOKMARK
        }
    }

    /**
     * UI-models for blocks containing images
     * @property id block's id
     */
    sealed class Picture(
        override val id: String
    ) : BlockView(), Indentable {

        /**
         * UI-model for block containing image, with state DONE.
         */
        data class View(
            override val id: String,
            override val indent: Int,
            val size: Long?,
            val name: String?,
            val mime: String?,
            val hash: String?,
            val url: String
        ) : BlockView.Picture(id) {
            override fun getViewType() = HOLDER_PICTURE
        }

        /**
         * UI-model for block containing image, with state EMPTY.
         */
        data class Placeholder(
            override val id: String,
            override val indent: Int
        ) : BlockView.Picture(id) {
            override fun getViewType() = HOLDER_PICTURE_PLACEHOLDER
        }

        /**
         * UI-model for block containing image, with state ERROR.
         */
        data class Error(
            override val id: String,
            override val indent: Int
        ) : BlockView.Picture(id) {
            override fun getViewType() = HOLDER_PICTURE_ERROR
        }

        /**
         * UI-model for block containing image, with state UPLOADING.
         */
        data class Upload(
            override val id: String,
            override val indent: Int
        ) : BlockView.Picture(id) {
            override fun getViewType() = HOLDER_PICTURE_UPLOAD
        }
    }

    /**
     * Footer block. Just holds space at the end of the page.
     */
    object Footer : BlockView() {
        override val id: String = FOOTER_ID
        override fun getViewType() = HOLDER_FOOTER
    }

    companion object {
        const val FOOTER_ID = "FOOTER"
    }
}