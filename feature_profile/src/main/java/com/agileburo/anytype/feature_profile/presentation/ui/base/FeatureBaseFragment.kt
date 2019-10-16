package com.agileburo.anytype.feature_profile.presentation.ui.base

import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.ui.ViewStateFragment
import com.agileburo.anytype.feature_profile.navigation.ProfileNavigation
import com.agileburo.anytype.feature_profile.navigation.ProfileNavigationProvider

abstract class FeatureBaseFragment<VS>(
    @LayoutRes private val layout: Int
) : ViewStateFragment<VS>(layout) {

    val navObserver = Observer<Event<ProfileNavigation.Command>> { event ->
        event.getContentIfNotHandled()?.let { navigate(it) }
    }

    private fun navigate(command: ProfileNavigation.Command) {
        when (command) {
            is ProfileNavigation.Command.OpenKeychainScreen -> {
                provideNavigation().openKeychainScreen()
            }
            is ProfileNavigation.Command.OpenPinCodeScreen -> {
                provideNavigation().openPinCodeScreen()
            }
        }
    }

    private fun provideNavigation(): ProfileNavigation {
        return (requireActivity() as? ProfileNavigationProvider)?.provide()
            ?: throw IllegalStateException(NAV_PROVIDER_ERROR)

    }

    companion object {
        const val NAV_PROVIDER_ERROR = "Activity should implement profile navigation"
    }
}