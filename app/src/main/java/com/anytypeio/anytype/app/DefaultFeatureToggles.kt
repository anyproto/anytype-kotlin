package com.anytypeio.anytype.app

import android.content.Context
import android.content.SharedPreferences
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.device.BuildProvider
import javax.inject.Inject

class DefaultFeatureToggles @Inject constructor(
    private val context: Context,
    @TogglePrefs private val prefs: SharedPreferences,
    private val buildProvider: BuildProvider
) : FeatureToggles {

    override val isLogFromMiddlewareLibrary =
        BuildConfig.LOG_FROM_MW_LIBRARY && buildProvider.isDebug()
    override val isLogMiddlewareInteraction =
        BuildConfig.LOG_MW_INTERACTION && buildProvider.isDebug()
    override val isLogDashboardReducer =
        BuildConfig.LOG_DASHBOARD_REDUCER && buildProvider.isDebug()
    override val isLogEditorViewModelEvents =
        BuildConfig.LOG_EDITOR_VIEWMODEL_EVENTS && buildProvider.isDebug()
    override val isLogEditorControlPanelMachine =
        BuildConfig.LOG_EDITOR_CONTROL_PANEL && buildProvider.isDebug()

    override val isTroubleshootingMode
        get() = prefs.getBoolean(context.getString(R.string.trouble_mode), true)

    override val isNewOnBoardingEnabled: Boolean = false

    override val isAutoUpdateEnabled: Boolean = false
}