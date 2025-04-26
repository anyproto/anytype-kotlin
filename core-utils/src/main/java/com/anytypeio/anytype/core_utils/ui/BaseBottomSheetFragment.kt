package com.anytypeio.anytype.core_utils.ui

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
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
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import com.google.android.material.R.id.design_bottom_sheet as BOTTOM_SHEET_ID

abstract class BaseBottomSheetFragment<T : ViewBinding>(
    private val fragmentScope: Boolean = true
) : BottomSheetDialogFragment() {

    private var _binding: T? = null
    val binding: T get() = _binding!!

    val sheet: FrameLayout? get() = dialog?.findViewById(BOTTOM_SHEET_ID)

    private val throttleFlow = MutableSharedFlow<() -> Unit>(0)
    val jobs = mutableListOf<Job>()

    protected fun throttle(task: () -> Unit) {
        jobs += this.lifecycleScope.launch {
            try {
                throttleFlow.emit { task() }
            } catch (e: Exception) {
                Timber.e(e, "Error during emit in throttle()")
            }
        }
    }

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
        if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
            expand()
        }
        proceed(throttleFlow.throttleFirst(LONG_THROTTLE_DURATION)) {
            try {
                it()
            } catch (e: Exception) {
                Timber.e(e, "Unhandled exception in throttled flow execution")
            }
        }
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
            try {
                throttleFlow.emit {
                    try {
                        show(this@BaseBottomSheetFragment.childFragmentManager, tag)
                    } catch (e: Exception) {
                        Timber.e(e, "Error while showing child dialog")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during emit in showChildFragment")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragmentScope) releaseDependencies()
    }

    fun skipCollapsed() {
        sheet?.let {
            BottomSheetBehavior.from(it).skipCollapsed = true
        }
    }

    fun expand() {
        sheet?.let {
            BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun setFullHeightSheet() {
        sheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun injectDependencies()
    abstract fun releaseDependencies()
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): T
}

fun <T> BaseBottomSheetFragment<*>.proceed(flow: Flow<T>, body: suspend (T) -> Unit) {
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