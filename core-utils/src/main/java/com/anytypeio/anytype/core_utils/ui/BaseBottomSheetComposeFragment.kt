package com.anytypeio.anytype.core_utils.ui

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
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
import timber.log.Timber

abstract class BaseBottomSheetComposeFragment : BottomSheetDialogFragment() {

    val jobs = mutableListOf<Job>()

    private val currentNavigationId by lazy { getNavigationId() }
    private val throttleFlow = MutableSharedFlow<() -> Unit>(0)

    @Deprecated("Not safe enough.")
    protected fun safeNavigate(
        @IdRes id: Int,
        args: Bundle? = null
    ) {
        jobs += this.lifecycleScope.launch {
            try {
                throttleFlow.emit {
                    if (currentNavigationId == getNavigationId()) {
                        try {
                            findNavController().navigate(id, args)
                        } catch (e: Exception) {
                            Timber.e(e, "safeNavigateMethod is not safe!")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during emit in safeNavigate")
            }
        }
    }

    protected fun throttle(task: () -> Unit) {
        jobs += this.lifecycleScope.launch { throttleFlow.emit { task() } }
    }

    override fun onStart() {
        super.onStart()
        if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
            expand()
        }
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
            try {
                throttleFlow.emit {
                    try {
                        show(this@BaseBottomSheetComposeFragment.childFragmentManager, tag)
                    } catch (e: Exception) {
                        Timber.e(e, "Error while showing child dialog")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during emit in showChildFragment")
            }
        }
    }

    fun expand() {
        (dialog as? BottomSheetDialog)?.behavior?.state = STATE_EXPANDED
    }

    fun skipCollapsed() {
        (dialog as? BottomSheetDialog)?.behavior?.skipCollapsed = true
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

    open fun injectDependencies() {
        // Inject nothing by default. Override to inject.
    }

    open fun releaseDependencies() {
        // Inject nothing by default. Override to inject.
    }

    companion object {
        const val DEFAULT_PADDING_TOP = 28
    }
}

fun Fragment.getNavigationId() = findNavController().currentDestination?.id

fun <T> BaseBottomSheetComposeFragment.proceed(flow: Flow<T>, body: suspend (T) -> Unit) {
    jobs += flow
        .cancellable()
        .onEach {
            try {
                body(it)
            } catch (e: Exception) {
                Timber.e(e, "Unhandled exception in proceed flow")
            }
        }
        .launchIn(lifecycleScope)
}