package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

/**
 * Reactive store
 * @param T stored type
 */
interface Store<T> {

    /**
     * @return streams of values
     */
    fun stream(): Flow<T>

    /**
     * @return current/last value
     */
    fun current(): T

    /**
     * Updates current values
     */
    suspend fun update(t: T)

    fun cancel()

    open class State<T>(initial: T) : Store<T> {

        private val state = MutableStateFlow(initial)

        override fun stream(): Flow<T> = state
        override fun current(): T = state.value
        override suspend fun update(t: T) {
            state.value = t
        }

        override fun cancel() {}
    }

    class Focus : State<Editor.Focus>(Editor.Focus.empty()) {
        override suspend fun update(t: Editor.Focus) {
            Timber.d("Update focus in store: $t")
            super.update(t)
        }
    }

    class Screen : State<List<BlockView>>(emptyList())

    class Details : State<Block.Details>(Block.Details()) {
        suspend fun add(target: Id, fields: Block.Fields) {
            update(current().copy(details = current().details + mapOf(target to fields)))
        }
    }

    class Relations : State<List<Relation>>(emptyList())
    class ObjectRestrictions : State<List<ObjectRestriction>>(emptyList())
    class TextSelection : State<Editor.TextSelection>(Editor.TextSelection.empty())
    class RelationLinks : State<List<RelationLink>>(emptyList())
}