package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_ui.relations.DatePickerContent
import com.anytypeio.anytype.core_ui.views.Title3
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.DateValueCommand
import com.anytypeio.anytype.presentation.sets.RelationDateValueViewModel
import com.anytypeio.anytype.ui.sets.modals.DatePickerFragment
import javax.inject.Inject

open class RelationDateValueFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: RelationDateValueViewModel.Factory
    val vm: RelationDateValueViewModel by viewModels { factory }

    private val ctx get() = argString(CONTEXT_ID)
    private val objectId get() = argString(OBJECT_ID)
    private val relationKey get() = argString(RELATION_KEY)
    private val flow get() = arg<Int>(FLOW_KEY)
    private val isLocked get() = argBoolean(LOCKED_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = MaterialTheme.typography.copy(bodyLarge = Title3)
            ) {
                DatePickerContent(
                    state = vm.views.collectAsStateWithLifecycle().value,
                    onDateSelected = vm::onDateSelected,
                    onClear = vm::onClearClicked,
                    onTodayClicked = vm::onTodayClicked,
                    onTomorrowClicked = vm::onTomorrowClicked
                )
            }
        }
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.commands) { observeCommands(it) }
        super.onStart()
        vm.onStart(ctx = ctx, objectId = objectId, relationKey = relationKey, isLocked = isLocked)
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    private fun observeCommands(command: DateValueCommand) {
        when (command) {
            is DateValueCommand.DispatchResult -> {
                dispatchResultAndDismiss(command.timeInSeconds)
                if (command.dismiss) dismiss()
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
                componentManager().dataViewRelationDateValueComponent.release()
            }
            FLOW_SET_OR_COLLECTION -> {
                componentManager().setOrCollectionRelationDateValueComponent.release()
            }
            else -> {
                componentManager().objectRelationDateValueComponent.release()
            }
        }
    }

    companion object {

        fun new(
            ctx: Id,
            relationKey: Key,
            objectId: Id,
            flow: Int = FLOW_DEFAULT,
            isLocked: Boolean = false
        ) = RelationDateValueFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID to ctx,
                RELATION_KEY to relationKey,
                OBJECT_ID to objectId,
                FLOW_KEY to flow,
                LOCKED_KEY to isLocked
            )
        }

        const val CONTEXT_ID = "arg.relation.date.context"
        const val RELATION_KEY = "arg.relation.date.relation.key"
        const val OBJECT_ID = "arg.relation.date.object.id"
        const val LOCKED_KEY = "arg.relation.date.object.locked"

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