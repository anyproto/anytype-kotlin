package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentRelationDateValueBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.DateValueCommand
import com.anytypeio.anytype.presentation.sets.DateValueView
import com.anytypeio.anytype.presentation.sets.RelationDateValueViewModel
import com.anytypeio.anytype.ui.sets.modals.DatePickerFragment
import javax.inject.Inject

open class RelationDateValueFragment : BaseBottomSheetFragment<FragmentRelationDateValueBinding>(),
    DatePickerFragment.DatePickerReceiver {

    @Inject
    lateinit var factory: RelationDateValueViewModel.Factory
    val vm: RelationDateValueViewModel by viewModels { factory }

    private val ctx get() = argString(CONTEXT_ID)
    private val objectId get() = argString(OBJECT_ID)
    private val relationKey get() = argString(RELATION_KEY)
    private val flow get() = arg<Int>(FLOW_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparentBackground()
        with(binding) {
            btnBottomAction.setOnClickListener { vm.onActionClicked() }
            tvNoDate.setOnClickListener { vm.onNoDateClicked() }
            ivExactDayCheck.setOnClickListener { vm.onExactDayClicked() }
            tvExactDay.setOnClickListener { vm.onExactDayClicked() }
            tvDate.setOnClickListener { vm.onExactDayClicked() }
            tvToday.setOnClickListener { vm.onTodayClicked() }
            ivTodayCheck.setOnClickListener { vm.onTodayClicked() }
            tvTomorrow.setOnClickListener { vm.onTomorrowClicked() }
            ivTomorrowCheck.setOnClickListener { vm.onTomorrowClicked() }
            tvYesterday.setOnClickListener { vm.onYesterdayClicked() }
            ivYesterdayCheck.setOnClickListener { vm.onYesterdayClicked() }
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.views) { observeState(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        super.onStart()
        vm.onStart(ctx = ctx, objectId = objectId, relationKey = relationKey)
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    private fun observeState(state: DateValueView) {
        with(binding) {
            tvRelationHeader.text = state.title
            ivNoDateCheck.gone()
            ivTodayCheck.gone()
            ivYesterdayCheck.gone()
            ivTomorrowCheck.gone()
            ivExactDayCheck.gone()
            tvDate.text = null
            if (state.isToday) {
                ivTodayCheck.visible()
            }
            if (state.isYesterday) {
                ivYesterdayCheck.visible()
            }
            if (state.isTomorrow) {
                ivTomorrowCheck.visible()
            }
            if (state.exactDayFormat != null) {
                tvDate.text = state.exactDayFormat
                ivExactDayCheck.visible()
            }
            if (state.timeInSeconds == null) {
                ivNoDateCheck.visible()
            }
        }
    }

    private fun observeCommands(command: DateValueCommand) {
        when (command) {
            is DateValueCommand.DispatchResult -> {
                dispatchResultAndDismiss(command.timeInSeconds)
            }
            is DateValueCommand.OpenDatePicker -> {
                DatePickerFragment.new(command.timeInSeconds)
                    .showChildFragment()
            }
        }
    }

    private fun dispatchResultAndDismiss(timeInSeconds: Double?) {
        withParent<DateValueEditReceiver> {
            onDateValueChanged(
                ctx = ctx,
                objectId = objectId,
                relationKey = relationKey,
                timeInSeconds = timeInSeconds
            )
        }
        dismiss()
    }

    private fun setTransparentBackground() {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onPickDate(timeInSeconds: Long) {
        vm.setDate(timeInSeconds)
    }

    override fun injectDependencies() {
        when (flow) {
            FLOW_DV -> {
                componentManager().dataViewRelationDateValueComponent.get(ctx).inject(this)
            }
            FLOW_SET_OR_COLLECTION -> {
                componentManager().setOrCollectionRelationDateValueComponent.get(ctx).inject(this)
            }
            else -> {
                componentManager().objectRelationDateValueComponent.get(ctx).inject(this)
            }
        }
    }

    override fun releaseDependencies() {
        when (flow) {
            FLOW_DV -> {
                componentManager().dataViewRelationDateValueComponent.release(ctx)
            }
            FLOW_SET_OR_COLLECTION -> {
                componentManager().setOrCollectionRelationDateValueComponent.release(ctx)
            }
            else -> {
                componentManager().objectRelationDateValueComponent.release(ctx)
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationDateValueBinding = FragmentRelationDateValueBinding.inflate(
        inflater, container, false
    )

    companion object {

        fun new(
            ctx: Id,
            relationKey: Key,
            objectId: Id,
            flow: Int = FLOW_DEFAULT
        ) = RelationDateValueFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID to ctx,
                RELATION_KEY to relationKey,
                OBJECT_ID to objectId,
                FLOW_KEY to flow
            )
        }

        const val CONTEXT_ID = "arg.relation.date.context"
        const val RELATION_KEY = "arg.relation.date.relation.key"
        const val OBJECT_ID = "arg.relation.date.object.id"

        const val FLOW_KEY = "arg.relation.date.flow"
        const val FLOW_DEFAULT = 0
        const val FLOW_DV = 1
        const val FLOW_SET_OR_COLLECTION = 2
    }

    interface DateValueEditReceiver {
        fun onDateValueChanged(
            ctx: Id,
            timeInSeconds: Number?,
            objectId: Id,
            relationKey: Key
        )
    }
}