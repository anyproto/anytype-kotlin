package com.anytypeio.anytype.ui.types.picker

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.setVisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.types.icon_picker.TypeIconPickerViewModel
import com.anytypeio.anytype.ui.editor.modals.IconPickerFragmentBase
import javax.inject.Inject

class TypeIconPickFragment : IconPickerFragmentBase<Unit>() {

    override val target: Unit = Unit

    @Inject
    lateinit var factory: TypeIconPickerViewModel.Factory
    override val vm by viewModels<TypeIconPickerViewModel> { factory }

    private val showRemoveButton
        get() = argBoolean(ARG_SHOW_REMOVE_BUTTON)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomToolbar.gone()
        binding.btnRemoveIcon.setVisible(showRemoveButton)
        subscribe(vm.actions) {
            when (it) {
                is TypeIconPickerViewModel.EmojiPickerAction.RemoveEmoji -> {
                    setFragmentResult(
                        requestKey = REQUEST_KEY_REMOVE_EMOJI,
                        result = bundleOf()
                    )
                    findNavController().popBackStack()
                }
                is TypeIconPickerViewModel.EmojiPickerAction.SetEmoji -> {
                    setFragmentResult(
                        requestKey = REQUEST_KEY_PICK_EMOJI,
                        result = bundleOf(
                            RESULT_EMOJI_UNICODE to it.emojiUnicode
                        )
                    )
                    findNavController().popBackStack()
                }
                is TypeIconPickerViewModel.EmojiPickerAction.Idle -> {
                    // do nothing
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().typeIconPickComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().typeIconPickComponent.release()
    }

}

const val REQUEST_KEY_PICK_EMOJI = "request_key.pick_emoji"
const val REQUEST_KEY_REMOVE_EMOJI = "request_key.remove_emoji"
const val RESULT_EMOJI_UNICODE = "result.emoji_unicode"
private const val ARG_SHOW_REMOVE_BUTTON = "arg.type_show_remove"