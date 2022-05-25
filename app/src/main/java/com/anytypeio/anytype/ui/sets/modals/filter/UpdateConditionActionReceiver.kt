package com.anytypeio.anytype.ui.sets.modals.filter

import com.anytypeio.anytype.presentation.sets.model.Viewer

interface UpdateConditionActionReceiver {
    fun update(condition: Viewer.Filter.Condition)
}