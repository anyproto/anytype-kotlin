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

        companion object {
            fun empty(): Fields = Fields(emptyMap())
        }
    }

    sealed class Content {

        data class Text(
            val text: String,
            val style: Style,
            val marks: List<Mark>
        ) : Content() {

            data class Mark(
                val range: IntRange,
                val type: Type,
                val param: Any?
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
                P, H1, H2, H3, H4, TITLE, QUOTE, CODE_SNIPPET, BULLET, NUMBERED, TOGGLE
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
}