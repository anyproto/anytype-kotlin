package com.anytypeio.anytype.domain.editor

import com.anytypeio.anytype.domain.common.Id

interface Editor {

    /**
     * @property id id of the focused block
     * @property cursor optional cursor/carriage associated with this focus
     * @property isEmpty defines whether focus has target or not
     */
    data class Focus(
        val id: String,
        val cursor: Cursor?
    ) : Editor {

        val isEmpty: Boolean get() = id == EMPTY_FOCUS

        companion object {
            fun empty() = Focus("", null)
            fun id(id: Id) = Focus(id, null)

            const val EMPTY_FOCUS = ""
        }
    }

    /**
     * @property id id of the focused block, empty if no block in focus
     * @property selection start and end of selection, could be null
     */
    data class TextSelection(
        val id: String,
        val selection: IntRange?
    ): Editor {
        val isNotEmpty: Boolean get() = id != EMPTY_FOCUS

        companion object {
            fun empty() = TextSelection(EMPTY_FOCUS, null)
            private const val EMPTY_FOCUS = ""
        }
    }

    sealed class Cursor {
        /**
         * Indicates that the cursor should be placed at the beginning of a text.
         */
        object Start : Cursor()

        /**
         * Indicates that the cursor should be placed at the end of a text.
         */
        object End : Cursor()

        /**
         * Indicates that the cursor should be placed at the concrete position of the text.
         * This position is defined by [range]
         */
        data class Range(val range: IntRange) : Cursor()
    }
}