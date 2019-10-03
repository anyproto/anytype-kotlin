package com.agileburo.anytype.feature_desktop.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.agileburo.anytype.feature_desktop.navigation.DesktopNavigationProvider
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment(
    private val fragmentScope : Boolean = true
) : Fragment() {

    val subscriptions by lazy { CompositeDisposable() }

    abstract fun injectDependencies()
    abstract fun releaseDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscriptions.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragmentScope) releaseDependencies()
    }

    private fun navigationProvider() = (requireActivity() as DesktopNavigationProvider)
}