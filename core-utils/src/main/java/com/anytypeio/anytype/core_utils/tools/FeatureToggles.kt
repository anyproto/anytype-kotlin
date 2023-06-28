package com.anytypeio.anytype.core_utils.tools

interface FeatureToggles {

    val isAutoUpdateEnabled: Boolean

    val isLogFromGoProcess: Boolean

    val isLogMiddlewareInteraction: Boolean

    val isConciseLogging: Boolean

    val excludeThreadStatusLogging: Boolean

    val isLogEditorViewModelEvents: Boolean

    val isLogEditorControlPanelMachine: Boolean

    val isNewOnBoardingEnabled: Boolean

}