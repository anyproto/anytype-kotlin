package com.agileburo.anytype.core_ui.state

/**
 * Control panels are UI-elements that allow user to interact with blocks on a page.
 * Each panel is currently represented as a toolbar.
 * @property blockToolbar block-toolbar state (main toolbar state)
 * @property markupToolbar markup toolbar state
 * @property colorToolbar color toolbar state
 * @property addBlockToolbar add-block toolbar state
 */
data class ControlPanelState(
    val blockToolbar: Toolbar.Block,
    val markupToolbar: Toolbar.Markup,
    val colorToolbar: Toolbar.Color,
    val addBlockToolbar: Toolbar.AddBlock
) {
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
         * TODO
         */
        data class TurnInto(
            val isVisible: Boolean
        )

        /**
         * TODO
         */
        data class BlockAction(
            val isVisible: Boolean
        )
    }

    companion object {

        /**
         * Factory function for creating initial state.
         */
        fun init(): ControlPanelState = ControlPanelState(
            blockToolbar = Toolbar.Block(
                isVisible = true,
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
            )
        )
    }
}