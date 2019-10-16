package com.agileburo.anytype.core_utils.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.agileburo.anytype.core_utils.di.CoreComponentProvider

abstract class BaseFragment(
    @LayoutRes private val layout: Int,
    private val fragmentScope: Boolean = true
) : Fragment(layout) {

    abstract fun injectDependencies()
    abstract fun releaseDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragmentScope) releaseDependencies()
    }

    fun provideCoreComponent() = (activity as? CoreComponentProvider)?.provideCoreComponent()
        ?: throw IllegalStateException(CORE_COMPONENT_PROVIDER_ERROR)

    companion object {
        const val CORE_COMPONENT_PROVIDER_ERROR = "Activity should implement core component provider"
    }
}