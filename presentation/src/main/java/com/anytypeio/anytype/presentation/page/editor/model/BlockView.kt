package com.anytypeio.anytype.presentation.page.editor.model

import android.os.Parcelable
import com.anytypeio.anytype.core_utils.ui.ViewType
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_ARCHIVE_TITLE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_BOOKMARK
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_BOOKMARK_ERROR
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_BOOKMARK_PLACEHOLDER
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_BULLET
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_CHECKBOX
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_CODE_SNIPPET
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_DIVIDER_DOTS
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_DIVIDER_LINE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_FILE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_FILE_ERROR
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_FILE_PLACEHOLDER
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_FILE_UPLOAD
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_HEADER_ONE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_HEADER_THREE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_HEADER_TWO
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_HIGHLIGHT
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_NUMBERED
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_PAGE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_PAGE_ARCHIVE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_PICTURE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_PICTURE_ERROR
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_PICTURE_PLACEHOLDER
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_PICTURE_UPLOAD
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_PROFILE_TITLE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_TITLE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_TOGGLE
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_VIDEO
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_VIDEO_ERROR
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_VIDEO_PLACEHOLDER
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_VIDEO_UPLOAD
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

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
    interface TextSupport {

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
     * Views implementing this interface are supposed to highlight search results.
     */
    interface Searchable {
        val searchFields: List<Field>

        data class Field(
            val key: String = DEFAULT_SEARCH_FIELD_KEY,
            val highlights: List<IntRange> = emptyList(),
            val target: IntRange = IntRange.EMPTY
        ) {

            val isTargeted = !target.isEmpty()

            companion object {
                const val DEFAULT_SEARCH_FIELD_KEY = "default"
            }
        }
    }

    interface TextBlockProps :
        Markup,
        Focusable,
        TextSupport,
        Cursor,
        Indentable,
        Permission,
        Alignable,
        Selectable {
        val id: String
    }

    sealed class Text : BlockView(), TextBlockProps, Searchable {

        // Dynamic properties (expected to be synchronised with framework widget)

        abstract override var text: String
        abstract override var marks: List<Markup.Mark>
        abstract override var isFocused: Boolean

        // Stable properties

        abstract override val color: String?
        abstract override val backgroundColor: String?
        abstract override val mode: Mode
        abstract override val cursor: Int?
        abstract override val alignment: Alignment?

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
            override val cursor: Int? = null,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList()
        ) : Text() {
            override fun getViewType() = HOLDER_PARAGRAPH
            override val body: String get() = text
        }

        sealed class Header : Text() {

            /**
             * UI-model for a header block.
             * @property id block's id
             * @property text header's content (i.e. a header's text)
             * @property color text color
             * @property marks markup
             */
            @Parcelize
            data class One(
                override val id: String,
                override var text: String,
                override var isFocused: Boolean = false,
                override val color: String? = null,
                override val backgroundColor: String? = null,
                override val indent: Int = 0,
                override var marks: List<Markup.Mark> = emptyList(),
                override val mode: Mode = Mode.EDIT,
                override val isSelected: Boolean = false,
                override val alignment: Alignment? = null,
                override val cursor: Int? = null,
                override val searchFields: @RawValue List<Searchable.Field> = emptyList()
            ) : Header() {
                override fun getViewType() = HOLDER_HEADER_ONE
                override val body: String get() = text
            }

            /**
             * UI-model for a header block.
             * @property id block's id
             * @property text header's content (i.e. a header's text)
             * @property color text color
             * @property marks markup
             */
            @Parcelize
            data class Two(
                override val id: String,
                override val color: String? = null,
                override var text: String,
                override var isFocused: Boolean = false,
                override val backgroundColor: String? = null,
                override val indent: Int = 0,
                override var marks: List<Markup.Mark> = emptyList(),
                override val mode: Mode = Mode.EDIT,
                override val isSelected: Boolean = false,
                override val alignment: Alignment? = null,
                override val cursor: Int? = null,
                override val searchFields: @RawValue List<Searchable.Field> = emptyList()
            ) : Header() {
                override fun getViewType() = HOLDER_HEADER_TWO
                override val body: String get() = text
            }

            /**
             * UI-model for a header block.
             * @property id block's id
             * @property text header's content (i.e. a header's text)
             * @property color text color
             * @property marks markup
             */
            @Parcelize
            data class Three(
                override val id: String,
                override val color: String? = null,
                override var text: String,
                override var isFocused: Boolean = false,
                override val backgroundColor: String? = null,
                override val indent: Int = 0,
                override var marks: List<Markup.Mark> = emptyList(),
                override val mode: Mode = Mode.EDIT,
                override val isSelected: Boolean = false,
                override val alignment: Alignment? = null,
                override val cursor: Int? = null,
                override val searchFields: @RawValue List<Searchable.Field> = emptyList()
            ) : Header() {
                override fun getViewType() = HOLDER_HEADER_THREE
                override val body: String get() = text
            }
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
            override var isFocused: Boolean = false,
            override var text: String,
            override val color: String? = null,
            override val backgroundColor: String? = null,
            override val indent: Int = 0,
            override var marks: List<Markup.Mark> = emptyList(),
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList()
        ) : Text() {
            override fun getViewType() = HOLDER_HIGHLIGHT
            override val body: String get() = text
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
            override var isFocused: Boolean = false,
            override var text: String,
            override val color: String? = null,
            override val backgroundColor: String? = null,
            override var isChecked: Boolean = false,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList()
        ) : Text(), Checkable {
            override fun getViewType() = HOLDER_CHECKBOX
            override val body: String get() = text
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
            override var isFocused: Boolean = false,
            override val color: String? = null,
            override val backgroundColor: String? = null,
            override var text: String,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList()
        ) : Text() {
            override fun getViewType() = HOLDER_BULLET
            override val body: String get() = text
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
            override var isFocused: Boolean = false,
            override val color: String? = null,
            override val backgroundColor: String? = null,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList(),
            val number: Int
        ) : Text() {
            override fun getViewType() = HOLDER_NUMBERED
            override val body: String get() = text
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
            override var isFocused: Boolean = false,
            override val color: String? = null,
            override val backgroundColor: String? = null,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList(),
            val toggled: Boolean = false,
            val isEmpty: Boolean = false
        ) : Text() {
            override fun getViewType() = HOLDER_TOGGLE
            override val body: String get() = text
        }
    }

    sealed class Title : BlockView(), Focusable, Cursor, Permission {

        abstract val image: String?
        abstract var text: String?

        /**
         * UI-model for a title block.
         * @property id block's id
         * @property text text content (i.e. title text)
         */
        @Parcelize
        data class Document(
            override val id: String,
            override var isFocused: Boolean = false,
            override var text: String?,
            val emoji: String? = null,
            override val image: String? = null,
            override val mode: Mode = Mode.EDIT,
            override val cursor: Int? = null,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList()
        ) : Title(), Searchable {
            override fun getViewType() = HOLDER_TITLE
        }

        /**
         * UI-model for a profile title block.
         * @property id block's id
         * @property text text content (i.e. title text)
         * @property image image as a page's logo (if present)
         */
        @Parcelize
        data class Profile(
            override val id: String,
            override var isFocused: Boolean,
            override var text: String?,
            override val image: String? = null,
            override val mode: Mode = Mode.EDIT,
            override val cursor: Int? = null,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList()
        ) : Title(), Searchable {
            override fun getViewType() = HOLDER_PROFILE_TITLE
        }

        /**
         * UI-model for a archive title block.
         * @property id block's id
         * @property text text content (i.e. title text)
         * @property image image as a page's logo (if present)
         */
        @Parcelize
        data class Archive(
            override val id: String,
            override var isFocused: Boolean = false,
            override var text: String?,
            override val image: String? = null,
            override val mode: Mode = Mode.READ,
            override val cursor: Int? = null
        ) : Title() {
            override fun getViewType() = HOLDER_ARCHIVE_TITLE
        }

    }

    /**
     * UI-model for a code-snippet block.
     * @property id block's id
     * @property text blocks's content (i.e. code snippet)
     */
    @Parcelize
    data class Code(
        override val id: String,
        override var text: String,
        override val mode: Mode = Mode.EDIT,
        override var isFocused: Boolean = false,
        override val isSelected: Boolean = false,
        override val color: String? = null,
        override val backgroundColor: String? = null,
        override val indent: Int = 0,
        val lang: String? = null
    ) : BlockView(), Permission, Selectable, Focusable, Indentable, TextSupport {
        override fun getViewType() = HOLDER_CODE_SNIPPET
    }

    sealed class Error : BlockView(), Indentable, Parcelable, Selectable, Permission {

        abstract override val id: String
        abstract override val indent: Int
        abstract override val mode: Mode
        abstract override val isSelected: Boolean

        /**
         * UI-model for block containing video, with state ERROR.
         * @property id block's id
         */
        @Parcelize
        data class File(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : Error() {
            override fun getViewType() = HOLDER_FILE_ERROR
        }

        /**
         * UI-model for block containing video, with state ERROR.
         * @property id block's id
         */
        @Parcelize
        data class Video(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : Error() {
            override fun getViewType() = HOLDER_VIDEO_ERROR
        }

        /**
         * UI-model for block containing image, with state ERROR.
         */
        @Parcelize
        data class Picture(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : Error() {
            override fun getViewType() = HOLDER_PICTURE_ERROR
        }

        /**
         * UI-model for a bookmark view in error state
         * @property url url originally entered by user to create a bookmark
         */
        @Parcelize
        data class Bookmark(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            val url: String
        ) : Error() {
            override fun getViewType(): Int = HOLDER_BOOKMARK_ERROR
        }
    }

    sealed class Upload : BlockView(), Indentable, Parcelable, Selectable, Permission {

        abstract override val id: String
        abstract override val indent: Int
        abstract override val mode: Mode
        abstract override val isSelected: Boolean

        /**
         * UI-model for block containing file, with state UPLOADING.
         * @property id block's id
         */
        @Parcelize
        data class File(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : Upload() {
            override fun getViewType() = HOLDER_FILE_UPLOAD
        }

        /**
         * UI-model for block containing video, with state UPLOADING.
         * @property id block's id
         */
        @Parcelize
        data class Video(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : Upload() {
            override fun getViewType() = HOLDER_VIDEO_UPLOAD
        }

        /**
         * UI-model for block containing image, with state UPLOADING.
         */
        @Parcelize
        data class Picture(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : Upload() {
            override fun getViewType() = HOLDER_PICTURE_UPLOAD
        }
    }

    sealed class MediaPlaceholder : BlockView(), Indentable, Parcelable, Selectable, Permission {

        abstract override val id: String
        abstract override val indent: Int
        abstract override val mode: Mode
        abstract override val isSelected: Boolean

        /**
         * UI-model for block containing file, with state EMPTY.
         * @property id block's id
         */
        @Parcelize
        data class File(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : MediaPlaceholder() {
            override fun getViewType() = HOLDER_FILE_PLACEHOLDER
        }

        /**
         * UI-model for block containing video, with state EMPTY.
         * @property id block's id
         */
        @Parcelize
        data class Video(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : MediaPlaceholder() {
            override fun getViewType() = HOLDER_VIDEO_PLACEHOLDER
        }

        /**
         * UI-model for a bookmark placeholder (used when bookmark url is not set)
         */
        @Parcelize
        data class Bookmark(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : MediaPlaceholder() {
            override fun getViewType() = HOLDER_BOOKMARK_PLACEHOLDER
        }

        /**
         * UI-model for block containing image, with state EMPTY.
         */
        @Parcelize
        data class Picture(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false
        ) : MediaPlaceholder() {
            override fun getViewType() = HOLDER_PICTURE_PLACEHOLDER
        }

    }

    sealed class Media : BlockView(), Indentable, Parcelable, Selectable, Permission {

        abstract override val id: String
        abstract override val indent: Int
        abstract override val mode: Mode
        abstract override val isSelected: Boolean

        /**
         * UI-model for block containing file, with state DONE.
         * @property id block's id
         */
        @Parcelize
        data class File(
            override val id: String,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList(),
            val size: Long?,
            val name: String?,
            val mime: String?,
            val hash: String?,
            val url: String
        ) : Media(), Searchable {
            override fun getViewType() = HOLDER_FILE
        }

        /**
         * UI-model for block containing video, with state DONE.
         */
        @Parcelize
        data class Video(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            val size: Long?,
            val name: String?,
            val mime: String?,
            val hash: String?,
            val url: String
        ) : Media() {
            override fun getViewType() = HOLDER_VIDEO
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
        data class Bookmark(
            override val id: String,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val searchFields: @RawValue List<Searchable.Field> = emptyList(),
            val url: String,
            val title: String?,
            val description: String?,
            val faviconUrl: String? = null,
            val imageUrl: String? = null,
        ) : Media(), Searchable {
            override fun getViewType() = HOLDER_BOOKMARK

            companion object {
                const val SEARCH_FIELD_DESCRIPTION_KEY = "description"
                const val SEARCH_FIELD_TITLE_KEY = "title"
                const val SEARCH_FIELD_URL_KEY = "url"
            }
        }

        /**
         * UI-model for block containing image, with state DONE.
         */
        @Parcelize
        data class Picture(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            val size: Long?,
            val name: String?,
            val mime: String?,
            val hash: String?,
            val url: String
        ) : Media() {
            override fun getViewType() = HOLDER_PICTURE
        }
    }

    /**
     * UI-model for blocks containing page links.
     * @property id block's id
     * @property text a page's name
     * @property emoji a page's emoji (if present)
     * @property isEmpty this property determines whether this page is empty or not
     */
    @Parcelize
    data class Page(
        override val id: String,
        override val indent: Int,
        override val isSelected: Boolean = false,
        override val searchFields: @RawValue List<Searchable.Field> = emptyList(),
        var text: String? = null,
        val emoji: String?,
        val image: String?,
        val isEmpty: Boolean = false
    ) : BlockView(), Indentable, Selectable, Searchable {
        override fun getViewType() = HOLDER_PAGE
    }

    /**
     * UI-model for blocks containing archived page links.
     * @property id block's id
     * @property text a page's name
     * @property emoji a page's emoji (if present)
     * @property isEmpty this property determines whether this page is empty or not
     */
    @Parcelize
    data class PageArchive(
        override val id: String,
        override val indent: Int,
        override val isSelected: Boolean = false,
        override val searchFields: @RawValue List<Searchable.Field> = emptyList(),
        var text: String? = null,
        val emoji: String?,
        val image: String?,
        val isEmpty: Boolean = false
    ) : BlockView(), Indentable, Selectable, Searchable {
        override fun getViewType() = HOLDER_PAGE_ARCHIVE
    }

    /**
     * UI-model for a line divider block.
     * @property id block's id
     */
    @Parcelize
    data class DividerLine(
        override val id: String,
        override val isSelected: Boolean = false,
        override val indent: Int = 0
    ) : BlockView(), Selectable, Indentable {
        override fun getViewType() = HOLDER_DIVIDER_LINE
    }

    /**
     * UI-model for a dots divider block.
     * @property id block's id
     */
    @Parcelize
    data class DividerDots(
        override val id: String,
        override val isSelected: Boolean = false,
        override val indent: Int = 0
    ) : BlockView(), Selectable, Indentable {
        override fun getViewType() = HOLDER_DIVIDER_DOTS
    }

    enum class Mode { READ, EDIT }
}