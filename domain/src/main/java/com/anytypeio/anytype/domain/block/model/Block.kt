package com.anytypeio.anytype.domain.block.model

import com.anytypeio.anytype.domain.block.model.Block.Content.Text.Mark
import com.anytypeio.anytype.domain.block.model.Block.Content.Text.Style
import com.anytypeio.anytype.domain.common.Hash
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.common.Url

/**
 * Represents block as basic data structure.
 * @property id block's id
 * @property children block's children ids
 * @property fields block's fields
 * @property content block's content
 */
data class Block(
    val id: String,
    val children: List<String>,
    val content: Content,
    val fields: Fields
) {

    /**
     * Block fields containing useful block properties.
     * @property map map containing fields
     */
    data class Fields(val map: Map<String?, Any?>) {

        private val default = map.withDefault { null }

        val name: String? by default
        val iconEmoji: String? by default
        val iconImage: String? by default
        val isArchived: Boolean? by default

        companion object {
            fun empty(): Fields = Fields(emptyMap())
            const val NAME_KEY = "name"
            const val IS_ARCHIVED_KEY = "isArchived"
        }
    }

    /**
     * Document metadata
     * @property details maps id of the block to its details (contained as fields)
     */
    data class Details(val details: Map<Id, Fields> = emptyMap())

    /**
     * Block's content.
     */
    sealed class Content {

        fun asText() = this as Text
        fun asLink() = this as Link

        /**
         * Smart block.
         */
        data class Smart(
            val type: Type
        ) : Content() {
            enum class Type { HOME, PAGE, ARCHIVE, BREADCRUMBS, PROFILE }
        }

        /**
         * Textual block.
         * @property text content text
         * @property marks markup related to [text],
         * @property isChecked whether this block is checked or not (see [Style.CHECKBOX])
         * @property color text color, which should be applied to the whole block (as opposed to [Mark.Type.TEXT_COLOR])
         * @property backgroundColor background color for the whole block, as opposed to [Mark.Type.BACKGROUND_COLOR]
         */
        data class Text(
            val text: String,
            val style: Style,
            val marks: List<Mark>,
            val isChecked: Boolean? = null,
            val color: String? = null,
            val backgroundColor: String? = null,
            val align: Align? = null
        ) : Content() {

            /**
             * Toggles checked/unchecked state.
             * Does not modify this instance's checked/unchecked state (preserves immutability)
             * @return new checked/unchecked state without modifying
             */
            fun toggleCheck(): Boolean = isChecked == null || isChecked == false

            /**
             * @return true if this is a title block.
             */
            fun isTitle() = style == Style.TITLE

            /**
             * @return true if this is a toggle block.
             */
            fun isToggle() = style == Style.TOGGLE

            /**
             * @return true if this text block is a list item.
             */
            fun isList(): Boolean {
                return style == Style.BULLET || style == Style.CHECKBOX || style == Style.NUMBERED
            }

            /**
             * Mark as a part of markup.
             * @property type markup type
             * @property param optional parameter (i.e. text color, url, etc)
             * @property range text range for markup (start == start char index, end == end char index + 1).
             */
            data class Mark(
                val range: IntRange,
                val type: Type,
                val param: String? = null
            ) {
                enum class Type {
                    STRIKETHROUGH,
                    KEYBOARD,
                    ITALIC,
                    BOLD,
                    UNDERSCORED,
                    LINK,
                    TEXT_COLOR,
                    BACKGROUND_COLOR,
                    MENTION
                }
            }

            enum class Style {
                P, H1, H2, H3, H4, TITLE, QUOTE, CODE_SNIPPET, BULLET, NUMBERED, TOGGLE, CHECKBOX
            }
        }

        data class Layout(val type: Type) : Content() {
            enum class Type { ROW, COLUMN, DIV, HEADER }
        }

        data class Page(val style: Style) : Content() {
            enum class Style { EMPTY, TASK, SET }
        }

        /**
         * A link to some other block.
         * @property target id of the target block
         * @property type type of the link
         * @property fields fields storing additional properties
         */
        data class Link(
            val target: Id,
            val type: Type,
            val fields: Fields
        ) : Content() {
            enum class Type { PAGE, DATA_VIEW, DASHBOARD, ARCHIVE }
        }

        /**
         * Page icon.
         * @property name conventional emoji short name.
         */
        data class Icon(
            val name: String
        ) : Content()

        /**
         * File block.
         * @property hash file hash
         * @property name filename
         * @property mime mime type
         * @property size file size (in bytes)
         * @property type file type
         * @property state file state
         */
        data class File(
            val hash: String? = null,
            val name: String? = null,
            val mime: String? = null,
            val size: Long? = null,
            val type: Type? = null,
            val state: State? = null
        ) : Content() {
            enum class Type { NONE, FILE, IMAGE, VIDEO }
            enum class State { EMPTY, UPLOADING, DONE, ERROR }
        }

        /**
         * @property url url associated with this bookmark
         * @property title optional bookmark title
         * @property description optional bookmark's content description
         * @property image optional hash of bookmark's image
         * @property favicon optional hash of bookmark's favicon
         */
        data class Bookmark(
            val url: Url?,
            val title: String?,
            val description: String?,
            val image: Hash?,
            val favicon: Hash?
        ) : Content()

        data class Divider(val type: Type) : Content() {
            enum class Type { LINE, DOTS }
        }
    }

    /**
     * Block prototype used as a model or a blueprint for a block to create.
     */
    sealed class Prototype {
        /**
         * Prototype of the textual block.
         * @param style style for a block to create
         */
        data class Text(
            val style: Content.Text.Style
        ) : Prototype()

        data class Page(
            val style: Content.Page.Style
        ) : Prototype()

        data class File(
            val type: Content.File.Type,
            val state: Content.File.State
        ) : Prototype()

        data class Link(
            val target: Id
        ) : Prototype()

        object DividerLine : Prototype()
        object DividerDots : Prototype()
        object Bookmark : Prototype()
    }

    /**
     * Block alignment property
     */
    sealed class Align {
        object AlignLeft : Align()
        object AlignCenter : Align()
        object AlignRight : Align()
    }
}