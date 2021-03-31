package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment(private val fragmentScope: Boolean = true) : DialogFragment() {

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
}