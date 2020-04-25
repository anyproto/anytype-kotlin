package com.agileburo.anytype.core_ui.features.page

import com.agileburo.anytype.core_ui.model.UiBlock

interface TurnIntoActionReceiver {
    fun onTurnIntoBlockClicked(block: UiBlock)
}