package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.proceed
import com.anytypeio.anytype.databinding.FragmentUserSettingsBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.PersonalizationSettingsViewModel
import com.anytypeio.anytype.ui.dashboard.ClearCacheAlertFragment
import com.anytypeio.anytype.ui.objects.types.pickers.AppDefaultObjectTypeFragment
import javax.inject.Inject

class PersonalizationSettingsFragment : BaseBottomSheetFragment<FragmentUserSettingsBinding>(),
    AppDefaultObjectTypeFragment.OnObjectTypeAction {

    @Inject
    lateinit var factory: PersonalizationSettingsViewModel.Factory

    private val vm by viewModels<PersonalizationSettingsViewModel> { factory }

    override fun onProceedWithUpdateType(id: Id, key: Key, name: String) {
        vm.proceedWithUpdateType(type = id, key = key, name = name)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDefaultObjectTypeTitle.setOnClickListener { vm.onObjectTypeClicked() }
        binding.btnDefaultObjectType.setOnClickListener { vm.onObjectTypeClicked() }
        binding.ivArrowForward.setOnClickListener { vm.onObjectTypeClicked() }
        binding.btnWallpaper.setOnClickListener { vm.onWallpaperClicked() }
    }

    override fun onStart() {
        super.onStart()
        proceed(vm.commands) { observe(it) }
        proceed(vm.defaultObjectTypeName) { binding.objectType.text = it }
    }

    private fun observe(command: PersonalizationSettingsViewModel.Command) {
        when (command) {
            is PersonalizationSettingsViewModel.Command.Exit -> throttle { dismiss() }
            is PersonalizationSettingsViewModel.Command.NavigateToObjectTypesScreen -> {
                AppDefaultObjectTypeFragment.newInstance(
                    excludeTypes = command.excludeTypes
                ).showChildFragment()
            }
            is PersonalizationSettingsViewModel.Command.NavigateToWallpaperScreen -> {
                findNavController().navigate(R.id.wallpaperSetScreen)
            }
            is PersonalizationSettingsViewModel.Command.Toast -> toast(command.msg)
            is PersonalizationSettingsViewModel.Command.ShowClearCacheAlert -> {
                val dialog = ClearCacheAlertFragment.new()
                dialog.onClearAccepted = { vm.proceedWithClearCache() }
                dialog.showChildFragment()
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
        componentManager().personalizationSettingsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().personalizationSettingsComponent.release()
    }
}