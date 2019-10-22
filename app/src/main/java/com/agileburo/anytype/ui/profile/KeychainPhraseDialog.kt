package com.agileburo.anytype.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.keychain.KeychainPhraseViewModel
import com.agileburo.anytype.presentation.keychain.KeychainPhraseViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_keychain_phrase.*
import javax.inject.Inject

class KeychainPhraseDialog : BottomSheetDialogFragment(), Observer<ViewState<String>> {

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(KeychainPhraseViewModel::class.java)
    }

    @Inject
    lateinit var factory: KeychainPhraseViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_keychain_phrase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(this, this)
    }

    override fun onChanged(state: ViewState<String>) {
        when (state) {
            is ViewState.Success -> {
                keychain.text = state.data
            }
            is ViewState.Error -> {
                // TODO
            }
            is ViewState.Loading -> {
                // TODO
            }
        }
    }

    private fun init() {
        doneButton.setOnClickListener { dismiss() }
    }

    private fun injectDependencies() {
        componentManager().keychainPhraseComponent.get().inject(this)
    }

    private fun releaseDependencies() {
        componentManager().keychainPhraseComponent.release()
    }
}