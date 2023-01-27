package com.anytypeio.anytype.core_utils.ui

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.core_utils.BuildConfig
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.core_utils.insets.RootViewDeferringInsetsCallback
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


abstract class BaseFragment<T : ViewBinding>(
    @LayoutRes private val layout: Int,
    private val fragmentScope: Boolean = true
) : Fragment(layout) {

    private var applyWindowRootInsets: Boolean = true
    protected val handler = Handler(Looper.getMainLooper())

    private var _binding: T? = null
    val binding: T get() = _binding!!
    val hasBinding get() = _binding != null

    val jobs = mutableListOf<Job>()
    private val throttleFlow = MutableSharedFlow<() -> Unit>(0)

    abstract fun injectDependencies()
    abstract fun releaseDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onStart() {
        super.onStart()
        proceed(throttleFlow.throttleFirst(THROTTLE_DURATION)) { it() }
    }

    protected fun throttle(task: () -> Unit) {
        jobs += this.lifecycleScope.launch { throttleFlow.emit { task() } }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragmentScope) releaseDependencies()
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
        if (applyWindowRootInsets) {
            onApplyWindowRootInsets()
        }
    }

    open fun onApplyWindowRootInsets() {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val deferringInsetsListener = RootViewDeferringInsetsCallback(
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime()
            )
            ViewCompat.setWindowInsetsAnimationCallback(binding.root, deferringInsetsListener)
            ViewCompat.setOnApplyWindowInsetsListener(binding.root, deferringInsetsListener)
        }
    }

    protected fun DialogFragment.showChildFragment(tag: String? = null) {
        jobs += this@BaseFragment.lifecycleScope.launch {
            throttleFlow.emit { show(this@BaseFragment.childFragmentManager, tag) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): T
}

fun <T> BaseFragment<*>.proceed(flow: Flow<T>, body: suspend (T) -> Unit) {
    jobs += flow.cancellable().onEach { body(it) }.launchIn(lifecycleScope)
}

const val THROTTLE_DURATION = 300L