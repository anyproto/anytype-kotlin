package com.anytypeio.anytype.ui.settings.system

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.DropDownPreference
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        pickerDelegate = PickerDelegate.Impl(this) { actions ->
            when (actions) {
                is PickerDelegate.Actions.OnProceedWithFilePath -> {
                    sharedPreferences.edit {
                        putString(NETWORK_CONFIG_FILE_PATH, actions.filePath)
                        commit()
                    }
                    setFilePathPreference()
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
        val dropDownPreference = DropDownPreference(context)
        dropDownPreference.apply {
            key = "key"
            title = getString(R.string.settings_network_mode)
            setEntries(R.array.settings_networks)
            setEntryValues(R.array.settings_networks)
        }
        screen.addPreference(dropDownPreference)
        filePathPreference = Preference(context).apply {
            title = "Network file"
            isSingleLineTitle = true
        }
        setFilePathPreference()
        filePathPreference.setOnPreferenceClickListener {
            openFilePicker()
            true
        }
        screen.addPreference(filePathPreference)
        preferenceScreen = screen
    }

    private fun setFilePathPreference() {
        filePathPreference.summary = sharedPreferences.getString(NETWORK_CONFIG_FILE_PATH, "")
    }

    private fun openFilePicker() {
        pickerDelegate.openFilePicker(Mimetype.MIME_TEXT_PLAIN)
    }

    override fun onDestroyView() {
        pickerDelegate.clearPickit()
        super.onDestroyView()
    }

    companion object {
        const val NETWORK_CONFIG_FILE_PATH = "network_config_file_path"
    }
}