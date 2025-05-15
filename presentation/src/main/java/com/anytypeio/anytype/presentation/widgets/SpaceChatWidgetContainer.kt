package com.anytypeio.anytype.presentation.widgets

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SpaceChatWidgetContainer @Inject constructor() : WidgetContainer {
    override val view: Flow<WidgetView> = flowOf(
        WidgetView.SpaceChat
    )
}