package com.agileburo.anytype.ui.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ui.BaseFragment
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.domain.config.GetDebugSettings
import com.agileburo.anytype.domain.config.UseCustomContextMenu
import kotlinx.android.synthetic.main.fragment_debug_settings.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class DebugSettingsFragment : BaseFragment(R.layout.fragment_debug_settings) {

    @Inject
    lateinit var useCustomContextMenu: UseCustomContextMenu

    @Inject
    lateinit var getDebugSettings: GetDebugSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            getDebugSettings(Unit).proceed(
                failure = {},
                success = { setContextMenuToggle(it.isAnytypeContextMenuEnabled) }
            )
        }

        anytypeContextMenuToggle.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                useCustomContextMenu.invoke(UseCustomContextMenu.Params(isChecked))
            }
        }
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