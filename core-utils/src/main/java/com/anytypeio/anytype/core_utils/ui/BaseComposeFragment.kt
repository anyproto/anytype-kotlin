package com.anytypeio.anytype.core_utils.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_utils.BuildConfig
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.insets.RootViewDeferringInsetsCallback
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseComposeFragment : Fragment() {

    val jobs = mutableListOf<Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onStop() {
        jobs.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseDependencies()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onApplyWindowRootInsets(view)
    }

    open fun onApplyWindowRootInsets(view: View) {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val deferringInsetsListener = RootViewDeferringInsetsCallback(
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime()
            )

            ViewCompat.setWindowInsetsAnimationCallback(view, deferringInsetsListener)
            ViewCompat.setOnApplyWindowInsetsListener(view, deferringInsetsListener)
        }
    }

    protected fun DialogFragment.showChildFragment(tag: String? = null) {
        show(this@BaseComposeFragment.childFragmentManager, tag)
    }


    abstract fun injectDependencies()
    abstract fun releaseDependencies()
}

fun <T> BaseComposeFragment.proceed(flow: Flow<T>, body: suspend (T) -> Unit) {
    jobs += flow.cancellable().onEach { body(it) }.launchIn(lifecycleScope)
}