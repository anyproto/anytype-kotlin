package com.anytypeio.anytype.ui.settings.system

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_CUSTOM
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_DEFAULT
import com.anytypeio.anytype.core_models.NetworkModeConstants.NETWORK_MODE_LOCAL
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_NETWORK_MODE_CODE
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.presentation.settings.PreferencesViewModel
import com.anytypeio.anytype.ui.editor.PickerDelegate
import javax.inject.Inject

class PreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var pickerDelegate: PickerDelegate
    private lateinit var filePathPreference: Preference
    private lateinit var networkModePreference: DropDownPreference
    private lateinit var useYamuxPreference: SwitchPreference

    @Inject
    lateinit var factory: PreferencesViewModel.Factory
    private val vm by viewModels<PreferencesViewModel> { factory }

    override fun onAttach(context: Context) {
        injectDependencies()
        super.onAttach(context)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            pickerDelegate.resolveActivityResult(requestCode, resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onStart() {
        super.onStart()
        with(lifecycleScope) {
            subscribe(vm.networkModeState) { state ->
                when (state.networkMode) {
                    NetworkMode.DEFAULT -> {
                        networkModePreference.value = NETWORK_MODE_DEFAULT
                        filePathPreference.isVisible = false
                    }
                    NetworkMode.LOCAL -> {
                        networkModePreference.value = NETWORK_MODE_LOCAL
                        filePathPreference.isVisible = false
                    }
                    NetworkMode.CUSTOM -> {
                        networkModePreference.value = NETWORK_MODE_CUSTOM
                        filePathPreference.isVisible = true
                        filePathPreference.summary = state.userFilePath
                    }
                }
            }
        }
        with(lifecycleScope) {
            subscribe(vm.reserveMultiplexSetting) { state ->
                useYamuxPreference.isChecked = state
            }
        }
        vm.onStart()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        networkModePreference = DropDownPreference(context).apply {
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            isSingleLineTitle = true
            isIconSpaceReserved = false
            title = getString(R.string.settings_network_mode)
            setEntries(R.array.settings_networks_entries)
            setEntryValues(R.array.settings_networks_entries_values)
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                vm.proceedWithNetworkMode(newValue as String)
                true
            }
        }
        screen.addPreference(networkModePreference)
        filePathPreference = Preference(context).apply {
            isIconSpaceReserved = false
            title = getString(R.string.settings_network_configuration_file)
            isSingleLineTitle = true
            summary = getString(R.string.settings_network_configuration_file_choose)
        }
        filePathPreference.setOnPreferenceClickListener {
            openFilePicker()
            true
        }
        screen.addPreference(filePathPreference)
        useYamuxPreference = SwitchPreference(context).apply {
            title = getString(R.string.settings_use_yamux)
            isIconSpaceReserved = false
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                vm.onChangeMultiplexLibrary(newValue as Boolean)
                true
            }
        }
        screen.addPreference(useYamuxPreference)
        preferenceScreen = screen
    }

    private fun openFilePicker() {
        pickerDelegate.openFilePicker(Mimetype.MIME_YAML, REQUEST_NETWORK_MODE_CODE)
    }

    override fun onDestroyView() {
        pickerDelegate.clearPickit()
        super.onDestroyView()
    }

    override fun onDestroy() {
        releaseDependencies()
        super.onDestroy()
    }

    private fun injectDependencies() {
        componentManager().appPreferencesComponent.get().inject(this)
    }

    private fun releaseDependencies() {
        componentManager().appPreferencesComponent.release()
    }
}