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
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.OtherSettingsViewModel
import com.anytypeio.anytype.ui.objects.ObjectTypeChangeFragment
import kotlinx.android.synthetic.main.fragment_user_settings.*
import javax.inject.Inject

class OtherSettingsFragment : BaseBottomSheetFragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_user_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvDefaultObjectTypeTitle.setOnClickListener { vm.onObjectTypeClicked() }
        btnDefaultObjectType.setOnClickListener { vm.onObjectTypeClicked() }
        btnClearFileCache.setOnClickListener { vm.onClearFileCacheClicked() }
        ivArrowForward.setOnClickListener { vm.onObjectTypeClicked() }
    }

    override fun onStart() {
        super.onStart()
        with(lifecycleScope) {
            jobs += subscribe(vm.commands) { observe(it) }
            jobs += subscribe(vm.isClearFileCacheInProgress) { isInProgress ->
                if (isInProgress)
                    clearFileCacheProgressBar.visible()
                else
                    clearFileCacheProgressBar.gone()
            }
        }
    }

    private fun observe(command: OtherSettingsViewModel.Command) {
        when (command) {
            is OtherSettingsViewModel.Command.Exit -> dismiss()
            is OtherSettingsViewModel.Command.NavigateToObjectTypesScreen -> {
                findNavController().navigate(
                    R.id.objectTypeChangeFragment,
                    bundleOf(
                        ObjectTypeChangeFragment.ARG_SMART_BLOCK_TYPE to command.smartBlockType
                    )
                )
            }
            is OtherSettingsViewModel.Command.SetObjectType -> {
                objectType.text = command.name
            }
            is OtherSettingsViewModel.Command.Toast -> toast(command.msg)
        }
    }

    override fun injectDependencies() {
        componentManager().otherSettingsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().otherSettingsComponent.release()
    }
}