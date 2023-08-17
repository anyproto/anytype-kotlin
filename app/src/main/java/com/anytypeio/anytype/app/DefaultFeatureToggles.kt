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

    override val isLogFromGoProcess =
        BuildConfig.LOG_FROM_MW_LIBRARY && buildProvider.isDebug()

    override val isLogMiddlewareInteraction =
        BuildConfig.LOG_MW_INTERACTION && buildProvider.isDebug()

    override val excludeThreadStatusLogging: Boolean = true

    override val isLogEditorViewModelEvents =
        BuildConfig.LOG_EDITOR_VIEWMODEL_EVENTS && buildProvider.isDebug()

    override val isLogEditorControlPanelMachine =
        BuildConfig.LOG_EDITOR_CONTROL_PANEL && buildProvider.isDebug()

    override val isNewOnBoardingEnabled: Boolean = true

    override val isAutoUpdateEnabled: Boolean = false

    override val isConciseLogging: Boolean = true

    override val enableSpaces: Boolean = true
}