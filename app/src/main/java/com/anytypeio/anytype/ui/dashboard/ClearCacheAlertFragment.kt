package com.anytypeio.anytype.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_clear_cache.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ClearCacheAlertFragment : BaseBottomSheetFragment() {

    var onClearAccepted: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_clear_cache, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnCancel
            .clicks()
            .onEach { dismiss() }
            .launchIn(lifecycleScope)

        btnClear
            .clicks()
            .onEach {
                onClearAccepted()
                dismiss()
            }
            .launchIn(lifecycleScope)
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        fun new(): ClearCacheAlertFragment = ClearCacheAlertFragment()
    }
}