package com.anytypeio.anytype.core_utils.tools

interface FeatureToggles {

    val isAutoUpdateEnabled: Boolean

    val isLogFromGoProcess: Boolean

    val isLogMiddlewareInteraction: Boolean

    val isConciseLogging: Boolean

    val isLogEditorViewModelEvents: Boolean

    val isLogEditorControlPanelMachine: Boolean

    val enableDiscussionDemo: Boolean

    val isSpaceLevelChatEnabled: Boolean

    val isNewSpaceHomeEnabled: Boolean
}