package com.anytypeio.anytype.ui.relations

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.ObjectRelationTextValueAdapter
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.EditGridCellAction
import com.anytypeio.anytype.presentation.sets.ObjectRelationTextValueView
import com.anytypeio.anytype.presentation.sets.ObjectRelationTextValueViewModel
import kotlinx.android.synthetic.main.fragment_object_relation_text_value.*
import javax.inject.Inject

open class ObjectRelationTextValueFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: ObjectRelationTextValueViewModel.Factory

    private val vm: ObjectRelationTextValueViewModel by viewModels { factory }

    private val ctx get() = arg<String>(CONTEXT_ID)
    private val relationId get() = arg<String>(RELATION_ID)
    private val objectId get() = arg<String>(OBJECT_ID)
    private val flow get() = arg<Int>(FLOW_KEY)

    private val relationValueAdapter by lazy {
        ObjectRelationTextValueAdapter(
            items = emptyList(),
            actionClick = { handleActions(it) },
            onEditCompleted = { view, txt ->
                if (view is ObjectRelationTextValueView.Number) {
                    try {
                        dispatchNumberResultAndExit(txt.toDouble())
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
    ): View = inflater.inflate(R.layout.fragment_object_relation_text_value, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = relationValueAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.views) { relationValueAdapter.update(it) }
            subscribe(vm.title) { tvRelationHeader.text = it }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(relationId = relationId, recordId = objectId)
    }

    private fun handleActions(action: EditGridCellAction) {
        when (action) {
            is EditGridCellAction.Url -> {
                try {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(action.url)
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
        withParent<EditObjectRelationTextValueReceiver> {
            onRelationTextValueChanged(
                ctx = ctx,
                relationId = relationId,
                objectId = objectId,
                text = txt
            )
        }
        dismiss()
    }

    private fun dispatchNumberResultAndExit(number: Number) {
        recycler.hideKeyboard()
        withParent<EditObjectRelationTextValueReceiver> {
            onRelationTextNumberValueChanged(
                ctx = ctx,
                relationId = relationId,
                objectId = objectId,
                number = number
            )
        }
        dismiss()
    }

    override fun injectDependencies() {
        if (flow == FLOW_DATAVIEW) {
            componentManager().editGridCellComponent.get(ctx).inject(this)
        } else {
            componentManager().editRelationCellComponent.get(ctx).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (flow == FLOW_DATAVIEW) {
            componentManager().editGridCellComponent.release(ctx)
        } else {
            componentManager().editRelationCellComponent.get(ctx).inject(this)
        }
    }

    companion object {

        fun new(
            ctx: Id,
            relationId: Id,
            objectId: Id,
            flow: Int = FLOW_DEFAULT
        ) = ObjectRelationTextValueFragment().apply {
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

    interface EditObjectRelationTextValueReceiver {
        fun onRelationTextValueChanged(
            ctx: Id,
            text: String,
            objectId: Id,
            relationId: Id
        )
        fun onRelationTextNumberValueChanged(
            ctx: Id,
            number: Number,
            objectId: Id,
            relationId: Id
        )
    }
}