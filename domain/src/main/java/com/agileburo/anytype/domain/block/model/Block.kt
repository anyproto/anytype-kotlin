package com.agileburo.anytype.domain.block.model

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
    data class Fields(val map: Map<String, Any?>) {

        val name: String by map
        val icon: String by map

        fun hasName() = map.containsKey(NAME_KEY)

        companion object {
            fun empty(): Fields = Fields(emptyMap())

            const val NAME_KEY = "name"
        }
    }

    sealed class Content {

        fun asText() = this as Text
        fun asDashboard() = this as Dashboard

        data class Text(
            val text: String,
            val style: Style,
            val marks: List<Mark>,
            val isChecked: Boolean? = null
        ) : Content() {

            /**
             * Toggles checked/unchecked state.
             * Does not modify this instance's checked/unchecked state (preserves immutability)
             * @return new checked/unchecked state without modifying
             */
            fun toggleCheck(): Boolean = isChecked == null || isChecked == false

            fun isTitle() = style == Style.TITLE

            data class Mark(
                val range: IntRange,
                val type: Type,
                val param: Any? = null
            ) {
                enum class Type {
                    STRIKETHROUGH,
                    KEYBOARD,
                    ITALIC,
                    BOLD,
                    UNDERSCORED,
                    LINK,
                    TEXT_COLOR,
                    BACKGROUND_COLOR
                }
            }

            enum class Style {
                P, H1, H2, H3, H4, TITLE, QUOTE, CODE_SNIPPET, BULLET, NUMBERED, TOGGLE, CHECKBOX
            }
        }

        data class Layout(val type: Type) : Content() {
            enum class Type { ROW, COLUMN }
        }

        data class Image(
            val path: String
        ) : Content()

        data class Dashboard(val type: Type) : Content() {
            enum class Type { MAIN_SCREEN, ARCHIVE }
        }

        data class Page(val style: Style) : Content() {
            enum class Style { EMPTY, TASK, SET }
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
    }
}