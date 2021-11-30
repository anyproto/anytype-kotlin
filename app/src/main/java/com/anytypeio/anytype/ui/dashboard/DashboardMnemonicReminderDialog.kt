package com.anytypeio.anytype.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BlurMaskFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModel
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_keychain_phrase.*
import javax.inject.Inject

class DashboardMnemonicReminderDialog : BottomSheetDialogFragment(), Observer<ViewState<String>> {

    private val vm : KeychainPhraseViewModel by viewModels { factory }

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
    ): View? = inflater.inflate(R.layout.dialog_dashboard_keychain_phrase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBlur()
        keychain.setOnClickListener {
            if (keychain.layerType == View.LAYER_TYPE_SOFTWARE) {
                removeBlur()
            }
        }
        btnCopy.setOnClickListener {
            copyMnemonicToClipboard()
        }
        root.setOnClickListener {
            setBlur()
        }

        vm.state.observe(viewLifecycleOwner, this)
    }

    private fun copyMnemonicToClipboard() {
        try {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(MNEMONIC_LABEL, keychain.text.toString())
            clipboard.setPrimaryClip(clip)
            toast("Mnemonic copied to clipboard.")
        } catch (e: Exception) {
            toast("Could not copy your mnemonic. Please try again later, or copy it manually.")
        }
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

    private fun setBlur() = with(keychain) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val radius = textSize / 3
        val filter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        paint.maskFilter = filter
    }

    private fun removeBlur() = with(keychain) {
        setLayerType(View.LAYER_TYPE_NONE, null)
        paint.maskFilter = null
        isFocusable = true
        setTextIsSelectable(true)
    }

    private fun injectDependencies() {
        componentManager().keychainPhraseComponent.get().inject(this)
    }

    private fun releaseDependencies() {
        componentManager().keychainPhraseComponent.release()
    }

    companion object {
        const val MNEMONIC_LABEL = "Your Anytype mnemonic phrase"
    }
}