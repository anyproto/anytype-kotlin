package com.agileburo.anytype.feature_desktop.ui

import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.ui.ViewStateFragment
import com.agileburo.anytype.feature_desktop.navigation.DesktopNavigation
import com.agileburo.anytype.feature_desktop.navigation.DesktopNavigationProvider

abstract class FeatureBaseFragment<VS>(
    @LayoutRes private val layout: Int
) : ViewStateFragment<VS>(layout) {

    val navObserver = Observer<Event<DesktopNavigation.Command>> { event ->
        event.getContentIfNotHandled()?.let { navigate(it) }
    }

    private fun navigate(command: DesktopNavigation.Command) {
        when (command) {
            is DesktopNavigation.Command.OpenDocument -> {
                provideNavigation().openDocument(command.id)
            }
            is DesktopNavigation.Command.OpenProfile -> {
                provideNavigation().openProfile()
            }
        }
    }

    private fun provideNavigation(): DesktopNavigation {
        return (requireActivity() as? DesktopNavigationProvider)?.provideDesktopNavigation()
            ?: throw IllegalStateException(NAV_PROVIDER_ERROR)

    }

    companion object {
        const val NAV_PROVIDER_ERROR = "Activity should implement profile navigation"
    }
}