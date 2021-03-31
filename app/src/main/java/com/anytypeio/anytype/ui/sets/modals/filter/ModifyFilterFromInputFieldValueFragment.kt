package com.anytypeio.anytype.ui.sets.modals.filter

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.relationIcon
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.disable
import com.anytypeio.anytype.core_utils.ext.enable
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.extension.getTextValue
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.sets.modals.PickFilterConditionFragment
import kotlinx.android.synthetic.main.fragment_create_or_update_filter_input_field_value.*
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

open class ModifyFilterFromInputFieldValueFragment : BaseBottomSheetFragment(), UpdateConditionActionReceiver {

    private val ctx: String get() = arg(CTX_KEY)
    private val relation: String get() = arg(RELATION_KEY)
    private val index: Int get() = arg(IDX_KEY)

    @Inject
    lateinit var factory: FilterViewModel.Factory

    private val vm: FilterViewModel by viewModels { factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_or_update_filter_input_field_value, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnBottomAction.setText(R.string.apply)
        with(lifecycleScope) {
            subscribe(btnBottomAction.clicks()) {
                vm.onModifyApplyClicked(
                    ctx = ctx,
                    input = enterTextValueInputField.text.toString()
                )
            }
            subscribe(tvFilterCondition.clicks()) {
                vm.onConditionClicked()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.relationState.filterNotNull()) {
                tvRelationName.text = it.title
                ivRelationIcon.setImageResource(it.format.relationIcon(true))
                setupFormat(it.format)
            }
            subscribe(vm.filterValueState) { value ->
                enterTextValueInputField.setText(value.getTextValue())
            }
            subscribe(vm.isCompleted) { isCompleted ->
                if (isCompleted) dismiss()
            }
            subscribe(vm.conditionState) {
                tvFilterCondition.text = it?.condition?.title
                if (it?.isFilterValueEnabled == true) {
                    enterTextValueInputField.enable()
                } else {
                    enterTextValueInputField.text = null
                    enterTextValueInputField.disable()
                }
            }
            subscribe(vm.commands) { observeCommands(it) }
        }
    }

    private fun observeCommands(commands: FilterViewModel.Commands) {
        when (commands) {
            is FilterViewModel.Commands.OpenDatePicker -> {
            }
            is FilterViewModel.Commands.OpenConditionPicker -> {
                PickFilterConditionFragment.new(
                    ctx = ctx,
                    mode = PickFilterConditionFragment.MODE_MODIFY,
                    type = commands.type,
                    index = commands.index
                ).show(childFragmentManager, null)
            }
        }
    }

    private fun setupFormat(format: ColumnView.Format) {
        val type = when (format) {
            ColumnView.Format.NUMBER -> InputType.TYPE_CLASS_NUMBER
            ColumnView.Format.URL -> InputType.TYPE_TEXT_VARIATION_URI
            ColumnView.Format.EMAIL -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            ColumnView.Format.PHONE -> InputType.TYPE_CLASS_PHONE
            else -> InputType.TYPE_CLASS_TEXT
        }
        enterTextValueInputField.inputType = type
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(relation, index)
    }

    override fun update(condition: Viewer.Filter.Condition) {
        vm.onConditionUpdate(condition)
    }

    override fun injectDependencies() {
        componentManager().modifyFilterComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().modifyFilterComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, relation: Id, index: Int) = ModifyFilterFromInputFieldValueFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, RELATION_KEY to relation, IDX_KEY to index)
        }

        const val CTX_KEY = "arg.modify-filter-relation.ctx"
        const val RELATION_KEY = "arg.modify-filter-relation.relation"
        const val IDX_KEY = "arg.modify-filter-relation.index"
    }
}