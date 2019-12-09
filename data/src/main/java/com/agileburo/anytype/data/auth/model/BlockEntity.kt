package com.agileburo.anytype.data.auth.model

/**
 * Block's data layer representation.
 */
data class BlockEntity(
    val id: String,
    val children: List<String>,
    val content: Content,
    val fields: Fields
) {
    data class Fields(val map: MutableMap<String, Any?> = mutableMapOf())

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

        data class Dashboard(val type: Type) : Content() {
            enum class Type { MAIN_SCREEN, ARCHIVE }
        }

        data class Page(val style: Style) : Content() {
            enum class Style { EMPTY, TASK, SET }
        }
    }
}