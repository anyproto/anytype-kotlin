package com.anytypeio.anytype.ui_settings.appearance

import androidx.appcompat.app.AppCompatDelegate
import com.anytypeio.anytype.core_models.ThemeMode
import javax.inject.Inject

interface ThemeApplicator {

    fun apply(theme: ThemeMode)

}

class ThemeApplicatorImpl @Inject constructor(): ThemeApplicator {
    override fun apply(theme: ThemeMode) {
        when(theme) {
            ThemeMode.Light -> apply(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeMode.Night -> apply(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeMode.System -> apply(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun apply(mode: Int) {
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}