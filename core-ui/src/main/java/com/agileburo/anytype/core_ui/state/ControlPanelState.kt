package com.agileburo.anytype.core_ui.state

import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.styling.StylingMode
import com.agileburo.anytype.core_ui.features.page.styling.StylingType
import com.agileburo.anytype.core_ui.widgets.toolbar.adapter.Mention

/**
 * Control panels are UI-elements that allow user to interact with blocks on a page.
 * Each panel is currently represented as a toolbar.
 * @property focus block currently associated with the control panel (if not present, control panel is not active)
 * @property mainToolbar block-toolbar state (main toolbar state)
 * @property stylingToolbar styling toolbar state
 */
data class ControlPanelState(
    val focus: Focus? = null,
    val mainToolbar: Toolbar.Main,
    val stylingToolbar: Toolbar.Styling,
    val multiSelect: Toolbar.MultiSelect,
    val mentionToolbar: Toolbar.MentionToolbar
) {

    sealed class Toolbar {

        /**
         * General property that defines whether this toolbar is visible or not.
         */
        abstract val isVisible: Boolean

        /**
         * Main toolbar allowing user-interface for CRUD-operations on block/page content.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class Main(
            override val isVisible: Boolean
        ) : Toolbar()

        /**
         * Basic color toolbar state.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class Styling(
            val target: Target? = null,
            val props: Props? = null,
            override val isVisible: Boolean,
            val mode: StylingMode?,
            val type: StylingType?
        ) : Toolbar() {

            /**
             * Target's properties corresponding to current selection or styling mode.
             */
            data class Props(
                val isBold: Boolean,
                val isItalic: Boolean,
                val isStrikethrough: Boolean,
                val isCode: Boolean,
                val isLinked: Boolean,
                val color: String?,
                val background: String?,
                val alignment: Alignment?
            )

            /**
             * Target block associated with this toolbar.
             */
            data class Target(
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
        ) : Toolbar()
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
            mainToolbar = Toolbar.Main(
                isVisible = false
            ),
            multiSelect = Toolbar.MultiSelect(
                isVisible = false,
                count = 0
            ),
            stylingToolbar = Toolbar.Styling(
                isVisible = false,
                type = null,
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
            focus = null
        )
    }
}