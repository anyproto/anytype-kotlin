package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentUserSettingsBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.OtherSettingsViewModel
import com.anytypeio.anytype.ui.dashboard.ClearCacheAlertFragment
import com.anytypeio.anytype.ui.objects.types.pickers.AppDefaultObjectTypeFragment
import javax.inject.Inject

class OtherSettingsFragment : BaseBottomSheetFragment<FragmentUserSettingsBinding>(),
    AppDefaultObjectTypeFragment.OnObjectTypeAction {

    @Inject
    lateinit var factory: OtherSettingsViewModel.Factory

    private val vm by viewModels<OtherSettingsViewModel> { factory }

    override fun onProceedWithUpdateType(id: Id, name: String) {
        vm.proceedWithUpdateType(type = id, name = name)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDefaultObjectTypeTitle.setOnClickListener { vm.onObjectTypeClicked() }
        binding.btnDefaultObjectType.setOnClickListener { vm.onObjectTypeClicked() }
        binding.ivArrowForward.setOnClickListener { vm.onObjectTypeClicked() }
    }

    override fun onStart() {
        super.onStart()
        with(lifecycleScope) {
            jobs += subscribe(vm.commands) { observe(it) }
            jobs += subscribe(vm.defaultObjectTypeName) { binding.objectType.text = it }
        }
    }

    private fun observe(command: OtherSettingsViewModel.Command) {
        when (command) {
            is OtherSettingsViewModel.Command.Exit -> dismiss()
            is OtherSettingsViewModel.Command.NavigateToObjectTypesScreen -> {
                val fr = AppDefaultObjectTypeFragment.newInstance(
                    excludeTypes = command.excludeTypes
                )
                fr.show(childFragmentManager, null)
            }
            is OtherSettingsViewModel.Command.Toast -> toast(command.msg)
            is OtherSettingsViewModel.Command.ShowClearCacheAlert -> {
                val dialog = ClearCacheAlertFragment.new()
                dialog.onClearAccepted = { vm.proceedWithClearCache() }
                dialog.show(childFragmentManager, null)
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserSettingsBinding = FragmentUserSettingsBinding.inflate(
        inflater, container, false
    )

    override fun injectDependencies() {
        componentManager().otherSettingsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().otherSettingsComponent.release()
    }
}