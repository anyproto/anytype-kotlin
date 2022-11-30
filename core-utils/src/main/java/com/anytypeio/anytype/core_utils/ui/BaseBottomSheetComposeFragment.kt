package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import android.view.View
import com.anytypeio.anytype.core_utils.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job

abstract class BaseBottomSheetComposeFragment : BottomSheetDialogFragment() {

    protected val jobs = mutableListOf<Job>()

    override fun onStop() {
        super.onStop()
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DefaultBottomDialogAnimation
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseDependencies()
    }

    abstract fun injectDependencies()
    abstract fun releaseDependencies()
}