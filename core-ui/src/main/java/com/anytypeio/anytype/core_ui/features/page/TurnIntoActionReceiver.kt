package com.anytypeio.anytype.core_ui.features.page

import com.anytypeio.anytype.core_ui.model.UiBlock

interface TurnIntoActionReceiver {
    /**
     * @param target id of the target block
     * @param block new block's type
     */
    fun onTurnIntoBlockClicked(target: String, block: UiBlock)

    /**
     * @param block new block's type
     */
    fun onTurnIntoMultiSelectBlockClicked(block: UiBlock)
}