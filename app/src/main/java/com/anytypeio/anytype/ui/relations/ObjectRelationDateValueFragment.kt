package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.DateValueCommand
import com.anytypeio.anytype.presentation.sets.DateValueView
import com.anytypeio.anytype.presentation.sets.ObjectRelationDateValueViewModel
import com.anytypeio.anytype.ui.sets.modals.DatePickerFragment
import kotlinx.android.synthetic.main.fragment_object_relation_date_value.*
import javax.inject.Inject

open class ObjectRelationDateValueFragment : BaseBottomSheetFragment(),
    DatePickerFragment.DatePickerReceiver {

    @Inject
    lateinit var factory: ObjectRelationDateValueViewModel.Factory
    val vm: ObjectRelationDateValueViewModel by viewModels { factory }

    private val ctx get() = argString(CONTEXT_ID)
    private val objectId get() = argString(OBJECT_ID)
    private val relationId get() = argString(RELATION_ID)
    private val flow get() = arg<Int>(FLOW_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_object_relation_date_value, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparentBackground()
        btnBottomAction.setOnClickListener { vm.onActionClicked() }
        btnClear.setOnClickListener { vm.onClearClicked() }
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

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.views) { observeState(it) }
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        super.onStart()
        vm.onStart(objectId = objectId, relationId = relationId)
    }

    private fun observeState(state: DateValueView) {
        tvRelationHeader.text = state.title
        ivTodayCheck.invisible()
        ivYesterdayCheck.invisible()
        ivTomorrowCheck.invisible()
        ivExactDayCheck.invisible()
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
    }

    private fun observeCommands(command: DateValueCommand) {
        when (command) {
            is DateValueCommand.DispatchResult -> {
                dispatchResultAndDismiss(command.timeInSeconds)
            }
            is DateValueCommand.OpenDatePicker -> {
                DatePickerFragment.new(command.timeInSeconds)
                    .show(childFragmentManager, null)
            }
        }
    }

    private fun dispatchResultAndDismiss(timeInSeconds: Double?) {
        withParent<EditObjectRelationDateValueReceiver> {
            onRelationDateValueChanged(
                ctx = ctx,
                objectId = objectId,
                relationId = relationId,
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
        if (flow == FLOW_DATAVIEW) {
            componentManager().objectSetObjectRelationDataValueComponent.get(ctx).inject(this)
        } else {
            componentManager().objectObjectRelationDateValueComponet.get(ctx).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (flow == FLOW_DATAVIEW) {
            componentManager().objectSetObjectRelationDataValueComponent.release(ctx)
        } else {
            componentManager().objectObjectRelationDateValueComponet.release(ctx)
        }
    }

    companion object {

        fun new(
            ctx: Id,
            relationId: Id,
            objectId: Id,
            flow: Int = FLOW_DEFAULT
        ) = ObjectRelationDateValueFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID to ctx,
                RELATION_ID to relationId,
                OBJECT_ID to objectId,
                FLOW_KEY to flow
            )
        }

        const val CONTEXT_ID = "arg.relation.date.context"
        const val RELATION_ID = "arg.relation.date.relation.id"
        const val OBJECT_ID = "arg.relation.date.object.id"

        const val FLOW_KEY = "arg.relation.date.flow"
        const val FLOW_DEFAULT = 0
        const val FLOW_DATAVIEW = 1
    }

    interface EditObjectRelationDateValueReceiver {
        fun onRelationDateValueChanged(
            ctx: Id,
            timeInSeconds: Number?,
            objectId: Id,
            relationId: Id
        )
    }
}