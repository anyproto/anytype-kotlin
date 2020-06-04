package com.agileburo.anytype.domain.editor

import com.agileburo.anytype.domain.common.Id

interface Editor {
    /**
     * @property id id of the focused block
     * @property range selection or cursor (when end == start value)
     */
    data class Focus(
        val id: String,
        val cursor: Cursor?
    ) : Editor {

        val isEmpty : Boolean
            get() = id == ""

        companion object {
            fun empty() = Focus("", null)
            fun id(id: Id) = Focus(id, null)
        }
    }

    sealed class Cursor {
        object Start : Cursor()
        object End: Cursor()
        data class Range(val range: IntRange) : Cursor()
    }
}