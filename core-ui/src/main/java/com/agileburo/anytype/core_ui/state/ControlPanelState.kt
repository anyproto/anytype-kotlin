package com.agileburo.anytype.core_ui.state

/**
 * Control panels are UI-elements that allow user to interact with blocks on a page.
 * Each panel is currently represented as a toolbar.
 * @property focus block currently associated with the control panel (if not present, control panel is not active)
 * @property blockToolbar block-toolbar state (main toolbar state)
 * @property markupToolbar markup toolbar state
 * @property colorToolbar color toolbar state
 * @property addBlockToolbar add-block toolbar state
 * @property actionToolbar action-toolbar state
 */
data class ControlPanelState(
    val focus: Focus? = null,
    val blockToolbar: Toolbar.Block,
    val markupToolbar: Toolbar.Markup,
    val colorToolbar: Toolbar.Color,
    val addBlockToolbar: Toolbar.AddBlock,
    val actionToolbar: Toolbar.BlockAction
) {

    fun isNotVisible(): Boolean = !blockToolbar.isVisible && !markupToolbar.isVisible

    sealed class Toolbar {

        /**
         * General property that defines whether this toolbar is visible or not.
         */
        abstract val isVisible: Boolean

        /**
         * Main toolbar allowing user-interface for CRUD-operations on block/page content.
         * @property selectedAction currently selected [Action], null if nothing is selected.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class Block(
            override val isVisible: Boolean,
            val selectedAction: Action? = null
        ) : Toolbar() {
            /**
             * Set of actions defined for this toolbar.
             */
            enum class Action { ADD, TURN_INTO, COLOR, BLOCK_ACTION }
        }

        /**
         * Basic markup toolbar state.
         * @property isVisible defines whether the toolbar is visible or not
         * @property selectedAction currently selected [Action], null if nothing is selected.
         */
        data class Markup(
            override val isVisible: Boolean,
            val selectedAction: Action? = null
        ) : Toolbar() {
            enum class Action { COLOR }
        }

        /**
         * Basic color toolbar state.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class Color(
            override val isVisible: Boolean
        ) : Toolbar()

        /**
         * Basic add-block toolbar state.
         * @property isVisible defines whether the toolbar is visible or not
         */
        data class AddBlock(
            override val isVisible: Boolean
        ) : Toolbar()

        /**
         * Basic action (delete, duplicate, undo, redo, etc.) toolbar state
         */
        data class BlockAction(
            override val isVisible: Boolean
        ) : Toolbar()

        /**
         * TODO
         */
        data class TurnInto(
            val isVisible: Boolean
        )
    }

    /**
     * Block currently associated with this panel.
     * @property id id of the focused block
     */
    data class Focus(val id: String)

    companion object {

        /**
         * Factory function for creating initial state.
         */
        fun init(): ControlPanelState = ControlPanelState(
            blockToolbar = Toolbar.Block(
                isVisible = false,
                selectedAction = null
            ),
            markupToolbar = Toolbar.Markup(
                isVisible = false,
                selectedAction = null
            ),
            colorToolbar = Toolbar.Color(
                isVisible = false
            ),
            addBlockToolbar = Toolbar.AddBlock(
                isVisible = false
            ),
            actionToolbar = Toolbar.BlockAction(
                isVisible = false
            ),
            focus = null
        )
    }
}