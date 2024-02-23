package com.anytypeio.anytype.ui.onboarding

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.NetworkModeConstants
import com.anytypeio.anytype.core_utils.const.FileConstants
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.PreferencesViewModel
import com.anytypeio.anytype.ui.editor.PickerDelegate
import com.anytypeio.anytype.ui.onboarding.screens.signin.NetworkSetupScreen
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class OnboardingNetworkSetupDialog : BaseBottomSheetComposeFragment() {

    private lateinit var pickerDelegate: PickerDelegate

    @Inject
    lateinit var factory: PreferencesViewModel.Factory
    private val vm by viewModels<PreferencesViewModel> { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.NetworkSettingDialogTheme)
        setup()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =  ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                NetworkSetupScreen(
                    config = vm.networkModeState.collectAsStateWithLifecycle().value,
                    onSelfHostNetworkClicked = {
                        vm.proceedWithNetworkMode(NetworkModeConstants.NETWORK_MODE_CUSTOM)
                    },
                    onSetSelfHostConfigConfigClicked = {
                        proceedWithFilePicker()
                    },
                    onLocalOnlyClicked = {
                        vm.proceedWithNetworkMode(NetworkModeConstants.NETWORK_MODE_LOCAL)
                    },
                    onAnytypeNetworkClicked = {
                        vm.proceedWithNetworkMode(NetworkModeConstants.NETWORK_MODE_DEFAULT)
                    },
                    onUseYamuxToggled = vm::onChangeMultiplexLibrary
                )
            }
        }
    }

    private fun setup() {
        pickerDelegate = PickerDelegate.Impl(this) { actions ->
            when (actions) {
                is PickerDelegate.Actions.OnProceedWithFilePath -> {
                    vm.onProceedWithFilePath(actions.filePath)
                }
                PickerDelegate.Actions.OnCancelCopyFileToCacheDir -> {
                    vm.onCancelCopyFileToCacheDir()
                }
                is PickerDelegate.Actions.OnPickedDocImageFromDevice -> {
                    vm.onPickedDocImageFromDevice(actions.ctx, actions.filePath)
                }
                is PickerDelegate.Actions.OnStartCopyFileToCacheDir -> {
                    vm.onStartCopyFileToCacheDir(actions.uri)
                }
            }
        }
        pickerDelegate.initPicker("")
    }

    private fun proceedWithFilePicker() {
        pickerDelegate.openFilePicker(
            Mimetype.MIME_YAML,
            FileConstants.REQUEST_NETWORK_MODE_CODE
        )
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            pickerDelegate.resolveActivityResult(requestCode, resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pickerDelegate.clearPickit()
    }

    override fun injectDependencies() {
        componentManager().appPreferencesComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().appPreferencesComponent.release()
    }
}