package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class BaseComposeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseDependencies()
    }

    abstract fun injectDependencies()
    abstract fun releaseDependencies()
}