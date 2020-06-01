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
    data class Fields(val map: MutableMap<String?, Any?> = mutableMapOf())
    data class Details(val details: Map<String, Fields>)

    sealed class Content {

        data class Smart(
            val type: Type
        ) : Content() {
            enum class Type { HOME, PAGE, ARCHIVE, BREADCRUMBS, PROFILE }
        }

        data class Text(
            val text: String,
            val style: Style,
            val marks: List<Mark>,
            val isChecked: Boolean? = null,
            val color: String? = null,
            val backgroundColor: String? = null,
            val align: Align? = null
        ) : Content() {

            data class Mark(
                val range: IntRange,
                val type: Type,
                val param: String?
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
            enum class Type { ROW, COLUMN, DIV }
        }

        data class Icon(
            val name: String
        ) : Content()

        data class Page(val style: Style) : Content() {
            enum class Style { EMPTY, TASK, SET }
        }

        data class Link(
            val target: String,
            val type: Type,
            val fields: Fields
        ) : Content() {
            enum class Type { PAGE, DATA_VIEW, DASHBOARD, ARCHIVE }
        }

        data class File(
            val hash: String? = null,
            val name: String? = null,
            val type: Type? = null,
            val mime: String? = null,
            val size: Long? = null,
            val state: State? = null
        ) : Content() {
            enum class Type { NONE, FILE, IMAGE, VIDEO }
            enum class State { EMPTY, UPLOADING, DONE, ERROR }
        }

        data class Bookmark(
            val url: String?,
            val title: String?,
            val description: String?,
            val image: String?,
            val favicon: String?
        ) : Content()

        object Divider : Content()
    }

    sealed class Prototype {
        class Text(
            val style: Content.Text.Style
        ) : Prototype()

        data class Page(
            val style: Content.Page.Style
        ) : Prototype()

        data class File(
            val state: Content.File.State,
            val type: Content.File.Type
        ) : Prototype()

        object Divider : Prototype()
        object Bookmark : Prototype()
    }

    sealed class Align {
        object AlignLeft : Align()
        object AlignCenter : Align()
        object AlignRight : Align()
    }
}