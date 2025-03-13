package com.anytypeio.anytype.core_utils.tools

interface FeatureToggles {
    val isLogFromGoProcess: Boolean
    val isLogMiddlewareInteraction: Boolean
    val isConciseLogging: Boolean
    val isLogEditorViewModelEvents: Boolean
    val isLogEditorControlPanelMachine: Boolean
}