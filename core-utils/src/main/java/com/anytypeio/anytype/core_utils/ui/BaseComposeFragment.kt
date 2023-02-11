package com.anytypeio.anytype.core_utils.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.core_utils.BuildConfig
import com.anytypeio.anytype.core_utils.insets.RootViewDeferringInsetsCallback
import kotlinx.coroutines.Job

abstract class BaseComposeFragment : Fragment() {

    val jobs = mutableListOf<Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
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

    abstract fun injectDependencies()
    abstract fun releaseDependencies()
}