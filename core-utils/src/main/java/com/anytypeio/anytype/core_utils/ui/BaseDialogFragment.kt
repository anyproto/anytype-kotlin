package com.anytypeio.anytype.core_utils.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.core_utils.ext.cancel
import kotlinx.coroutines.Job

abstract class BaseDialogFragment<T : ViewBinding>(private val fragmentScope: Boolean = true) : DialogFragment() {

    private var _binding: T? = null
    protected val binding: T get() = _binding!!

    protected val jobs = mutableListOf<Job>()

    abstract fun injectDependencies()
    abstract fun releaseDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
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

    override fun onStop() {
        super.onStop()
        jobs.cancel()
    }

    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): T
}