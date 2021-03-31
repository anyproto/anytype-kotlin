package com.anytypeio.anytype.ui.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.reactive.editorActionEvents
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordViewModel
import kotlinx.android.synthetic.main.fragment_set_object_set_record_name.*
import javax.inject.Inject

class SetObjectSetRecordNameFragment : BaseBottomSheetFragment() {

    private val ctx: String get() = argString(CONTEXT_KEY)

    @Inject
    lateinit var factory: ObjectSetRecordViewModel.Factory
    private val vm: ObjectSetRecordViewModel by viewModels { factory }

    private val handler: (Int) -> Boolean = { action ->
        action == EditorInfo.IME_ACTION_DONE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_set_object_set_record_name, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.subscribe(textInputField.editorActionEvents(handler)) {
            textInputField.clearFocus()
            textInputField.hideKeyboard()
            vm.onComplete(ctx, textInputField.text.toString())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.subscribe(vm.isCompleted) { isCompleted -> if (isCompleted) dismiss() }
    }

    override fun injectDependencies() {
        componentManager().objectSetRecordComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSetRecordComponent.release(ctx)
    }

    companion object {
        const val CONTEXT_KEY = "arg.object-set-record.context"

        fun new(ctx: Id) = SetObjectSetRecordNameFragment().apply {
            arguments = bundleOf(CONTEXT_KEY to ctx)
        }
    }
}