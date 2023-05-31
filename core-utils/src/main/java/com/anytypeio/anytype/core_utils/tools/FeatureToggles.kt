package com.anytypeio.anytype.core_utils.tools

interface FeatureToggles {

    val isLogFromMiddlewareLibrary: Boolean

    val isLogMiddlewareInteraction: Boolean

    val isLogDashboardReducer: Boolean

    val isLogEditorViewModelEvents: Boolean

    val isLogEditorControlPanelMachine: Boolean

    val isTroubleshootingMode: Boolean

    val isNewOnBoardingEnabled: Boolean

}