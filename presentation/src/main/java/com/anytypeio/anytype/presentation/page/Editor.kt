package com.anytypeio.anytype.presentation.page

import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.editor.Editor.Focus
import com.anytypeio.anytype.presentation.page.editor.Proxy
import com.anytypeio.anytype.presentation.page.editor.Store
import com.anytypeio.anytype.presentation.page.selection.SelectionStateHolder

interface Editor {

    class Storage {
        val views: Store<List<BlockView>> = Store.Screen()
        val focus: Store<Focus> = Store.Focus()
        val details: Store.Details = Store.Details()
        val textSelection: Store<Editor.TextSelection> = Store.TextSelection()
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
}