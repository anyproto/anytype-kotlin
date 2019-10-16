package com.agileburo.anytype.feature_profile.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agileburo.anytype.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class KeychainPhraseDialog : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(): KeychainPhraseDialog = KeychainPhraseDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_keychain_phrase, container, false)
}