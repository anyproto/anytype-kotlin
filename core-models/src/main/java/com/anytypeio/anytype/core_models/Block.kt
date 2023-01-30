package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.Block.Content.Text.Mark
import com.anytypeio.anytype.core_models.Block.Content.Text.Style
import com.anytypeio.anytype.core_models.ext.typeOf

/**
 * Represents block as basic data structure.
 * @property id block's id
 * @property children block's children ids
 * @property fields block's fields
 * @property content block's content
 * @property backgroundColor background color for the whole block
 */
data class Block(
    val id: String,
    val children: List<String>,
    val content: Content,
    val fields: Fields,
    val backgroundColor: String? = null
) {

    /**
     * Block fields containing useful block properties.
     * @property map map containing fields
     */
    data class Fields(val map: Map<String, Any?>) {

        private val default = map.withDefault { null }

        val featuredRelations: List<String>? by default
        val name: String? by default
        val iconEmoji: String? by default
        val coverId: String? by default
        val coverType: Double? by default
        val iconImage: String? by default
        val isArchived: Boolean? by default
        val isLocked: Boolean? by default
        val isDeleted: Boolean? by default
        val isFavorite: Boolean? by default
        val done: Boolean? by default
        val lang: String? by default
        val fileExt: String? by default
        val fileMimeType: String? by default
        val type: List<String>
            get() = when (val value = map[TYPE_KEY]) {
                is String -> listOf(value)
                is List<*> -> value.typeOf()
                else -> emptyList()
            }

        val id: Id? by default
        val isDraft: Boolean? by default
        val snippet: String? by default

        val layout: Double?
            get() = when (val value = map[Relations.LAYOUT]) {
                is Double -> value
                is Int -> value.toDouble()
                else -> null
            }


        val analyticsContext: String? by default

        companion object {
            fun empty(): Fields = Fields(emptyMap())
            const val NAME_KEY = "name"
            const val TYPE_KEY = "type"
            const val IS_LOCKED_KEY = "isLocked"
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
        data class Smart(val type: SmartBlockType = SmartBlockType.PAGE) : Content()

        /**
         * Textual block.
         * @property text content text
         * @property marks markup related to [text],
         * @property isChecked whether this block is checked or not (see [Style.CHECKBOX])
         * @property color text color, which should be applied to the whole block (as opposed to [Mark.Type.TEXT_COLOR])
         * @property iconEmoji used for [Style.CALLOUT] could be empty
         * @property iconImage used for [Style.CALLOUT] could be empty
         */
        data class Text(
            val text: String,
            val style: Style,
            val marks: List<Mark>,
            val isChecked: Boolean? = null,
            val color: String? = null,
            val align: Align? = null,
            val iconEmoji: String? = null,
            val iconImage: String? = null,
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

            fun isHeader(): Boolean {
                return style == Style.H1 || style == Style.H2 || style == Style.H3 || style == Style.H4
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
                    UNDERLINE,
                    LINK,
                    TEXT_COLOR,
                    BACKGROUND_COLOR,
                    MENTION,
                    EMOJI,
                    OBJECT
                }

                fun isClickableMark(): Boolean =
                    type == Type.LINK || type == Type.MENTION || type == Type.OBJECT
            }

            /**
             * Style H4 is depricated
             */
            enum class Style {
                P, H1, H2, H3, H4, TITLE, QUOTE, CODE_SNIPPET, BULLET, NUMBERED, TOGGLE, CHECKBOX, DESCRIPTION, CALLOUT
            }
        }

        data class Layout(val type: Type) : Content() {
            enum class Type { ROW, COLUMN, DIV, HEADER, TABLE_ROW, TABLE_COLUMN }
        }

        @Deprecated("Legacy class")
        data class Page(val style: Style) : Content() {
            enum class Style { EMPTY, TASK, SET }
        }

        /**
         * A link to some other block.
         * @property target id of the target block
         * @property type type of the link
         * @property fields fields storing additional properties
         */
        //
        data class Link(
            val target: Id,
            val type: Type,
            val iconSize: IconSize,
            val cardStyle: CardStyle,
            val description: Description,
            val relations: Set<Relation>,
        ) : Content() {
            sealed interface Relation {
                object COVER : Relation
                object NAME : Relation
                object TYPE : Relation
                data class UNKNOWN(val value: String) : Relation
            }

            enum class Type { PAGE, DATA_VIEW, DASHBOARD, ARCHIVE }
            enum class IconSize { NONE, SMALL, MEDIUM }
            enum class CardStyle { TEXT, CARD, INLINE }
            enum class Description { NONE, ADDED, CONTENT }

            val hasName: Boolean
                get() = relations.contains(Relation.NAME)

            val hasCover: Boolean
                get() = relations.contains(Relation.COVER)

            val hasType: Boolean
                get() = relations.contains(Relation.TYPE)
        }

        /**
         * Page icon.
         * @property name conventional emoji short name.
         */
        @Deprecated("To be deleted")
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
            enum class Type { NONE, FILE, IMAGE, VIDEO, AUDIO, PDF }
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
            @Deprecated("Deprecated. Will be deleted in favor of values from target object's relations.")
            val url: Url?,
            @Deprecated("Deprecated. Will be deleted in favor of values from target object's relations.")
            val title: String?,
            @Deprecated("Deprecated. Will be deleted in favor of values from target object's relations.")
            val description: String?,
            @Deprecated("Deprecated. Will be deleted in favor of values from target object's relations.")
            val image: Hash?,
            @Deprecated("Deprecated. Will be deleted in favor of values from target object's relations.")
            val favicon: Hash?,
            val targetObjectId: Id?,
            val state: State
        ) : Content() {
            enum class State { EMPTY, FETCHING, DONE, ERROR }
        }

        data class Divider(val style: Style) : Content() {
            enum class Style { LINE, DOTS }
        }

        object FeaturedRelations : Content()

        data class RelationBlock(val key: Id?) : Content()

        data class DataView(
            val viewers: List<Viewer>,
            @Deprecated("To be deleted")
            val relations: List<Relation>,
            val relationsIndex: List<RelationLink> = emptyList(),
            val targetObjectId: Id = "",
        ) : Content() {

            data class Viewer(
                val id: String,
                val name: String,
                val type: Type,
                val sorts: List<Sort>,
                val filters: List<Filter>,
                val viewerRelations: List<ViewerRelation>,
                val cardSize: Size = Size.SMALL,
                val hideIcon: Boolean = false,
                val coverFit: Boolean = false,
                val coverRelationKey: String? = null
            ) {

                enum class Type { GRID, LIST, GALLERY, BOARD }

                enum class Size { SMALL, MEDIUM, LARGE }

                //relations fields/columns options, also used to provide the order
                data class ViewerRelation(
                    val key: String,
                    val isVisible: Boolean,
                    val width: Int? = null,
                    val dateFormat: DateFormat? = null,
                    val timeFormat: TimeFormat? = null,
                    val isDateIncludeTime: Boolean? = null
                )
            }

            enum class DateFormat(val format: String) {
                MONTH_ABBR_BEFORE_DAY("MMM dd, yyyy"),  // Jul 30, 2020
                MONTH_ABBR_AFTER_DAY("dd MMM yyyy"),    // 30 Jul 2020
                SHORT("dd/MM/yyyy"),                    // 30/07/2020
                SHORTUS("MM/dd/yyyy"),                  // 07/30/2020
                ISO("yyyy-MM-dd")                       // 2020-07-30
            }

            enum class TimeFormat { H12, H24 }

            data class Sort(
                val id: Id = "",
                val relationKey: String,
                val type: Type
            ) {
                enum class Type { ASC, DESC, CUSTOM }
            }

            /**
             * [relationFormat] optional relation format, which should be specified for date-related filtering.
             */
            data class Filter(
                val id: Id = "",
                val relation: Key,
                val relationFormat: RelationFormat? = null,
                val operator: Operator = Operator.AND,
                val condition: Condition,
                val quickOption: QuickOption = QuickOption.EXACT_DATE,
                val value: Any? = null
            ) {
                enum class Operator { AND, OR }
                enum class Condition {
                    EQUAL, NOT_EQUAL, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL,
                    LIKE, NOT_LIKE, IN, NOT_IN, EMPTY, NOT_EMPTY, ALL_IN, NOT_ALL_IN, NONE,
                    EXACT_IN, NOT_EXACT_IN
                }

                enum class QuickOption {
                    EXACT_DATE, YESTERDAY, TODAY, TOMORROW, LAST_WEEK, CURRENT_WEEK, NEXT_WEEK,
                    LAST_MONTH, CURRENT_MONTH, NEXT_MONTH, DAYS_AGO, DAYS_AHEAD,
                }
            }
        }

        data class Latex(val latex: String) : Content()
        object TableOfContents : Content()
        object Unsupported : Content()

        object Table : Content()
        data class TableRow(val isHeader: Boolean) : Content()
        object TableColumn : Content()

        data class Widget(
            val layout: Layout
        ) : Content() {
            enum class Layout {
                TREE, LINK
            }
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
            val target: Id,
            val cardStyle: Content.Link.CardStyle = Content.Link.CardStyle.TEXT,
            val iconSize: Content.Link.IconSize = Content.Link.IconSize.SMALL,
            val description: Content.Link.Description = Content.Link.Description.NONE
        ) : Prototype()

        object DividerLine : Prototype()
        object DividerDots : Prototype()
        sealed class Bookmark : Prototype() {
            /**
             * Creates placeholder block for bookmark
             */
            object New : Bookmark()
            /**
             * Creates bookmark block from an existing bookmark object
             * @property [target] bookmark object id
             */
            data class Existing(val target: Id) : Bookmark()
        }
        object Latex : Prototype()
        data class Relation(
            val key: Id
        ) : Prototype()

        object TableOfContents : Prototype()
        object SimpleTable : Prototype()
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