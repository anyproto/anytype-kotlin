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
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.databinding.DialogDashboardKeychainPhraseBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModel
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class DashboardMnemonicReminderDialog : BottomSheetDialogFragment(), Observer<ViewState<String>> {

    private var _binding: DialogDashboardKeychainPhraseBinding? = null
    private val binding: DialogDashboardKeychainPhraseBinding get() = _binding!!

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
    ): View? {
        _binding = DialogDashboardKeychainPhraseBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBlur()
        binding.keychain.setOnClickListener {
            if (binding.keychain.layerType == View.LAYER_TYPE_SOFTWARE) {
                removeBlur()
            }
        }
        binding.btnCopy.setOnClickListener {
            copyMnemonicToClipboard()
        }
        binding.root.setOnClickListener {
            setBlur()
        }

        vm.state.observe(viewLifecycleOwner, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            is ViewState.Init -> {

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