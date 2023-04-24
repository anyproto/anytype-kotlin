package com.anytypeio.anytype.domain.editor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.editor.Editor.Focus.Companion.id

interface Editor {

    /**
     * @property id id of the focused block
     * @property cursor optional cursor/carriage associated with this focus
     * @property isEmpty defines whether focus has target or not
     * @property isPending focus is pending if we do not know whether the target widget has gained focus.
     */
    data class Focus(
        var target: Target,
        val cursor: Cursor?,
        val isPending: Boolean = true
    ) : Editor {

        val isEmpty: Boolean get() = target is Target.None

        fun isTarget(block: Id) : Boolean {
            val cur = target
            return (cur is Target.Block && cur.id == block)
        }
        fun requireTarget() : Id = (target as Target.Block).id
        fun targetOrNull() : Id? = (target as? Target.Block)?.id

        companion object {
            fun empty() = Focus(
                target = Target.None,
                cursor = null,
                isPending = false
            )
            fun id(id: Id, isPending: Boolean = true) = Focus(
                target = Target.Block(id),
                cursor = null,
                isPending = isPending
            )
            const val EMPTY_FOCUS = ""
        }

        sealed class Target {
            object None: Target()
            object FirstTextBlock : Target()
            data class Block(val id: Id) : Target()
        }
    }

    /**
     * @property id id of the focused block, empty if no block in focus
     * @property selection start and end of selection, could be null
     */
    data class TextSelection(
        val id: String,
        val selection: IntRange?
    ) : Editor {
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