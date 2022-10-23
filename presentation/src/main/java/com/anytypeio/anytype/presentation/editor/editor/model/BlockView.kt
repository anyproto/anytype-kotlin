package com.anytypeio.anytype.presentation.editor.editor.model

import com.anytypeio.anytype.core_models.Block.Content.Link.*
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_utils.ui.ViewType
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_ARCHIVE_TITLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BOOKMARK
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BOOKMARK_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BOOKMARK_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BOOKMARK_UPLOAD
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BULLET
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_CALLOUT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_CHECKBOX
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_CODE_SNIPPET
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_DESCRIPTION
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_DIVIDER_DOTS
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_DIVIDER_LINE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FEATURED_RELATION
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE_UPLOAD
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_ONE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_THREE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_TWO
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HIGHLIGHT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_LATEX
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_NUMBERED
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_ARCHIVE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_CARD_MEDIUM_ICON
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_CARD_MEDIUM_ICON_COVER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_CARD_SMALL_ICON
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_CARD_SMALL_ICON_COVER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_DEFAULT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_DELETED
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_LOADING
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_TYPE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE_UPLOAD
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PROFILE_TITLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_CHECKBOX
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_DEFAULT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_FILE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_OBJECT
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_SOURCE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_STATUS
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_RELATION_TAGS
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TABLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TITLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TOC
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TODO_TITLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TOGGLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_UNSUPPORTED
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_UPLOAD
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView

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
    interface TextSupport {

        /**
         * Base text color for the text.
         * If not present, default color will be applied.
         */
        val color: ThemeColor

        /**
         * Background color for the whole block as opposed to text highlight background.
         * If not present, default color will be applied.
         */
        val background: ThemeColor

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
     * Views implementing this interface can change IME action on keyboard
     * @property inputAction -
     */
    interface SupportInputAction {
        val inputAction: InputAction
    }

    sealed interface InputAction {
        object NewLine : InputAction
        object Done : InputAction
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
     * Views implementing this interface can indicate that its content is loading
     */
    interface Loadable {
        val isLoading: Boolean
    }

    interface Decoratable {
        val decorations: List<Decoration>
    }

    data class Decoration(
        val style: Style = Style.None,
        val background: ThemeColor = ThemeColor.DEFAULT
    ) {
        sealed class Style {
            fun isCard() = this is Card || this is Callout
            fun isCode() = this is Code

            object None : Style()
            sealed class Highlight : Style() {
                /**
                 * [Itself] represents style of highlight block itself at a given indent level,
                 * as opposed to highlight block style inherited from parent blocks.
                 */
                data class Itself(val hasChildren: Boolean = false) : Highlight()
                object Start : Highlight()
                object Middle : Highlight()
                object End : Highlight()
            }

            sealed class Callout : Style() {
                object Start : Callout()
                object Middle : Callout()
                object End : Callout()
                object Full : Callout()
            }

            sealed class Header : Style() {
                object H1 : Header()
                object H2 : Header()
                object H3 : Header()
            }
            object Code : Style()
            object Card : Style()
        }
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

    interface SupportGhostEditorSelection {
        val ghostEditorSelection: IntRange?
    }

    interface TextBlockProps :
        Markup,
        Focusable,
        TextSupport,
        Cursor,
        Indentable,
        Permission,
        Alignable,
        Selectable,
        SupportInputAction {
        val id: String
    }

    interface Appearance {

        data class InEditor(
            val isCard: Boolean,
            val showIcon: Boolean,
            val description: Description,
            val showCover: Boolean,
            val showType: Boolean,
            val icon: Icon
        ) {
            sealed interface Description {
                object NONE : Description
                object SNIPPET : Description
                object RELATION : Description
            }

            sealed interface Icon {
                object NONE : Icon
                object SMALL : Icon
                object MEDIUM : Icon
            }
        }

        data class Menu(
            val preview: MenuItem.PreviewLayout,
            val icon: MenuItem.Icon?,
            val iconMenus: List<ObjectAppearanceChooseSettingsView.Icon>,
            val cover: MenuItem.Cover?,
            val description: MenuItem.Description?,
            val objectType: MenuItem.ObjectType,
        )

        sealed interface MenuItem {
            sealed interface Icon : MenuItem {
                object NONE : Icon
                object SMALL : Icon
                object MEDIUM : Icon
            }

            sealed interface Description : MenuItem {
                object NONE : Description
                object ADDED : Description
                object CONTENT : Description
            }

            sealed interface Cover : MenuItem {
                object WITH : Cover
                object WITHOUT : Cover
            }

            sealed interface PreviewLayout : MenuItem {
                object TEXT : PreviewLayout
                object CARD : PreviewLayout
                object INLINE : PreviewLayout
            }

            sealed interface ObjectType : MenuItem {
                fun isChecked(): Boolean = when (this) {
                    WITH -> true
                    WITHOUT -> false
                }

                object WITH : ObjectType
                object WITHOUT : ObjectType
            }
        }
    }

    sealed class Text : BlockView(), TextBlockProps, Searchable, SupportGhostEditorSelection,
        Decoratable {

        // Dynamic properties (expected to be synchronised with framework widget)

        abstract override var text: String
        abstract override var marks: List<Markup.Mark>
        abstract override var isFocused: Boolean

        // Stable properties

        abstract override val color: ThemeColor
        abstract override val background: ThemeColor
        abstract override val mode: Mode
        abstract override var cursor: Int?
        abstract override val alignment: Alignment?

        val isListBlock: Boolean get() = (this is Bulleted || this is Checkbox || this is Numbered || this is Toggle)

        /**
         * UI-model for a basic paragraph block.
         * @property id block's id
         * @property text block's content text
         * @property marks markup
         * @property isFocused whether this block is currently focused or not
         * @property color text color
         */
        data class Paragraph(
            override val id: String,
            override var text: String,
            override var marks: List<Markup.Mark> = emptyList(),
            override var isFocused: Boolean = false,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val alignment: Alignment? = null,
            override var cursor: Int? = null,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val ghostEditorSelection: IntRange? = null,
            override val decorations: List<Decoration> = emptyList(),
            override var inputAction: InputAction = InputAction.NewLine
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
            data class One(
                override val id: String,
                override var text: String,
                override var isFocused: Boolean = false,
                override val color: ThemeColor = ThemeColor.DEFAULT,
                override val background: ThemeColor = ThemeColor.DEFAULT,
                override val indent: Int = 0,
                override var marks: List<Markup.Mark> = emptyList(),
                override val mode: Mode = Mode.EDIT,
                override val isSelected: Boolean = false,
                override val alignment: Alignment? = null,
                override var cursor: Int? = null,
                override val searchFields: List<Searchable.Field> = emptyList(),
                override val ghostEditorSelection: IntRange? = null,
                override val decorations: List<Decoration> = emptyList(),
                override var inputAction: InputAction = InputAction.NewLine
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
            data class Two(
                override val id: String,
                override val color: ThemeColor = ThemeColor.DEFAULT,
                override var text: String,
                override var isFocused: Boolean = false,
                override val background: ThemeColor = ThemeColor.DEFAULT,
                override val indent: Int = 0,
                override var marks: List<Markup.Mark> = emptyList(),
                override val mode: Mode = Mode.EDIT,
                override val isSelected: Boolean = false,
                override val alignment: Alignment? = null,
                override var cursor: Int? = null,
                override val searchFields: List<Searchable.Field> = emptyList(),
                override val ghostEditorSelection: IntRange? = null,
                override val decorations: List<Decoration> = emptyList(),
                override var inputAction: InputAction = InputAction.NewLine
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
            data class Three(
                override val id: String,
                override val color: ThemeColor = ThemeColor.DEFAULT,
                override var text: String,
                override var isFocused: Boolean = false,
                override val background: ThemeColor = ThemeColor.DEFAULT,
                override val indent: Int = 0,
                override var marks: List<Markup.Mark> = emptyList(),
                override val mode: Mode = Mode.EDIT,
                override val isSelected: Boolean = false,
                override val alignment: Alignment? = null,
                override var cursor: Int? = null,
                override val searchFields: List<Searchable.Field> = emptyList(),
                override val ghostEditorSelection: IntRange? = null,
                override val decorations: List<Decoration> = emptyList(),
                override var inputAction: InputAction = InputAction.NewLine
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
        data class Highlight(
            override val id: String,
            override var isFocused: Boolean = false,
            override var text: String,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val indent: Int = 0,
            override var marks: List<Markup.Mark> = emptyList(),
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override var cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val ghostEditorSelection: IntRange? = null,
            override val decorations: List<Decoration> = emptyList(),
            override var inputAction: InputAction = InputAction.NewLine
        ) : Text() {
            override fun getViewType() = HOLDER_HIGHLIGHT
            override val body: String get() = text
        }

        data class Callout(
            override val id: String,
            override var isFocused: Boolean = false,
            override var text: String,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val indent: Int = 0,
            override var marks: List<Markup.Mark> = emptyList(),
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override var cursor: Int? = null,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val ghostEditorSelection: IntRange? = null,
            override val decorations: List<Decoration> = emptyList(),
            val icon: ObjectIcon,
            override var inputAction: InputAction = InputAction.NewLine
        ) : Text() {
            override val alignment: Alignment? = null
            override fun getViewType() = HOLDER_CALLOUT
            override val body: String get() = text
        }

        /**
         * UI-model for checkbox blocks.
         * @property id block's id
         * @property text checkbox's content text
         * @property isChecked immutable checkbox state (whether this checkbox is checked or not)
         */
        data class Checkbox(
            override val id: String,
            override var marks: List<Markup.Mark> = emptyList(),
            override var isFocused: Boolean = false,
            override var text: String,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override var isChecked: Boolean = false,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override var cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val ghostEditorSelection: IntRange? = null,
            override val decorations: List<Decoration> = emptyList(),
            override var inputAction: InputAction = InputAction.NewLine
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
        data class Bulleted(
            override val id: String,
            override var marks: List<Markup.Mark> = emptyList(),
            override var isFocused: Boolean = false,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override var text: String,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override var cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val ghostEditorSelection: IntRange? = null,
            override val decorations: List<Decoration> = emptyList(),
            override var inputAction: InputAction = InputAction.NewLine
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
        data class Numbered(
            override val id: String,
            override var text: String,
            override var marks: List<Markup.Mark> = emptyList(),
            override var isFocused: Boolean = false,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override var cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val ghostEditorSelection: IntRange? = null,
            override val decorations: List<Decoration> = emptyList(),
            val number: Int,
            override var inputAction: InputAction = InputAction.NewLine
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
        data class Toggle(
            override val id: String,
            override var text: String,
            override var marks: List<Markup.Mark> = emptyList(),
            override var isFocused: Boolean = false,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override var cursor: Int? = null,
            override val alignment: Alignment? = null,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val decorations: List<Decoration> = emptyList(),
            override val ghostEditorSelection: IntRange? = null,
            val toggled: Boolean = false,
            val isEmpty: Boolean = false,
            override var inputAction: InputAction = InputAction.NewLine
        ) : Text() {
            override fun getViewType() = HOLDER_TOGGLE
            override val body: String get() = text
            val isCreateBlockButtonVisible: Boolean
                get() {
                    return mode == Mode.EDIT && toggled && isEmpty
                }
        }
    }

    data class Description(
        override val id: String,
        override val mode: Mode = Mode.EDIT,
        override var text: String,
        override var isFocused: Boolean = false,
        override var cursor: Int? = null,
        override val color: ThemeColor = ThemeColor.DEFAULT,
        override val background: ThemeColor = ThemeColor.DEFAULT,
    ) : BlockView(), TextSupport, Focusable, Cursor, Permission {
        override fun getViewType(): Int = HOLDER_DESCRIPTION
    }

    sealed class Title : BlockView(), TextSupport, Focusable, Cursor, Permission {

        abstract val image: String?
        abstract override var text: String
        abstract var coverColor: CoverColor?
        abstract var coverImage: Url?
        abstract var coverGradient: String?
        override val color: ThemeColor = ThemeColor.DEFAULT
        abstract override val background: ThemeColor

        val hasCover get() = coverColor != null || coverImage != null || coverGradient != null

        /**
         * UI-model for a basic-layout title block.
         * @property id block's id
         * @property text text content (i.e. title text)
         */
        data class Basic(
            override val id: String,
            override var isFocused: Boolean = false,
            override var text: String,
            override var coverColor: CoverColor? = null,
            override var coverImage: Url? = null,
            override var coverGradient: String? = null,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            val emoji: String? = null,
            override val image: String? = null,
            override val mode: Mode = Mode.EDIT,
            override var cursor: Int? = null,
            override val searchFields: List<Searchable.Field> = emptyList()
        ) : Title(), Searchable {
            override fun getViewType() = HOLDER_TITLE
        }

        /**
         * UI-model for a profile-layout title block.
         * @property id block's id
         * @property text text content (i.e. title text)
         * @property image image as a page's logo (if present)
         */
        data class Profile(
            override val id: String,
            override var isFocused: Boolean = false,
            override var text: String,
            override var coverColor: CoverColor? = null,
            override var coverImage: Url? = null,
            override var coverGradient: String? = null,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val image: String? = null,
            override val mode: Mode = Mode.EDIT,
            override var cursor: Int? = null,
            override val searchFields: List<Searchable.Field> = emptyList()
        ) : Title(), Searchable {
            override fun getViewType() = HOLDER_PROFILE_TITLE
        }

        /**
         * UI-model for a todo-layout title block.
         * @property id block's id
         * @property text text content (i.e. title text)
         */
        data class Todo(
            override val id: String,
            override var isFocused: Boolean = false,
            override var text: String,
            override val image: String? = null,
            override var coverColor: CoverColor? = null,
            override var coverImage: Url? = null,
            override var coverGradient: String? = null,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val mode: Mode = Mode.EDIT,
            override var cursor: Int? = null,
            override val searchFields: List<Searchable.Field> = emptyList(),
            var isChecked: Boolean = false,
        ) : Title(), Searchable {
            override fun getViewType() = HOLDER_TODO_TITLE
        }

        /**
         * UI-model for a archive title block.
         * @property id block's id
         * @property text text content (i.e. title text)
         * @property image image as a page's logo (if present)
         */
        data class Archive(
            override val id: String,
            override var isFocused: Boolean = false,
            override var text: String,
            override val image: String? = null,
            override var coverColor: CoverColor? = null,
            override var coverImage: Url? = null,
            override var coverGradient: String? = null,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val color: ThemeColor = ThemeColor.DEFAULT,
            override val mode: Mode = Mode.READ,
            override var cursor: Int? = null
        ) : Title() {
            override fun getViewType() = HOLDER_ARCHIVE_TITLE
        }
    }

    /**
     * UI-model for a code-snippet block.
     * @property id block's id
     * @property text blocks's content (i.e. code snippet)
     */
    data class Code(
        override val id: String,
        override var text: String,
        override val mode: Mode = Mode.EDIT,
        override var isFocused: Boolean = false,
        override val isSelected: Boolean = false,
        override val color: ThemeColor = ThemeColor.DEFAULT,
        override var cursor: Int? = null,
        override val background: ThemeColor = ThemeColor.DEFAULT,
        override val indent: Int = 0,
        override val decorations: List<Decoration> = emptyList(),
        val lang: String? = null
    ) : BlockView(), Permission, Selectable, Focusable, Cursor, Indentable, TextSupport, Decoratable {
        override fun getViewType() = HOLDER_CODE_SNIPPET
    }

    sealed class Error : BlockView(), Indentable, Selectable, Permission, Decoratable {

        abstract override val id: String
        abstract override val indent: Int
        abstract override val mode: Mode
        abstract override val isSelected: Boolean
        abstract val background: ThemeColor

        /**
         * UI-model for block containing video, with state ERROR.
         * @property id block's id
         */
        data class File(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList()
        ) : Error() {
            override fun getViewType() = HOLDER_FILE_ERROR
        }

        /**
         * UI-model for block containing video, with state ERROR.
         * @property id block's id
         */
        data class Video(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList()
        ) : Error() {
            override fun getViewType() = HOLDER_VIDEO_ERROR
        }

        /**
         * UI-model for block containing image, with state ERROR.
         */
        data class Picture(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList()
        ) : Error() {
            override fun getViewType() = HOLDER_PICTURE_ERROR
        }

        /**
         * UI-model for a bookmark view in error state
         * @property url url originally entered by user to create a bookmark
         */
        data class Bookmark(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList(),
            val url: String,
        ) : Error() {
            override fun getViewType(): Int = HOLDER_BOOKMARK_ERROR
        }
    }

    sealed class Upload : BlockView(), Indentable, Selectable, Permission, Decoratable {

        abstract override val id: String
        abstract override val indent: Int
        abstract override val mode: Mode
        abstract override val isSelected: Boolean
        abstract val background: ThemeColor

        /**
         * UI-model for block containing file, with state UPLOADING.
         * @property id block's id
         */
        data class File(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList()
        ) : Upload() {
            override fun getViewType() = HOLDER_FILE_UPLOAD
        }

        /**
         * UI-model for block containing video, with state UPLOADING.
         * @property id block's id
         */
        data class Video(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList()
        ) : Upload() {
            override fun getViewType() = HOLDER_VIDEO_UPLOAD
        }

        /**
         * UI-model for block containing image, with state UPLOADING.
         */
        data class Picture(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList()
        ) : Upload() {
            override fun getViewType() = HOLDER_PICTURE_UPLOAD
        }

        /**
         * UI-model for bookmark block, with state UPLOADING.
         * @property id block's id
         */
        data class Bookmark(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList(),
            val url: Url? = null
        ) : Upload() {
            override fun getViewType() = HOLDER_BOOKMARK_UPLOAD
        }
    }

    sealed class MediaPlaceholder : BlockView(), Indentable, Selectable, Permission, Decoratable {

        abstract override val id: String
        abstract override val indent: Int
        abstract override val mode: Mode
        abstract override val isSelected: Boolean
        abstract val background: ThemeColor
        abstract val isPreviousBlockMedia: Boolean

        /**
         * UI-model for block containing file, with state EMPTY.
         * @property id block's id
         */
        data class File(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val isPreviousBlockMedia: Boolean,
            override val decorations: List<Decoration> = emptyList()
        ) : MediaPlaceholder() {
            override fun getViewType() = HOLDER_FILE_PLACEHOLDER
        }

        /**
         * UI-model for block containing video, with state EMPTY.
         * @property id block's id
         */
        data class Video(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val isPreviousBlockMedia: Boolean,
            override val decorations: List<Decoration> = emptyList()
        ) : MediaPlaceholder() {
            override fun getViewType() = HOLDER_VIDEO_PLACEHOLDER
        }

        /**
         * UI-model for a bookmark placeholder (used when bookmark url is not set)
         */
        data class Bookmark(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val isPreviousBlockMedia: Boolean,
            override val decorations: List<Decoration> = emptyList(),
            val isLoading: Boolean = false
        ) : MediaPlaceholder() {
            override fun getViewType() = HOLDER_BOOKMARK_PLACEHOLDER
        }

        /**
         * UI-model for block containing image, with state EMPTY.
         */
        data class Picture(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val isPreviousBlockMedia: Boolean,
            override val decorations: List<Decoration> = emptyList()
        ) : MediaPlaceholder() {
            override fun getViewType() = HOLDER_PICTURE_PLACEHOLDER
        }

    }

    sealed class Media : BlockView(), Indentable, Selectable, Permission, Decoratable {

        abstract override val id: String
        abstract override val indent: Int
        abstract override val mode: Mode
        abstract override val isSelected: Boolean
        abstract val background: ThemeColor

        /**
         * UI-model for block containing file, with state DONE.
         * @property id block's id
         */
        data class File(
            override val id: String,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList(),
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
        data class Video(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList(),
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
        data class Bookmark(
            override val id: String,
            override val indent: Int = 0,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList(),
            val url: String,
            val title: String?,
            val description: String?,
            val faviconUrl: String? = null,
            val imageUrl: String? = null,
            val isPreviousBlockMedia: Boolean
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
        data class Picture(
            override val id: String,
            override val indent: Int,
            override val mode: Mode = Mode.EDIT,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            override val decorations: List<Decoration> = emptyList(),
            val size: Long?,
            val name: String?,
            val mime: String?,
            val hash: String?,
            val url: String
        ) : Media() {
            override fun getViewType() = HOLDER_PICTURE
        }
    }

    sealed class LinkToObject : BlockView(), Indentable, Selectable, Decoratable {

        /**
         * UI-model for blocks containing links to objects.
         * @property id block's id
         * @property text a page's name
         * @property emoji a page's emoji (if present)
         * @property isEmpty this property determines whether this page is empty or not
         * @property isArchived this property determines whether this linked object is archived or not
         * @property isDeleted this property determines whether this linked object is deleted or not.
         * Whenever isDeleted is true, we don't care about isArchived flags
         */
        sealed class Default : LinkToObject(), Searchable {

            abstract val text: String?
            abstract val description: String?
            abstract val icon: ObjectIcon
            abstract val background: ThemeColor
            abstract val objectTypeName: String?

            data class Text(
                override val id: String,
                override val indent: Int = 0,
                override val isSelected: Boolean = false,
                override val searchFields: List<Searchable.Field> = emptyList(),
                override val text: String? = null,
                override val description: String? = null,
                override val icon: ObjectIcon,
                override val background: ThemeColor = ThemeColor.DEFAULT,
                override val decorations: List<Decoration> = emptyList(),
                override val objectTypeName: String? = null
            ) : Default() {
                override fun getViewType() = HOLDER_OBJECT_LINK_DEFAULT
            }

            sealed class Card: Default() {

                abstract val isPreviousBlockMedia: Boolean

                data class SmallIcon(
                    override val id: String,
                    override val indent: Int = 0,
                    override val isSelected: Boolean = false,
                    override val searchFields: List<Searchable.Field> = emptyList(),
                    override val text: String? = null,
                    override val description: String? = null,
                    override val icon: ObjectIcon,
                    override val background: ThemeColor,
                    override val decorations: List<Decoration> = emptyList(),
                    override val objectTypeName: String? = null,
                    override val isPreviousBlockMedia: Boolean
                ) : Card() {
                    override fun getViewType() = HOLDER_OBJECT_LINK_CARD_SMALL_ICON
                }

                data class MediumIcon(
                    override val id: String,
                    override val indent: Int = 0,
                    override val isSelected: Boolean = false,
                    override val searchFields: List<Searchable.Field> = emptyList(),
                    override val text: String? = null,
                    override val description: String? = null,
                    override val icon: ObjectIcon,
                    override val background: ThemeColor,
                    override val decorations: List<Decoration> = emptyList(),
                    override val objectTypeName: String? = null,
                    override val isPreviousBlockMedia: Boolean
                ) : Card() {
                    override fun getViewType() = HOLDER_OBJECT_LINK_CARD_MEDIUM_ICON
                }

                data class SmallIconCover(
                    override val id: String,
                    override val indent: Int = 0,
                    override val isSelected: Boolean = false,
                    override val searchFields: List<Searchable.Field> = emptyList(),
                    override val text: String? = null,
                    override val description: String? = null,
                    override val icon: ObjectIcon,
                    override val background: ThemeColor,
                    override val decorations: List<Decoration> = emptyList(),
                    override val objectTypeName: String? = null,
                    override val isPreviousBlockMedia: Boolean,
                    val cover : Cover?
                ) : Card() {
                    override fun getViewType() = HOLDER_OBJECT_LINK_CARD_SMALL_ICON_COVER
                }

                data class MediumIconCover(
                    override val id: String,
                    override val indent: Int = 0,
                    override val isSelected: Boolean = false,
                    override val searchFields: List<Searchable.Field> = emptyList(),
                    override val text: String? = null,
                    override val description: String? = null,
                    override val icon: ObjectIcon,
                    override val background: ThemeColor,
                    override val decorations: List<Decoration> = emptyList(),
                    override val objectTypeName: String? = null,
                    override val isPreviousBlockMedia: Boolean,
                    val cover : Cover?
                ) : Card() {
                    override fun getViewType() = HOLDER_OBJECT_LINK_CARD_MEDIUM_ICON_COVER
                }

                sealed class Cover {
                    data class Color(val color: CoverColor) : Cover()
                    data class Image(val url: Url) : Cover()
                    data class Gradient(val gradient: String) : Cover()
                }
            }
        }

        /**
         * UI-model for blocks containing links to archived objects.
         * @property id block's id
         * @property text a page's name
         * @property emoji a page's emoji (if present)
         * @property isEmpty this property determines whether this page is empty or not
         */
        data class Archived(
            override val id: String,
            override val indent: Int = 0,
            override val isSelected: Boolean = false,
            override val searchFields: List<Searchable.Field> = emptyList(),
            override val decorations: List<Decoration> = emptyList(),
            var text: String? = null,
            val emoji: String? = null,
            val image: String? = null,
            val isEmpty: Boolean = false
        ) : LinkToObject(), Searchable {
            override fun getViewType() = HOLDER_OBJECT_LINK_ARCHIVE
        }

        data class Deleted(
            override val id: String,
            override val indent: Int = 0,
            override val isSelected: Boolean = false,
            override val decorations: List<Decoration> = emptyList()
        ) : LinkToObject() {
            override fun getViewType() = HOLDER_OBJECT_LINK_DELETED
        }

        data class Loading(
            override val id: String,
            override val indent: Int = 0,
            override val isSelected: Boolean = false,
            override val decorations: List<Decoration> = emptyList()
        ) : LinkToObject() {
            override fun getViewType() = HOLDER_OBJECT_LINK_LOADING
        }
    }

    /**
     * UI-model for a line divider block.
     * @property id block's id
     */
    data class DividerLine(
        override val id: String,
        override val isSelected: Boolean = false,
        override val indent: Int = 0,
        override val decorations: List<Decoration> = emptyList(),
        val background: ThemeColor = ThemeColor.DEFAULT
    ) : BlockView(), Selectable, Indentable, Decoratable {
        override fun getViewType() = HOLDER_DIVIDER_LINE
    }

    /**
     * UI-model for a dots divider block.
     * @property id block's id
     */
    data class DividerDots(
        override val id: String,
        override val isSelected: Boolean = false,
        override val indent: Int = 0,
        override val decorations: List<Decoration> = emptyList(),
        val background: ThemeColor = ThemeColor.DEFAULT
    ) : BlockView(), Selectable, Indentable, Decoratable {
        override fun getViewType() = HOLDER_DIVIDER_DOTS
    }

    data class FeaturedRelation(
        override val id: String,
        val relations: List<DocumentRelationView>,
        val allowChangingObjectType : Boolean = true
    ) : BlockView() {
        override fun getViewType(): Int = HOLDER_FEATURED_RELATION
    }

    sealed class Relation : BlockView(), Selectable, Indentable, Decoratable {

        abstract val background: ThemeColor

        data class Placeholder(
            override val id: String,
            override val indent: Int = 0,
            override val isSelected: Boolean = false,
            override val decorations: List<Decoration>,
            override val background: ThemeColor = ThemeColor.DEFAULT
        ) : Relation() {
            override fun getViewType(): Int = HOLDER_RELATION_PLACEHOLDER
        }

        data class Related(
            override val id: String,
            override val indent: Int = 0,
            override val isSelected: Boolean = false,
            override val background: ThemeColor = ThemeColor.DEFAULT,
            val view: DocumentRelationView,
            override val decorations: List<Decoration>
        ) : Relation() {
            override fun getViewType(): Int = when (view) {
                is DocumentRelationView.Default -> HOLDER_RELATION_DEFAULT
                is DocumentRelationView.Checkbox -> HOLDER_RELATION_CHECKBOX
                is DocumentRelationView.Status -> HOLDER_RELATION_STATUS
                is DocumentRelationView.Tags -> HOLDER_RELATION_TAGS
                is DocumentRelationView.Object -> HOLDER_RELATION_OBJECT
                is DocumentRelationView.File -> HOLDER_RELATION_FILE
                is DocumentRelationView.ObjectType -> HOLDER_OBJECT_TYPE
                is DocumentRelationView.Source -> HOLDER_RELATION_SOURCE
            }
        }
    }

    data class Unsupported(
        override val id: String,
        override val indent: Int,
        override val isSelected: Boolean = false
    ) : BlockView(), Indentable, Selectable {
        override fun getViewType(): Int = HOLDER_UNSUPPORTED
    }

    data class Latex(
        override val id: String,
        override val indent: Int,
        override val isSelected: Boolean,
        val latex: String,
        val background: ThemeColor = ThemeColor.DEFAULT
    ) : BlockView(), Indentable, Selectable {
        override fun getViewType(): Int = HOLDER_LATEX
    }

    data class TableOfContents(
        override val id: String,
        override val isSelected: Boolean,
        val items: List<TableOfContentsItem>,
        val background: ThemeColor = ThemeColor.DEFAULT
    ) : BlockView(), Selectable {
        override fun getViewType(): Int = HOLDER_TOC
    }

    data class TableOfContentsItem(
        val id: Id,
        val name: String,
        val depth: Int
    )

    enum class Mode { READ, EDIT }

    data class Table(
        override val id: String,
        override val isSelected: Boolean,
        val background: ThemeColor = ThemeColor.DEFAULT,
        val columns: List<Column>,
        val cells: List<Cell>,
        val rowCount: Int
    ) : BlockView(), Selectable {
        override fun getViewType(): Int = HOLDER_TABLE

        data class Column(val id: String, val background: ThemeColor)

        sealed interface Cell {

            data class Text(
                val rowId: Id,
                val columnId: Id,
                val settings: CellSettings = CellSettings.empty(),
                val block: BlockView.Text.Paragraph
            ) : Cell {
                fun getId() = "$rowId-$columnId"
            }

            data class Empty(
                val rowId: Id,
                val columnId: Id,
                val settings: CellSettings = CellSettings.empty(),
            ) : Cell {
                fun getId() = "$rowId-$columnId"
            }
        }

        data class CellSettings(
            val width: Int = 0,
            val isHeader: Boolean = false,
            val top: Boolean = false,
            val left: Boolean = false,
            val right: Boolean = false,
            val bottom: Boolean = false
        ) {
            fun applyAllBorders() = this.copy(left = true, top = true, right = true, bottom = true)
            fun removeAllBorders() =
                this.copy(left = false, top = false, right = false, bottom = false)

            fun isAllBordersApply() = left && top && right && bottom

            companion object {
                fun empty() = CellSettings()
            }
        }
    }
}