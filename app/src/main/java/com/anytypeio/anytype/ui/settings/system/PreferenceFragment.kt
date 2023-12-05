package com.anytypeio.anytype.ui.settings.system

import android.os.Bundle
import android.util.Log
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.persistence.repo.DefaultUserSettingsCache

class PreferenceFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        getSharedPrefs()
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
        val singleLinePreference = Preference(context).apply {
            title = "Network file"
            isSingleLineTitle = true
        }
        screen.addPreference(singleLinePreference)
        preferenceScreen = screen

        //setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    private fun getSharedPrefs() {
        val activityContext = activity
        if (activityContext != null) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activityContext)
            val filePath = sharedPreferences.getString(NETWORK_CONFIG_FILE_PATH, "")
            Log.d("Test1983", "Network file path : $filePath")
        }
    }

    companion object {
        const val NETWORK_CONFIG_FILE_PATH = "network_config_file_path"
    }
}