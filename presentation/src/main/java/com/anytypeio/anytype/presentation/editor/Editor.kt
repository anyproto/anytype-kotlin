package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.editor.Editor.Focus
import com.anytypeio.anytype.presentation.editor.editor.Proxy
import com.anytypeio.anytype.presentation.editor.editor.Store
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder

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
    }

    class Storage {
        val views: Store<List<BlockView>> = Store.Screen()
        val focus: Store<Focus> = Store.Focus()
        val details: Store.Details = Store.Details()
        val relations: Store.Relations = Store.Relations()
        val objectTypes: Store.ObjectTypes = Store.ObjectTypes()
        val textSelection: Store<Editor.TextSelection> = Store.TextSelection()
        val objectRestrictions: Store.ObjectRestrictions = Store.ObjectRestrictions()
        val objectIsDraft: Store.ObjectIsDraft = Store.ObjectIsDraft()
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
}