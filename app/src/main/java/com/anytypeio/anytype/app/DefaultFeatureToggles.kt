package com.anytypeio.anytype.app

import android.content.Context
import android.content.SharedPreferences
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import javax.inject.Inject

class DefaultFeatureToggles @Inject constructor(
    private val context: Context,
    @TogglePrefs private val prefs: SharedPreferences
) : FeatureToggles {

    override val isLogFromMiddlewareLibrary = BuildConfig.LOG_FROM_MW_LIBRARY && isDebug
    override val isLogMiddlewareInteraction = BuildConfig.LOG_DASHBOARD_REDUCER && isDebug
    override val isLogDashboardReducer = BuildConfig.LOG_DASHBOARD_REDUCER && isDebug
    override val isLogEditorViewModelEvents = BuildConfig.LOG_EDITOR_VIEWMODEL_EVENTS && isDebug
    override val isLogEditorControlPanelMachine = BuildConfig.LOG_EDITOR_CONTROL_PANEL && isDebug

    override val isTroubleshootingMode
        get() = prefs.getBoolean(context.getString(R.string.trouble_mode), BuildConfig.DEBUG)

    override val isDebug: Boolean
        get() = prefs.getBoolean(context.getString(R.string.debug_mode), BuildConfig.DEBUG)

    override val isNewOnBoardingEnabled: Boolean = false

}