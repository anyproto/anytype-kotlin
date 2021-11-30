package com.anytypeio.anytype.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import kotlinx.android.synthetic.main.alert_mnemonic_reminder.*

class MnemonicReminderDialog : BaseBottomSheetFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? = inflater.inflate(R.layout.alert_mnemonic_reminder, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnClose.setOnClickListener { dismiss() }
        btnSettings.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.keychainDialog)
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}