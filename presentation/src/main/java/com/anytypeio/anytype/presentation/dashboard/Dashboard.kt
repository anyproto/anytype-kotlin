package com.anytypeio.anytype.presentation.dashboard

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.editor.editor.Store

interface Dashboard {
    data class Storage(
        val oTypes: Store.State<List<ObjectType>> = Store.State(emptyList())
    )
}