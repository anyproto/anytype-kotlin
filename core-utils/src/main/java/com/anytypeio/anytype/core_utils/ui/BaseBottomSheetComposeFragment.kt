package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetComposeFragment : BottomSheetDialogFragment() {

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