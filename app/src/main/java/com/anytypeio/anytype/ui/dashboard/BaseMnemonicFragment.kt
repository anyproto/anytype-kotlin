package com.anytypeio.anytype.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModel
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModelFactory
import com.anytypeio.anytype.presentation.keychain.KeychainViewState
import com.anytypeio.anytype.ui.profile.RoundedBackgroundColorSpan
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseMnemonicFragment<T : ViewBinding> : BaseBottomSheetFragment<T>() {

    protected val vm: KeychainPhraseViewModel by viewModels { factory }

    private val fakePhrase by lazy {
        val bg = RoundedBackgroundColorSpan(
            requireContext().getColor(R.color.palette_light_ice),
            dimen(R.dimen.shimmer_radius).toFloat(),
            dimen(R.dimen.shimmer_radius).toFloat(),
            dimen(R.dimen.shimmer_line_height).toFloat()
        )
        buildSpannedString { inSpans(bg) { append(SHIMMER_PHRASE) } }
    }

    protected abstract val keychain: TextView
    protected abstract val anchor: View
    protected abstract val btnCopy: TextView

    @Inject
    lateinit var factory: KeychainPhraseViewModelFactory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keychain.clicks().onEach { vm.onKeychainClicked() }.launchIn(lifecycleScope)
        btnCopy.clicks().onEach { vm.onKeychainClicked() }.launchIn(lifecycleScope)
        binding.root.clicks().onEach { vm.onRootClicked() }.launchIn(lifecycleScope)

        vm.state.observe(viewLifecycleOwner) { render(it) }
    }

    private fun render(state: KeychainViewState) {
        when (state) {
            is KeychainViewState.Displayed -> {
                setKeychainPhrase(state.mnemonic)
                copyMnemonicToClipboard(state.mnemonic)
            }
            is KeychainViewState.Blurred -> {
                setBlurredPhrase()
            }
        }
    }

    private fun setupFullHeight() {
        binding.apply { root.layoutParams.height = resources.displayMetrics.heightPixels }

        val bottomSheetDialog = dialog as BottomSheetDialog
        val behavior = bottomSheetDialog.behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun animateBlurChange() {
        val transitionSet = TransitionSet().apply {
            addTransition(ChangeBounds())
            duration = ANIMATION_LENGTH
            interpolator = DecelerateInterpolator(ANIMATION_FACTOR)
            ordering = TransitionSet.ORDERING_TOGETHER
        }
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transitionSet)
    }

    private fun setBlurredPhrase() {
        keychain
        animateBlurChange()
        keychain.setShadowLayer(0f, 0f, 0f, 0)
        keychain.setTextColor(requireContext().getColor(R.color.palette_light_ice))
        keychain.setShadowLayer(dimen(R.dimen.shimmer_radius).toFloat(), 0f, 0f, 0)
        keychain.text = fakePhrase
    }

    private fun setKeychainPhrase(mnemonic: String) {
        animateBlurChange()
        keychain.setTextColor(requireContext().getColor(R.color.keychain_text_color))
        keychain.setShadowLayer(0f, 0f, 0f, 0)
        keychain.text = mnemonic
    }

    private fun copyMnemonicToClipboard(keychainPhrase: String) {
        try {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                DashboardMnemonicReminderDialog.MNEMONIC_LABEL,
                keychainPhrase
            )
            clipboard.setPrimaryClip(clip)
            toast(getString(R.string.recovery_phrase_copied))
        } catch (e: Exception) {
            toast("Could not copy your recovery phrase. Please try again later, or copy it manually.")
        }
    }
}

private const val SHIMMER_LENGTH = 50
private val SHIMMER_PHRASE = "- ".repeat(SHIMMER_LENGTH)
private const val ANIMATION_LENGTH = 700L
private const val ANIMATION_FACTOR = 2.5f