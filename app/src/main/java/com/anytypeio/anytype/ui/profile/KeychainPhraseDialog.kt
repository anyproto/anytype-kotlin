package com.anytypeio.anytype.ui.profile

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
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.databinding.DialogKeychainPhraseBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModel
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModelFactory
import javax.inject.Inject

class KeychainPhraseDialog : BaseBottomSheetFragment<DialogKeychainPhraseBinding>(), Observer<ViewState<String>> {

    private val vm : KeychainPhraseViewModel by viewModels { factory }

    @Inject
    lateinit var factory: KeychainPhraseViewModelFactory

    private val screenType get() = arg<String>(ARG_SCREEN_TYPE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.sendShowEvent(screenType)
        setBlur()
        binding.keychain.setOnClickListener {
            if (binding.keychain.layerType == View.LAYER_TYPE_SOFTWARE) {
                removeBlur()
            }
        }
        binding.btnCopy.setOnClickListener {
            vm.onCopyClickedFromLogout()
            copyMnemonicToClipboard()
        }
        binding.root.setOnClickListener {
            setBlur()
        }
    }

    private fun copyMnemonicToClipboard() {
        try {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(MNEMONIC_LABEL, binding.keychain.text.toString())
            clipboard.setPrimaryClip(clip)
            toast("Mnemonic copied to clipboard.")
        } catch (e: Exception) {
            toast("Could not copy your mnemonic. Please try again later, or copy it manually.")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
    }

    override fun onChanged(state: ViewState<String>) {
        when (state) {
            is ViewState.Success -> {
                binding.keychain.text = state.data
            }
            is ViewState.Error -> {
                // TODO
            }
            is ViewState.Loading -> {
                // TODO
            }
            ViewState.Init -> {
                // Do nothing
            }
        }
    }

    private fun setBlur() = with(binding.keychain) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val radius = textSize / 3
        val filter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        paint.maskFilter = filter
    }

    private fun removeBlur() = with(binding.keychain) {
        setLayerType(View.LAYER_TYPE_NONE, null)
        paint.maskFilter = null
        isFocusable = true
        setTextIsSelectable(true)
    }

    override fun injectDependencies() {
        componentManager().keychainPhraseComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().keychainPhraseComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogKeychainPhraseBinding = DialogKeychainPhraseBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val MNEMONIC_LABEL = "Your Anytype mnemonic phrase"
        const val ARG_SCREEN_TYPE = "arg.keychain.screen.type"
    }
}