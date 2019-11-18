package com.agileburo.anytype.core_ui.features.page

/**
 * UI-models for different types of blocks.
 */
sealed class BlockView {

    /**
     * Each block view has a corresponding block id.
     */
    abstract val id: String

    /**
     * UI-model for a basic text block.
     * @property id block's id
     * @property text block's content text
     */
    data class Text(
        override val id: String,
        val text: String
    ) : BlockView()

    /**
     * UI-model for a title block.
     * @property id block's id
     * @property text text content (i.e. title text)
     */
    data class Title(
        override val id: String,
        val text: String
    ) : BlockView()

    /**
     * UI-model for a header block.
     * @property id block's id
     * @property text header's content (i.e. a header's text)
     */
    data class HeaderOne(
        override val id: String,
        val text: String
    ) : BlockView()

    /**
     * UI-model for a header block.
     * @property id block's id
     * @property text header's content (i.e. a header's text)
     */
    data class HeaderTwo(
        override val id: String,
        val text: String
    ) : BlockView()

    /**
     * UI-model for a header block.
     * @property id block's id
     * @property text header's content (i.e. a header's text)
     */
    data class HeaderThree(
        override val id: String,
        val text: String
    ) : BlockView()

    /**
     * UI-model for a code-snippet block.
     * @property id block's id
     * @property snippet blocks's content (i.e. code snippet)
     */
    data class Code(
        override val id: String,
        val snippet: String
    ) : BlockView()

    /**
     * UI-model for checkbox blocks.
     * @property id block's id
     * @property text checkbox's content text
     * @property checked immutable checkbox state (whether this checkbox is checked or not)
     */
    data class Checkbox(
        override val id: String,
        val text: String,
        val checked: Boolean = false
    ) : BlockView()

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
    ) : BlockView()

    /**
     * UI-model for items of a bulleted list.
     * @property id block's id
     * @property text bullet list item content
     * @property indent indentation value
     */
    data class Bulleted(
        override val id: String,
        val text: String,
        val indent: Int
    ) : BlockView()

    /**
     * UI-model for items of a numbered list.
     * @property id block's id
     * @property text numbered list item content
     * @property number number value
     * @property indent indentation value
     */
    data class Numbered(
        override val id: String,
        val text: String,
        val number: String,
        val indent: Int
    ) : BlockView()

    /**
     * UI-model for a toggle block.
     * @property id block's id
     * @property text toggle block's content
     * @property indent indentation value
     * @property toggled toggle state (whether this toggle is expanded or not)
     */
    data class Toggle(
        override val id: String,
        val text: String,
        val indent: Int,
        val toggled: Boolean = false
    ) : BlockView()

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
    ) : BlockView()

    /**
     * UI-model for blocks containing files.
     * @property id block's id
     * @property size a file's size
     * @property filename a filename
     */
    data class File(
        override val id: String,
        val size: String,
        val filename: String
    ) : BlockView()

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
        val text: String,
        val emoji: String?,
        val isEmpty: Boolean = false,
        val isArchived: Boolean = false
    ) : BlockView()

    /**
     * UI-model for a divider block.
     * @property id block's id
     */
    data class Divider(
        override val id: String
    ) : BlockView()

    /**
     * UI-model for a bookmark block
     * @property id block's id
     * @property title website's title
     * @property title website's content description
     * @property url website's url
     * @property logoUrl website's logo url
     * @property logoUrl content's main image url
     */
    data class Bookmark(
        override val id: String,
        val title: String,
        val description: String,
        val url: String,
        val logoUrl: String,
        val imageUrl: String
    ) : BlockView()

    /**
     * UI-model for a picture block
     * @property id block's id
     */
    data class Picture(
        override val id: String
    ) : BlockView()
}