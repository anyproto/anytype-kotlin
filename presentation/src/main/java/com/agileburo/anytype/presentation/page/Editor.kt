package com.agileburo.anytype.presentation.page

import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.editor.Editor.Focus
import com.agileburo.anytype.presentation.page.editor.Proxy
import com.agileburo.anytype.presentation.page.editor.Store
import com.agileburo.anytype.presentation.page.selection.SelectionStateHolder

interface Editor {

    class Storage {
        val views: Store<List<BlockView>> = Store.Screen()
        val focus: Store<Focus> = Store.Focus()
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