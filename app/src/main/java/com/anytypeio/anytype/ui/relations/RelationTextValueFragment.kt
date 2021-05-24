package com.anytypeio.anytype.ui.relations

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.RelationTextValueAdapter
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.EditGridCellAction
import com.anytypeio.anytype.presentation.sets.RelationTextValueView
import com.anytypeio.anytype.presentation.sets.RelationTextValueViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_relation_text_value.*
import javax.inject.Inject
import com.google.android.material.R.id.design_bottom_sheet as BOTTOM_SHEET_ID

open class RelationTextValueFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: RelationTextValueViewModel.Factory

    private val vm: RelationTextValueViewModel by viewModels { factory }

    private val ctx get() = arg<String>(CONTEXT_ID)
    private val relationId get() = arg<String>(RELATION_ID)
    private val objectId get() = arg<String>(OBJECT_ID)
    private val flow get() = arg<Int>(FLOW_KEY)

    private val relationValueAdapter by lazy {
        RelationTextValueAdapter(
            items = emptyList(),
            actionClick = { handleActions(it) },
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
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_relation_text_value, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = relationValueAdapter
        }
        onHideKeyboardWhenBottomSheetHidden()
    }

    override fun onStart() {
        jobs += lifecycleScope.subscribe(vm.views) { relationValueAdapter.update(it) }
        jobs += lifecycleScope.subscribe(vm.title) { tvRelationHeader.text = it }
        super.onStart()
        vm.onStart(relationId = relationId, recordId = objectId)
    }

    private fun handleActions(action: EditGridCellAction) {
        when (action) {
            is EditGridCellAction.Url -> {
                try {
                    Intent(Intent.ACTION_VIEW).apply {
                        val url = action.url.normalizeUrl()
                        data = Uri.parse(url)
                    }.let {
                        startActivity(it)
                    }
                } catch (e: Exception) {
                    toast("An error occurred. Url may be invalid: ${e.message}")
                }
            }
            is EditGridCellAction.Email -> {
                try {
                    Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:" + action.email)
                    }.let {
                        startActivity(it)
                    }
                } catch (e: Exception) {
                    toast("An error occurred. Email address may be invalid: ${e.message}")
                }
            }
            is EditGridCellAction.Phone -> {
                try {
                    Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${action.phone}")
                    }.let {
                        startActivity(it)
                    }
                } catch (e: Exception) {
                    toast("An error occurred. Phone number may be invalid: ${e.message}")
                }
            }
            else -> {
                toast("Unexpected action")
            }
        }
    }

    private fun dispatchTextResultAndExit(txt: String) {
        recycler.hideKeyboard()
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
        recycler.hideKeyboard()
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
        if (flow == FLOW_DATAVIEW) {
            componentManager().relationTextValueDVComponent.get(ctx).inject(this)
        } else {
            componentManager().relationTextValueComponent.get(ctx).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (flow == FLOW_DATAVIEW) {
            componentManager().relationTextValueDVComponent.release(ctx)
        } else {
            componentManager().relationTextValueComponent.release(ctx)
        }
    }

    companion object {

        fun new(
            ctx: Id,
            relationId: Id,
            objectId: Id,
            flow: Int = FLOW_DEFAULT
        ) = RelationTextValueFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID to ctx,
                RELATION_ID to relationId,
                OBJECT_ID to objectId,
                FLOW_KEY to flow
            )
        }

        const val CONTEXT_ID = "arg.edit-relation-value.context"
        const val RELATION_ID = "arg.edit-relation-value.relation.id"
        const val OBJECT_ID = "arg.edit-relation-value.object.id"
        const val FLOW_KEY = "arg.edit-relation-value.flow"

        const val FLOW_DEFAULT = 0
        const val FLOW_DATAVIEW = 1
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