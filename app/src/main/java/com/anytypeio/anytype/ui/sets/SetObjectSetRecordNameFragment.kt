package com.anytypeio.anytype.ui.sets

import android.os.Bundle
import android.text.InputType.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.reactive.editorActionEvents
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentSetObjectSetRecordNameBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordViewModel.Command
import com.anytypeio.anytype.ui.editor.EditorFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetObjectSetRecordNameFragment : BaseBottomSheetFragment<FragmentSetObjectSetRecordNameBinding>() {

    private val ctx: String get() = argString(CONTEXT_KEY)

    @Inject
    lateinit var factory: ObjectSetRecordViewModel.Factory
    private val vm: ObjectSetRecordViewModel by viewModels { factory }

    private val handler: (Int) -> Boolean = { action ->
        action == EditorInfo.IME_ACTION_DONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textInputField.apply {
            setRawInputType(TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_SENTENCES or TYPE_TEXT_FLAG_AUTO_CORRECT)
        }
        binding.icExpand.setOnClickListener {
            vm.onExpandButtonClicked(
                ctx = ctx,
                input = binding.textInputField.text.toString()
            )
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { subscribeCommands() }
                launch { subscribeTextInputActions() }
                launch { subscribeIsCompleted() }
            }
        }
    }

    private suspend fun subscribeTextInputActions() {
        binding.textInputField.editorActionEvents(handler).collect {
            binding.textInputField.clearFocus()
            binding.textInputField.hideKeyboard()
            vm.onComplete(ctx, binding.textInputField.text.toString())
        }
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

    private suspend fun subscribeIsCompleted() {
        vm.isCompleted.collect { isCompleted -> if (isCompleted) dismiss() }
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

    companion object {
        const val CONTEXT_KEY = "arg.object-set-record.context"
    }
}