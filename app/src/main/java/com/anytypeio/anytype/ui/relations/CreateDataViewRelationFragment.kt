package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.features.relations.create.RelationFormatAdapter
import com.anytypeio.anytype.core_ui.reactive.editorActionEvents
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.CreateDataViewRelationViewModel
import com.anytypeio.anytype.presentation.relations.CreateDataViewRelationViewModelFactory
import kotlinx.android.synthetic.main.fragment_create_data_view_relation.*
import javax.inject.Inject

class CreateDataViewRelationFragment : BaseBottomSheetFragment() {

    private val actionHandler: (Int) -> Boolean = { action ->
        action == EditorInfo.IME_ACTION_DONE
    }

    private val formatAdapter by lazy {
        RelationFormatAdapter { vm.onFormatClicked(it) }
    }

    private val ctx: String get() = argString(CONTEXT_KEY)
    private val target: String get() = argString(TARGET_KEY)

    private val vm by viewModels<CreateDataViewRelationViewModel> { factory }

    @Inject
    lateinit var factory: CreateDataViewRelationViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_create_data_view_relation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        formatRecycler.adapter = formatAdapter
        lifecycleScope.subscribe(nameInputField.editorActionEvents(actionHandler)) {
            if (nameInputField.text.isNotEmpty()) {
                nameInputField.hideKeyboard()
                dispatchResult()
            } else {
                toast("Relation name is required")
            }
        }
    }

    private fun dispatchResult() {
        withParent<OnAddDataViewRelationRequestReceiver> {
            onAddDataViewRelationRequest(
                context = ctx,
                target = target,
                name = nameInputField.text.toString(),
                format = vm.formats.value.first { it.isSelected }.format
            )
        }
        dismiss()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lifecycleScope.subscribe(vm.formats) { formatAdapter.update(it) }
    }

    override fun injectDependencies() {
        componentManager().createDataViewRelationComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createDataViewRelationComponent.release()
    }

    interface OnAddDataViewRelationRequestReceiver {
        fun onAddDataViewRelationRequest(context: String, target: Id, name: String, format: Relation.Format)
    }

    companion object {
        fun new(ctx: Id, target: Id) = CreateDataViewRelationFragment().apply {
            arguments = bundleOf(CONTEXT_KEY to ctx, TARGET_KEY to target)
        }

        private const val CONTEXT_KEY = "arg.create-dv-relation.context"
        private const val TARGET_KEY = "arg.create-dv-relation.target"
    }
}