package com.anytypeio.anytype.presentation.page.editor.control

import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.mention.Mention
import com.anytypeio.anytype.presentation.page.editor.model.Alignment
import com.anytypeio.anytype.presentation.page.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.page.editor.styling.StyleConfig
import com.anytypeio.anytype.presentation.page.editor.styling.StylingMode
import com.anytypeio.anytype.presentation.page.markup.MarkupStyleDescriptor

/**
 * Control panels are UI-elements that allow user to interact with blocks on a page.
 * Each panel is currently represented as a toolbar.
 * @property mainToolbar block-toolbar state (main toolbar state)
 * @property stylingToolbar styling toolbar state
 */
data class ControlPanelState(
    val navigationToolbar: Toolbar.Navigation,
    val mainToolbar: Toolbar.Main,
    val stylingToolbar: Toolbar.Styling,
    val styleExtraToolbar: Toolbar.Styling.Other = Toolbar.Styling.Other(),
    val styleColorToolbar: Toolbar.Styling.Color = Toolbar.Styling.Color(),
    val markupMainToolbar: Toolbar.MarkupMainToolbar = Toolbar.MarkupMainToolbar.reset(),
    val markupUrlToolbar: Toolbar.MarkupUrlToolbar = Toolbar.MarkupUrlToolbar(),
    val markupColorToolbar: Toolbar.MarkupColorToolbar = Toolbar.MarkupColorToolbar(),
    val multiSelect: Toolbar.MultiSelect,
    val mentionToolbar: Toolbar.MentionToolbar,
    val slashWidget: Toolbar.SlashWidget,
    val searchToolbar: Toolbar.SearchToolbar = Toolbar.SearchToolbar(isVisible = false)
) {

    sealed class Toolbar {

        /**
         * General property that defines whether this toolbar is visible or not.
         */
        abstract val isVisible: Boolean

        /**
         * Navigation bottom toolbar allowing user open Search, Naviagation Screens
         * or add new Document on Dashboard
         *
         * @property isVisible
         */
        data class Navigation(
            override val isVisible: Boolean
        ) : Toolbar()

        /**
         * Main toolbar allowing user-interface for CRUD-operations on block/page content.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class Main(
            override val isVisible: Boolean
        ) : Toolbar()

        /**
         * Main toolbar allowing user-interface for markup operations.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class MarkupMainToolbar(
            override val isVisible: Boolean,
            val style: MarkupStyleDescriptor? = null,
            val supportedTypes: List<Markup.Type> = emptyList(),
            val isTextColorSelected: Boolean = false,
            val isBackgroundColorSelected: Boolean = false
        ) : Toolbar() {
            companion object {
                fun reset(): MarkupMainToolbar = MarkupMainToolbar(
                    isVisible = false,
                    style = null,
                    isTextColorSelected = false,
                    isBackgroundColorSelected = false
                )
            }
        }

        /**
         * Main toolbar allowing user-interface for markup operations.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class MarkupColorToolbar(
            override val isVisible: Boolean = false
        ) : Toolbar()

        /**
         * TODO Markup toolbar allowing user-interface for markup operations.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class MarkupUrlToolbar(
            override val isVisible: Boolean = false,
            val url: String? = null
        ) : Toolbar() {
            companion object {
                fun reset() = MarkupUrlToolbar()
            }
        }

        /**
         * Basic color toolbar state.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class Styling(
            override val isVisible: Boolean,
            val target: Target? = null,
            val config: StyleConfig? = null,
            val props: Props? = null,
            val mode: StylingMode? = null,
            val style: TextStyle? = TextStyle.P
        ) : Toolbar() {

            companion object {
                fun reset() = Styling(
                    isVisible = false,
                    target = null,
                    config = null,
                    props = null,
                    mode = null
                )
            }

            data class Other(override val isVisible: Boolean = false) : Toolbar()
            data class Color(override val isVisible: Boolean = false) : Toolbar()

            /**
             * Target's properties corresponding to current selection or styling mode.
             */
            data class Props(
                val isBold: Boolean = false,
                val isItalic: Boolean = false,
                val isStrikethrough: Boolean = false,
                val isCode: Boolean = false,
                val isLinked: Boolean = false,
                val color: String? = null,
                val background: String? = null,
                val alignment: Alignment? = null
            )

            /**
             * Target block associated with this toolbar.
             */
            data class Target(
                val id: String,
                val text: String,
                val color: String?,
                val background: String?,
                val alignment: Alignment?,
                val marks: List<Markup.Mark>
            ) {

                val isBold: Boolean = marks.any { mark ->
                    mark.type == Markup.Type.BOLD && mark.from == 0 && mark.to == text.length
                }

                val isItalic: Boolean = marks.any { mark ->
                    mark.type == Markup.Type.ITALIC && mark.from == 0 && mark.to == text.length
                }

                val isStrikethrough: Boolean = marks.any { mark ->
                    mark.type == Markup.Type.STRIKETHROUGH && mark.from == 0 && mark.to == text.length
                }

                val isCode: Boolean = marks.any { mark ->
                    mark.type == Markup.Type.KEYBOARD && mark.from == 0 && mark.to == text.length
                }

                val isLinked: Boolean = marks.any { mark ->
                    mark.type == Markup.Type.LINK && mark.from == 0 && mark.to == text.length
                }
            }
        }

        /**
         * Basic multi select mode toolbar state.
         * @property isVisible defines whether we are in multi select mode or not
         * @property count number of selected blocks
         */
        data class MultiSelect(
            override val isVisible: Boolean,
            val isScrollAndMoveEnabled: Boolean = false,
            val isQuickScrollAndMoveMode: Boolean = false,
            val count: Int = 0
        ) : Toolbar()

        /**
         * Toolbar with list of mentions and add new page item.
         * @property isVisible defines whether the toolbar is visible or not
         * @property mentionFrom first position of the mentionFilter in text
         * @property mentionFilter sequence of symbol @ and characters, using for filtering mentions
         * @property cursorCoordinate y coordinate bottom of the cursor, using for define top border of the toolbar
         * @property mentions list of all mentions
         */
        data class MentionToolbar(
            override val isVisible: Boolean,
            val mentionFrom: Int?,
            val mentionFilter: String?,
            val cursorCoordinate: Int?,
            val updateList: Boolean = false,
            val mentions: List<Mention> = emptyList()
        ) : Toolbar() {
            companion object {
                fun reset(): MentionToolbar = MentionToolbar(
                    isVisible = false,
                    mentionFilter = null,
                    mentionFrom = null,
                    cursorCoordinate = null,
                    mentions = emptyList(),
                    updateList = false
                )
            }
        }

        /**
         * Search toolbar.
         */
        data class SearchToolbar(
            override val isVisible: Boolean
        ) : Toolbar()

        data class SlashWidget(
            override val isVisible: Boolean,
            val from: Int? = null,
            val filter: String? = null,
            val cursorCoordinate: Int? = null,
            val updateList: Boolean = false,
            val items: List<String> = emptyList(),
            val widgetState: SlashWidgetState? = null
        ) : Toolbar() {
            companion object {
                fun reset(): SlashWidget = SlashWidget(
                    isVisible = false,
                    filter = null,
                    from = null,
                    cursorCoordinate = null,
                    items = emptyList(),
                    updateList = false,
                    widgetState = null
                )
            }
        }
    }

    /**
     * Block currently associated with this panel.
     * @property id id of the focused block
     */
    data class Focus(
        val id: String,
        val type: Type
    ) {
        enum class Type {
            P, H1, H2, H3, H4, TITLE, QUOTE, CODE_SNIPPET, BULLET, NUMBERED, TOGGLE, CHECKBOX, BOOKMARK
        }
    }

    companion object {

        /**
         * Factory function for creating initial state.
         */
        fun init(): ControlPanelState = ControlPanelState(
            navigationToolbar = Toolbar.Navigation(
                isVisible = true
            ),
            mainToolbar = Toolbar.Main(
                isVisible = false
            ),
            markupMainToolbar = Toolbar.MarkupMainToolbar(
                isVisible = false
            ),
            markupUrlToolbar = Toolbar.MarkupUrlToolbar(),
            markupColorToolbar = Toolbar.MarkupColorToolbar(),
            multiSelect = Toolbar.MultiSelect(
                isVisible = false,
                count = 0
            ),
            stylingToolbar = Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            mentionToolbar = Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                updateList = false,
                mentionFrom = null,
                mentions = emptyList()
            ),
            searchToolbar = Toolbar.SearchToolbar(
                isVisible = false
            ),
            slashWidget = Toolbar.SlashWidget.reset()
        )
    }
}