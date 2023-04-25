package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.core_utils.R
import com.anytypeio.anytype.core_utils.ext.LONG_THROTTLE_DURATION
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class BaseBottomSheetComposeFragment : BottomSheetDialogFragment() {

    val jobs = mutableListOf<Job>()

    private val currentNavigationId by lazy { getNavigationId() }
    private val throttleFlow = MutableSharedFlow<() -> Unit>(0)

    protected fun safeNavigate(
        @IdRes id: Int,
        args: Bundle? = null
    ) {
        jobs += this.lifecycleScope.launch {
            throttleFlow.emit {
                if (currentNavigationId == getNavigationId()) {
                    findNavController().navigate(id, args)
                }
            }
        }
    }

    protected fun throttle(task: () -> Unit) {
        jobs += this.lifecycleScope.launch { throttleFlow.emit { task() } }
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
        jobs += this@BaseBottomSheetComposeFragment.lifecycleScope.launch {
            throttleFlow.emit {
                show(
                    this@BaseBottomSheetComposeFragment.childFragmentManager,
                    tag
                )
            }
        }
    }

    fun expand() {
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
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

fun Fragment.getNavigationId() = findNavController().currentDestination?.id

fun <T> BaseBottomSheetComposeFragment.proceed(flow: Flow<T>, body: suspend (T) -> Unit) {
    jobs += flow.cancellable().onEach { body(it) }.launchIn(lifecycleScope)
}