package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.anytypeio.anytype.core_utils.ext.cancel
import kotlinx.coroutines.Job

abstract class BaseDialogFragment(private val fragmentScope: Boolean = true) : DialogFragment() {

    protected val jobs = mutableListOf<Job>()

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

    override fun onStop() {
        super.onStop()
        jobs.cancel()
    }
}