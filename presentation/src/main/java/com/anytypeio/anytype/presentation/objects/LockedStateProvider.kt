package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.Editor

interface LockedStateProvider {
    fun isLocked(ctx: Id) : Boolean
    class EditorLockedStateProvider(
        private val storage: Editor.Storage
    ) : LockedStateProvider {
        override fun isLocked(ctx: Id): Boolean {
            val doc = storage.document.get().find { it.id == ctx }
            return doc?.fields?.isLocked ?: false
        }
    }
    object DataViewLockedStateProvider : LockedStateProvider {
        // Sets or Collections can't be locked currently.
        override fun isLocked(ctx: Id): Boolean = false
    }
}