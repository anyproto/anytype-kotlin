package com.anytypeio.anytype.ui.sets.modals.filter

import androidx.fragment.app.Fragment
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.hasSpan
import com.anytypeio.anytype.localization.R
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import timber.log.Timber

class FilterHelper {
    fun handleOpenNumberPicker(
        fragment: Fragment,
        command: FilterViewModel.Commands.OpenNumberPicker,
        ctx: Id,
        space: Id,
    ) {
        fragment.arguments?.apply {
            putSerializable(KEY_OPTION, command.option)
        }
        runCatching {
            val name = when (command.option) {
                DVFilterQuickOption.DAYS_AGO -> fragment.getString(R.string.number_days_ago)
                DVFilterQuickOption.DAYS_AHEAD -> fragment.getString(R.string.number_days_from)
                else -> ""
            }
            val fm = fragment.childFragmentManager
            if (fm.findFragmentByTag(RELATION_TEXT_VALUE_TAG) == null) {
                RelationTextValueFragment.new(
                    ctx = ctx,
                    space = space,
                    name = name,
                    value = command.value
                ).show(fm, RELATION_TEXT_VALUE_TAG)
            }
        }.onFailure {
            Timber.e(it, "Error while navigation")
        }
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
private const val RELATION_TEXT_VALUE_TAG = "tag.relation_text_value"