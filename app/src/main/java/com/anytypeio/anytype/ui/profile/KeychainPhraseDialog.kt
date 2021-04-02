package com.anytypeio.anytype.ui.profile

import android.graphics.BlurMaskFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModel
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_keychain_phrase.*
import javax.inject.Inject

class KeychainPhraseDialog : BottomSheetDialogFragment(), Observer<ViewState<String>> {

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
    ): View? = inflater.inflate(R.layout.dialog_keychain_phrase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBlur()
        keychain.setOnClickListener {
            if (keychain.layerType == View.LAYER_TYPE_SOFTWARE) {
                removeBlur()
            }
        }
        root.setOnClickListener {
            setBlur()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
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
}