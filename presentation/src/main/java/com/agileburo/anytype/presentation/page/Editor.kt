package com.agileburo.anytype.presentation.page

import com.agileburo.anytype.presentation.page.editor.Proxy
import com.agileburo.anytype.presentation.page.editor.Store
import com.agileburo.anytype.presentation.page.selection.SelectionStateHolder

interface Editor {

    class Storage {
        val focus: Store<String> = Store.Focus()
        val details: Store.Details = Store.Details()
    }

    class Proxer(
        val intents: Proxy.Intents = Proxy.Intents(),
        val changes: Proxy.Text.Changes = Proxy.Text.Changes(),
        val saves: Proxy.Text.Saves = Proxy.Text.Saves(),
        val payloads: Proxy.Payloads = Proxy.Payloads(),
        val errors: Proxy.Error = Proxy.Error()
    )

    class Memory(
        val selections: SelectionStateHolder
    )
}