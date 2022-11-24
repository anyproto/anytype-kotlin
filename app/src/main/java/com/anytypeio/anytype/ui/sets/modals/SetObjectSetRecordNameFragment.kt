package com.anytypeio.anytype.ui.sets.modals

import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.databinding.FragmentSetObjectSetRecordNameBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordViewModel.Command
import com.anytypeio.anytype.ui.editor.EditorFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetObjectSetRecordNameFragment : SetObjectCreateRecordFragmentBase<FragmentSetObjectSetRecordNameBinding>() {

    override val textInputType: Int = TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_SENTENCES or TYPE_TEXT_FLAG_AUTO_CORRECT
    override val textInputField: EditText
        get() = binding.textInputField
    override val button: AppCompatImageButton
        get() = binding.icExpand

    @Inject
    lateinit var factory: ObjectSetRecordViewModel.Factory
    override val vm: ObjectSetRecordViewModel by viewModels { factory }

    private val target: String get() = argString(TARGET_KEY)

    override fun onStart(scope: CoroutineScope) {
        super.onStart(scope)
        scope.launch { subscribeCommands() }
    }

    private suspend fun subscribeCommands() {
        vm.commands.collect { command ->
            when (command) {
                is Command.OpenObject -> {
                    findNavController().navigate(
                        R.id.objectNavigation,
                        bundleOf(EditorFragment.ID_KEY to command.ctx)
                    )
                }
            }
        }
    }

    override fun onButtonClicked() {
        vm.onButtonClicked(
            target = target,
            input = textInputField.text.toString()
        )
    }

    override fun onKeyboardActionDone() {
        vm.onActionDone(
            target = target,
            input = textInputField.text.toString()
        )
    }

    override fun injectDependencies() {
        componentManager().objectSetRecordComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetRecordComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetObjectSetRecordNameBinding = FragmentSetObjectSetRecordNameBinding.inflate(
        inflater, container, false
    )
}