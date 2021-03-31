package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Job

abstract class BaseFragment(
    @LayoutRes private val layout: Int,
    private val fragmentScope: Boolean = true
) : Fragment(layout) {

    protected val jobs = mutableListOf<Job>()

    abstract fun injectDependencies()
    abstract fun releaseDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onStop() {
        super.onStop()
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragmentScope) releaseDependencies()
    }
}