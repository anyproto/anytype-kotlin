package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.core_ui.reactive.editorActionEvents
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.presentation.sets.ObjectSetCreateRecordViewModelBase
import com.anytypeio.anytype.ui.sets.modals.SetObjectSetRecordNameFragment.Companion.CONTEXT_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class SetObjectCreateRecordFragmentBase<T: ViewBinding> :
    BaseBottomSheetFragment<T>() {

    protected abstract val textInputType: Int
    private val ctx: String get() = argString(CONTEXT_KEY)
    protected abstract val textInputField: EditText
    protected abstract val button: View

    protected abstract val vm: ObjectSetCreateRecordViewModelBase

    private val handler: (Int) -> Boolean = { action ->
        action == EditorInfo.IME_ACTION_DONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textInputField.apply {
            setRawInputType(textInputType)
        }
        button.setOnClickListener {
            vm.onButtonClicked(
                ctx = ctx,
                input = textInputField.text.toString()
            )
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                onStart(this)
            }
        }
    }

    @CallSuper
    protected open fun onStart(scope: CoroutineScope) {
        with(scope) {
            launch { subscribeTextInputActions() }
            launch { subscribeIsCompleted() }
            launch { subscribeToasts() }
        }
    }

    private suspend fun subscribeToasts() {
        vm.toasts.collect { toast(it) }
    }

    private suspend fun subscribeTextInputActions() {
        textInputField.editorActionEvents(handler).collect {
            textInputField.clearFocus()
            textInputField.hideKeyboard()
            vm.onComplete(ctx, textInputField.text.toString())
        }
    }

    private suspend fun subscribeIsCompleted() {
        vm.isCompleted.collect { isCompleted -> if (isCompleted) dismiss() }
    }
}