package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Document
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.editor.Editor.Focus
import com.anytypeio.anytype.presentation.editor.editor.Proxy
import com.anytypeio.anytype.presentation.editor.editor.Store
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface Editor {

    sealed class Mode {
        object Edit : Mode()
        object Select : Mode()
        object SAM : Mode()
        object Action: Mode()
        object Search : Mode()
        sealed class Styling : Mode() {
            data class Single(
                val target: Id,
                val cursor: Int?
            ) : Styling()
            data class Multi(val targets: Set<Id>) : Styling()
        }
        object Locked: Mode()
    }

    class Storage {
        val document: DocumentProvider = DocumentProvider.Default()
        val views: Store<List<BlockView>> = Store.Screen()
        val focus: Store<Focus> = Store.Focus()
        val details: Store.Details = Store.Details()
        val relations: Store.Relations = Store.Relations()
        val objectTypes: Store.ObjectTypes = Store.ObjectTypes()
        val textSelection: Store<Editor.TextSelection> = Store.TextSelection()
        val objectRestrictions: Store.ObjectRestrictions = Store.ObjectRestrictions()
    }

    class Proxer(
        val intents: Proxy.Intents = Proxy.Intents(),
        val changes: Proxy.Text.Changes = Proxy.Text.Changes(),
        val saves: Proxy.Text.Saves = Proxy.Text.Saves(),
        val payloads: Proxy.Payloads = Proxy.Payloads(),
        val errors: Proxy.Error = Proxy.Error(),
        val toasts: Proxy.Toast = Proxy.Toast()
    )

    class Memory(
        val selections: SelectionStateHolder
    )

    sealed class Restore {
        data class Selection(
            val target: Id,
            val range: IntRange,
        ) : Restore()
    }

    sealed class Event {
        sealed class Text : Event() {
            data class OnSelectionChanged(val id: Id, val selection: IntRange) : Text()
        }
    }

    interface DocumentProvider {
        fun get(): Document
        fun update(document: Document)
        fun observe() : Flow<Document>
        fun clear()
        class Default : DocumentProvider {
            val doc = MutableStateFlow<Document>(emptyList())
            override fun observe(): Flow<Document> = doc
            override fun get(): Document = doc.value
            override fun update(document: Document) { this.doc.tryEmit(document) }
            override fun clear() { this.doc.value = emptyList() }
        }
    }
}