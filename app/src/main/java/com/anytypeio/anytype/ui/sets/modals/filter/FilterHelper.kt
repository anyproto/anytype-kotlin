package com.anytypeio.anytype.ui.sets.modals.filter

import androidx.fragment.app.Fragment
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.presentation.relations.toName
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment

class FilterHelper {
    fun handleOpenNumberPicker(
        fragment: Fragment,
        command: FilterViewModel.Commands.OpenNumberPicker,
        ctx: String,
    ) {
        fragment.arguments?.apply {
            putSerializable(KEY_OPTION, command.option)
        }

        RelationTextValueFragment.new(
            ctx = ctx,
            name = command.option.toName(),
            value = command.value
        ).show(fragment.childFragmentManager, null)
    }

    fun handleNumberValueChanged(
        fragment: Fragment,
        number: Double?,
        vm: FilterViewModel
    ) {
        number?.let {
            fragment.arguments
                ?.let { it.getSerializable(KEY_OPTION) as? DVFilterQuickOption? }
                ?.let {
                    vm.onExactNumberOfDays(it, number.toLong())
                }
        }
    }
}

private const val KEY_OPTION = "arg.filter-helper.option"