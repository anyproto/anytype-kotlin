package com.agileburo.anytype.core_ui.features.page

import android.os.Parcelable
import com.agileburo.anytype.core_ui.common.*
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK_ERROR
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
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PROFILE_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TASK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TOGGLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_ERROR
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_UPLOAD
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * UI-models for different types of blocks.
 */
sealed class BlockView : ViewType, Parcelable {

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
        var text: String
    }

    /**
     * Views implementing this interface would have an indent.
     */
    interface Indentable {
        val indent: Int
    }

    /**
     * Views implementing this interface can be selected in multi-select mode.
     */
    interface Selectable {
        val isSelected: Boolean
    }

    /**
     * Views implementing this interface support alignment.
     */
    interface Alignable {
        val alignment: Alignment?
    }

    /**
     * Views implementing this interface support read/write mode switch.
     */
    interface Permission {
        val mode: Mode
    }

    /**
     * Views implementing this interface support cursor/carriage positioning.
     * @property cursor cursor position
     */
    interface Cursor {
        val cursor: Int?
    }

    /**
     * Views implementing this interface can scroll by Y coordinate on screen.
     */
    interface Scrollable {
        val target: String?
        val scrollTo: Int?
    }

    /**
     * UI-model for a basic paragraph block.
     * @property id block's id
     * @property text block's content text
     * @property marks markup
     * @property isFocused whether this block is currently focused or not
     * @property color text color
     */
    @Parcelize
    data class Paragraph(
        override val id: String,
        override var text: String,
        override var marks: List<Markup.Mark> = emptyList(),
        override var isFocused: Boolean = false,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val indent: Int = 0,
        override val mode: Mode = Mode.EDIT,
        override val isSelected: Boolean = false,
        override val alignment: Alignment? = null,
        override val cursor: Int? = null
    ) : BlockView(), Markup, Focusable, Text, Cursor, Indentable, Permission, Selectable, Alignable {
        override fun getViewType() = HOLDER_PARAGRAPH
        override val body: String get() = text
    }

    /**
     * UI-model for a title block.
     * @property id block's id
     * @property text text content (i.e. title text)
     * @property emoji emoji as a page's logo (if present)
     */
    @Parcelize
    data class Title(
        override val id: String,
        override val isFocused: Boolean,
        var text: String?,
        val emoji: String? = null,
        val image: String? = null,
        override val mode: Mode = Mode.EDIT,
        override val cursor: Int? = null
    ) : BlockView(), Focusable, Cursor, Permission {
        override fun getViewType() = HOLDER_TITLE
    }

    /**
     * UI-model for a profile title block.
     * @property id block's id
     * @property text text content (i.e. title text)
     * @property image image as a page's logo (if present)
     */
    @Parcelize
    data class ProfileTitle(
        override val id: String,
        override val isFocused: Boolean,
        var text: String?,
        val image: String? = null,
        override val mode: Mode = Mode.EDIT,
        override val cursor: Int? = null
    ) : BlockView(), Focusable, Cursor, Permission {
        override fun getViewType() = HOLDER_PROFILE_TITLE
    }

    /**
     * UI-model for a header block.
     * @property id block's id
     * @property text header's content (i.e. a header's text)
     * @property color text color
     * @property marks markup
     */
    @Parcelize
    data class HeaderOne(
        override val id: String,
        override var text: String,
        override val isFocused: Boolean = false,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val indent: Int = 0,
        override var marks: List<Markup.Mark> = emptyList(),
        override val mode: Mode = Mode.EDIT,
        override val isSelected: Boolean = false,
        override val alignment: Alignment? = null,
        override val cursor: Int? = null
    ) : BlockView(), Text, Markup, Cursor, Focusable, Indentable, Permission, Selectable, Alignable {
        override fun getViewType() = HOLDER_HEADER_ONE
        override val body: String = text
    }

    /**
     * UI-model for a header block.
     * @property id block's id
     * @property text header's content (i.e. a header's text)
     * @property color text color
     * @property marks markup
     */
    @Parcelize
    data class HeaderTwo(
        override val id: String,
        override val color: String? = null,
        override var text: String,
        override val isFocused: Boolean = false,
        override val backgroundColor: String? = null,
        override val indent: Int = 0,
        override var marks: List<Markup.Mark> = emptyList(),
        override val mode: Mode = Mode.EDIT,
        override val isSelected: Boolean = false,
        override val alignment: Alignment? = null,
        override val cursor: Int? = null
    ) : BlockView(), Text, Markup, Focusable, Cursor, Indentable, Permission, Selectable, Alignable {
        override fun getViewType() = HOLDER_HEADER_TWO
        override val body: String = text
    }

    /**
     * UI-model for a header block.
     * @property id block's id
     * @property text header's content (i.e. a header's text)
     * @property color text color
     * @property marks markup
     */
    @Parcelize
    data class HeaderThree(
        override val id: String,
        override val color: String? = null,
        override var text: String,
        override val isFocused: Boolean = false,
        override val backgroundColor: String? = null,
        override val indent: Int = 0,
        override var marks: List<Markup.Mark> = emptyList(),
        override val mode: Mode = Mode.EDIT,
        override val isSelected: Boolean = false,
        override val alignment: Alignment? = null,
        override val cursor: Int? = null
    ) : BlockView(), Text, Markup, Focusable, Cursor, Indentable, Permission, Selectable, Alignable {
        override fun getViewType() = HOLDER_HEADER_THREE
        override val body: String = text
    }

    /**
     * UI-model for a highlight block (analogue of quote block)
     * @property id block's id
     * @property text block's content
     * @property marks markup
     */
    @Parcelize
    data class Highlight(
        override val id: String,
        override val isFocused: Boolean = false,
        override var text: String,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val indent: Int = 0,
        override var marks: List<Markup.Mark> = emptyList(),
        override val mode: Mode = Mode.EDIT,
        override val isSelected: Boolean = false,
        override val cursor: Int? = null
    ) : BlockView(), Text, Markup, Focusable, Cursor, Indentable, Permission, Selectable {
        override fun getViewType() = HOLDER_HIGHLIGHT
        override val body: String = text
    }

    /**
     * UI-model for a code-snippet block.
     * @property id block's id
     * @property text blocks's content (i.e. code snippet)
     */
    @Parcelize
    data class Code(
        override val id: String,
        var text: String,
        override val mode: Mode = Mode.EDIT,
        override var isFocused: Boolean = false,
        override val isSelected: Boolean = false
    ) : BlockView(), Permission, Selectable, Focusable {
        override fun getViewType() = HOLDER_CODE_SNIPPET
    }

    /**
     * UI-model for checkbox blocks.
     * @property id block's id
     * @property text checkbox's content text
     * @property isChecked immutable checkbox state (whether this checkbox is checked or not)
     */
    @Parcelize
    data class Checkbox(
        override val id: String,
        override var marks: List<Markup.Mark> = emptyList(),
        override val isFocused: Boolean = false,
        override var text: String,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val isChecked: Boolean = false,
        override val indent: Int,
        override val mode: Mode = Mode.EDIT,
        override val isSelected: Boolean = false,
        override val cursor: Int? = null
    ) : BlockView(), Markup, Focusable, Text, Cursor, Checkable, Indentable, Permission, Selectable {
        override fun getViewType() = HOLDER_CHECKBOX
        override val body: String = text
    }

    /**
     * UI-model for task blocks.
     * @property id block's id
     * @property text task's content text
     * @property checked immutable taks state (whether this task is completed or not)
     */
    @Parcelize
    data class Task(
        override val id: String,
        var text: String,
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
    @Parcelize
    data class Bulleted(
        override val id: String,
        override var marks: List<Markup.Mark> = emptyList(),
        override val isFocused: Boolean = false,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override var text: String,
        override val indent: Int,
        override val mode: Mode = Mode.EDIT,
        override val isSelected: Boolean = false,
        override val cursor: Int? = null
    ) : BlockView(), Markup, Focusable, Cursor, Text, Indentable, Permission, Selectable {
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
    @Parcelize
    data class Numbered(
        override val id: String,
        override var text: String,
        override var marks: List<Markup.Mark> = emptyList(),
        override val isFocused: Boolean = false,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val indent: Int,
        override val mode: Mode = Mode.EDIT,
        override val isSelected: Boolean = false,
        override val cursor: Int? = null,
        val number: Int
    ) : BlockView(), Markup, Focusable, Cursor, Text, Indentable, Permission, Selectable {
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
    @Parcelize
    data class Toggle(
        override val id: String,
        override var text: String,
        override var marks: List<Markup.Mark> = emptyList(),
        override var isFocused: Boolean,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val indent: Int = 0,
        override val mode: Mode = Mode.EDIT,
        override val isSelected: Boolean = false,
        override val cursor: Int? = null,
        val toggled: Boolean = false,
        val isEmpty: Boolean = false
    ) : BlockView(), Markup, Focusable, Text, Cursor, Indentable, Permission, Selectable {
        override fun getViewType() = HOLDER_TOGGLE
        override val body: String = text
    }

    /**
     * UI-model for a contact block.
     * @property id block's
     * @property name a person's name
     * @property avatar a person's avatar image
     */
    @Parcelize
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
    ) : BlockView(), Indentable, Parcelable, Selectable, Permission {

        /**
         * UI-model for block containing file, with state DONE.
         * @property id block's id
         */
        @Parcelize
        data class View(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
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
        @Parcelize
        data class Upload(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : BlockView.File(id) {
            override fun getViewType() = HOLDER_FILE_UPLOAD
        }

        /**
         * UI-model for block containing file, with state EMPTY.
         * @property id block's id
         */
        @Parcelize
        data class Placeholder(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : BlockView.File(id) {
            override fun getViewType() = HOLDER_FILE_PLACEHOLDER
        }

        /**
         * UI-model for block containing file, with state ERROR.
         * @property id block's id
         */
        @Parcelize
        data class Error(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
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
    ) : BlockView(), Indentable, Parcelable, Selectable, Permission {

        /**
         * UI-model for block containing video, with state DONE.
         */
        @Parcelize
        data class View(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
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
        @Parcelize
        data class Upload(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : BlockView.Video(id) {
            override fun getViewType() = HOLDER_VIDEO_UPLOAD
        }

        /**
         * UI-model for block containing video, with state EMPTY.
         * @property id block's id
         */
        @Parcelize
        data class Placeholder(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : BlockView.Video(id) {
            override fun getViewType() = HOLDER_VIDEO_PLACEHOLDER
        }

        /**
         * UI-model for block containing video, with state ERROR.
         * @property id block's id
         */
        @Parcelize
        data class Error(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
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
    @Parcelize
    data class Page(
        override val id: String,
        override val indent: Int,
        override val isSelected: Boolean = false,
        var text: String? = null,
        val emoji: String?,
        val image: String?,
        val isEmpty: Boolean = false,
        val isArchived: Boolean = false
    ) : BlockView(), Indentable, Selectable {
        override fun getViewType() = HOLDER_PAGE
    }

    /**
     * UI-model for a divider block.
     * @property id block's id
     */
    @Parcelize
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
    ) : BlockView(), Indentable, Parcelable, Selectable, Permission {

        /**
         * UI-model for a bookmark placeholder (used when bookmark url is not set)
         */
        @Parcelize
        data class Placeholder(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
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
        @Parcelize
        data class View(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            val url: String,
            val title: String?,
            val description: String?,
            val faviconUrl: String?,
            val imageUrl: String?
        ) : Bookmark(id = id) {
            override fun getViewType() = HOLDER_BOOKMARK
        }

        /**
         * UI-model for a bookmark view in error state
         * @property url url originally entered by user to create a bookmark
         */
        @Parcelize
        data class Error(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            val url: String
        ) : Bookmark(id = id) {
            override fun getViewType(): Int = HOLDER_BOOKMARK_ERROR
        }
    }

    /**
     * UI-models for blocks containing images
     * @property id block's id
     */
    sealed class Picture(
        override val id: String
    ) : BlockView(), Indentable, Parcelable, Selectable, Permission {

        /**
         * UI-model for block containing image, with state DONE.
         */
        @Parcelize
        data class View(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
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
        @Parcelize
        data class Placeholder(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : BlockView.Picture(id) {
            override fun getViewType() = HOLDER_PICTURE_PLACEHOLDER
        }

        /**
         * UI-model for block containing image, with state ERROR.
         */
        @Parcelize
        data class Error(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : BlockView.Picture(id) {
            override fun getViewType() = HOLDER_PICTURE_ERROR
        }

        /**
         * UI-model for block containing image, with state UPLOADING.
         */
        @Parcelize
        data class Upload(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : BlockView.Picture(id) {
            override fun getViewType() = HOLDER_PICTURE_UPLOAD
        }
    }

    /**
     * Footer block. Just holds space at the end of the page.
     */
    @Parcelize
    object Footer : BlockView() {
        @IgnoredOnParcel
        override val id: String = FOOTER_ID

        override fun getViewType() = HOLDER_FOOTER
    }

    enum class Mode { READ, EDIT }

    companion object {
        const val FOOTER_ID = "FOOTER"
    }
}