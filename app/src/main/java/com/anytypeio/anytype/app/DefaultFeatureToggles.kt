package com.anytypeio.anytype.app

import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import javax.inject.Inject

class DefaultFeatureToggles @Inject constructor() : FeatureToggles {

    override val isLogFromMiddlewareLibrary =
        BuildConfig.LOG_FROM_MW_LIBRARY && BuildConfig.DEBUG

    override val isLogMiddlewareInteraction =
        BuildConfig.LOG_MW_INTERACTION && BuildConfig.DEBUG

    override val isLogDashboardReducer =
        BuildConfig.LOG_DASHBOARD_REDUCER && BuildConfig.DEBUG

    override val isLogEditorViewModelEvents =
        BuildConfig.LOG_EDITOR_VIEWMODEL_EVENTS && BuildConfig.DEBUG

    override val isLogEditorControlPanelMachine =
        BuildConfig.LOG_EDITOR_CONTROL_PANEL && BuildConfig.DEBUG

}