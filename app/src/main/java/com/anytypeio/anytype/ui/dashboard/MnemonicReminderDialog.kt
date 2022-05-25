package com.anytypeio.anytype.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.AlertMnemonicReminderBinding

class MnemonicReminderDialog : BaseBottomSheetFragment<AlertMnemonicReminderBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSettings.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.keychainDialog)
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AlertMnemonicReminderBinding = AlertMnemonicReminderBinding.inflate(
        inflater, container, false
    )
}