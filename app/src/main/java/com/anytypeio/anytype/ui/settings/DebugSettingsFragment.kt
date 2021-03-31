package com.anytypeio.anytype.ui.settings

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.config.UseCustomContextMenu
import com.anytypeio.anytype.domain.dataview.interactor.DebugSync
import kotlinx.android.synthetic.main.fragment_debug_settings.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class DebugSettingsFragment : BaseFragment(R.layout.fragment_debug_settings) {

    @Inject
    lateinit var useCustomContextMenu: UseCustomContextMenu

    @Inject
    lateinit var getDebugSettings: GetDebugSettings

    @Inject
    lateinit var debugSync: DebugSync

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            getDebugSettings(Unit).proceed(
                failure = {},
                success = { setContextMenuToggle(it.isAnytypeContextMenuEnabled) }
            )
        }

        btnSync.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                debugSync.invoke(Unit).proceed(
                    failure = {},
                    success = { status -> setSyncStatus(status) }
                )
            }
        }

        tvSync.setOnClickListener {
            val cm = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            cm.text = tvSync.text
            requireContext().toast("Sync status is copied to the clipboard")
        }

        anytypeContextMenuToggle.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                useCustomContextMenu.invoke(UseCustomContextMenu.Params(isChecked))
            }
        }
    }

    private fun setSyncStatus(status: String) {
        scrollContainer.visible()
        tvSync.text = status
    }

    private fun setContextMenuToggle(value: Boolean) {
        anytypeContextMenuToggle.isChecked = value
    }

    override fun injectDependencies() {
        componentManager().debugSettingsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().debugSettingsComponent.release()
    }
}