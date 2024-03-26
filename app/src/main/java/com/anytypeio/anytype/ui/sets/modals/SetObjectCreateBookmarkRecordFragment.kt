package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.text.InputType.TYPE_TEXT_VARIATION_URI
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.databinding.FragmentSetObjectCreateBookmarkRecordBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.ObjectSetCreateBookmarkRecordViewModel
import javax.inject.Inject

class SetObjectCreateBookmarkRecordFragment :
    SetObjectCreateRecordFragmentBase<FragmentSetObjectCreateBookmarkRecordBinding>() {

    override val textInputType: Int = TYPE_TEXT_VARIATION_URI
    override val textInputField: EditText
        get() = binding.textInputField
    override val button: TextView
        get() = binding.button

    @Inject
    lateinit var factory: ObjectSetCreateBookmarkRecordViewModel.Factory
    override val vm: ObjectSetCreateBookmarkRecordViewModel by viewModels { factory }


    override fun injectDependencies() {
        componentManager().objectSetCreateBookmarkRecordComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetCreateBookmarkRecordComponent.release()
    }

    override fun onButtonClicked() {
        vm.onButtonClicked(input = textInputField.text.toString())
    }

    override fun onKeyboardActionDone() {
        vm.onActionDone(input = textInputField.text.toString())
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetObjectCreateBookmarkRecordBinding = FragmentSetObjectCreateBookmarkRecordBinding.inflate(
            inflater, container, false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        focusSearchInput()
    }

    private fun focusSearchInput() {
        textInputField.apply {
            post {
                requestFocus()
                context.imm().showSoftInput(this, InputMethodManager.SHOW_FORCED)
            }
        }
    }
}