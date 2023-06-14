package com.anytypeio.anytype.core_utils.tools

interface FeatureToggles {

    val isAutoUpdateEnabled: Boolean

    val isLogFromMiddlewareLibrary: Boolean

    val isLogMiddlewareInteraction: Boolean

    val isLogDashboardReducer: Boolean

    val isLogEditorViewModelEvents: Boolean

    val isLogEditorControlPanelMachine: Boolean

    val isNewOnBoardingEnabled: Boolean

}