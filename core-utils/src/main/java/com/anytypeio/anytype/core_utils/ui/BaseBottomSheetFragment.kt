package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import android.widget.FrameLayout
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetFragment(
    private val fragmentScope: Boolean = true
) : BottomSheetDialogFragment() {

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

    fun expand() {
        dialog?.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.let { sheet ->
            BottomSheetBehavior.from(sheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }
}