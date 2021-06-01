package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job
import com.google.android.material.R.id.design_bottom_sheet as BOTTOM_SHEET_ID

abstract class BaseBottomSheetFragment(
    private val fragmentScope: Boolean = true
) : BottomSheetDialogFragment() {

    protected val jobs = mutableListOf<Job>()

    abstract override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?

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

    fun expand() {
        dialog?.findViewById<FrameLayout>(BOTTOM_SHEET_ID)?.let { sheet ->
            BottomSheetBehavior.from(sheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    abstract fun injectDependencies()
    abstract fun releaseDependencies()
}