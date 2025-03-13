package com.anytypeio.anytype.app

import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.device.BuildProvider
import javax.inject.Inject

class DefaultFeatureToggles @Inject constructor(
    buildProvider: BuildProvider
) : FeatureToggles {

    override val isLogFromGoProcess =
        BuildConfig.LOG_FROM_MW_LIBRARY && buildProvider.isDebug()

    override val isLogMiddlewareInteraction = BuildConfig.LOG_MW_INTERACTION && buildProvider.isDebug()

    override val isLogEditorViewModelEvents =
        BuildConfig.LOG_EDITOR_VIEWMODEL_EVENTS && buildProvider.isDebug()

    override val isLogEditorControlPanelMachine =
        BuildConfig.LOG_EDITOR_CONTROL_PANEL && buildProvider.isDebug()

    override val isConciseLogging: Boolean = true
}