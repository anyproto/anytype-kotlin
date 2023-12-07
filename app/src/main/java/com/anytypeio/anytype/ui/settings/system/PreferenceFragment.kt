package com.anytypeio.anytype.ui.settings.system

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.anytypeio.anytype.R

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}