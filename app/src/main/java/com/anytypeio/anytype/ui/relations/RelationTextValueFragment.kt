package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.RelationActionAdapter
import com.anytypeio.anytype.core_ui.features.relations.RelationTextValueAdapter
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.intents.proceedWithAction
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetImeOffsetFragment
import com.anytypeio.anytype.databinding.FragmentRelationTextValueBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.RelationTextValueView
import com.anytypeio.anytype.presentation.sets.RelationTextValueViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueAction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject
import com.google.android.material.R.id.design_bottom_sheet as BOTTOM_SHEET_ID

open class RelationTextValueFragment :
    BaseBottomSheetImeOffsetFragment<FragmentRelationTextValueBinding>() {

    @Inject
    lateinit var factory: RelationTextValueViewModel.Factory

    private val vm: RelationTextValueViewModel by viewModels { factory }

    private val ctx get() = arg<String>(CONTEXT_ID)
    private val relationId get() = arg<String>(RELATION_ID)
    private val objectId get() = arg<String>(OBJECT_ID)
    private val flow get() = arg<Int>(FLOW_KEY)
    private val isLocked get() = arg<Boolean>(LOCKED_KEY)
    private val value get() = argOrNull<Long?>(KEY_VALUE)
    private val name get() = arg<String>(KEY_NAME)

    private val relationValueAdapter by lazy {
        RelationTextValueAdapter(
            items = emptyList(),
            onEditCompleted = { view, txt ->
                if (view is RelationTextValueView.Number) {
                    try {
                        if (txt.isBlank()) {
                            dispatchNumberResultAndExit(null)
                        } else {
                            dispatchNumberResultAndExit(txt.toDouble())
                        }
                    } catch (e: NumberFormatException) {
                        toast("Invalid number format. Please try again.")
                    }
                } else {
                    dispatchTextResultAndExit(txt)
                }
            },
            focusListener = vm::onUrlEditEvent
        )
    }

    private val relationValueActionAdapter by lazy {
        RelationActionAdapter { action ->
            val input = binding.recycler.findViewById<TextView?>(R.id.textInputField)
            val parsed = input?.text?.toString().orEmpty()
            // Workaround for updating relation value when before reloading content
            if (action is RelationValueAction.Url.Reload && parsed != action.url) {
                withParent<TextValueEditReceiver> {
                    onTextValueChanged(
                        ctx = ctx,
                        relationId = relationId,
                        objectId = objectId,
                        text = parsed
                    )
                }
                vm.onAction(
                    target = objectId,
                    action = action.copy(url = parsed)
                )
            } else {
                vm.onAction(
                    target = objectId,
                    action = action
                )
            }
        }
    }

    private val concatAdapter by lazy {
        ConcatAdapter(relationValueAdapter, relationValueActionAdapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(
                        drawable(R.drawable.default_divider_offset_20_dp)
                    )
                }
            )
        }
        onHideKeyboardWhenBottomSheetHidden()
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.views) { relationValueAdapter.update(it) }
        jobs += lifecycleScope.subscribe(vm.actions) { relationValueActionAdapter.submitList(it)  }
        jobs += lifecycleScope.subscribe(vm.intents) { proceedWithAction(it) }
        jobs += lifecycleScope.subscribe(vm.title) { binding.tvRelationHeader.text = it }
        jobs += lifecycleScope.subscribe(vm.isDismissed) { isDismissed ->
            if (isDismissed) dismiss()
        }
        super.onStart()

        if (flow == FLOW_CHANGE_DATE) {
            vm.onDateStart(
                name = name,
                value = value
            )
        } else {
            vm.onStart(
                relationId = relationId,
                recordId = objectId,
                isLocked = isLocked,
            )
        }
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    private fun dispatchTextResultAndExit(txt: String) {
        binding.recycler.hideKeyboard()
        withParent<TextValueEditReceiver> {
            onTextValueChanged(
                ctx = ctx,
                relationId = relationId,
                objectId = objectId,
                text = txt
            )
        }
        dismiss()
    }

    private fun dispatchNumberResultAndExit(number: Double?) {
        binding.recycler.hideKeyboard()
        withParent<TextValueEditReceiver> {
            onNumberValueChanged(
                ctx = ctx,
                relationId = relationId,
                objectId = objectId,
                number = number
            )
        }
        dismiss()
    }

    private fun onHideKeyboardWhenBottomSheetHidden() {
        dialog?.findViewById<FrameLayout>(BOTTOM_SHEET_ID)?.let { sheet ->
            BottomSheetBehavior.from(sheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                isHideable = true
                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        if (slideOffset == -1F) {
                            bottomSheet.hideKeyboard()
                        }
                    }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {}
                })
            }
        }
    }

    override fun injectDependencies() {
        if (flow == FLOW_DATAVIEW || flow == FLOW_CHANGE_DATE) {
            componentManager().relationTextValueDVComponent.get(ctx).inject(this)
        } else {
            componentManager().relationTextValueComponent.get(ctx).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (flow == FLOW_DATAVIEW || flow == FLOW_CHANGE_DATE) {
            componentManager().relationTextValueDVComponent.release(ctx)
        } else {
            componentManager().relationTextValueComponent.release(ctx)
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRelationTextValueBinding = FragmentRelationTextValueBinding.inflate(
        inflater, container, false
    )

    companion object {

        fun new(
            ctx: Id,
            relationId: Id,
            objectId: Id,
            flow: Int = FLOW_DEFAULT,
            isLocked: Boolean = false,
        ) = RelationTextValueFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID to ctx,
                RELATION_ID to relationId,
                OBJECT_ID to objectId,
                FLOW_KEY to flow,
                LOCKED_KEY to isLocked
            )
        }

        fun new(
            ctx: Id,
            name: String = "",
            value: Long? = null
        ) = new(ctx, "", "", FLOW_CHANGE_DATE)
            .apply {
                arguments?.apply {
                    putString(KEY_NAME, name)
                    value?.let { putLong(KEY_VALUE, it) }
                }
            }

        const val CONTEXT_ID = "arg.edit-relation-value.context"
        const val RELATION_ID = "arg.edit-relation-value.relation.id"
        const val OBJECT_ID = "arg.edit-relation-value.object.id"
        const val FLOW_KEY = "arg.edit-relation-value.flow"
        const val LOCKED_KEY = "arg.edit-relation-value.locked"
        const val KEY_VALUE = "arg.edit-relation-value.value"
        const val KEY_NAME = "arg.edit-relation-value.name"

        const val FLOW_DEFAULT = 0
        const val FLOW_DATAVIEW = 1
        const val FLOW_CHANGE_DATE = 2
    }

    interface TextValueEditReceiver {
        fun onTextValueChanged(
            ctx: Id,
            text: String,
            objectId: Id,
            relationId: Id
        )

        fun onNumberValueChanged(
            ctx: Id,
            number: Double?,
            objectId: Id,
            relationId: Id
        )
    }
}