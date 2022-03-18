package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentUserSettingsBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.OtherSettingsViewModel
import com.anytypeio.anytype.ui.dashboard.ClearCacheAlertFragment
import com.anytypeio.anytype.ui.objects.ObjectTypeChangeFragment
import javax.inject.Inject

class OtherSettingsFragment : BaseBottomSheetFragment<FragmentUserSettingsBinding>() {

    @Inject
    lateinit var factory: OtherSettingsViewModel.Factory

    private val vm by viewModels<OtherSettingsViewModel> { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(ObjectTypeChangeFragment.OBJECT_TYPE_REQUEST_KEY) { _, bundle ->
            val id = bundle.getString(ObjectTypeChangeFragment.OBJECT_TYPE_URL_KEY)
            val name = bundle.getString(ObjectTypeChangeFragment.OBJECT_TYPE_NAME_KEY)
            vm.proceedWithUpdateType(type = id, name = name)
        }
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
        }
    }

    private fun observe(command: OtherSettingsViewModel.Command) {
        when (command) {
            is OtherSettingsViewModel.Command.Exit -> dismiss()
            is OtherSettingsViewModel.Command.NavigateToObjectTypesScreen -> {
                findNavController().navigate(
                    R.id.objectTypeChangeScreen,
                    bundleOf(
                        ObjectTypeChangeFragment.ARG_SMART_BLOCK_TYPE to command.smartBlockType
                    )
                )
            }
            is OtherSettingsViewModel.Command.SetObjectType -> {
                binding.objectType.text = command.name
            }
            is OtherSettingsViewModel.Command.Toast -> toast(command.msg)
            OtherSettingsViewModel.Command.ShowClearCacheAlert -> {
                vm.sendFileOffloadScreenEvent()
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