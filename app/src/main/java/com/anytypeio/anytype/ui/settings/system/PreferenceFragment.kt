package com.anytypeio.anytype.ui.settings.system

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.ui.editor.PickerDelegate

class PreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var pickerDelegate: PickerDelegate
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var filePathPreference: Preference
    private lateinit var networkModePreference: DropDownPreference

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        pickerDelegate = PickerDelegate.Impl(this) { actions ->
            when (actions) {
                is PickerDelegate.Actions.OnProceedWithFilePath -> {
                    sharedPreferences.edit {
                        putString(NETWORK_CONFIG_FILE_PATH_PREF, actions.filePath)
                        commit()
                    }
                    getCustomSettingsFromPrefs()
                }

                else -> {
                    //do nothing
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

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        networkModePreference = DropDownPreference(context).apply {
            key = NETWORK_MODE_PREF
            setDefaultValue(NETWORK_MODE_DEFAULT)
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            isSingleLineTitle = true
            isIconSpaceReserved = false
            title = getString(R.string.settings_network_mode)
            setEntries(R.array.settings_networks_entries)
            setEntryValues(R.array.settings_networks_entries_values)
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                setFilePathVisibility(newValue as String)
                sharedPreferences?.edit {
                    putString(NETWORK_MODE_PREF, newValue)
                    commit()
                }
                true
            }
        }
        screen.addPreference(networkModePreference)
        filePathPreference = Preference(context).apply {
            key = NETWORK_CONFIG_FILE_PATH_PREF
            isIconSpaceReserved = false
            title = getString(R.string.settings_network_configuration_file)
            isSingleLineTitle = true
        }
        filePathPreference.setOnPreferenceClickListener {
            openFilePicker()
            true
        }
        screen.addPreference(filePathPreference)
        preferenceScreen = screen

        setFilePathVisibility()
        getCustomSettingsFromPrefs()
    }

    private fun getCustomSettingsFromPrefs() {
        val filePath = sharedPreferences.getString(NETWORK_CONFIG_FILE_PATH_PREF, "")
        if (filePath.isNullOrBlank()) {
            filePathPreference.summary =
                getString(R.string.settings_network_configuration_file_choose)
        } else {
            filePathPreference.summary = filePath
        }
    }

    private fun openFilePicker() {
        pickerDelegate.openFilePicker(Mimetype.MIME_TEXT_PLAIN)
    }

    private fun setFilePathVisibility(newValue: String? = null) {
        when (newValue ?: sharedPreferences.getString(NETWORK_MODE_PREF, NETWORK_MODE_DEFAULT)) {
            NETWORK_MODE_LOCAL -> {
                filePathPreference.isVisible = false
            }
            NETWORK_MODE_DEFAULT -> {
                filePathPreference.isVisible = false
            }
            NETWORK_MODE_CUSTOM -> {
                filePathPreference.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        pickerDelegate.clearPickit()
        super.onDestroyView()
    }

    companion object {
        const val NETWORK_MODE_PREF = "pref.network_mode"
        const val NETWORK_CONFIG_FILE_PATH_PREF = "pref.network_config_file_path"

        const val NETWORK_MODE_LOCAL = "local"
        const val NETWORK_MODE_DEFAULT = "default"
        const val NETWORK_MODE_CUSTOM = "custom"
    }
}