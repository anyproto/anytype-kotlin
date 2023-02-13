package com.anytypeio.anytype.ui.base

import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.core_utils.ui.getNavigationId
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.AppNavigation.Command
import timber.log.Timber

@Deprecated("Use NavigationRouter directly")
abstract class NavigationFragment<BINDING : ViewBinding>(
    @LayoutRes private val layout: Int
) : BaseFragment<BINDING>(layout) {

    private val currentNavigationId by lazy { getNavigationId() }
    private val navigationRouter by lazy {
        NavigationRouter((requireActivity() as AppNavigation.Provider).nav())
    }

    val navObserver = Observer<EventWrapper<Command>> { event ->
        event.getContentIfNotHandled()?.let {
            try {
                if (currentNavigationId == getNavigationId()) {
                    throttle { navigationRouter.navigate(it) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Navigation: $it")
                if (BuildConfig.DEBUG) {
                    throw e
                }
            }
        }
    }
}