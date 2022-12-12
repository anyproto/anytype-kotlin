package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.core_utils.R
import com.anytypeio.anytype.core_utils.ext.LONG_THROTTLE_DURATION
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.google.android.material.R.id.design_bottom_sheet as BOTTOM_SHEET_ID

abstract class BaseBottomSheetFragment<T : ViewBinding>(
    private val fragmentScope: Boolean = true
) : BottomSheetDialogFragment() {

    private var _binding: T? = null
    val binding: T get() = _binding!!

    val sheet: FrameLayout? get() = dialog?.findViewById(BOTTOM_SHEET_ID)

    private val throttleFlow = MutableSharedFlow<() -> Unit>(0)

    protected fun throttle(task: () -> Unit) {
        jobs += this.lifecycleScope.launch { throttleFlow.emit { task() } }
    }

    val jobs = mutableListOf<Job>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DefaultBottomDialogAnimation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onStart() {
        super.onStart()
        proceed(throttleFlow.throttleFirst(LONG_THROTTLE_DURATION)) { it() }
    }

    override fun onStop() {
        super.onStop()
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    protected fun DialogFragment.showChildFragment(tag: String? = null) {
        jobs += this@BaseBottomSheetFragment.lifecycleScope.launch {
            throttleFlow.emit { show(this@BaseBottomSheetFragment.childFragmentManager, tag) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragmentScope) releaseDependencies()
    }

    fun skipCollapsed() {
        sheet?.let { sheet ->
            BottomSheetBehavior.from(sheet).apply {
                skipCollapsed = true
            }
        }
    }

    fun expand() {
        sheet?.let { sheet ->
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

fun <T> BaseBottomSheetFragment<*>.proceed(flow: Flow<T>, body: suspend (T) -> Unit) {
    jobs += flow.onEach { body(it) }.launchIn(lifecycleScope)
}