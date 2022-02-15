package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job
import com.google.android.material.R.id.design_bottom_sheet as BOTTOM_SHEET_ID

abstract class BaseBottomSheetFragment<T : ViewBinding>(
    private val fragmentScope: Boolean = true
) : BottomSheetDialogFragment() {

    private var _binding: T? = null
    protected val binding: T get() = _binding!!

    protected val jobs = mutableListOf<Job>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        return _binding?.root
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): T
}