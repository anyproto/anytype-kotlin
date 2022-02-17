package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.Job

abstract class BaseFragment<T : ViewBinding>(
    @LayoutRes private val layout: Int,
    private val fragmentScope: Boolean = true
) : Fragment(layout) {

    private var _binding: T? = null
    val binding: T get() = _binding!!

    protected val jobs = mutableListOf<Job>()

    abstract fun injectDependencies()
    abstract fun releaseDependencies()

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        return _binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): T
}